package su.nightexpress.excellentshop.feature.playershop.impl;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.playershop.PlayerShop;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.ChestUtils;
import su.nightexpress.excellentshop.feature.playershop.core.ChestConfig;
import su.nightexpress.excellentshop.feature.playershop.core.ChestPerms;
import su.nightexpress.excellentshop.feature.playershop.exception.PlayerShopLoadException;
import su.nightexpress.excellentshop.feature.playershop.rent.RentSettings;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.product.price.FlatPricing;
import su.nightexpress.excellentshop.shop.AbstractShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.user.UserInfo;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChestShop extends AbstractShop<ChestProduct> implements PlayerShop {

    private final ChestShopModule module;
    private final Map<UUID, UserInfo> trustedPlayers;

    private String   worldName;
    private BlockPos blockPos;
    private ChunkPos chunkPos;

    private UserInfo ownerInfo;
    private boolean  adminShop;
    private boolean  itemCreated;

    protected String  name;
    protected boolean buyingAllowed;
    protected boolean sellingAllowed;

    private boolean hologramEnabled;
    private boolean showcaseEnabled;
    private String  showcaseId;

    private RentSettings rentSettings;
    private UserInfo     renterInfo;
    private long         rentedUntil;

    private Block block;

    public ChestShop(@NonNull ShopPlugin plugin, @NonNull ChestShopModule module, @NonNull Path path, @NonNull String id) {
        super(plugin, path, id);
        this.module = module;
        this.trustedPlayers = new HashMap<>();
    }

    public void load() throws PlayerShopLoadException {
        FileConfig config = this.loadConfig();
        // ============== LOCATION UPDATE START ==============
        if (config.contains("Location")) {
            String raw = config.getString("Location", "");
            String[] split = raw.split(",");
            if (split.length != 6) {
                throw new PlayerShopLoadException("Invalid shop location data. Expected 6 params, but found %s".formatted(split.length));
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
            throw new PlayerShopLoadException("Location is null");
        }

        this.chunkPos = this.blockPos.toChunkPos();

        try {
            UUID ownerId = UUID.fromString(config.getString("Owner.Id", ""));
            if (!config.contains("Owner.Name")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
                config.set("Owner.Name", offlinePlayer.getName());
            }
            String ownerName = String.valueOf(config.getString("Owner.Name"));
            this.ownerInfo = new UserInfo(ownerId, ownerName);
        }
        catch (IllegalArgumentException exception) {
            throw new PlayerShopLoadException("Invalid owner data");
        }

        this.setName(config.getString("Name", this.getOwnerName()));
        this.setAdminShop(config.getBoolean("AdminShop"));
        this.setItemCreated(config.getBoolean("ItemCreated", false));
        this.setBuyingAllowed(config.getBoolean("Transaction_Allowed.BUY", true));
        this.setSellingAllowed(config.getBoolean("Transaction_Allowed.SELL", true));

        if (ChestConfig.isRentEnabled()) {
            this.rentSettings = RentSettings.read(config, "Rent.Settings");
            try {
                UUID renterId = UUID.fromString(config.getString("Rent.RenterId", ""));
                if (!config.contains("Rent.RenterName")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(renterId);
                    config.set("Rent.RenterName", offlinePlayer.getName());
                }
                String renterName = String.valueOf(config.getString("Rent.RenterName"));
                this.renterInfo = new UserInfo(renterId, renterName);
            }
            catch (IllegalArgumentException ignored) {}

            this.rentedUntil = config.getLong("Rent.RentedUntil", -1);
        }

        this.setHologramEnabled(config.getBoolean("Display.Hologram.Enabled", true));
        this.setShowcaseEnabled(config.getBoolean("Display.Showcase.Enabled", true));
        this.setShowcaseId(config.getString("Display.Showcase.Type"));

        config.getSection("TrustedPlayers").forEach(sId -> {
            try {
                UUID trustedId = UUID.fromString(sId);
                String trustedName = String.valueOf(config.getString("TrustedPlayers." + sId + ".Name"));

                this.trustedPlayers.put(trustedId, new UserInfo(trustedId, trustedName));
            }
            catch (IllegalArgumentException ignored) {}
        });

        this.loadProducts(config);
    }

    private void loadProducts(@NonNull FileConfig config) {
        config.getSection("Products").forEach(id -> {
            try {
                UUID globalId = UUID.fromString(id);
                ChestProduct product = ChestProduct.load(config, "Products." + id, globalId, this);
                this.addProduct(product);
            }
            catch (IllegalArgumentException | IllegalStateException exception) {
                exception.printStackTrace();
                this.module.error("Product '" + id + "' not loaded: " + exception.getMessage());
            }
        });
    }

    @Override
    public void write(@NonNull FileConfig config) {
        this.blockPos.write(config, "Placement.BlockPos");
        config.set("Placement.World", this.worldName);
        config.set("Name", this.getName());
        config.set("Owner.Id", this.ownerInfo.id().toString());
        config.set("Owner.Name", this.getOwner().map(Player::getName).orElse(this.getOwnerName()));
        config.set("AdminShop", this.adminShop);
        config.set("ItemCreated", this.isItemCreated());
        config.set("Transaction_Allowed.BUY", this.buyingAllowed);
        config.set("Transaction_Allowed.SELL", this.sellingAllowed);
        if (ChestConfig.isRentEnabled()) {
            config.set("Rent.Settings", this.rentSettings);
            config.set("Rent.RenterId", this.renterInfo == null ? null : this.renterInfo.id().toString());
            config.set("Rent.RenterName", this.renterInfo == null ? null : this.renterInfo.name());
            config.set("Rent.RentedUntil", this.renterInfo == null ? null : this.rentedUntil);
        }
        config.set("Display.Hologram.Enabled", this.isHologramEnabled());
        config.set("Display.Showcase.Enabled", this.isShowcaseEnabled());
        config.set("Display.Showcase.Type", this.getShowcaseId());

        config.getSection("Products").stream().filter(sId -> !this.hasProduct(sId)).forEach(sId -> config.remove("Products." + sId));

        config.remove("TrustedPlayers");
        this.trustedPlayers.forEach((id, profile) -> {
            config.set("TrustedPlayers." + id + ".Name", profile.name());
        });

        this.getProducts().forEach(product -> product.write(config, "Products." + product.getId()));
    }

    @Override
    @NonNull
    public PlaceholderResolver placeholders() {
        return ShopPlaceholders.CHEST_SHOP.resolver(this);
    }

    public boolean isAccessible() {
        return this.block != null;
    }

    public boolean isChunk(@NonNull Chunk chunk) {
        return this.isChunk(chunk.getX(), chunk.getZ());
    }

    public boolean isChunk(int x, int z) {
        return this.chunkPos.getX() == x && this.chunkPos.getZ() == z;
    }

    public void activate(@NonNull Chunk chunk) throws IllegalStateException {
        if (!this.isChunk(chunk)) return;

        World world = chunk.getWorld();
        Location location = this.blockPos.toLocation(world);
        Block block = location.getBlock();
        if (!ChestUtils.isContainer(block)) throw new IllegalStateException("Shop block is not a container anymore!");

        this.block = block;
        this.module.onShopActivation(this);
        //this.plugin.debug("Shop " + this.getId() + " activated");
    }

    public void deactivate2() {
        if (!this.isAccessible()) return;

        this.module.onShopDeactivation(this);
        this.updateStockCache();
        this.block = null;
        //this.plugin.debug("Shop " + this.getId() + " deactivated");
    }

    private void checkAccess() {
        if (!this.isAccessible()) throw new IllegalStateException("Shop is not accessible");
    }

    @NonNull
    public Block getBlock() {
        this.checkAccess();

        return this.block;
    }

    @NonNull
    public Location getBukkitLocation() {
        this.checkAccess();

        return this.block.getLocation();
    }

    @NonNull
    public Location getTeleportLocation() {
        Location location = this.getBukkitLocation();
        BlockData data = this.block.getBlockData();

        if (data instanceof Directional directional) {
            Block opposite = this.block.getRelative(directional.getFacing()).getLocation().clone().add(0, 0.5, 0).getBlock();
            location = LocationUtil.setCenter3D(opposite.getLocation());
            location.setDirection(directional.getFacing().getOppositeFace().getDirection());
            location.setPitch(35F);
        }

        return location;
    }

    @NonNull
    public Optional<BlockInventoryHolder> getContainer() {
        this.checkAccess();

        if (this.block.getState() instanceof BlockInventoryHolder box) {
            return Optional.of(box);
        }

        return Optional.empty();
    }

    @NonNull
    public Optional<Inventory> getInventory() {
        return this.getContainer().map(BlockInventoryHolder::getInventory);
    }

    public void setLocation(@NonNull World world, @NonNull Location location) {
        this.worldName = world.getName();
        this.blockPos = BlockPos.from(location);
        this.chunkPos = this.blockPos.toChunkPos();
    }

    public void updateStockCache() {
        this.getProducts().forEach(ChestProduct::updateStockCache);
    }

    @NonNull
    public ChestShopModule getModule() {
        return this.module;
    }

    @NonNull
    public String getWorldName() {
        return this.worldName;
    }

    @NonNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    @NonNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NonNull String name) {
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

    @Override
    @NonNull
    public CompletableFuture<Double> queryBalance(@NonNull Currency currency) {
        return this.module.queryShopBalance(this, currency);
    }

    @Override
    @NonNull
    public CompletableFuture<Boolean> depositBalance(@NonNull Currency currency, double amount) {
        return this.module.depositShopBalance(this, currency, amount);
    }

    @Override
    @NonNull
    public CompletableFuture<Boolean> withdrawBalance(@NonNull Currency currency, double amount) {
        return this.module.withdrawShopBalance(this, currency, amount);
    }

    @Override
    public void open(@NonNull Player player, int page, boolean force) {
        this.module.openShop(player, this, page, force);
    }

    @Override
    public boolean canAccess(@NonNull Player player, boolean notify) {
        return true;
    }

    public boolean canManage(@NonNull Player player) {
        return this.isOwnerOrRenter(player) || this.isTrusted(player) || player.hasPermission(ChestPerms.EDIT_OTHERS);
    }

    public boolean canRename(@NonNull Player player) {
        return this.canManage(player);
    }

    public boolean canManageProducts(@NonNull Player player) {
        return this.canManage(player);
    }

    @Deprecated
    public boolean canManageBank(@NonNull Player player) {
        return this.canManage(player);
    }

    public boolean canManageRent(@NonNull Player player) {
        return ChestUtils.hasRentPermission(player) && (this.isOwner(player) || player.hasPermission(ChestPerms.EDIT_OTHERS));
    }

    public boolean canManageDisplay(@NonNull Player player) {
        return this.canManage(player) && player.hasPermission(ChestPerms.DISPLAY_CUSTOMIZATION);
    }

    public boolean canRemove(@NonNull Player player) {
        return this.isOwner(player) || player.hasPermission(ChestPerms.REMOVE_OTHERS);
    }

    public boolean canDecorate(@NonNull Player player) {
        return this.canManage(player);
    }

    public boolean isOwner(@NonNull Player player) {
        return this.ownerInfo.isUser(player);
    }

    public boolean isRenter(@NonNull Player player) {
        return this.renterInfo != null && this.renterInfo.isUser(player);
    }

    public boolean isOwnerOrRenter(@NonNull Player player) {
        return this.isOwner(player) || this.isRenter(player);
    }

    public boolean isTrusted(@NonNull Player player) {
        return this.isTrusted(player.getUniqueId());
    }

    public boolean isTrusted(@NonNull UUID playerId) {
        return this.trustedPlayers.containsKey(playerId);
    }

    @Override
    public boolean isAdminShop() {
        return this.adminShop;
    }

    public void setAdminShop(boolean adminShop) {
        this.adminShop = adminShop;
    }

    @NonNull
    public ChestProduct createProduct(@NonNull Player player, @NonNull ItemStack item, boolean bypassHandler) throws IllegalArgumentException {
        if (item.getType().isAir()) throw new IllegalArgumentException("Item must not be air");
        if (this.isProduct(item)) throw new IllegalArgumentException("Item is already a shop product");

        ItemStack stack = new ItemStack(item);
        if (ChestConfig.SHOP_PRODUCT_NEW_PRODUCTS_SINGLE_AMOUNT.get()) {
            stack.setAmount(1);
        }

        UUID globalId = UUID.randomUUID();
        ItemContent content = ContentTypes.fromItem(stack, this.module::isItemProviderAllowed);
        ChestProduct product = new ChestProduct(globalId, this);

        content.setCompareNbt(true); // Explicit enable NBT comparasion for ChestShop items.
        product.setCurrencyId(this.module.getDefinition().getDefaultCurrency());
        product.setContent(content);
        product.setPricing(FlatPricing.of(ChestConfig.SHOP_PRODUCT_INITIAL_BUY_PRICE.get(), ChestConfig.SHOP_PRODUCT_INITIAL_SELL_PRICE.get()));
        product.updatePrice(false);

        this.addProduct(product);
        return product;
    }

    @Nullable
    public ChestProduct getProduct(@NonNull ItemStack item) {
        return this.getValidProducts().stream()
            .filter(product -> product.getContent().isItemMatches(item))
            .findFirst().orElse(null);
    }

    @Nullable
    public ChestProduct getBestProduct(@NonNull ItemStack item, @NonNull TradeType tradeType) {
        return this.getBestProduct(item, tradeType, null);
    }

    @Nullable
    public ChestProduct getBestProduct(@NonNull ItemStack itemStack, @NonNull TradeType tradeType, @Nullable Player player) {
        if (!this.isTradeAllowed(tradeType)) return null;

        int stackSize = itemStack.getAmount();
        Set<ChestProduct> candidates = new HashSet<>();

        this.getValidProducts().forEach(product -> {
            if (!product.isTradeable(tradeType)) return;
            if (!product.getContent().isItemMatches(itemStack)) return;
            if (stackSize < product.getUnitSize()) return;
            if (product.getStock() == 0) return;

            candidates.add(product);
        });

        return ShopUtils.getBestProduct(candidates, tradeType, stackSize, player);
    }

    public boolean isProduct(@NonNull ItemStack item) {
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

    @NonNull
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
        return ChestConfig.isRentEnabled() && this.renterInfo != null && !this.isRentExpired();
    }

    public boolean isRentExpired() {
        return TimeUtil.isPassed(this.rentedUntil);
    }

    @NonNull
    public UserInfo getEffectiveMerchantProfile() {
        return this.renterInfo != null ? this.renterInfo : this.ownerInfo;
    }

    @NonNull
    public Optional<Player> getEffectiveMerchant() {
        return this.renterInfo == null ? this.getOwner() : this.getRenter();
    }

    @NonNull
    public Optional<Player> getRenter() {
        return Optional.ofNullable(this.renterInfo).map(profile -> Players.getPlayer(profile.id()));
    }

    @NonNull
    @Deprecated
    public Optional<Player> getRenterOrOwner() {
        return this.renterInfo == null ? this.getOwner() : this.getRenter();
    }

    @Nullable
    public UUID getRenterId() {
        return this.renterInfo == null ? null : this.renterInfo.id();
    }

    @Nullable
    public String getRenterName() {
        return this.renterInfo == null ? null : this.renterInfo.name();
    }

    public long getRentedUntil() {
        return this.rentedUntil;
    }

    public void setRentedUntil(long rentedUntil) {
        this.rentedUntil = rentedUntil;
    }

    public void setRentedBy(@NonNull Player player) {
        this.setRentedBy(UserInfo.of(player));
    }

    public void setRentedBy(@NonNull UserInfo profile) {
        this.renterInfo = profile;
    }

    public void cancelRent() {
        this.renterInfo = null;
        this.rentedUntil = -1;
    }

    public void extendRent() {
        if (this.renterInfo == null) return;

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

    @NonNull
    public UUID getOwnerId() {
        return this.ownerInfo.id();
    }

    @NonNull
    public String getOwnerName() {
        return this.ownerInfo.name();
    }

    @NonNull
    public UserInfo getOwnerInfo() {
        return this.ownerInfo;
    }

    public void setOwner(@NonNull UserInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    public void setOwner(@NonNull Player player) {
        this.setOwner(UserInfo.of(player));
    }

    @NonNull
    public Optional<Player> getOwner() {
        return Optional.ofNullable(Players.getPlayer(this.getOwnerId()));
    }

    @NonNull
    public Map<UUID, UserInfo> getTrustedPlayers() {
        return this.trustedPlayers;
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
        if (!this.isAccessible()) return null;
        if (!this.isShowcaseEnabled()) return null;

        Material blockType = this.block.getType();
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
}
