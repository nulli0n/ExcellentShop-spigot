package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.AbstractTimed;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.*;
import su.nightexpress.nexshop.api.shop.chest.IProductChest;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyId;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.chest.ChestDisplayHandler;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChest;
import su.nightexpress.nexshop.shop.chest.type.ChestType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ShopChest extends AbstractShop<IProductChest> implements IShopChest {

    private final ChestShop chestShop;

    private final Location        location;
    private final UUID            ownerId;
    private final String          ownerName;
    private final OfflinePlayer   ownerPlayer;
    private final BankChest       bank;
    private       ChestType       type;
    private       Chest           chest;
    private       EditorShopChest editor;

    //private boolean      displayCreated;
    private List<String> displayText;
    private Location     displayHologramLoc;
    private Location     displayItemLoc;

    private AbstractShopView<IShopChest> view;

    public ShopChest(
        @NotNull ChestShop chestShop,
        @NotNull Player owner,
        @NotNull Chest chest,
        @NotNull UUID id,
        @NotNull ChestType type) {
        super(chestShop.plugin(), new JYML(chestShop.getFullPath() + ChestShop.DIR_SHOPS, id + ".yml"));
        this.chestShop = chestShop;
        this.bank = new BankChest(this);

        this.location = chest.getLocation();
        this.setChest(chest);
        this.setType(type);

        this.ownerId = owner.getUniqueId();
        this.ownerName = this.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME : owner.getName();
        this.ownerPlayer = owner;

        this.setName(owner.getName() + "'s shop");

        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, true);
        }
    }

    public ShopChest(@NotNull ChestShop chestShop, @NotNull JYML cfg) {
        super(chestShop.plugin(), cfg);
        this.chestShop = chestShop;
        this.bank = new BankChest(this);

        Location location = cfg.getLocation("Location");
        if (location == null || !ChestShop.isValidChest(location.getBlock())) {
            throw new IllegalStateException("Shop block is not a chest/container!");
        }
        this.setChest((Chest) location.getBlock().getState()); // TODO
        this.location = this.getChest().getLocation().clone();

        if (cfg.getBoolean("Admin_Shop")) {
            this.setType(ChestType.ADMIN);
        }
        else this.setType(cfg.getEnum("Type", ChestType.class, ChestType.PLAYER));

        this.ownerId = UUID.fromString(cfg.getString("Owner.Id", ""));
        this.ownerPlayer = plugin.getServer().getOfflinePlayer(this.getOwnerId());
        this.ownerName = this.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME : this.ownerPlayer.getName();
        if (this.ownerName == null) {
            throw new IllegalStateException("Shop owner is null! (Deleted player data?)");
        }

        this.setName(cfg.getString("Name", this.getOwnerName()));

        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }

        for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
            this.getBank().deposit(currency, cfg.getDouble("Bank." + currency.getId()));
        }

        for (String sId : cfg.getSection("Products")) {
            String path = "Products." + sId + ".";

            String path2 = path + "Purchase.";
            String currencyId = cfg.getString(path2 + "Currency", CurrencyId.VAULT);
            ICurrency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency == null) {
                currency = ChestConfig.DEFAULT_CURRENCY;
                chestShop.error("Invalid product currency of '" + sId + "' product in '" + getId() + "' shop! Changed to '" + currency.getId() + "'.");
            }

            Map<TradeType, double[]> priceMinMax = new HashMap<>();
            for (TradeType buyType : TradeType.values()) {
                String path3 = path2 + buyType.name() + ".";
                double priceMin = cfg.getDouble(path3 + "Price_Min");
                double priceMax = cfg.getDouble(path3 + "Price_Max");

                priceMinMax.put(buyType, new double[]{priceMin, priceMax});
            }

            path2 = path + "Reward.";
            ItemStack rewardItem = cfg.getItemEncoded(path2 + "Item");
            if (rewardItem == null) {
                throw new IllegalStateException("Invalid product item of '" + sId + "' in '" + getId() + "' shop!");
            }

            path2 = path + "Purchase.Randomizer.";
            boolean isRndEnabled = cfg.getBoolean(path2 + "Enabled");
            Set<DayOfWeek> rndDays = AbstractTimed.parseDays(cfg.getString(path2 + "Times.Days", ""));
            Set<LocalTime[]> rndTimes = AbstractTimed.parseTimes(cfg.getStringList(path2 + "Times.Times"));

            IProductPricer pricer = new ProductPricer(priceMinMax, isRndEnabled, rndDays, rndTimes);
            IProductChest product = new ProductChest(this, sId, currency, pricer, rewardItem);

            this.products.put(product.getId(), product);
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        Location location = this.getLocation();
        World world = this.getChest().getWorld();

        return str -> super.replacePlaceholders().apply(str
            .replace(Placeholders.SHOP_CHEST_OWNER, this.getOwnerName())
            .replace(Placeholders.SHOP_CHEST_LOCATION_X, NumberUtil.format(location.getX()))
            .replace(Placeholders.SHOP_CHEST_LOCATION_Y, NumberUtil.format(location.getY()))
            .replace(Placeholders.SHOP_CHEST_LOCATION_Z, NumberUtil.format(location.getZ()))
            .replace(Placeholders.SHOP_CHEST_LOCATION_WORLD, EngineConfig.getWorldName(world.getName()))
            .replace(Placeholders.SHOP_CHEST_IS_ADMIN, LangManager.getBoolean(this.isAdminShop()))
            .replace(Placeholders.SHOP_CHEST_TYPE, plugin.getLangManager().getEnum(this.getType()))
        );
    }

    @Override
    public void clear() {
        ChestDisplayHandler displayHandler = this.chestShop.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.remove(this);
        }

        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @Override
    public void onSave() {
        cfg.set("Location", this.getLocation());
        cfg.set("Name", this.getName());
        cfg.set("Owner.Id", this.getOwnerId().toString());
        cfg.set("Type", this.getType().name());
        this.purchaseAllowed.forEach((type, isAllowed) -> {
            cfg.set("Transaction_Allowed." + type.name(), isAllowed);
        });

        int c = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        cfg.set("Discounts.Custom", null);
        for (IShopDiscount discount : this.getDiscounts()) {
            String path = "Discounts.Custom." + (c++) + ".";
            cfg.set(path + "Discount", discount.getDiscountRaw());
            cfg.set(path + "Times.Days", discount.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            cfg.set(path + "Times.Times", discount.getTimes().stream()
                .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                .collect(Collectors.toList()));
        }

        this.getBank().getBalance().forEach((currencyId, balance) -> {
            cfg.set("Bank." + currencyId, balance);
        });

        cfg.set("Products", null);
        for (IProduct shopProduct : this.getProducts()) {
            String path = "Products." + shopProduct.getId() + ".";

            cfg.set(path + "Purchase.Currency", shopProduct.getCurrency().getId());
            IProductPricer pricer = shopProduct.getPricer();
            for (TradeType buyType : TradeType.values()) {
                String path2 = path + "Purchase." + buyType.name() + ".";
                cfg.set(path2 + "Price_Min", pricer.getPriceMin(buyType));
                cfg.set(path2 + "Price_Max", pricer.getPriceMax(buyType));
            }

            String path3 = path + "Purchase.Randomizer.";
            cfg.set(path3 + "Enabled", pricer.isRandomizerEnabled());
            cfg.set(path3 + "Times.Days", pricer.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            cfg.set(path3 + "Times.Times", pricer.getTimes().stream()
                .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                .collect(Collectors.toList()));

            cfg.setItemEncoded(path + "Reward.Item", shopProduct.getItem());
            //cfg.set(path + "Reward.Commands", shopProduct.getCommands());
        }
    }

    @Override
    public void setupView() {
        this.view = new ShopChestView(this);
    }

    @Override
    @NotNull
    public AbstractShopView<IShopChest> getView() {
        return this.view;
    }

    @Override
    @NotNull
    public EditorShopChest getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopChest(this.plugin, this);
        }
        return this.editor;
    }

    @Override
    public void open(@NotNull Player player, int page) {
        this.getView().open(player, 1);
    }

    @Override
    public boolean createProduct(@NotNull Player player, @NotNull ItemStack item) {
        if (item.getType().isAir() || this.isProduct(item)) {
            return false;
        }
        if (!ChestConfig.isAllowedItem(item)) {
            plugin.getMessage(Lang.Shop_Product_Error_BadItem).send(player);
            return false;
        }

        IProductChest shopProduct = new ProductChest(this, ChestConfig.DEFAULT_CURRENCY, item);
        this.getProductMap().put(shopProduct.getId(), shopProduct);
        return true;
    }

    @Override
    public void teleport(@NotNull Player player) {
        Location location = this.getLocation().clone();
        Block block = location.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Directional directional) {
            Block opposite = block.getRelative(directional.getFacing()).getLocation().clone().add(0, 0.5, 0).getBlock();
            location = LocationUtil.getCenter(opposite.getLocation());
            location.setDirection(directional.getFacing().getOppositeFace().getDirection());
            location.setPitch(35F);
        }
        player.teleport(location);
    }

    @NotNull
    @Override
    public BankChest getBank() {
        return this.bank;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = StringUtil.color(name);
    }

    @Override
    @NotNull
    public ChestType getType() {
        return type;
    }

    @Override
    public void setType(@NotNull ChestType type) {
        this.type = type;
    }

    @Override
    public boolean isAdminShop() {
        return this.getType() == ChestType.ADMIN;
    }

    @Override
    @NotNull
    public Chest getChest() {
        return this.chest;
    }

    @Override
    public void setChest(@NotNull Chest chest) {
        this.chest = chest;
    }

    @Override
    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @Override
    @NotNull
    public UUID getOwnerId() {
        return this.ownerId;
    }

    @Override
    @NotNull
    public String getOwnerName() {
        return this.ownerName;
    }

    @Override
    @NotNull
    public OfflinePlayer getOwner() {
        return this.ownerPlayer;
    }

    /*@Override
    public boolean isDisplayCreated() {
        return this.displayCreated;
    }

    @Override
    public void setDisplayCreated(boolean displayCreated) {
        this.displayCreated = displayCreated;
    }*/

    @Override
    @NotNull
    public List<String> getDisplayText() {
        if (this.displayText == null) this.updateDisplayText();
        return this.displayText;
    }

    @Override
    public void updateDisplayText() {
        this.displayText = new ArrayList<>(ChestConfig.getDisplayText(this.getType()));
        this.displayText.replaceAll(this.replacePlaceholders());
    }

    @Override
    public void updateDisplay() {
        this.displayHologramLoc = null;
        this.displayItemLoc = null;

        this.updateDisplayText();

        ChestDisplayHandler displayHandler = this.chestShop.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.remove(this);
            displayHandler.create(this);
        }
    }

    @Override
    @NotNull
    public Location getDisplayLocation() {
        if (this.displayHologramLoc == null) {
            Location invLocation = this.getChestInventory().getLocation();
            if (invLocation == null || !this.isChestDouble()) {
                this.displayHologramLoc = LocationUtil.getCenter(this.getLocation().clone().add(0, -1, 0));
            }
            else {
                this.displayHologramLoc = invLocation.add(0.5, -0.5, 0.5);
            }
        }
        return this.displayHologramLoc;
    }

    @Override
    @NotNull
    public Location getDisplayItemLocation() {
        if (this.displayItemLoc == null) {
            Location glassLocation = this.getDisplayLocation();
            this.displayItemLoc = glassLocation.clone().add(0, 1.4, 0);
        }
        return this.displayItemLoc;
    }
}
