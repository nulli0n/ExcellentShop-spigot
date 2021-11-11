package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.*;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyType;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.chest.ChestDisplayHandler;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ShopChest extends AbstractLoadableItem<ExcellentShop> implements IShopChest {

    private final ChestShop chestShop;

    private final Location      location;
    private final UUID          ownerId;
    private final String        ownerName;
    private final OfflinePlayer ownerPlayer;
    private final Map<TradeType, Boolean>        purchaseAllowed;
    private final Map<String, IShopChestProduct> products;
    private       String                         name;
    private       boolean                        isAdmin;
    private Chest           chest;
    private EditorShopChest editor;

    private boolean      displayHas;
    private List<String> displayText;
    private Location     displayHologramLoc;
    private Location     displayItemLoc;

    private AbstractShopView<IShopChest> view;

    public ShopChest(
            @NotNull ChestShop chestShop,
            @NotNull Player owner,
            @NotNull Chest chest,
            @NotNull UUID id,
            boolean admin) {
        super(chestShop.plugin(), chestShop.getFullPath() + ChestShop.DIR_SHOPS + id + ".yml");
        this.chestShop = chestShop;

        this.location = chest.getLocation();
        this.setChest(chest);

        this.ownerId = owner.getUniqueId();
        this.ownerName = owner.getName();
        this.ownerPlayer = owner;

        this.setName(owner.getName() + "'s shop");
        this.setAdminShop(admin);

        this.purchaseAllowed = new HashMap<>();
        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, true);
        }

        this.products = new LinkedHashMap<>();

        this.setupView();
        this.updateDisplay();
        this.save();
    }

    public ShopChest(@NotNull ChestShop chestShop, @NotNull JYML cfg) {
        super(chestShop.plugin(), cfg);
        this.chestShop = chestShop;

        Location location = cfg.getLocation("Location");
        BlockState state = location != null ? location.getBlock().getState() : null;
        if (!(state instanceof Chest)) {
            throw new IllegalStateException("Shop block is not a chest!");
        }
        this.setChest((Chest) state);
        this.location = this.getChest().getLocation().clone();

        this.ownerId = UUID.fromString(cfg.getString("Owner.Id", ""));
        this.ownerPlayer = plugin.getServer().getOfflinePlayer(this.getOwnerId());
        this.ownerName = this.ownerPlayer.getName();

        this.setName(cfg.getString("Name", this.getOwnerName()));
        this.setAdminShop(cfg.getBoolean("Admin_Shop"));

        this.purchaseAllowed = new HashMap<>();
        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }

        this.products = new LinkedHashMap<>();
        for (String sId : cfg.getSection("Products")) {
            String path = "Products." + sId + ".";

            String path2 = path + "Purchase.";
            String currencyId = cfg.getString(path2 + "Currency", CurrencyType.VAULT);
            IShopCurrency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency == null) {
                currency = plugin.getCurrencyManager().getCurrencyFirst();
                chestShop.error("Invalid product currency of '" + sId + "' in '" + getId() + "' shop! Changed to " + currency.getId());
            }
            if (!currency.hasOfflineSupport()) {
                chestShop.error("Product currency '" + currency.getId() + "' can not be used due to no offline support! Product Id: '" + getId() + "'.");
                continue;
            }

            Map<TradeType, double[]> priceMinMax = new HashMap<>();
            for (TradeType buyType : TradeType.values()) {
                String path3 = path2 + buyType.name() + ".";
                double priceMin = cfg.getDouble(path3 + "Price_Min");
                double priceMax = cfg.getDouble(path3 + "Price_Max");

                priceMinMax.put(buyType, new double[]{priceMin, priceMax});
            }

            path2 = path + "Reward.";
            ItemStack rewardItem = cfg.getItem64(path2 + "Item");
            if (rewardItem == null) {
                throw new IllegalStateException("Invalid product item of '" + sId + "' in '" + getId() + "' shop!");
            }

            path2 = path + "Purchase.Randomizer.";
            boolean isRndEnabled = cfg.getBoolean(path2 + "Enabled");
            Set<DayOfWeek> rndDays = AbstractTimed.parseDays(cfg.getString(path2 + "Times.Days", ""));
            Set<LocalTime[]> rndTimes = AbstractTimed.parseTimes(cfg.getStringList(path2 + "Times.Times"));

            IProductPricer pricer = new ProductPricer(priceMinMax, isRndEnabled, rndDays, rndTimes);

            IShopChestProduct product = new ShopChestProduct(
                    this, sId,

                    currency, pricer,

                    rewardItem);

            this.products.put(product.getId(), product);
        }

        this.setupView();
        this.updateDisplay();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    @NotNull
    public ExcellentShop plugin() {
        return this.plugin;
    }

    @Override
    public void clear() {
        this.chestShop.getDisplayHandler().remove(this);
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
        cfg.set("Admin_Shop", this.isAdminShop());
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

        cfg.set("Products", null);
        for (IShopProduct shopProduct : this.getProducts()) {
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

            cfg.setItem64(path + "Reward.Item", shopProduct.getItem());
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
        if (ItemUT.isAir(item) || this.isProduct(item)) {
            return false;
        }
        if (!ChestShopConfig.isAllowedItem(item)) {
            plugin.lang().Chest_Shop_Product_Error_BadItem.send(player);
            return false;
        }

        IShopCurrency currency = ChestShopConfig.DEFAULT_CURRENCY;
        IShopChestProduct shopProduct = new ShopChestProduct(this, currency, item);
        this.getProductMap().put(shopProduct.getId(), shopProduct);

        return true;
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        if (name.length() > 32) {
            name = name.substring(0, 32);
        }
        this.name = name;
    }

    @Override
    public boolean isAdminShop() {
        return this.isAdmin;
    }

    @Override
    public void setAdminShop(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean isPurchaseAllowed(@NotNull TradeType buyType) {
        return this.purchaseAllowed.getOrDefault(buyType, true);
    }

    @Override
    public void setPurchaseAllowed(@NotNull TradeType buyType, boolean isAllowed) {
        this.purchaseAllowed.put(buyType, isAllowed);
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

    @Override
    @NotNull
    public Collection<IShopDiscount> getDiscounts() {
        return Collections.emptySet();
    }

    @Override
    @NotNull
    public Map<String, IShopChestProduct> getProductMap() {
        return this.products;
    }

    @Override
    public boolean isDisplayHas() {
        return this.displayHas;
    }

    @Override
    public void setDisplayHas(boolean displayHas) {
        this.displayHas = displayHas;
    }

    @Override
    @NotNull
    public List<String> getDisplayText() {
        if (this.displayText == null) this.updateDisplayText();
        return this.displayText;
    }

    @Override
    public void updateDisplayText() {
        this.displayText = new ArrayList<>(ChestShopConfig.DISPLAY_TEXT);
        this.displayText.replaceAll(this.replacePlaceholders());
    }

    public void updateDisplay() {
        this.updateDisplayText();
        ChestDisplayHandler display = this.chestShop.getDisplayHandler();
        display.remove(this);
        display.create(this);
    }

    @Override
    @NotNull
    public Location getDisplayLocation() {
        if (this.displayHologramLoc == null) {
            Location invLocation = this.getChestInventory().getLocation();
            if (invLocation == null || !this.isChestDouble()) {
                this.displayHologramLoc = LocUT.getCenter(this.getLocation().clone().add(0, -1, 0));
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
