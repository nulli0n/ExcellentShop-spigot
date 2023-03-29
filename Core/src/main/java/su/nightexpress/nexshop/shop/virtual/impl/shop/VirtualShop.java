package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.ShopView;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopMain;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class VirtualShop extends Shop<VirtualShop, VirtualProduct> implements ICleanable {

    private final JYML configProducts;
    private final JYML configView;

    private final Set<VirtualDiscount> discountConfigs;
    private       String               name;
    private       List<String>        description;
    private       int                 pages;
    private       boolean             isPermissionRequired;
    private       ItemStack           icon;
    private       int[]               citizensIds = new int[0];

    private ShopView<VirtualShop> view;
    private EditorShopMain        editor;

    public VirtualShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module.plugin(), cfg, id);
        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "products.yml");
        this.configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "view.yml");
        this.configView.addMissing("Title", StringUtil.capitalizeFully(this.getId()));
        this.configView.addMissing("Size", 54);
        this.configView.saveChanges();
        this.discountConfigs = new HashSet<>();
    }

    @Override
    public boolean load() {
        this.setBank(new VirtualShopBank(this));
        this.setName(cfg.getString("Name", this.configView.getString("Title", this.getId())));
        this.setDescription(cfg.getStringList("Description"));
        this.setPages(cfg.getInt("Pages", 1));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));
        this.setIcon(cfg.getItem("Icon"));
        this.setCitizensIds(cfg.getIntArray("Citizens.Attached_NPC"));
        for (TradeType buyType : TradeType.values()) {
            this.setTransactionEnabled(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }
        for (String sId : cfg.getSection("Discounts")) {
            this.addDiscountConfig(VirtualDiscount.read(cfg, "Discounts." + sId));
        }
        this.getDiscountConfigs().forEach(VirtualDiscount::startScheduler);

        this.loadProducts();
        this.setupView();
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
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
            .replace(Placeholders.SHOP_BANK_BALANCE, plugin.getCurrencyManager().getCurrencies().stream()
                .map(currency -> currency.format(this.getBank().getBalance(currency))).collect(Collectors.joining(DELIMITER_DEFAULT)))
            .replace(Placeholders.SHOP_VIRTUAL_DESCRIPTION, String.join("\n", this.getDescription()))
            .replace(Placeholders.SHOP_VIRTUAL_PERMISSION_NODE, Perms.PREFIX_VIRTUAL_SHOP + this.getId())
            .replace(Placeholders.SHOP_VIRTUAL_PERMISSION_REQUIRED, LangManager.getBoolean(this.isPermissionRequired()))
            .replace(Placeholders.SHOP_VIRTUAL_ICON_NAME, ItemUtil.getItemName(this.getIcon()))
            .replace(Placeholders.SHOP_VIRTUAL_ICON_TYPE, this.getIcon().getType().name())
            .replace(Placeholders.SHOP_VIRTUAL_PAGES, String.valueOf(this.getPages()))
            .replace(Placeholders.SHOP_VIRTUAL_VIEW_SIZE, String.valueOf(this.getView().getSize()))
            .replace(Placeholders.SHOP_VIRTUAL_VIEW_TITLE, this.getView().getTitle())
            .replace(Placeholders.SHOP_VIRTUAL_NPC_IDS, String.join(", ", IntStream.of(this.getCitizensIds()).boxed()
                .map(String::valueOf).toList()))
        );
    }

    @Override
    public boolean canAccess(@NotNull Player player, boolean notify) {
        if (!this.hasPermission(player)) {
            if (notify) plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player); // TODO Message
            return false;
        }

        VirtualShopModule module = plugin.getVirtualShop();
        return module != null && module.isAvailable(player, notify);
    }

    @Override
    @NotNull
    protected VirtualShop get() {
        return this;
    }

    @Override
    @NotNull
    public ShopView<VirtualShop> getView() {
        return this.view;
    }

    @Override
    public void setupView() {
        this.configView.reload();
        this.view = new VirtualShopView(this, this.getConfigView());
        // this.getEditor().rebuild();
        this.getEditor().getEditorViewDesign().setTitle(this.getView().getTitle());
        this.getEditor().getEditorViewDesign().setSize(this.getView().getSize());

        this.getEditor().getEditorProducts().setTitle(this.getView().getTitle());
        this.getEditor().getEditorProducts().setSize(this.getView().getSize());
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.clear();
            this.view = null;
        }
        this.products.values().forEach(Product::clear);
        this.products.clear();
        this.discountConfigs.forEach(VirtualDiscount::clear);
        this.discountConfigs.clear();
    }

    @Override
    public void onSave() {
        configView.set("Title", this.view.getTitle());
        configView.set("Size", this.view.getSize());
        configView.saveChanges();

        cfg.set("Name", this.getName());
        cfg.set("Description", this.getDescription());
        cfg.set("Pages", this.getPages());
        cfg.set("Permission_Required", this.isPermissionRequired());
        this.transactions.forEach((type, isAllowed) -> cfg.set("Transaction_Allowed." + type.name(), isAllowed));
        cfg.setItem("Icon", this.getIcon());
        cfg.setIntArray("Citizens.Attached_NPC", this.getCitizensIds());
        cfg.set("Discounts", null);
        this.discountConfigs.forEach(discountConfig -> VirtualDiscount.write(discountConfig, cfg, "Discounts." + UUID.randomUUID()));

        this.saveProducts();
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

    @NotNull
    public JYML getConfigView() {
        return this.configView;
    }

    @Override
    @NotNull
    public EditorShopMain getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopMain(this.plugin(), this);
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
        return player.hasPermission(Perms.PREFIX_VIRTUAL_SHOP + this.getId());
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

    public int[] getCitizensIds() {
        return this.citizensIds;
    }

    public void setCitizensIds(int[] npcIds) {
        this.citizensIds = npcIds;
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
