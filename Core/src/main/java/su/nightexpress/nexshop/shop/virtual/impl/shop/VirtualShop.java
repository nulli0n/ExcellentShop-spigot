package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ShopMainEditor;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.List;
import java.util.Objects;

public abstract class VirtualShop<
    S extends VirtualShop<S, P>,
    P extends VirtualProduct<P, S>> extends Shop<S, P> {

    protected final VirtualShopModule module;
    protected final VirtualShopView<S, P>   view;
    protected final JYML              configProducts;

    protected String       name;
    protected List<String> description;
    protected boolean      isPermissionRequired;
    protected ItemStack    icon;

    private ShopMainEditor editor;

    public VirtualShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module.plugin(), cfg, id);
        this.module = module;
        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "products.yml");

        JYML configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "view.yml");
        this.view = new VirtualShopView<>(this.get(), configView);

        this.placeholderMap
            .add(Placeholders.SHOP_TYPE, () -> plugin.getLangManager().getEnum(this.getType()))
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.SHOP_DESCRIPTION, () -> String.join("\n", this.getDescription()))
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.SHOP_PERMISSION_NODE, () -> VirtualPerms.PREFIX_SHOP + this.getId())
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.SHOP_PERMISSION_REQUIRED, () -> LangManager.getBoolean(this.isPermissionRequired()))
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.SHOP_VIEW_SIZE, () -> String.valueOf(this.getView().getOptions().getSize()))
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.SHOP_VIEW_TITLE, () -> this.getView().getOptions().getTitle());
    }

    @Override
    public final boolean load() {
        this.setName(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(cfg.getStringList("Description"));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));
        this.setIcon(cfg.getItem("Icon"));
        for (TradeType buyType : TradeType.values()) {
            this.setTransactionEnabled(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }
        if (!this.loadAdditionalData()) return false;
        this.loadProducts();
        return true;
    }

    protected abstract boolean loadAdditionalData();

    private void loadProducts() {
        this.getProductMap().clear();
        this.getConfigProducts().reload();
        this.getConfigProducts().getSection("List").stream().map(productId -> {
            try {
                return this.loadProduct(this.getConfigProducts(), "List." + productId, productId);
            }
            catch (Exception e) {
                this.plugin.error("Could not load '" + productId + "' product in '" + getId() + "' shop!");
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).forEach(this::addProduct);
    }

    @NotNull
    protected abstract P loadProduct(@NotNull JYML cfg, @NotNull String path, @NotNull String id);

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.hasPermission(player)) {
            if (notify) plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return false;
        }
        return this.getModule().isAvailable(player, notify);
    }

    @NotNull
    public abstract VirtualShopType getType();

    @NotNull
    public VirtualShopModule getModule() {
        return module;
    }

    @Override
    @NotNull
    public VirtualShopView<S, P> getView() {
        return this.view;
    }

    public final void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.clear();
        }
        this.clearAdditionalData();
        this.products.values().forEach(VirtualProduct::clear);
        this.products.clear();
    }

    protected abstract void clearAdditionalData();

    @Override
    public final void onSave() {
        this.saveSettings();
        this.saveProducts();
    }

    public final void saveSettings() {
        view.getConfig().set("Title", this.view.getOptions().getTitle());
        view.getConfig().set("Size", this.view.getOptions().getSize());
        view.getConfig().saveChanges();

        cfg.set("Name", this.getName());
        cfg.set("Description", this.getDescription());
        cfg.set("Permission_Required", this.isPermissionRequired());
        cfg.setItem("Icon", this.getIcon());
        this.transactions.forEach((type, isAllowed) -> cfg.set("Transaction_Allowed." + type.name(), isAllowed));
        this.saveAdditionalSettings();
        cfg.saveChanges();
    }

    protected abstract void saveAdditionalSettings();

    public final void saveProducts() {
        configProducts.set("List", null);
        this.saveAdditionalProducts();
        configProducts.saveChanges();
    }

    protected abstract void saveAdditionalProducts();

    @NotNull
    public JYML getConfigProducts() {
        return this.configProducts;
    }

    /*@NotNull
    public JYML getConfigView() {
        return this.configView;
    }*/

    @NotNull
    public ShopMainEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ShopMainEditor(this.plugin(), this);
        }
        return this.editor;
    }

    @NotNull
    public List<String> getDescription() {
        return description;
    }

    public void setDescription(@NotNull List<String> description) {
        this.description = Colorizer.apply(description);
    }

    public boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;
        return player.hasPermission(VirtualPerms.PREFIX_SHOP + this.getId());
    }

    public boolean isPermissionRequired() {
        return this.isPermissionRequired;
    }

    public void setPermissionRequired(boolean isPermissionRequired) {
        this.isPermissionRequired = isPermissionRequired;
    }

    @NotNull
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }

    public void setIcon(@NotNull ItemStack icon) {
        this.icon = new ItemStack(icon);
        this.icon.setAmount(1);
    }
}
