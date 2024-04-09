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
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.handler.VaultEconomyHandler;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.Placeholders;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.display.DisplayHandler;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nexshop.shop.impl.handler.VanillaItemHandler;

import java.util.*;

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

    //private List<String> displayText;
    private Location displayTextLocation;
    private Location displayItemLocation;
    private Location displayShowcaseLocation;

    private final ChestShopView view;
    private final ChestStock stock;

    public ChestShop(@NotNull ChestShopModule module, @NotNull JYML cfg) {
        super(module.plugin(), cfg, cfg.getFile().getName().replace(".yml", "").toLowerCase());
        this.module = module;
        this.view = new ChestShopView(this.plugin, this);
        this.stock = new ChestStock(this.plugin, this);

        this.placeholderMap.add(Placeholders.forShop(this));
    }

    @Override
    public boolean load() {
        Location location = cfg.getLocation("Location");
        if (location == null || !ChestUtils.isValidContainer(location.getBlock())) {
            this.plugin.error("Shop block is not a valid container!");
            return false;
        }
        this.updateLocation(location);

        try {
            this.ownerId = UUID.fromString(cfg.getString("Owner.Id", ""));
            this.ownerPlayer = plugin.getServer().getOfflinePlayer(this.getOwnerId());
            this.ownerName = this.ownerPlayer.getName() == null ? "?" : this.ownerPlayer.getName();
        }
        catch (IllegalArgumentException exception) {
            this.plugin.error("Invalid UUID of the shop owner!");
            return false;
        }

        this.setName(cfg.getString("Name", this.getOwnerName()));
        this.setType(cfg.getEnum("Type", ShopType.class, ShopType.PLAYER));

        for (TradeType tradeType : TradeType.values()) {
            this.setTransactionEnabled(tradeType, cfg.getBoolean("Transaction_Allowed." + tradeType.name(), true));
        }

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
        String currencyId = cfg.getString(path + ".Currency", VaultEconomyHandler.ID);
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

    @Override
    public void clear() {
        DisplayHandler displayHandler = this.module.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.remove(this);
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
        this.cfg.remove("Products");
        this.getProducts().forEach(product -> product.write(cfg, "Products." + product.getId()));
    }

    public void updateLocation(@NotNull Location location) {
        this.location = location.clone();
        this.chunkX = location.getBlockX() >> 4;
        this.chunkZ = location.getBlockZ() >> 4;
        this.blockType = location.getBlock().getType();
        this.doubleChest = this.getContainer().getInventory() instanceof DoubleChestInventory;
        this.updateDisplayLocations();
    }

    private void updateDisplayLocations() {
        Location location;
        Location invLocation = this.getInventory().getLocation();
        if (invLocation == null || !this.isDoubleChest()) {
            location = LocationUtil.getCenter(this.getLocation(), false);
        }
        else {
            location = invLocation.clone().add(0.5, 0, 0.5);
        }

        double height = location.getBlock().getBoundingBox().getHeight();
        double heightOff = 1D - height;

        this.displayTextLocation = location.clone().add(0, 1.5D - heightOff, 0);
        this.displayItemLocation = location.clone().add(0, height, 0);
        this.displayShowcaseLocation = location.clone().add(0, -0.35D - heightOff, 0);
    }

    public void openMenu(@NotNull Player player) {
        this.getModule().getSettingsMenu().open(player, this, 1);
    }

    public void openProductsMenu(@NotNull Player player) {
        this.getModule().getProductsMenu().open(player, this, 1);
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

    @Nullable
    public ChestProduct createProduct(@NotNull Player player, @NotNull ItemStack item) {
        if (item.getType().isAir() || this.isProduct(item)) {
            return null;
        }
        if (!ChestUtils.isAllowedItem(item)) {
            plugin.getMessage(ChestLang.SHOP_PRODUCT_ERROR_BAD_ITEM).send(player);
            return null;
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
        return product;
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

    @Nullable
    public ChestProduct getProductAtSlot(int slot) {
        List<ChestProduct> products = new ArrayList<>(this.getProducts());
        if (products.size() <= slot) return null;

        return products.get(slot);
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
        if (this.isDoubleChest() && inventory.getHolder() instanceof DoubleChest chest) {
            return chest.getInventory();
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
        this.ownerName = player.getName();
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

    @Nullable
    public ChestProduct getRandomProduct() {
        Set<ChestProduct> products = new HashSet<>(this.getProducts());
        return products.isEmpty() ? null : Rnd.get(products);
    }

    /*@Nullable
    public ItemStack getDisplayProduct() {
        ChestProduct product = this.getRandomProduct();
        return product == null ? null : product.getPreview();
    }*/

    @Nullable
    public ItemStack getShowcaseItem() {
        var map = ChestConfig.DISPLAY_SHOWCASE.get();

        ItemStack showcase = map.getOrDefault(this.getBlockType().name().toLowerCase(), map.get(Placeholders.DEFAULT));
        if (showcase == null || showcase.getType().isAir()) return null;

        return showcase;
    }

    @NotNull
    public List<String> getDisplayText() {
        List<String> displayText = new ArrayList<>(ChestConfig.DISPLAY_HOLOGRAM_TEXT.get().getOrDefault(this.getType(), Collections.emptyList()));
        //this.displayText.replaceAll(this.replacePlaceholders());

        //if (this.displayText == null) this.updateDisplayText();
        //return this.displayText;

        return displayText;
    }

    /*@Deprecated
    public void updateDisplayText() {
        //this.displayText = new ArrayList<>(ChestConfig.DISPLAY_HOLOGRAM_TEXT.get().getOrDefault(this.getType(), Collections.emptyList()));
        //this.displayText.replaceAll(this.replacePlaceholders());
    }*/

    @NotNull
    public Location getDisplayTextLocation() {
        return this.displayTextLocation;
    }

    @NotNull
    public Location getDisplayShowcaseLocation() {
        return this.displayShowcaseLocation;
    }

    @NotNull
    public Location getDisplayItemLocation() {
        return this.displayItemLocation;
    }
}
