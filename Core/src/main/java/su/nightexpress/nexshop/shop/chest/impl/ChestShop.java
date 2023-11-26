package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.chest.ChestDisplayHandler;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.menu.ShopSettingsMenu;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nexshop.shop.impl.handler.VanillaItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChestShop extends AbstractShop<ChestProduct> {

    private final ChestShopModule module;

    private Location location;
    private Material blockType;
    private int      chunkX;
    private int      chunkZ;
    private boolean doubleChest;

    private UUID          ownerId;
    private String        ownerName;
    private OfflinePlayer ownerPlayer;
    private ShopType      type;

    private ShopSettingsMenu settingsMenu;

    private List<String> displayText;
    private Location     displayHologramLoc;
    private Location     displayItemLoc;

    private final ChestShopView view;
    private final ChestStock stock;

    public ChestShop(@NotNull ChestShopModule module, @NotNull JYML cfg) {
        super(module.plugin(), cfg, cfg.getFile().getName().replace(".yml", "").toLowerCase());
        this.module = module;
        this.view = new ChestShopView(this.plugin, this);
        this.stock = new ChestStock(this.plugin, this);

        this.placeholderMap
            .add(Placeholders.SHOP_BANK_BALANCE, () -> ChestUtils.getAllowedCurrencies().stream()
                .map(currency -> currency.format(this.getOwnerBank().getBalance(currency))).collect(Collectors.joining(", ")))
            .add(Placeholders.SHOP_CHEST_OWNER, this::getOwnerName)
            .add(Placeholders.SHOP_CHEST_LOCATION_X, () -> NumberUtil.format(this.getLocation().getX()))
            .add(Placeholders.SHOP_CHEST_LOCATION_Y, () -> NumberUtil.format(this.getLocation().getY()))
            .add(Placeholders.SHOP_CHEST_LOCATION_Z, () -> NumberUtil.format(this.getLocation().getZ()))
            .add(Placeholders.SHOP_CHEST_LOCATION_WORLD, () -> LocationUtil.getWorldName(this.getLocation()))
            .add(Placeholders.SHOP_CHEST_IS_ADMIN, () -> LangManager.getBoolean(this.isAdminShop()))
            .add(Placeholders.SHOP_CHEST_TYPE, () -> plugin.getLangManager().getEnum(this.getType()));

        List<ChestProduct> products = new ArrayList<>(this.getProducts());
        for (TradeType tradeType : TradeType.values()) {
            for (int slot = 0; slot < 27; slot++) {
                int index = slot;
                this.placeholderMap.add(Placeholders.SHOP_CHEST_PRODUCT_PRICE.apply(tradeType, slot), () -> {
                    if (products.size() <= index) return "-";

                    ChestProduct product = products.get(index);
                    return product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
        }
    }

    @Override
    public boolean load() {
        Location location = cfg.getLocation("Location");
        if (location == null || !ChestUtils.isValidContainer(location.getBlock())) {
            this.plugin.error("Shop block is not a valid container!");
            return false;
        }
        this.setLocation(location);

        this.setType(cfg.getEnum("Type", ShopType.class, ShopType.PLAYER));
        try {
            this.ownerId = UUID.fromString(cfg.getString("Owner.Id", ""));
            this.ownerPlayer = plugin.getServer().getOfflinePlayer(this.getOwnerId());
            if (this.ownerPlayer.getName() == null) {
                throw new IllegalArgumentException("Invalid owner!");
            }
        }
        catch (IllegalArgumentException e) {
            this.plugin.error("Shop owner is invalid!");
            return false;
        }

        this.ownerName = this.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : this.ownerPlayer.getName();
        this.setName(cfg.getString("Name", this.getOwnerName()));

        for (TradeType tradeType : TradeType.values()) {
            this.setTransactionEnabled(tradeType, cfg.getBoolean("Transaction_Allowed." + tradeType.name(), true));
        }
        /*for (Currency currency : ChestShopModule.ALLOWED_CURRENCIES) {
            this.getBank().deposit(currency, cfg.getDouble("Bank." + currency.getId()));
        }*/

        this.loadProducts();
        return true;
    }

    private void loadProducts() {
        this.cfg.getSection("Products").forEach(id -> {
            ChestProduct product = this.loadProduct(id, "Products." + id);
            if (product == null) {
                this.plugin.warn("Product not loaded: '" + id + "' in '" + this.getId() + "' shop.");
                return;
            }
            this.addProduct(product);
        });
    }

    @Nullable
    private ChestProduct loadProduct(@NotNull String id, @NotNull String path) {
        String currencyId = cfg.getString(path + ".Currency", CurrencyManager.VAULT);
        Currency currency = this.plugin.getCurrencyManager().getCurrency(currencyId);
        if (currency == null || !ChestUtils.isAllowedCurrency(currency)) {
            currency = this.getModule().getDefaultCurrency();
        }

        String itemOld = cfg.getString(path + ".Reward.Item");
        if (itemOld != null) {
            cfg.remove(path + ".Reward.Item");
            cfg.set(path + ".Content.Item", itemOld);
        }

        String handlerId = cfg.getString(path + ".Handler", VanillaItemHandler.NAME);
        ProductHandler handler = ProductHandlerRegistry.getHandler(handlerId);
        if (handler == null) {
            handler = ProductHandlerRegistry.forBukkitItem();
            this.getModule().warn("Invalid handler '" + handlerId + "' for '" + id + "' product in '" + this.getId() + "' shop. Using default one...");
        }

        ProductPacker packer = handler.createPacker();
        if (!packer.load(cfg, path)) {
            this.getModule().warn("Invalid data for '" + id + "' product in '" + this.getId() + "' shop.");
            return null;
        }
        if (packer instanceof ItemPacker itemPacker) {
            itemPacker.setUsePreview(false);
        }

        ChestProduct product = new ChestProduct(id, this, currency, handler, packer);
        product.setPricer(AbstractProductPricer.read(cfg, path + ".Price"));
        return product;
    }

    public void clear() {
        ChestDisplayHandler displayHandler = this.module.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.remove(this);
        }

        if (this.settingsMenu != null) {
            this.settingsMenu.clear();
            this.settingsMenu = null;
        }
        this.products.values().forEach(ChestProduct::clear);
        this.products.clear();
    }

    @Override
    public void onSave() {
        cfg.set("Location", this.getLocation());
        cfg.set("Name", this.getName());
        cfg.set("Owner.Id", this.getOwnerId().toString());
        cfg.set("Type", this.getType().name());
        this.transactions.forEach((type, isAllowed) -> {
            cfg.set("Transaction_Allowed." + type.name(), isAllowed);
        });
        this.saveProducts();
    }

    @Override
    public void saveProducts() {
        this.cfg.set("Products", null);
        this.getProducts().forEach(product -> product.write(cfg, "Products." + product.getId()));
    }

    @NotNull
    public ChestShopModule getModule() {
        return module;
    }

    @Override
    @NotNull
    public ChestShopView getView() {
        return this.view;
    }

    @NotNull
    @Override
    public ChestStock getStock() {
        return stock;
    }

    @NotNull
    public ShopSettingsMenu getEditor() {
        if (this.settingsMenu == null) {
            this.settingsMenu = new ShopSettingsMenu(this);
        }
        return this.settingsMenu;
    }

    @NotNull
    public ChestPlayerBank getOwnerBank() {
        return this.getModule().getPlayerBank(this);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        return true;
    }

    @Override
    public void addProduct(@NotNull Product product) {
        if (product instanceof ChestProduct chestProduct) {
            this.addProduct(chestProduct);
        }
    }

    public boolean createProduct(@NotNull Player player, @NotNull ItemStack item) {
        if (item.getType().isAir() || this.isProduct(item)) {
            return false;
        }
        if (!ChestUtils.isAllowedItem(item)) {
            plugin.getMessage(ChestLang.SHOP_PRODUCT_ERROR_BAD_ITEM).send(player);
            return false;
        }

        String id = UUID.randomUUID().toString();

        ProductHandler handler = ProductHandlerRegistry.getHandler(item);
        ProductPacker packer = handler.createPacker();
        if (packer instanceof ItemPacker itemPacker) {
            itemPacker.load(item);
        }

        Currency currency = this.getModule().getDefaultCurrency();
        ChestProduct product = new ChestProduct(id, this, currency, handler, packer);
        this.addProduct(product);
        return true;
    }

    @NotNull
    public ShopType getType() {
        return type;
    }

    public void setType(@NotNull ShopType type) {
        this.type = type;
    }

    public boolean isAdminShop() {
        return this.getType() == ShopType.ADMIN;
    }

    public boolean isValid() {
        return ChestUtils.isValidContainer(this.getLocation().getBlock());
    }

    @NotNull
    public Container getContainer() {
        return (Container) this.getLocation().getBlock().getState();
    }

    public boolean isDoubleChest() {
        return this.doubleChest;
    }

    public boolean isProduct(@NotNull ItemStack item) {
        return this.getProducts().stream().anyMatch(product -> {
            return product.getPacker() instanceof ItemPacker handler && handler.isItemMatches(item);
        });
    }

    public void updateContainerInfo() {
        this.doubleChest = this.getContainer().getInventory() instanceof DoubleChestInventory;
    }

    @NotNull
    public Pair<Container, Container> getSides() {
        if (!this.isDoubleChest()) {
            Container container = this.getContainer();
            return Pair.of(container, container);
        }

        DoubleChestInventory inventory = (DoubleChestInventory) this.getInventory();
        Chest left = (Chest) inventory.getLeftSide().getHolder();
        Chest right = (Chest) inventory.getRightSide().getHolder();
        return Pair.of(left != null ? left : this.getContainer(), right != null ? right : this.getContainer());
    }

    @NotNull
    public Inventory getInventory() {
        Inventory inventory = this.getContainer().getInventory();
        if (this.isDoubleChest() && inventory.getHolder() instanceof DoubleChest doubleChest) {
            return doubleChest.getInventory();
        }
        return inventory;
    }

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
    public Material getBlockType() {
        return blockType;
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location.clone();
        this.chunkX = location.getBlockX() >> 4;
        this.chunkZ = location.getBlockZ() >> 4;
        this.blockType = location.getBlock().getType();
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @NotNull
    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwner(@NotNull OfflinePlayer player) {
        this.ownerId = player.getUniqueId();
        this.ownerName = this.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : player.getName();
        this.ownerPlayer = player;
    }

    @NotNull
    public String getOwnerName() {
        return this.ownerName;
    }

    @NotNull
    public OfflinePlayer getOwner() {
        return this.ownerPlayer;
    }

    public boolean isOwner(@NotNull Player player) {
        return this.getOwnerId().equals(player.getUniqueId());
    }

    /*@Override
    public boolean isDisplayCreated() {
        return this.displayCreated;
    }

    @Override
    public void setDisplayCreated(boolean displayCreated) {
        this.displayCreated = displayCreated;
    }*/

    @NotNull
    public List<String> getDisplayText() {
        if (this.displayText == null) this.updateDisplayText();
        return this.displayText;
    }

    public void updateDisplayText() {
        this.displayText = new ArrayList<>(ChestUtils.getHologramLines(this.getType()));
        this.displayText.replaceAll(this.replacePlaceholders());
    }

    public void updateDisplay() {
        this.displayHologramLoc = null;
        this.displayItemLoc = null;

        this.updateDisplayText();

        ChestDisplayHandler displayHandler = this.module.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.remove(this);
            displayHandler.create(this);
        }
    }

    private double getDisplayYOffset() {
        if (this.getContainer() instanceof Chest) {
            return -1D;
        }
        else return -0.85D;
    }

    @NotNull
    public Location getDisplayLocation() {
        if (this.displayHologramLoc == null) {
            Location invLocation = this.getInventory().getLocation();
            if (invLocation == null || !this.isDoubleChest()) {
                Location center = LocationUtil.getCenter(this.getLocation().clone());
                this.displayHologramLoc = center.add(0, this.getDisplayYOffset(), 0);
            }
            else {
                this.displayHologramLoc = invLocation.add(0.5, -0.5, 0.5);
            }
        }
        return this.displayHologramLoc;
    }

    @NotNull
    public Location getDisplayItemLocation() {
        if (this.displayItemLoc == null) {
            Location glassLocation = this.getDisplayLocation();
            this.displayItemLoc = glassLocation.clone().add(0, 1.4, 0);
        }
        return this.displayItemLoc;
    }
}
