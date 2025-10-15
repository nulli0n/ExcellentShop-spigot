package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.content.ContentTypes;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.rent.RentSettings;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class ChestShop extends AbstractShop<ChestProduct> {

    private final ChestShopModule module;

    private String       worldName;
    private BlockPos     blockPos;
    private ShopLocation location;

    private UUID    ownerId;
    private String  ownerName;
    private boolean adminShop;
    private boolean itemCreated;

    protected String  name;
    protected boolean buyingAllowed;
    protected boolean sellingAllowed;

    private boolean hologramEnabled;
    private boolean showcaseEnabled;
    private String  showcaseId;

    private RentSettings rentSettings;
    private UUID         renterId;
    private String       renterName;
    private long         rentedUntil;

    public ChestShop(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull Path path, @NotNull String id) {
        super(plugin, path, id);
        this.module = module;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forChestShop(this);
    }

    public boolean load() {
        FileConfig config = this.loadConfig();
        // ============== LOCATION UPDATE START ==============
        if (config.contains("Location")) {
            String raw = config.getString("Location", "");
            String[] split = raw.split(",");
            if (split.length != 6) {
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
        if (this.worldName == null || this.blockPos.isEmpty()) return false;

        try {
            this.ownerId = UUID.fromString(config.getString("Owner.Id", ""));
            this.ownerName = String.valueOf(this.getOwner().getName());
        }
        catch (IllegalArgumentException exception) {
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
        this.setShowcaseId(config.getString("Display.Showcase.Type"));

        this.loadProducts(config);
        return true;
    }

    private void loadProducts(@NotNull FileConfig config) {
        config.getSection("Products").forEach(id -> {
            try {
                ChestProduct product = ChestProduct.load(config, "Products." + id, id, this);
                this.addProduct(product);
            }
            catch (IllegalStateException exception) {
                this.module.error("Product '" + id + "' not loaded: " + exception.getMessage());
            }
        });
    }

    @Override
    public void write(@NotNull FileConfig config) {
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
        config.set("Display.Showcase.Type", this.getShowcaseId());

        config.getSection("Products").stream().filter(sId -> !this.hasProduct(sId)).forEach(sId -> config.remove("Products." + sId));

        this.getProducts().forEach(product -> product.write(config, "Products." + product.getId()));
    }

    @NotNull
    public ShopLocation location() {
        if (!this.isActive()) throw new IllegalStateException("Shop is not active! You must check ChestShop#isActive before access shop location.");

        return this.location;
    }

    @Nullable
    public Container container() {
        if (!this.isActive()) return null;
        if (!this.location().isValid()) return null;

        return (Container) this.location().getBlock().getState();
    }

    @Nullable
    public Inventory inventory() {
        Container container = this.container();
        return container == null ? null : container.getInventory();
    }

    public boolean isActive() {
        return this.location != null;
    }

    public boolean isInactive() {
        return !this.isActive();
    }

    public boolean isChunkLoaded() {
        return this.location != null && this.location.isChunkLoaded();
    }

    @Override
    public void update() {
        if (ChestConfig.isRentEnabled() && this.isRented() && this.isRentExpired()) {
            this.cancelRent();
        }
    }

    public void setLocation(@NotNull World world, @NotNull Location location) {
        this.worldName = world.getName();
        this.blockPos = BlockPos.from(location);
    }

    public boolean activate(@NotNull World world) {
        if (this.isActive()) return false;

        this.location = new ShopLocation(world, this.blockPos);

        if (this.location.isChunkLoaded()) {
            this.updateStockCache();
        }
        //this.plugin.debug("Shop activated: " + this.blockPos);
        return true;
    }

    public void deactivate() {
        this.location = null;
        //this.plugin.debug("Shop deactivated: " + this.blockPos);
    }

    public void updateStockCache() {
        this.getProducts().forEach(ChestProduct::updateStockCache);
    }

    @NotNull
    public ChestShopModule getModule() {
        return this.module;
    }

    @NotNull
    public String getWorldName() {
        return this.worldName;
    }

    @NotNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean isBuyingAllowed() {
        return this.buyingAllowed;
    }

    @Override
    public void setBuyingAllowed(boolean buyingAllowed) {
        this.buyingAllowed = buyingAllowed;
    }

    @Override
    public boolean isSellingAllowed() {
        return this.sellingAllowed;
    }

    @Override
    public void setSellingAllowed(boolean sellingAllowed) {
        this.sellingAllowed = sellingAllowed;
    }

    // TODO Update stock placeholders when closing chest

    @NotNull
    public ChestBank getOwnerBank() {
        return this.module.getPlayerBank(this);
    }

    @NotNull
    public ChestBank getRentersOrOwnerBank() {
        return this.module.getPlayerBank(this.renterId == null ? this.ownerId : this.renterId);
    }

    @Override
    public void open(@NotNull Player player, int page, boolean force) {
        this.module.openShop(player, this, page, force);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        return true;
    }

    public boolean canManage(@NotNull Player player) {
        return this.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.EDIT_OTHERS);
    }

    public boolean canRename(@NotNull Player player) {
        return this.canManage(player);
    }

    public boolean canManageProducts(@NotNull Player player) {
        return this.canManage(player);
    }

    public boolean canManageBank(@NotNull Player player) {
        return this.canManage(player);
    }

    public boolean canManageRent(@NotNull Player player) {
        return ChestUtils.hasRentPermission(player) && (this.isOwner(player) || player.hasPermission(ChestPerms.EDIT_OTHERS));
    }

    public boolean canManageDisplay(@NotNull Player player) {
        return (this.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.EDIT_OTHERS)) && player.hasPermission(ChestPerms.DISPLAY_CUSTOMIZATION);
    }

    public boolean canRemove(@NotNull Player player) {
        return this.isOwner(player) || player.hasPermission(ChestPerms.REMOVE_OTHERS);
    }

    public boolean canDecorate(@NotNull Player player) {
        return this.canManage(player);
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

    public double getBalance(@NotNull Currency currency) {
        if (this.isAdminShop()) return -1D;

        return this.getRentersOrOwnerBank().getBalance(currency);
    }

    @Nullable
    public ChestProduct createProduct(@NotNull Player player, @NotNull ItemStack item, boolean bypassHandler) {
        if (item.getType().isAir() || this.isProduct(item)) {
            return null;
        }
        if (!ChestUtils.isAllowedItem(item)) {
            ChestLang.SHOP_PRODUCT_ERROR_BAD_ITEM.message().send(player);
            return null;
        }
        ItemStack stack = new ItemStack(item);
        if (ChestConfig.SHOP_PRODUCT_NEW_PRODUCTS_SINGLE_AMOUNT.get()) {
            stack.setAmount(1);
        }

        String id = UUID.randomUUID().toString();
        ProductContent content = ContentTypes.fromItem(stack, this.module::isItemProviderAllowed);
        ChestProduct product = new ChestProduct(id, this);

        product.setCurrencyId(this.module.getSettings().getDefaultCurrency());
        product.setContent(content);
        product.setPricing(FlatPricing.of(ChestConfig.SHOP_PRODUCT_INITIAL_BUY_PRICE.get(), ChestConfig.SHOP_PRODUCT_INITIAL_SELL_PRICE.get()));
        product.updatePrice(false);

        this.addProduct(product);
        return product;
    }

    @Nullable
    public ChestProduct getProduct(@NotNull ItemStack item) {
        return this.getValidProducts().stream()
            .filter(product -> product.getContent() instanceof ItemContent typing && typing.isItemMatches(item))
            .findFirst().orElse(null);
    }

    public boolean isProduct(@NotNull ItemStack item) {
        return this.getProduct(item) != null;
    }

    public boolean hasProducts() {
        return this.countProducts() > 0;
    }

    @Nullable
    public ChestProduct getProductByIndex(int index) {
        List<ChestProduct> products = new ArrayList<>(this.getValidProducts());
        if (products.size() <= index) return null;

        return products.get(index);
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
        if (this.renterId == null) return;

        if (this.isRentExpired() || this.rentedUntil < 0) {
            this.rentedUntil = System.currentTimeMillis() + this.rentSettings.getDurationMillis();
        }
        else {
            this.rentedUntil += this.rentSettings.getDurationMillis();
        }
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    public void setItemCreated(boolean itemCreated) {
        this.itemCreated = itemCreated;
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
        return this.hologramEnabled;
    }

    public void setHologramEnabled(boolean hologramEnabled) {
        this.hologramEnabled = hologramEnabled;
    }

    public boolean isShowcaseEnabled() {
        return this.showcaseEnabled;
    }

    public void setShowcaseEnabled(boolean showcaseEnabled) {
        this.showcaseEnabled = showcaseEnabled;
    }

    @Nullable
    public String getShowcaseId() {
        return this.showcaseId;
    }

    public void setShowcaseId(@Nullable String showcaseId) {
        this.showcaseId = showcaseId;
    }

    public boolean hasCustomShowcase() {
        return this.showcaseId != null;
    }

    @Nullable
    public Showcase getShowcase() {
        if (!this.isChunkLoaded()) return null;
        if (!this.isShowcaseEnabled()) return null;

        Material blockType = this.location().getBlockType();
        ShopBlock shopBlock = this.module.getShopBlock(blockType);
        if (shopBlock == null) return null;

        Showcase showcase = null;
        if (this.showcaseId != null) {
            showcase = ChestUtils.getShowcaseFromCatalog(this.showcaseId);
        }
        if (showcase == null) {
            showcase = shopBlock.getShowcase();
        }

        return showcase;
    }

    @NotNull
    public List<String> getDisplayText() {
        return new ArrayList<>((this.isAdminShop() ? ChestConfig.DISPLAY_HOLOGRAM_TEXT_ADMIN_SHOP : ChestConfig.DISPLAY_HOLOGRAM_TEXT_PLAYER_SHOP).get());
    }

    @Nullable
    public List<String> getDisplayText(@Nullable ChestProduct product) {
        if (!ChestConfig.DISPLAY_HOLOGRAM_ENABLED.get()) return null;
        if (!this.isHologramEnabled()) return null;

        List<String> text;
        Replacer replacer = Replacer.create().replace(this.replacePlaceholders());

        if (!this.hasProducts()) {
            text = ChestConfig.DISPLAY_HOLOGRAM_TEXT_ABSENT.get();
        }
        else if (this.isRentable() && !this.isRented()) {
            text = ChestConfig.DISPLAY_HOLOGRAM_TEXT_RENT.get();
        }
        else {
            text = this.getDisplayText();

            if (product != null) {
                replacer
                    .replace(product.replacePlaceholders())
                    .replace(Placeholders.GENERIC_BUY, product.isBuyable() ? ChestConfig.DISPLAY_HOLOGRAM_TEXT_BUY.get() : "")
                    .replace(Placeholders.GENERIC_SELL, product.isSellable() ? ChestConfig.DISPLAY_HOLOGRAM_TEXT_SELL.get() : "");
            }
        }

        List<String> result = replacer.apply(text);
        result.removeIf(String::isBlank);

        return result.reversed();
    }
}
