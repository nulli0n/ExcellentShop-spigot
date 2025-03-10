package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.rent.RentSettings;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Pair;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;

public class ChestShop extends AbstractShop<ChestProduct> {

    private final ChestShopModule module;

    private boolean  active;
    private World    world;
    private String   worldName;
    private BlockPos blockPos;
    private Material blockType;

    private UUID    ownerId;
    private String  ownerName;
    private boolean adminShop;
    private boolean itemCreated;

    private boolean hologramEnabled;
    private boolean showcaseEnabled;
    private String showcaseType;

    private RentSettings rentSettings;
    private UUID renterId;
    private String renterName;
    private long rentedUntil;

    private Location displayTextLocation;
    private Location displayItemLocation;
    private Location displayShowcaseLocation;

    public ChestShop(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull File file, @NotNull String id) {
        super(plugin, file, id);
        this.module = module;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forChestShop(this);
    }

    @Override
    protected boolean onLoad(@NotNull FileConfig config) {
        // ============== LOCATION UPDATE START ==============
        if (config.contains("Location")) {
            String raw = config.getString("Location", "");
            String[] split = raw.split(",");
            if (split.length != 6) {
                this.plugin.error("Invalid shop location!");
                return false;
            }

            String worldName = split[5];
            BlockPos blockPos = BlockPos.deserialize(raw);

            blockPos.write(config, "Placement.BlockPos");
            config.set("Placement.World", worldName);
            config.remove("Location");
        }

        if (config.contains("Type")) {
            config.set("AdminShop", config.getString("Type", "player").equalsIgnoreCase("admin"));
            config.remove("Type");
        }
        // ============== LOCATION UPDATE END ==============

        this.worldName = config.getString("Placement.World");
        this.blockPos = BlockPos.read(config, "Placement.BlockPos");
        if (this.worldName == null || this.blockPos.isEmpty()) {
            this.module.error("Invalid shop location data!");
            return false;
        }

        try {
            this.ownerId = UUID.fromString(config.getString("Owner.Id", ""));
            this.ownerName = String.valueOf(this.getOwner().getName());
        }
        catch (IllegalArgumentException exception) {
            this.plugin.error("Invalid UUID of the shop owner!");
            return false;
        }

        this.setName(config.getString("Name", this.getOwnerName()));
        this.setAdminShop(config.getBoolean("AdminShop"));
        this.setItemCreated(config.getBoolean("ItemCreated", false));
        this.setBuyingAllowed(config.getBoolean("Transaction_Allowed.BUY", true));
        this.setSellingAllowed(config.getBoolean("Transaction_Allowed.SELL", true));

        if (ChestConfig.isRentEnabled()) {
            this.rentSettings = RentSettings.read(config, "Rent.Settings");
            try {
                this.renterId = UUID.fromString(config.getString("Rent.RenterId", ""));
                this.renterName = String.valueOf(this.getRenter().getName());
            }
            catch (IllegalArgumentException exception) {
                this.renterId = null;
            }
            this.rentedUntil = config.getLong("Rent.RentedUntil", -1);
        }

        this.setHologramEnabled(config.getBoolean("Display.Hologram.Enabled", true));
        this.setShowcaseEnabled(config.getBoolean("Display.Showcase.Enabled", true));
        this.setShowcaseType(config.getString("Display.Showcase.Type"));

        this.loadProducts(config);
        return true;
    }

    private void loadProducts(@NotNull FileConfig config) {
        config.getSection("Products").forEach(id -> {
            ChestProduct product = this.loadProduct(config, id, "Products." + id);
//            if (product == null) {
//                this.plugin.warn("Product not loaded: '" + id + "' in '" + this.getId() + "' shop.");
//                return;
//            }
            this.addProduct(product);
        });
    }

    @NotNull
    private ChestProduct loadProduct(@NotNull FileConfig config, @NotNull String id, @NotNull String path) {
        String currencyId = CurrencyId.reroute(config.getString(path + ".Currency", CurrencyId.VAULT));
        Currency currency = EconomyBridge.getCurrency(currencyId);
        if (currency == null || !this.module.isAllowedCurrency(currency)) {
            currency = this.module.getDefaultCurrency();
        }

        String itemOld = config.getString(path + ".Reward.Item");
        if (itemOld != null) {
            config.remove(path + ".Reward.Item");
            config.set(path + ".Content.Item", itemOld);
        }

        if (!config.contains(path + ".Type")) {
            String handlerId = config.getString(path + ".Handler", "bukkit_item");
            if (handlerId.equalsIgnoreCase("bukkit_command")) {
                config.set(path + ".Type", ProductType.COMMAND.name());
            }
            else if (handlerId.equalsIgnoreCase("bukkit_item")) {
                config.set(path + ".Type", ProductType.VANILLA.name());
            }
            else {
                config.set(path + ".Type", ProductType.PLUGIN.name());
            }
        }

        ProductType typed = config.getEnum(path + ".Type", ProductType.class, ProductType.VANILLA);
        ProductTyping typing = ProductTypes.read(this.module, typed, config, path);

        int infQuantity = config.getInt(path + ".InfiniteStorage.Quantity");

        ChestProduct product = new ChestProduct(this.plugin, id, this, currency, typing);
        product.setPricer(AbstractProductPricer.read(config, path + ".Price"));
        product.setQuantity(infQuantity);
        return product;
    }

    @Override
    protected void onSave(@NotNull FileConfig config) {
        this.writeSettings(config);
        this.writeProducts(config);
    }

    @Override
    public void saveSettings() {
        FileConfig config = this.getConfig();
        this.writeSettings(config);
        config.saveChanges();
    }

    @Override
    public void saveRotations() {

    }

    private void writeSettings(@NotNull FileConfig config) {
        this.blockPos.write(config, "Placement.BlockPos");
        config.set("Placement.World", this.worldName);
        config.set("Name", this.getName());
        config.set("Owner.Id", this.getOwnerId().toString());
        config.set("AdminShop", this.adminShop);
        config.set("ItemCreated", this.isItemCreated());
        config.set("Transaction_Allowed.BUY", this.buyingAllowed);
        config.set("Transaction_Allowed.SELL", this.sellingAllowed);
        if (ChestConfig.isRentEnabled()) {
            config.set("Rent.Settings", this.rentSettings);
            config.set("Rent.RenterId", this.renterId == null ? null : this.renterId.toString());
            config.set("Rent.RentedUntil", this.renterId == null ? null : this.rentedUntil);
        }
        config.set("Display.Hologram.Enabled", this.isHologramEnabled());
        config.set("Display.Showcase.Enabled", this.isShowcaseEnabled());
        config.set("Display.Showcase.Type", this.getShowcaseType());
    }

    @Override
    public void saveProducts() {
        FileConfig config = this.getConfig();
        this.writeProducts(config);
        config.saveChanges();
    }

    public void saveProductQuantity() {
        FileConfig config = this.getConfig();
        this.getValidProducts().forEach(product -> product.writeQuantity(config, "Products." + product.getId()));
        config.saveChanges();
    }

    private void writeProducts(@NotNull FileConfig config) {
        config.remove("Products");
        this.getValidProducts().forEach(product -> product.write(config, "Products." + product.getId()));
    }

    @Override
    public void saveProduct(@NotNull Product product) {
        ChestProduct chestProduct = this.getProductById(product.getId());
        if (chestProduct == null) return;

        FileConfig config = this.getConfig();
        chestProduct.write(config, "Products." + product.getId());
        config.saveChanges();
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInactive() {
        return !this.isActive();
    }

    @NotNull
    public String getWorldName() {
        return worldName;
    }

    @NotNull
    public BlockPos getBlockPos() {
        return blockPos;
    }

    public World getWorld() {
        return world;
    }

    public Location toLocation(@NotNull BlockPos blockPos) {
        return blockPos.toLocation(this.world);
    }

    public Location getLocation() {
        return this.toLocation(this.blockPos);
    }

    public Block getBlock() {
        return this.blockPos.toBlock(this.world);
    }

    public Container getContainer() {
        return (Container) this.getBlock().getState();
    }

    public Inventory getInventory() {
        return this.getContainer().getInventory();
    }

    @NotNull
    public Pair<Container, Container> getSides() {
        Container container = this.getContainer();
        Inventory inventory = container.getInventory();

        if (inventory instanceof DoubleChestInventory chestInventory) {
            Chest left = (Chest) chestInventory.getLeftSide().getHolder();
            Chest right = (Chest) chestInventory.getRightSide().getHolder();
            return Pair.of(left != null ? left : container, right != null ? right : container);
        }
        return Pair.of(container, container);
    }

    public void assignLocation(@NotNull World world, @NotNull Location location) {
        this.worldName = world.getName();
        this.blockPos = BlockPos.from(location);
        this.updatePosition();
    }

    public void deactivate() {
        this.world = null;
        this.active = false;
    }

    @Override
    public void update() {
        super.update();

        if (ChestConfig.isRentEnabled() && this.isRented() && this.isRentExpired()) {
            // TODO Notify
            this.cancelRent();
        }

        this.updateStockCache();
    }

    public void updatePosition() {
        this.deactivate();

        World world = this.plugin.getServer().getWorld(this.worldName);
        if (world == null) {
            //module.warn("World is invalid [" + this.getId() + "]");
            return;
        }

        Block block = this.blockPos.toBlock(world);
        if (!(block.getState() instanceof Container)) {
            //module.warn("Shop block is not a container [" + this.getId() + "]");
            return;
        }

        this.active = true;
        this.world = world;
        this.blockType = block.getType();
        this.updateDisplayLocations();
        this.updateStockCache();
        this.module.getShopMap().updatePositionCache(this);

        //this.module.info("Shop activated: " + this.getId());
    }

    public void updateStockCache() {
        this.getProducts().forEach(ChestProduct::updateStockCache);
    }

    private void updateDisplayLocations() {
        Inventory inventory = this.getInventory();

        Location shopLocation;
        Location invLocation = inventory.getLocation();
        if (invLocation == null || !(inventory instanceof DoubleChestInventory)) {
            shopLocation = LocationUtil.setCenter2D(this.getLocation());
        }
        else {
            shopLocation = invLocation.clone().add(0.5, 0, 0.5);
        }

        double height = shopLocation.getBlock().getBoundingBox().getHeight();
        double heightOff = 1D - height;

        this.displayTextLocation = shopLocation.clone().add(0, 1.5D - heightOff, 0);
        this.displayItemLocation = shopLocation.clone().add(0, height, 0);
        this.displayShowcaseLocation = shopLocation.clone().add(0, -0.35D - heightOff, 0);

        if (ChestUtils.canUseDisplayEntities()) {
            this.displayTextLocation = this.displayTextLocation.add(0, 0.3, 0);
            this.displayShowcaseLocation = this.displayShowcaseLocation.add(0, 1.7, 0);
        }
    }

    @NotNull
    public ChestShopModule getModule() {
        return module;
    }

//    @NotNull
//    @Override
//    public ChestStock getStock() {
//        return stock;
//    }

    @NotNull
    public ChestBank getOwnerBank() {
        return this.module.getPlayerBank(this);
    }

    @Override
    public void open(@NotNull Player player, int page, boolean force) {
        this.module.openShop(player, this, page, force);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        return true;
    }

    public boolean canRename(@NotNull Player player) {
        return this.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.EDIT_OTHERS);
    }

    public boolean canManageProducts(@NotNull Player player) {
        return this.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.EDIT_OTHERS);
    }

    public boolean canManageBank(@NotNull Player player) {
        return this.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.COMMAND_BANK_OTHERS);
    }

    public boolean canManageRent(@NotNull Player player) {
        return this.isOwner(player) || player.hasPermission(ChestPerms.EDIT_OTHERS);
    }

    public boolean canRemove(@NotNull Player player) {
        return this.isOwner(player) || player.hasPermission(ChestPerms.REMOVE_OTHERS);
    }

    public boolean isOwner(@NotNull Player player) {
        return this.ownerId.equals(player.getUniqueId());
    }

    public boolean isRenter(@NotNull Player player) {
        return this.renterId != null && this.renterId.equals(player.getUniqueId());
    }

    public boolean isOwnerOrRenter(@NotNull Player player) {
        return this.isOwner(player) || this.isRenter(player);
    }

    public boolean isAdminShop() {
        return this.adminShop;
    }

    public void setAdminShop(boolean adminShop) {
        this.adminShop = adminShop;
    }

    @Nullable
    public ChestProduct getRandomProduct() {
        Set<ChestProduct> products = new HashSet<>(this.getValidProducts());
        return products.isEmpty() ? null : Rnd.get(products);
    }

//    @Override
//    public void addProduct(@NotNull Product product) {
//        if (product instanceof ChestProduct chestProduct) {
//            this.addProduct(chestProduct);
//        }
//    }

    @Nullable
    public ChestProduct createProduct(@NotNull Player player, @NotNull ItemStack item, boolean bypassHandler) {
        if (item.getType().isAir() || this.isProduct(item)) {
            return null;
        }
        if (!ChestUtils.isAllowedItem(item)) {
            ChestLang.SHOP_PRODUCT_ERROR_BAD_ITEM.getMessage().send(player);
            return null;
        }
        ItemStack stack = new ItemStack(item);
        if (ChestConfig.SHOP_PRODUCT_NEW_PRODUCTS_SINGLE_AMOUNT.get()) {
            stack.setAmount(1);
        }

        String id = UUID.randomUUID().toString();
        ProductTyping typing = ProductTypes.fromItem(stack, bypassHandler);
        Currency currency = this.module.getDefaultCurrency();
        ChestProduct product = new ChestProduct(this.plugin, id, this, currency, typing);

        product.setPrice(TradeType.BUY, ChestConfig.SHOP_PRODUCT_INITIAL_BUY_PRICE.get());
        product.setPrice(TradeType.SELL, ChestConfig.SHOP_PRODUCT_INITIAL_SELL_PRICE.get());

        this.addProduct(product);
        return product;
    }

    @Nullable
    public ChestProduct getProduct(@NotNull ItemStack item) {
        return this.getValidProducts().stream()
            .filter(product -> product.getType() instanceof PhysicalTyping typing && typing.isItemMatches(item))
            .findFirst().orElse(null);
    }

    public boolean isProduct(@NotNull ItemStack item) {
        return this.getProduct(item) != null;
//        return this.getValidProducts().stream().anyMatch(product -> {
//            return product.getPacker() instanceof ItemPacker handler && handler.isItemMatches(item);
//        });
    }

    @Nullable
    public ChestProduct getProductAtSlot(int slot) {
        List<ChestProduct> products = new ArrayList<>(this.getValidProducts());
        if (products.size() <= slot) return null;

        return products.get(slot);
    }

    public boolean teleport(@NotNull Player player) {
        if (this.isInactive()) return false;

        Location location = this.getLocation();

        if (ChestConfig.CHECK_SAFE_LOCATION.get()) {
            Block block = location.getBlock();
            BlockData data = block.getBlockData();
            if (data instanceof Directional directional) {
                Block opposite = block.getRelative(directional.getFacing()).getLocation().clone().add(0, 0.5, 0).getBlock();
                location = LocationUtil.setCenter3D(opposite.getLocation());
                location.setDirection(directional.getFacing().getOppositeFace().getDirection());
                location.setPitch(35F);
            }
        }

        if (!this.isOwner(player) && !ChestUtils.isSafeLocation(location)) {
            ChestLang.SHOP_TELEPORT_ERROR_UNSAFE.getMessage().send(player);
            return false;
        }

        return player.teleport(location);
    }

    @NotNull
    public RentSettings getRentSettings() {
        return this.rentSettings;
    }

    public void setRentSettings(@Nullable RentSettings rentSettings) {
        this.rentSettings = rentSettings;
    }

    public boolean isRentable() {
        return ChestConfig.isRentEnabled() && this.rentSettings.isEnabled();
    }

    public boolean isRented() {
        return ChestConfig.isRentEnabled() && this.renterId != null && !this.isRentExpired();
    }

    public boolean isRentExpired() {
        return TimeUtil.isPassed(this.rentedUntil);
    }

    @NotNull
    public OfflinePlayer getRenter() {
        return this.plugin.getServer().getOfflinePlayer(this.renterId);
    }

    @NotNull
    public OfflinePlayer getRenterOrOwner() {
        return this.renterId == null ? this.getOwner() : this.getRenter();
    }

    @Nullable
    public UUID getRenterId() {
        return this.renterId;
    }

    @NotNull
    public String getRenterName() {
        return this.renterName;
    }

    public long getRentedUntil() {
        return this.rentedUntil;
    }

    public void setRentedUntil(long rentedUntil) {
        this.rentedUntil = rentedUntil;
    }

    public void setRentedBy(@NotNull Player player) {
        this.setRentedBy(player.getUniqueId());
        this.renterName = player.getName();
    }

    public void setRentedBy(@NotNull UUID playerId) {
        this.renterId = playerId;
    }

    public void cancelRent() {
        this.renterId = null;
        this.rentedUntil = -1;
    }

    public void extendRent() {
        if (!this.isRented()) return;

        this.rentedUntil += this.rentSettings.getDurationMillis();
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    public void setItemCreated(boolean itemCreated) {
        this.itemCreated = itemCreated;
    }

    @NotNull
    public Material getBlockType() {
        return blockType;
    }

    @NotNull
    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwner(@NotNull OfflinePlayer player) {
        this.ownerId = player.getUniqueId();
        this.ownerName = player.getName();
    }

    @NotNull
    public String getOwnerName() {
        return this.ownerName;
    }

    @NotNull
    public OfflinePlayer getOwner() {
        return this.plugin.getServer().getOfflinePlayer(this.ownerId);
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public void setHologramEnabled(boolean hologramEnabled) {
        this.hologramEnabled = hologramEnabled;
    }

    public boolean isShowcaseEnabled() {
        return showcaseEnabled;
    }

    public void setShowcaseEnabled(boolean showcaseEnabled) {
        this.showcaseEnabled = showcaseEnabled;
    }

    @Nullable
    public String getShowcaseType() {
        return showcaseType;
    }

    public void setShowcaseType(@Nullable String showcaseType) {
        this.showcaseType = showcaseType;
    }

    @NotNull
    public List<String> getDisplayText() {
        return new ArrayList<>((this.isAdminShop() ? ChestConfig.DISPLAY_HOLOGRAM_TEXT_ADMIN : ChestConfig.DISPLAY_HOLOGRAM_TEXT_NORMAL).get());
    }

    @NotNull
    public List<String> getHologramText(@Nullable ChestProduct product) {
        if (this.isRentable() && !this.isRented()) {
            return new ArrayList<>(ChestConfig.DISPLAY_HOLOGRAM_TEXT_RENT.get()).reversed();
        }

        var buyTextMap = ChestConfig.DISPLAY_HOLOGRAM_TEXT_BUY.get();
        var sellTextMap = ChestConfig.DISPLAY_HOLOGRAM_TEXT_SELL.get();

        boolean isBuyable = product != null && product.isBuyable();
        boolean isSellable = product != null && product.isSellable();

        List<String> text = new ArrayList<>();
        for (String line : this.getDisplayText()) {
            text.addFirst(line
                .replace(Placeholders.GENERIC_BUY, !isBuyable ? "" : buyTextMap.getOrDefault(this.isAdminShop() ? ShopType.ADMIN : ShopType.PLAYER, ""))
                .replace(Placeholders.GENERIC_SELL, !isSellable ? "" : sellTextMap.getOrDefault(this.isAdminShop() ? ShopType.ADMIN : ShopType.PLAYER, ""))
                .trim()
            );
        }
        text.removeIf(String::isBlank);

        return text;
    }

    @NotNull
    public Location getDisplayTextLocation() {
        return this.displayTextLocation.clone();
    }

    @NotNull
    public Location getDisplayShowcaseLocation() {
        return this.displayShowcaseLocation.clone();
    }

    @NotNull
    public Location getDisplayItemLocation() {
        return this.displayItemLocation.clone();
    }
}
