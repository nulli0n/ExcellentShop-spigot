package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class VirtualShop extends AbstractShop<VirtualProduct> {

    public static final String FILE_NAME     = "config.yml";
    public static final String FILE_PRODUCTS = "products.yml";

    private final VirtualShopModule     module;
    private final FileConfig            configProducts;
    private final Set<Discount>         discounts;
    private final Map<Integer, String>  pageLayouts;
    private final Map<String, Rotation> rotationByIdMap;

    private Set<String>  aliases;
    private List<String> description;
    private boolean      permissionRequired;
    private NightItem    icon;
    private int          mainMenuSlot;
    private String       layoutName;
    private int          pages;

    public VirtualShop(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull File file, @NotNull String id) {
        super(plugin, file, id);
        this.module = module;

        this.setName(StringUtil.capitalizeUnderscored(id));
        this.setDescription(new ArrayList<>());
        this.setIcon(NightItem.fromType(Material.CHEST));
        this.setDefaultLayout(Placeholders.DEFAULT);
        this.setPages(1);
        this.setAliases(new HashSet<>());
        this.configProducts = new FileConfig(this.getFile().getParentFile().getAbsolutePath(), FILE_PRODUCTS);
        this.discounts = new HashSet<>();
        this.pageLayouts = new HashMap<>();
        this.rotationByIdMap = new HashMap<>();
        this.mainMenuSlot = -1;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forVirtualShop(this);
    }

    @Override
    protected boolean onLoad(@NotNull FileConfig config) {
        this.setName(config.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(config.getStringList("Description"));
        this.setPermissionRequired(config.getBoolean("Permission_Required", false));
        this.setIcon(config.getCosmeticItem("Icon"));
        this.setMainMenuSlot(config.getInt("MainMenu.Slot", -1));
        this.setPages(config.getInt("Pages", 1));
        this.setAliases(config.getStringSet("Aliases"));

        this.setDefaultLayout(ConfigValue.create("Layout.Name", Placeholders.DEFAULT).read(config));
        config.getSection("Layout.ByPage").forEach(sId -> {
            int page = NumberUtil.getIntegerAbs(sId, -1);
            if (page < 0) return;

            this.pageLayouts.put(page, config.getString("Layout.ByPage." + sId, Placeholders.DEFAULT));
        });

        this.setBuyingAllowed(config.getBoolean("Transaction_Allowed.BUY", true));
        this.setSellingAllowed(config.getBoolean("Transaction_Allowed.SELL", true));

        this.loadProducts();
        this.loadRotations(config);

        return true;
    }

    @Override
    protected void onSave(@NotNull FileConfig config) {
        this.writeSettings(config);
        this.writeRotations(config);
        this.saveProducts();
    }

    @Override
    public void saveSettings() {
        this.writeSection(this::writeSettings);
    }

    @Override
    public void saveRotations() {
        this.writeSection(this::writeRotations);
    }

    private void writeSection(@NotNull Consumer<FileConfig> consumer) {
        FileConfig config = this.getConfig();
        consumer.accept(config);
        config.saveChanges();
    }

    private void writeSettings(@NotNull FileConfig config) {

        config.set("Name", this.name);
        config.set("Description", this.description);
        config.set("Icon", this.icon);
        config.set("Pages", this.pages);
        config.set("Permission_Required", this.permissionRequired);
        config.set("Transaction_Allowed.BUY", this.buyingAllowed);
        config.set("Transaction_Allowed.SELL", this.sellingAllowed);
        config.set("MainMenu.Slot", this.mainMenuSlot);
        config.set("Aliases", this.aliases);
        config.set("Layout.Name", this.layoutName);
        config.remove("Layout.ByPage");
        this.pageLayouts.forEach((page, lName) -> config.set("Layout.ByPage." + page, lName));
    }

    private void writeRotations(@NotNull FileConfig config) {
        config.remove("Rotations");
        this.rotationByIdMap.forEach((id, rotation) -> config.set("Rotations." + id, rotation));
    }

    @Override
    public void saveProducts() {
        this.configProducts.remove("List");

        this.getValidProducts().stream()
            .sorted(Comparator.comparingInt(VirtualProduct::getPage).thenComparingInt(VirtualProduct::getSlot))
            .forEach(this::writeProduct);

        this.configProducts.saveChanges();
    }

    @Override
    @Deprecated
    public void saveProduct(@NotNull Product product) {
        VirtualProduct virtualProduct = this.getProductById(product.getId());
        if (virtualProduct == null) return;

        this.saveProduct(virtualProduct);
    }

    public void saveProduct(@NotNull VirtualProduct product) {
        this.writeProduct(product);
        this.configProducts.saveChanges();
    }

    private void writeProduct(@NotNull VirtualProduct product) {
        this.configProducts.set("List." + product.getId(), product);
    }

    private void loadProducts() {
        this.products.clear();
        this.configProducts.reload();
        this.configProducts.getSection("List").forEach(productId -> {
            VirtualProduct product = this.loadProduct(this.configProducts, "List." + productId, productId);
            if (product == null) {
                this.module.warn("Product not loaded: '" + productId + "' in '" + this.getId() + "' shop.");
                return;
            }
            this.addProduct(product);
        });
        this.configProducts.saveChanges();
    }

    private void loadRotations(@NotNull FileConfig config) {
        config.getSection("Rotations").forEach(sId -> {
            String path = "Rotations." + sId;

            Rotation rotation = new Rotation(sId, this);
            rotation.load(config, path);

            this.addRotation(rotation);
        });
    }

    @NotNull
    @Deprecated
    public VirtualProduct createProduct(@NotNull Currency currency, @NotNull ProductTyping typing) {
        return new VirtualProduct(plugin, ShopUtils.generateProductId(this, typing), this, currency, typing);
    }

    @Nullable
    protected VirtualProduct loadProduct(@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        String currencyId = CurrencyId.reroute(config.getString(path + ".Currency", CurrencyId.VAULT));
        Currency currency = EconomyBridge.getCurrencyOrDummy(currencyId);
        if (currency.isDummy()) {
            this.module.warn("Invalid currency '" + currencyId + "' for '" + id + "' product in '" + this.getId() + "' shop. Install missing plugin or change currency in editor.");
        }

        // Legacy stuff
        if (!config.contains(path + ".Handler")) {
            config.set(path + ".Handler", "bukkit_item");
        }
        // Legacy end

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

        ProductType type = config.getEnum(path + ".Type", ProductType.class, ProductType.VANILLA);
        ProductTyping typing = ProductTypes.read(this.module, type, config, path);

        VirtualProduct product = new VirtualProduct(plugin, id, this, currency, typing);
        product.load(config, path);

        return product;
    }

    @Override
    public void removeProduct(@NotNull String id) {
        // Remove product from rotation's configs.
        this.getRotations().forEach(rotation -> rotation.getItemMap().remove(id.toLowerCase()));

        super.removeProduct(id);
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
            if (!(product.getType() instanceof PhysicalTyping typing)) return;
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

//        var stream = this.getValidProducts().stream().filter(product -> {
//            if (!product.isTradeable(tradeType)) return false;
//            if (!(product.getType() instanceof PhysicalTyping typing)) return false;
//            if (!typing.isItemMatches(itemStack)) return false;
//            if (stackSize < product.getUnitAmount()) return false;
//            if (product.isRotating() && !product.isInRotation()) return false;
//
//            if (player != null) {
//                return product.hasAccess(player) && product.getAvailableAmount(player, tradeType) != 0;
//            }
//
//            return true;
//        });
//
//        Comparator<VirtualProduct> comparator = Comparator.comparingDouble(product -> product.getPrice(tradeType, player) * UnitUtils.amountToUnits(product, stackSize));
//
//        return (tradeType == TradeType.BUY ? stream.min(comparator) : stream.max(comparator)).orElse(null);
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

        super.update();

//        this.getDiscountConfigs().forEach(discount -> {
//            if (discount.isDiscountTime()) {
//                discount.update();
//            }
//        });
    }

    public boolean isDataLoaded() {
        return this.plugin.getDataManager().isLoaded();
    }

    @NotNull
    private RotationData getRotationData(@NotNull Rotation rotation) {
        return this.plugin.getDataManager().getRotationDataOrCreate(rotation);
    }

    public void performRotation() {
        if (!this.isDataLoaded()) return;

        this.getRotations().forEach(this::performRotation);
    }

    public void performRotation(@NotNull Rotation rotation) {
        if (!this.isDataLoaded()) return;

        RotationData data = this.getRotationData(rotation);

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
        this.plugin.getDataManager().resetPriceDatas(products);
        this.plugin.getDataManager().resetStockDatas(products);
    }

    public boolean tryRotate() {
        if (!this.isDataLoaded()) return false;

        AtomicInteger rotated = new AtomicInteger(0);

        this.getRotations().forEach(rotation -> {
            if (rotation.countItems() == 0) return;
            if (rotation.countAllSlots() == 0) return;

            RotationData data = this.getRotationData(rotation);
            if (!data.isRotationTime()) return;

            this.performRotation(rotation);
            rotated.addAndGet(rotation.countAllSlots());
        });
        if (rotated.get() == 0) return false;

        Players.getOnline().forEach(player -> {
            if (!this.canAccess(player, false)) return;

            VirtualLang.SHOP_ROTATION_NOTIFY.getMessage().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, rotated.get()).replace(this.replacePlaceholders()));
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
            if (notify) Lang.ERROR_NO_PERMISSION.getMessage(this.plugin).send(player);
            return false;
        }

        return true;
    }

    public boolean isMainMenuSlotDisabled() {
        return this.mainMenuSlot < 0;
    }

    @NotNull
    public VirtualShopModule getModule() {
        return this.module;
    }

    @NotNull
    public FileConfig getConfigProducts() {
        return this.configProducts;
    }

    @NotNull
    public Set<String> getAliases() {
        return this.aliases;
    }

    public void setAliases(@NotNull Set<String> aliases) {
        this.aliases = new HashSet<>(aliases);
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    public void setDescription(@NotNull List<String> description) {
        this.description = description;
    }

    public boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;

        return player.hasPermission(VirtualPerms.PREFIX_SHOP + this.getId());
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public void setPermissionRequired(boolean isPermissionRequired) {
        this.permissionRequired = isPermissionRequired;
    }

    @NotNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    public void setIcon(@NotNull NightItem icon) {
        this.icon = icon.copy().setAmount(1);
    }

    public int getMainMenuSlot() {
        return this.mainMenuSlot;
    }

    public void setMainMenuSlot(int mainMenuSlot) {
        this.mainMenuSlot = mainMenuSlot;
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
    }

    @NotNull
    public String getDefaultLayout() {
        return this.layoutName;
    }

    public void setDefaultLayout(@NotNull String layoutName) {
        this.layoutName = layoutName.toLowerCase();
    }

    @NotNull
    public String getLayout(int page) {
        return this.pageLayouts.getOrDefault(page, this.layoutName);
    }

    public void setLayout(int page, @Nullable String layoutName) {
        if (layoutName == null) {
            this.pageLayouts.remove(page);
        }
        else this.pageLayouts.put(page, layoutName.toLowerCase());
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
