package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.nexshop.exception.ShopLoadException;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.content.ContentTypes;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.product.content.impl.CommandContent;
import su.nightexpress.nexshop.product.content.impl.EmptyContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class VirtualShop extends AbstractShop<VirtualProduct> {

    private final VirtualShopModule     module;
    private final ShopSettings          settings;
    private final Set<Discount>         discounts;
    private final Map<String, Rotation> rotationByIdMap;

    public VirtualShop(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull Path path, @NotNull String id) {
        super(plugin, path, id);
        this.module = module;
        this.settings = new ShopSettings();
        this.discounts = new HashSet<>();
        this.rotationByIdMap = new HashMap<>();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forVirtualShop(this);
    }

    public void load() throws ShopLoadException {
        FileConfig config = this.loadConfig();

        this.loadSettings(config, "Settings");
        this.loadProducts(config, "Items");
        this.loadRotations(config, "Rotations");

        config.saveChanges();
    }

    @Override
    public void write(@NotNull FileConfig config) {
        config.set("Settings", this.settings);

        config.remove("Items");
        config.remove("Rotations");

        this.getProducts().stream()
            .sorted(Comparator.comparingInt(VirtualProduct::getPage).thenComparingInt(VirtualProduct::getSlot))
            .forEach(product -> config.set("Items." + product.getId(), product));

        this.getRotations().forEach(rotation -> config.set("Rotations." + rotation.getId(), rotation));

        config.saveChanges();
    }

    public void loadSettings(@NotNull FileConfig config, @NotNull String path) {
        this.settings.load(config, path);
    }

    public void loadProducts(@NotNull FileConfig config, @NotNull String path) {
        config.getSection(path).forEach(productId -> {
            try {
                VirtualProduct product = new VirtualProduct(productId, this);
                product.load(config, path + "." + productId);

                this.addProduct(product);
            }
            catch (ProductLoadException exception) {
                if (exception.isFatal()) exception.printStackTrace();
                this.module.error("Product '" + productId + "' in '" + this.getId() + "' shop not loaded.");
            }
        });
    }

    public void loadRotations(@NotNull FileConfig config, @NotNull String path) {
        config.getSection(path).forEach(sId -> {
            Rotation rotation = new Rotation(sId, this);
            rotation.load(config, path + "." + sId);

            this.addRotation(rotation);
        });
    }

    @Override
    public void removeProduct(@NotNull String id) {
        // Remove product from rotation's configs.
        this.getRotations().forEach(rotation -> rotation.getItemMap().remove(id.toLowerCase()));

        super.removeProduct(id);
    }

    @NotNull
    public VirtualProduct createProduct(@NotNull ContentType type, @NotNull ItemStack itemStack) {
        ProductContent content = switch (type) {
            case ITEM -> ContentTypes.fromItem(itemStack, this.module::isItemProviderAllowed);
            case COMMAND -> new CommandContent(itemStack, new ArrayList<>());
            case EMPTY -> EmptyContent.VALUE;
        };

        String id = ShopUtils.generateProductId(this, content);
        VirtualProduct product = new VirtualProduct(id, this);

        product.setCurrencyId(this.module.getSettings().getDefaultCurrency());
        product.setContent(content);
        return product;
    }

    @Nullable
    public VirtualProduct getBestProduct(@NotNull ItemStack item, @NotNull TradeType tradeType) {
        return this.getBestProduct(item, tradeType, null);
    }

    @Nullable
    public VirtualProduct getBestProduct(@NotNull ItemStack itemStack, @NotNull TradeType tradeType, @Nullable Player player) {
        if (!this.isTradeAllowed(tradeType)) return null;

        int stackSize = itemStack.getAmount();
        Set<VirtualProduct> candidates = new HashSet<>();

        this.getValidProducts().forEach(product -> {
            if (!product.isTradeable(tradeType)) return;
            if (!(product.getContent() instanceof ItemContent typing)) return;
            if (!typing.isItemMatches(itemStack)) return;
            if (stackSize < product.getUnitAmount()) return;
            if (product.isRotating() && !product.isInRotation()) return;

            if (player != null) {
                if (!product.hasAccess(player)) return;
                if (product.getAvailableAmount(player, tradeType) == 0) return;
            }

            candidates.add(product);
        });

        return ShopUtils.getBestProduct(candidates, tradeType, stackSize, player);
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        super.onTransaction(event);

        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        Player player = event.getPlayer();
        TradeType tradeType = result.getTradeType();
        int amount = result.getUnits();

        product.consumeStock(tradeType, amount, null); // Consume global stock if present.
        product.consumeStock(tradeType, amount, player.getUniqueId()); // Consume player stock if present.
        product.storeStock(tradeType.getOpposite(), amount, null); // Populate global stock if present.
    }

    @Override
    public void update() {
        this.tryRotate();

//        this.getDiscountConfigs().forEach(discount -> {
//            if (discount.isDiscountTime()) {
//                discount.update();
//            }
//        });
    }

    public void performRotation() {
        this.getRotations().forEach(this::performRotation);
    }

    public void performRotation(@NotNull Rotation rotation) {
        this.plugin.dataAccess(dataManager -> {
            RotationData data = dataManager.getRotationDataOrCreate(rotation);

            data.setNextRotationDate(rotation.createNextRotationTimestamp());
            data.setProducts(rotation.generateRotationProducts());
            data.setSaveRequired(true);

            Set<Product> products = new HashSet<>();
            data.getProducts().values().forEach(productIds -> {
                products.addAll(productIds.stream().map(this::getProductById).filter(Objects::nonNull).toList());
            });

            // Reset price & stock datas (mark all as 'expired').
            // It's useful when those products has dynamic/float prices and stock values.
            // So we start from scratch on each rotation.
            dataManager.resetPriceDatas(products);
            dataManager.resetStockDatas(products);
        });
    }

    public boolean tryRotate() {
        AtomicInteger rotated = new AtomicInteger(0);

        this.plugin.dataAccess(dataManager -> {
            this.getRotations().forEach(rotation -> {
                if (rotation.countItems() == 0) return;
                if (rotation.countAllSlots() == 0) return;

                RotationData data = dataManager.getRotationDataOrCreate(rotation);
                if (!data.isRotationTime()) return;

                this.performRotation(rotation);
                rotated.addAndGet(rotation.countAllSlots());
            });
        });

        if (rotated.get() == 0) return false;

        Players.getOnline().forEach(player -> {
            if (!this.canAccess(player, false)) return;

            VirtualLang.SHOP_ROTATION_NOTIFY.message().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, rotated.get()).replace(this.replacePlaceholders()));
        });

        return true;
    }

    @Override
    public void open(@NotNull Player player, int page, boolean force) {
        this.module.openShop(player, this, page, force);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.hasPermission(player)) {
            if (notify) CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(player);
            return false;
        }

        return true;
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

    @NotNull
    public VirtualShopModule getModule() {
        return this.module;
    }

    @NotNull
    public Set<String> getAliases() {
        return this.settings.getAliases();
    }

    @NotNull
    public Set<String> getSlashedAliases() {
        return this.settings.getAliases().stream().map(s -> "/" + s).collect(Collectors.toSet());
    }

    public void setAliases(@NotNull Set<String> aliases) {
        this.settings.setAliases(aliases);
    }

    @Override
    @NotNull
    public String getName() {
        return this.settings.getName();
    }

    @Override
    public void setName(@NotNull String name) {
        this.settings.setName(name);
    }

    @NotNull
    public List<String> getDescription() {
        return this.settings.getDescription();
    }

    public void setDescription(@NotNull List<String> description) {
        this.settings.setDescription(description);
    }

    public boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;

        return player.hasPermission(VirtualPerms.PREFIX_SHOP + this.getId());
    }

    public boolean isPermissionRequired() {
        return this.settings.isPermissionRequired();
    }

    public void setPermissionRequired(boolean isPermissionRequired) {
        this.settings.setPermissionRequired(isPermissionRequired);
    }

    @NotNull
    public NightItem getIcon() {
        return this.settings.getIcon();
    }

    public void setIcon(@NotNull NightItem icon) {
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

    @NotNull
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

    public void setMenuSlots(@NotNull Set<Integer> menuSlots) {
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

    @NotNull
    public String getLayout(int page) {
        return this.settings.getLayout(page);
    }

    public void setPageLayout(int page, @NotNull String layoutName) {
        this.settings.setPageLayout(page, layoutName);
    }

    public void removePageLayout(int page) {
        this.settings.removePageLayout(page);
    }

    public boolean hasRotation(@NotNull String id) {
        return this.rotationByIdMap.containsKey(id);
    }

    public void addRotation(@NotNull Rotation rotation) {
        this.rotationByIdMap.put(rotation.getId(), rotation);
    }

    public void removeRotation(@NotNull Rotation rotation) {
        this.rotationByIdMap.remove(rotation.getId());
    }

    @NotNull
    public Map<String, Rotation> getRotationByIdMap() {
        return this.rotationByIdMap;
    }

    @NotNull
    public Set<Rotation> getRotations() {
        return new HashSet<>(this.rotationByIdMap.values());
    }

    @Nullable
    public Rotation getRotationById(@NotNull String id) {
        return this.rotationByIdMap.get(id.toLowerCase());
    }

    @NotNull
    public Set<Discount> getDiscounts() {
        this.discounts.removeIf(Discount::isExpired);
        return this.discounts;
    }

    public boolean hasDiscount() {
        return this.getDiscountPlain() != 0D;
    }

    public boolean hasDiscount(@NotNull VirtualProduct product) {
        return this.getDiscountPlain(product) != 0D;
    }

    public double getDiscountModifier() {
        return 1D - this.getDiscountPlain() / 100D;
    }

    public double getDiscountModifier(@NotNull VirtualProduct product) {
        return 1D - this.getDiscountPlain(product) / 100D;
    }

    public double getDiscountPlain() {
        return Math.min(100D, this.getDiscounts().stream().mapToDouble(Discount::getDiscountPlain).sum());
    }

    public double getDiscountPlain(@NotNull VirtualProduct product) {
        return product.isDiscountAllowed() ? this.getDiscountPlain() : 0D;
    }
}
