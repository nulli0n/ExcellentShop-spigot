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
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class VirtualShop extends Shop<VirtualShop, VirtualProduct> {

    private final VirtualShopModule module;
    private final VirtualShopView view;
    private final JYML configProducts;
    //private final JYML configView;

    private final Set<VirtualDiscount> discountConfigs;
    private final Set<Integer> npcIds;

    private String       name;
    private List<String> description;
    private int          pages;
    private boolean      isPermissionRequired;
    private ItemStack    icon;

    private ShopMainEditor editor;

    public VirtualShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module.plugin(), cfg, id);
        this.module = module;
        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "products.yml");
        this.discountConfigs = new HashSet<>();
        this.npcIds = new HashSet<>();

        JYML configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "view.yml");
        this.view = new VirtualShopView(this, configView);

        this.placeholderMap
            .add(Placeholders.SHOP_BANK_BALANCE, () -> plugin.getCurrencyManager().getCurrencies().stream()
                .map(currency -> currency.format(this.getBank().getBalance(currency))).collect(Collectors.joining(", ")))
            .add(Placeholders.SHOP_VIRTUAL_DESCRIPTION, () -> String.join("\n", this.getDescription()))
            .add(Placeholders.SHOP_VIRTUAL_PERMISSION_NODE, () -> VirtualPerms.PREFIX_SHOP + this.getId())
            .add(Placeholders.SHOP_VIRTUAL_PERMISSION_REQUIRED, () -> LangManager.getBoolean(this.isPermissionRequired()))
            .add(Placeholders.SHOP_VIRTUAL_PAGES, () -> String.valueOf(this.getPages()))
            .add(Placeholders.SHOP_VIRTUAL_VIEW_SIZE, () -> String.valueOf(this.getView().getOptions().getSize()))
            .add(Placeholders.SHOP_VIRTUAL_VIEW_TITLE, () -> this.getView().getOptions().getTitle())
            .add(Placeholders.SHOP_VIRTUAL_NPC_IDS, () -> String.join(", ", this.getNPCIds().stream().map(String::valueOf).toList()))
            ;
    }

    @Override
    public boolean load() {
        this.setBank(new VirtualShopBank(this));
        this.setName(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(cfg.getStringList("Description"));
        this.setPages(cfg.getInt("Pages", 1));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));
        this.setIcon(cfg.getItem("Icon"));
        this.getNPCIds().addAll(IntStream.of(cfg.getIntArray("Citizens.Attached_NPC")).boxed().toList());
        for (TradeType buyType : TradeType.values()) {
            this.setTransactionEnabled(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }
        for (String sId : cfg.getSection("Discounts")) {
            this.addDiscountConfig(VirtualDiscount.read(cfg, "Discounts." + sId));
        }
        this.loadProducts();
        return true;
    }

    private void loadProducts() {
        this.getProductMap().clear();
        this.getConfigProducts().reload();
        this.getConfigProducts().getSection("List").stream().map(productId -> {
            try {
                return VirtualProduct.read(this.getConfigProducts(), "List." + productId, productId);
            }
            catch (Exception e) {
                this.plugin.error("Could not load '" + productId + "' product in '" + getId() + "' shop!");
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).forEach(this::addProduct);
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.hasPermission(player)) {
            if (notify) plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return false;
        }
        return this.getModule().isAvailable(player, notify);
    }

    @Override
    @NotNull
    protected VirtualShop get() {
        return this;
    }

    @NotNull
    public VirtualShopModule getModule() {
        return module;
    }

    @Override
    @NotNull
    public VirtualShopView getView() {
        return this.view;
    }

    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.clear();
        }
        this.products.values().forEach(VirtualProduct::clear);
        this.products.clear();
        this.discountConfigs.forEach(VirtualDiscount::clear);
        this.discountConfigs.clear();
    }

    @Override
    public void onSave() {
        this.saveSettings();
        this.saveProducts();
    }

    public void saveSettings() {
        view.getConfig().set("Title", this.view.getOptions().getTitle());
        view.getConfig().set("Size", this.view.getOptions().getSize());
        view.getConfig().saveChanges();

        cfg.set("Name", this.getName());
        cfg.set("Description", this.getDescription());
        cfg.set("Pages", this.getPages());
        cfg.set("Permission_Required", this.isPermissionRequired());
        this.transactions.forEach((type, isAllowed) -> cfg.set("Transaction_Allowed." + type.name(), isAllowed));
        cfg.setItem("Icon", this.getIcon());
        cfg.setIntArray("Citizens.Attached_NPC", this.getNPCIds().stream().mapToInt(Number::intValue).toArray());
        cfg.set("Discounts", null);
        this.discountConfigs.forEach(discountConfig -> VirtualDiscount.write(discountConfig, cfg, "Discounts." + UUID.randomUUID()));
        cfg.saveChanges();
    }

    public void saveProducts() {
        configProducts.set("List", null);
        this.getProducts()
            .stream().sorted(Comparator.comparingInt(VirtualProduct::getSlot).thenComparingInt(VirtualProduct::getPage))
            .forEach(product -> VirtualProduct.write(product, configProducts, "List." + product.getId()));
        configProducts.saveChanges();
    }

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

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
    }

    public Set<Integer> getNPCIds() {
        return this.npcIds;
    }

    @NotNull
    public Set<VirtualDiscount> getDiscountConfigs() {
        return new HashSet<>(this.discountConfigs);
    }

    public void addDiscountConfig(@NotNull VirtualDiscount config) {
        if (this.discountConfigs.add(config)) {
            config.setShop(this);
        }
    }

    public void removeDiscountConfig(@NotNull VirtualDiscount config) {
        if (this.discountConfigs.remove(config)) {
            config.clear();
        }
    }
}
