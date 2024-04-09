package su.nightexpress.nexshop.shop.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.CommandPacker;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.currency.handler.VaultEconomyHandler;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.impl.handler.VanillaCommandHandler;
import su.nightexpress.nexshop.shop.impl.handler.VanillaItemHandler;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ShopMainEditor;
import su.nightexpress.nexshop.shop.virtual.impl.Discount;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nexshop.shop.virtual.menu.ShopMenu;

import java.util.*;
import java.util.stream.IntStream;

public abstract class AbstractVirtualShop<P extends AbstractVirtualProduct<?>> extends AbstractShop<P> implements VirtualShop {

    public static final    String CONFIG_NAME   = "config.yml";
    protected static final String FILE_PRODUCTS = "products.yml";
    protected static final String FILE_VIEW     = "view.yml";

    protected final VirtualShopModule module;
    protected final ShopMenu          view;
    protected final VirtualStock      stock;
    protected final JYML              configProducts;
    protected final Set<Discount>     discounts;
    protected final Set<Integer>      npcIds;

    protected boolean      loaded;
    protected String       name;
    protected List<String> description;
    protected boolean      permissionRequired;
    protected ItemStack    icon;

    private ShopMainEditor editor;

    public AbstractVirtualShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module.plugin(), cfg, id);
        this.module = module;
        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), FILE_PRODUCTS);

        JYML configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), FILE_VIEW);
        this.view = new ShopMenu(this.plugin, this.module, this, configView);

        this.stock = new VirtualStock(this.plugin, this);

        this.discounts = new HashSet<>();
        this.npcIds = new HashSet<>();

        this.placeholderMap
            .add(Placeholders.SHOP_TYPE, () -> plugin.getLangManager().getEnum(this.getType()))
            .add(Placeholders.SHOP_DESCRIPTION, () -> String.join("\n", this.getDescription()))
            .add(Placeholders.SHOP_PERMISSION_NODE, () -> VirtualPerms.PREFIX_SHOP + this.getId())
            .add(Placeholders.SHOP_PERMISSION_REQUIRED, () -> LangManager.getBoolean(this.isPermissionRequired()))
            .add(Placeholders.SHOP_VIEW_SIZE, () -> String.valueOf(this.getView().getOptions().getSize()))
            .add(Placeholders.SHOP_VIEW_TITLE, () -> this.getView().getOptions().getTitle())
            .add(Placeholders.SHOP_DISCOUNT_AMOUNT, () -> NumberUtil.format(this.getDiscountPlain()))
            .add(Placeholders.SHOP_NPC_IDS, () -> String.join(", ", this.getNPCIds().stream().map(String::valueOf).toList()));
    }

    @Override
    public final boolean load() {
        this.setName(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(cfg.getStringList("Description"));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));
        this.setIcon(cfg.getItem("Icon"));
        this.getNPCIds().addAll(IntStream.of(cfg.getIntArray("Citizens.Attached_NPC")).boxed().toList());

        for (TradeType buyType : TradeType.values()) {
            this.setTransactionEnabled(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }
        if (!this.loadAdditional()) return false;
        this.loadProducts();
        return true;
    }

    protected abstract boolean loadAdditional();

    private void loadProducts() {
        this.getProductMap().clear();
        this.getConfigProducts().reload();
        this.getConfigProducts().getSection("List").forEach(productId -> {
            P product = this.loadProduct(this.getConfigProducts(), "List." + productId, productId);
            if (product == null) {
                this.getModule().warn("Product not loaded: '" + productId + "' in '" + this.getId() + "' shop.");
                return;
            }
            this.addProduct(product);
        });
        this.getConfigProducts().saveChanges();
    }

    @NotNull
    public P createProduct(@NotNull Currency currency, @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        return this.createProduct(this.generateProductId(packer), currency, handler, packer);
    }

    @NotNull
    public abstract P createProduct(@NotNull String id, @NotNull Currency currency,
                                    @NotNull ProductHandler handler, @NotNull ProductPacker packer);

    @Nullable
    protected P loadProduct(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        String currencyId = cfg.getString(path + ".Currency", VaultEconomyHandler.ID);
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            currency = CurrencyManager.DUMMY_CURRENCY;
            this.getModule().warn("Invalid currency '" + currencyId + "' for '" + id + "' product in '" + this.getId() + "' shop. Install missing plugin or change currency in editor.");
        }

        if (!cfg.contains(path + ".Handler")) {
            ItemStack item = cfg.getItemEncoded(path + ".Content.Item");
            if (item != null && !item.getType().isAir()) {
                cfg.set(path + ".Handler", VanillaItemHandler.NAME);
            }
            else cfg.set(path + ".Handler", VanillaCommandHandler.NAME);
        }

        String handlerId = cfg.getString(path + ".Handler", VanillaItemHandler.NAME);
        ProductHandler handler = ProductHandlerRegistry.getHandler(handlerId);
        if (handler == null) {
            handler = ProductHandlerRegistry.forBukkitItem();
            this.getModule().warn("Invalid handler '" + handlerId + "' for '" + id + "' product in '" + this.getId() + "' shop. Using default one...");
        }

        ProductPacker packer = handler.createPacker();
        if (!packer.load(cfg, path)) {
            this.getModule().error("Invalid data for '" + id + "' product in '" + this.getId() + "' shop.");
            return null;
        }

        P product = this.createProduct(id, currency, handler, packer);

        product.loadAdditional(cfg, path);
        product.setAllowedRanks(cfg.getStringSet(path + ".Allowed_Ranks"));
        product.setRequiredPermissions(cfg.getStringSet(path + ".Required_Permissions"));
        product.setPricer(AbstractProductPricer.read(cfg, path + ".Price"));
        product.setStockValues(StockValues.read(cfg, path + ".Stock.GLOBAL"));
        product.setLimitValues(StockValues.read(cfg, path + ".Stock.PLAYER"));
        return product;
    }

    @Override
    @Nullable
    public VirtualProduct getBestProduct(@NotNull Player player, @NotNull ItemStack item, @NotNull TradeType tradeType) {
        if (!this.canAccess(player, false)) return null;
        if (!this.isTransactionEnabled(tradeType)) return null;

        var stream = this.getProducts().stream().filter(product -> {
            if (!product.isTradeable(tradeType) || !product.hasAccess(player)) return false;
            if (!(product.getPacker() instanceof ItemPacker itemPacker)) return false;
            if (!itemPacker.isItemMatches(item)) return false;
            if (product instanceof RotatingProduct rotatingProduct && !rotatingProduct.isInRotation()) return false;
            return product.getAvailableAmount(player, tradeType) != 0;
        });

        Comparator<VirtualProduct> comparator = Comparator.comparingDouble(product -> product.getPrice(player, tradeType));

        return (tradeType == TradeType.BUY ? stream.min(comparator) : stream.max(comparator)).orElse(null);
    }

    @Override
    public final void onSave() {
        this.saveSettings();
        this.saveProducts();
    }

    public final void saveSettings() {
        view.getConfig().set("Settings.Title", this.view.getOptions().getTitle());
        view.getConfig().set("Settings.Size", this.view.getOptions().getSize());
        view.getConfig().saveChanges();

        cfg.set("Name", this.getName());
        cfg.set("Description", this.getDescription());
        cfg.set("Permission_Required", this.isPermissionRequired());
        cfg.setItem("Icon", this.getIcon());
        cfg.setIntArray("Citizens.Attached_NPC", this.getNPCIds().stream().mapToInt(Number::intValue).toArray());
        this.transactions.forEach((type, isAllowed) -> cfg.set("Transaction_Allowed." + type.name(), isAllowed));
        this.saveAdditionalSettings();
        cfg.saveChanges();
    }

    protected abstract void saveAdditionalSettings();

    @Override
    public final void saveProducts() {
        configProducts.set("List", null);
        this.saveAdditionalProducts();
        configProducts.saveChanges();
    }

    protected abstract void saveAdditionalProducts();

    protected abstract void clearAdditionalData();

    @Override
    public final void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.clear();
        }
        this.clearAdditionalData();
        this.products.values().forEach(AbstractVirtualProduct::clear);
        this.products.clear();
    }

    @Override
    public void open(@NotNull Player player, int page) {
        if (!this.isLoaded()) {
            return;
        }

        super.open(player, page);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.isLoaded()) {
            return false;
        }

        if (!this.hasPermission(player)) {
            if (notify) plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return false;
        }

        return this.getModule().isAvailable(player, notify);
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @NotNull
    public VirtualShopModule getModule() {
        return module;
    }

    @Override
    @NotNull
    public ShopMenu getView() {
        return this.view;
    }

    @NotNull
    public JYML getConfigProducts() {
        return this.configProducts;
    }

    @NotNull
    public VirtualStock getStock() {
        return stock;
    }

    @NotNull
    public ShopMainEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ShopMainEditor(this.plugin(), this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return description;
    }

    @Override
    public void setDescription(@NotNull List<String> description) {
        this.description = Colorizer.apply(description);
    }

    @Override
    public boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;
        return player.hasPermission(VirtualPerms.PREFIX_SHOP + this.getId());
    }

    @Override
    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    @Override
    public void setPermissionRequired(boolean isPermissionRequired) {
        this.permissionRequired = isPermissionRequired;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }

    @Override
    public void setIcon(@NotNull ItemStack icon) {
        this.icon = new ItemStack(icon);
        this.icon.setAmount(1);
    }

    @Override
    @NotNull
    public Set<Discount> getDiscounts() {
        this.discounts.removeIf(Discount::isExpired);
        return this.discounts;
    }

    @Override
    @NotNull
    public Set<Integer> getNPCIds() {
        return this.npcIds;
    }

    @NotNull
    public String generateProductId(@NotNull ProductPacker packer) {
        String id;
        if (packer instanceof ItemPacker itemPacker) {
            if (itemPacker.getItem().getType() == Material.BARRIER) {
                id = "new_item";
            }
            else {
                id = ItemUtil.getItemName(itemPacker.getItem());
            }
        }
        else if (packer instanceof CommandPacker) {
            id = "command_item";
        }
        else id = UUID.randomUUID().toString();

        id = StringUtil.lowerCaseUnderscore(id);

        int count = 0;
        while (this.getProductById(id) != null) {
            id = id + "_" + (++count);
        }

        return id;
    }
}
