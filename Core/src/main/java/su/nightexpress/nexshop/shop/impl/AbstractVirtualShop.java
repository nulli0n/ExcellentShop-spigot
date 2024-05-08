package su.nightexpress.nexshop.shop.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.CommandPacker;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
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
import su.nightexpress.nexshop.shop.virtual.impl.Discount;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.StringUtil;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

public abstract class AbstractVirtualShop<P extends AbstractVirtualProduct<?>> extends AbstractShop<P> implements VirtualShop {

    public static final String FILE_NAME     = "config.yml";
    public static final String FILE_PRODUCTS = "products.yml";

    protected final VirtualShopModule module;
    protected final VirtualStock      stock;
    protected final FileConfig        configProducts;
    protected final Set<Discount>     discounts;
    protected final Set<Integer>      npcIds;

    protected boolean      loaded;
    protected String       name;
    protected List<String> description;
    protected boolean      permissionRequired;
    protected ItemStack    icon;
    protected String layoutName;

    public AbstractVirtualShop(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull File file, @NotNull String id) {
        super(plugin, file, id);
        this.module = module;

        this.configProducts = new FileConfig(this.getFile().getParentFile().getAbsolutePath(), FILE_PRODUCTS);
        this.stock = new VirtualStock(this.plugin, this);
        this.discounts = new HashSet<>();
        this.npcIds = new HashSet<>();

        this.placeholderMap.add(Placeholders.forVirtualShop(this));
    }

    @Override
    protected boolean onLoad(@NotNull FileConfig config) {
        this.setName(config.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(config.getStringList("Description"));
        this.setPermissionRequired(config.getBoolean("Permission_Required", false));
        this.setIcon(config.getItem("Icon"));
        this.setLayoutName(ConfigValue.create("Layout.Name", this.getId()).read(config));
        this.getNPCIds().addAll(IntStream.of(config.getIntArray("Citizens.Attached_NPC")).boxed().toList());

        for (TradeType buyType : TradeType.values()) {
            this.setTransactionEnabled(buyType, config.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }
        if (!this.loadAdditional(config)) return false;
        this.loadProducts();

        return true;
    }

    @Override
    protected void onSave(@NotNull FileConfig config) {
        this.saveSettings(config);
        this.saveProducts();
    }

    protected abstract boolean loadAdditional(@NotNull FileConfig config);

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
    protected P loadProduct(@NotNull FileConfig cfg, @NotNull String path, @NotNull String id) {
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
        if (!this.module.isAvailable(player, false)) return null;
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
    public final void saveSettings() {
        FileConfig config = this.getConfig();
        this.saveSettings(config);
        config.saveChanges();
    }

    protected final void saveSettings(@NotNull FileConfig config) {
        config.set("Name", this.getName());
        config.set("Description", this.getDescription());
        config.set("Permission_Required", this.isPermissionRequired());
        config.setItem("Icon", this.getIcon());
        config.set("Layout.Name", this.getLayoutName());
        config.setIntArray("Citizens.Attached_NPC", this.getNPCIds().stream().mapToInt(Number::intValue).toArray());
        this.transactions.forEach((type, isAllowed) -> config.set("Transaction_Allowed." + type.name(), isAllowed));
        this.saveAdditionalSettings(config);
    }

    protected abstract void saveAdditionalSettings(@NotNull FileConfig config);

    @Override
    public final void saveProducts() {
        configProducts.set("List", null);
        this.saveAdditionalProducts();
        configProducts.saveChanges();
    }

    @Override
    public void saveProduct(@NotNull Product product) {
        P virtualProduct = this.getProductById(product.getId());
        if (virtualProduct == null) return;

        this.writeProduct(virtualProduct);
        this.configProducts.saveChanges();
    }

    protected abstract void writeProduct(@NotNull P product);

    protected abstract void saveAdditionalProducts();

    @NotNull
    public final String getProductSavePath(@NotNull Product product) {
        return "List." + product.getId();
    }

    @Override
    public void open(@NotNull Player player, int page) {
        this.module.openShop(player, this, page);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.isLoaded()) {
            return false;
        }

        if (!this.hasPermission(player)) {
            if (notify) Lang.ERROR_NO_PERMISSION.getMessage().send(player);
            return false;
        }

        return true;
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

    @NotNull
    public FileConfig getConfigProducts() {
        return this.configProducts;
    }

    @NotNull
    public VirtualStock getStock() {
        return stock;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return description;
    }

    @Override
    public void setDescription(@NotNull List<String> description) {
        this.description = description;
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
    public String getLayoutName() {
        return layoutName;
    }

    @Override
    public void setLayoutName(@NotNull String layoutName) {
        this.layoutName = layoutName.toLowerCase();
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
