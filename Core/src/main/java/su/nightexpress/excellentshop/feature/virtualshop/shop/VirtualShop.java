package su.nightexpress.excellentshop.feature.virtualshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualPerms;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.nexshop.exception.ShopLoadException;
import su.nightexpress.excellentshop.shop.AbstractShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VirtualShop extends AbstractShop<VirtualProduct> {

    private final VirtualShopModule     module;
    private final DataManager           dataManager;
    private final VirtualShopDefinition settings;
    private final Map<String, Rotation> rotationByIdMap;

    public VirtualShop(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module, @NonNull DataManager dataManager, @NonNull Path path, @NonNull String id) {
        super(plugin, path, id);
        this.module = module;
        this.dataManager = dataManager;
        this.settings = new VirtualShopDefinition();
        this.rotationByIdMap = new HashMap<>();
    }

    @Override
    @NonNull
    public PlaceholderResolver placeholders() {
        return ShopPlaceholders.VIRTUAL_SHOP.resolver(this);
    }

    public void load() throws ShopLoadException {
        FileConfig config = this.loadConfig();

        this.loadSettings(config, "Settings");
        this.loadProducts(config, "Items");
        this.loadRotations(config, "Rotations");

        config.saveChanges();
    }

    @Override
    public void write(@NonNull FileConfig config) {
        config.set("Settings", this.settings);

        config.remove("Items");
        config.remove("Rotations");

        this.getProducts().stream()
            .sorted(Comparator.comparingInt(VirtualProduct::getPage).thenComparingInt(VirtualProduct::getSlot))
            .forEach(product -> config.set("Items." + product.getId(), product));

        this.getRotations().forEach(rotation -> config.set("Rotations." + rotation.getId(), rotation));

        config.saveChanges();
    }

    public void loadSettings(@NonNull FileConfig config, @NonNull String path) {
        this.settings.load(config, path);
    }

    public void loadProducts(@NonNull FileConfig config, @NonNull String path) {
        config.getSection(path).forEach(productId -> {
            try {
                String productPath = path + "." + productId;
                UUID globalId = config.get(ConfigTypes.UUID, productPath + ".GlobalId", UUID.randomUUID());
                VirtualProduct product = new VirtualProduct(this.dataManager, globalId, productId, this);
                product.load(config, productPath);

                this.addProduct(product);
            }
            catch (ProductLoadException exception) {
                if (exception.isFatal()) exception.printStackTrace();
                this.module.error("Product '" + productId + "' in '" + this.getId() + "' shop not loaded.");
            }
        });
    }

    public void loadRotations(@NonNull FileConfig config, @NonNull String path) {
        config.getSection(path).forEach(rotationName -> {
            String rotationPath = path + "." + rotationName;
            UUID globalId = config.get(ConfigTypes.UUID, rotationPath + ".GlobalId", UUID.randomUUID());

            Rotation rotation = new Rotation(globalId, rotationName, this);
            rotation.load(config, rotationPath);

            this.addRotation(rotation);
        });
    }

    @Override
    public void removeProduct(@NonNull String id) {
        // Remove product from rotation's configs.
        this.getRotations().forEach(rotation -> rotation.getItemMap().remove(id.toLowerCase()));

        super.removeProduct(id);
    }

    @NonNull
    public VirtualProduct createProduct(@NonNull ContentType type, @NonNull ItemStack itemStack) {
        ProductContent content = ContentTypes.create(type, itemStack, this.module::isItemProviderAllowed);
        /*ProductContent content = switch (type) {
            case ITEM -> ContentTypes.fromItem(itemStack, this.module::isItemProviderAllowed);
            case COMMAND -> new CommandContent(itemStack, new ArrayList<>());
            case EMPTY -> EmptyContent.VALUE;
        };*/

        UUID globalId = UUID.randomUUID();
        String id = ShopUtils.generateProductId(this, content);
        VirtualProduct product = new VirtualProduct(this.dataManager, globalId, id, this);

        product.setCurrencyId(this.module.getDefinition().getDefaultCurrency());
        product.setContent(content);
        return product;
    }

    @Nullable
    public VirtualProduct getBestProduct(@NonNull ItemStack item, @NonNull TradeType tradeType) {
        return this.getBestProduct(item, tradeType, null);
    }

    @Nullable
    public VirtualProduct getBestProduct(@NonNull ItemStack itemStack, @NonNull TradeType tradeType, @Nullable Player player) {
        if (!this.isTradeAllowed(tradeType)) return null;

        int stackSize = itemStack.getAmount();
        Set<VirtualProduct> candidates = new HashSet<>();

        this.getValidProducts().forEach(product -> {
            if (!product.isTradeable(tradeType)) return;
            if (!(product.getContent() instanceof ItemContent typing)) return;
            if (!typing.isItemMatches(itemStack)) return;
            if (stackSize < product.getUnitSize()) return;
            if (product.isRotating() && !product.isInRotation()) return;

            if (player != null) {
                if (!product.canTrade(player)) return;
                //if (product.getStockAmount(player, tradeType) == 0) return;
            }

            candidates.add(product);
        });

        return ShopUtils.getBestProduct(candidates, tradeType, stackSize, player);
    }

    public void invalidateData() {
        this.getProducts().forEach(VirtualProduct::invalidateData);

        this.getRotations().forEach(rotation -> {
            RotationData data = this.dataManager.getRotationData(rotation.getGlobalId());
            if (data != null) {
                data.markRemoved();
            }
        });
    }

    @Override
    public boolean isAdminShop() {
        return true;
    }

    @NonNull
    public RotationData getRotationData(@NonNull Rotation rotation) {
        RotationData data = this.dataManager.getRotationData(this, rotation);
        if (data != null && !data.isRemoved()) return data;

        RotationData newData = new RotationData(rotation.getGlobalId(), 0L, new ArrayList<>());
        this.dataManager.loadRotationData(newData);
        return newData;
    }

    public void performRotation() {
        this.getRotations().forEach(this::performRotation);
    }

    public void performRotation(@NonNull Rotation rotation) {
        RotationData data = this.getRotationData(rotation);

        data.setNextRotationDate(rotation.createNextRotationTimestamp());
        data.setProducts(rotation.generateRotationProducts());
        data.markDirty();

        data.getProducts().forEach(itemData -> {
            Product product = this.getProductById(itemData.productId());
            if (product == null) return;

            product.invalidateData();
        });
    }

    @Override
    public void open(@NonNull Player player, int page, boolean force) {
        this.module.openShop(player, this, page, force);
    }

    @Override
    public boolean canAccess(@NonNull Player player, boolean notify) {
        if (!this.hasPermission(player)) {
            if (notify) CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(player);
            return false;
        }

        return true;
    }

    @Override
    @NonNull
    public CompletableFuture<Double> queryBalance(@NonNull Currency currency) {
        return CompletableFuture.completedFuture(-1D);
    }

    @Override
    @NonNull
    public CompletableFuture<Boolean> depositBalance(@NonNull Currency currency, double amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    @NonNull
    public CompletableFuture<Boolean> withdrawBalance(@NonNull Currency currency, double amount) {
        return CompletableFuture.completedFuture(false);
    }

    public boolean hasDescription() {
        return !this.settings.getDescription().isEmpty();
    }

    public boolean hasAliases() {
        return !this.settings.getAliases().isEmpty();
    }

    public boolean hasMenuSlots() {
        return !this.settings.getMenuSlots().isEmpty();
    }

    public boolean isMenuSlot(int slot) {
        return this.settings.getMenuSlots().contains(slot);
    }

    @NonNull
    public VirtualShopModule getModule() {
        return this.module;
    }

    @NonNull
    public Set<String> getAliases() {
        return this.settings.getAliases();
    }

    @NonNull
    public Set<String> getSlashedAliases() {
        return this.settings.getAliases().stream().map(s -> "/" + s).collect(Collectors.toSet());
    }

    public void setAliases(@NonNull Set<String> aliases) {
        this.settings.setAliases(aliases);
    }

    @Override
    @NonNull
    public String getName() {
        return this.settings.getName();
    }

    @Override
    public void setName(@NonNull String name) {
        this.settings.setName(name);
    }

    @NonNull
    public List<String> getDescription() {
        return this.settings.getDescription();
    }

    public void setDescription(@NonNull List<String> description) {
        this.settings.setDescription(description);
    }

    public boolean hasPermission(@NonNull Player player) {
        if (!this.isPermissionRequired()) return true;

        return player.hasPermission(VirtualPerms.PREFIX_SHOP + this.getId());
    }

    public boolean isPermissionRequired() {
        return this.settings.isPermissionRequired();
    }

    public void setPermissionRequired(boolean isPermissionRequired) {
        this.settings.setPermissionRequired(isPermissionRequired);
    }

    @NonNull
    public NightItem getIcon() {
        return this.settings.getIcon();
    }

    public void setIcon(@NonNull NightItem icon) {
        this.settings.setIcon(icon);
    }

    @Override
    public boolean isBuyingAllowed() {
        return this.settings.isBuyingAllowed();
    }

    @Override
    public void setBuyingAllowed(boolean buyingAllowed) {
        this.settings.setBuyingAllowed(buyingAllowed);
    }

    @Override
    public boolean isSellingAllowed() {
        return this.settings.isSellingAllowed();
    }

    @Override
    public void setSellingAllowed(boolean sellingAllowed) {
        this.settings.setSellingAllowed(sellingAllowed);
    }

    @NonNull
    public Set<Integer> getMenuSlots() {
        return this.settings.getMenuSlots();
    }

    public void addMenuSlot(int slot) {
        this.settings.getMenuSlots().add(slot);
    }

    public void removeMenuSlot(int slot) {
        this.settings.getMenuSlots().remove(slot);
    }

    public void clearMenuSlots() {
        this.settings.getMenuSlots().clear();
    }

    public void setMenuSlots(@NonNull Set<Integer> menuSlots) {
        this.settings.setMenuSlots(menuSlots);
    }

    public int getPages() {
        return this.settings.getPages();
    }

    public void setPages(int pages) {
        this.settings.setPages(pages);
    }

    public boolean isPaginatedLayouts() {
        return this.settings.isPaginatedLayouts();
    }

    public void setPaginatedLayouts(boolean paginatedLayouts) {
        this.settings.setPaginatedLayouts(paginatedLayouts);
    }

    @NonNull
    public String getLayout(int page) {
        return this.settings.getLayout(page);
    }

    public void setPageLayout(int page, @NonNull String layoutName) {
        this.settings.setPageLayout(page, layoutName);
    }

    public void removePageLayout(int page) {
        this.settings.removePageLayout(page);
    }

    public boolean hasRotation(@NonNull String id) {
        return this.rotationByIdMap.containsKey(id);
    }

    public void addRotation(@NonNull Rotation rotation) {
        this.rotationByIdMap.put(rotation.getId(), rotation);
    }

    public void removeRotation(@NonNull Rotation rotation) {
        RotationData data = this.dataManager.getRotationData(rotation.getGlobalId());
        if (data != null) data.markRemoved();

        this.rotationByIdMap.remove(rotation.getId());
    }

    @NonNull
    public Map<String, Rotation> getRotationByIdMap() {
        return this.rotationByIdMap;
    }

    @NonNull
    public Set<Rotation> getRotations() {
        return Set.copyOf(this.rotationByIdMap.values());
    }

    @Nullable
    public Rotation getRotationById(@NonNull String id) {
        return this.rotationByIdMap.get(id.toLowerCase());
    }
}
