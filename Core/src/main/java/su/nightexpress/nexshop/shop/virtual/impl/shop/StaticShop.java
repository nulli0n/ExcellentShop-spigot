package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.util.Placeholders;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class StaticShop extends VirtualShop<StaticShop, StaticProduct> {

    private final Set<VirtualDiscount> discountConfigs;

    private int pages;

    public StaticShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module, cfg, id);
        this.discountConfigs = new HashSet<>();
        this.placeholderMap
            .add(Placeholders.SHOP_PAGES, () -> String.valueOf(this.getPages()));
    }

    @Override
    protected boolean loadAdditionalData() {
        this.setPages(cfg.getInt("Pages", 1));
        for (String sId : cfg.getSection("Discounts")) {
            this.addDiscountConfig(VirtualDiscount.read(cfg, "Discounts." + sId));
        }
        return true;
    }

    @Override
    @NotNull
    protected StaticProduct loadProduct(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        return VirtualProduct.read(cfg, path, id, StaticProduct.class);
    }

    @Override
    @NotNull
    protected StaticShop get() {
        return this;
    }

    @Override
    protected void clearAdditionalData() {
        this.discountConfigs.forEach(VirtualDiscount::clear);
        this.discountConfigs.clear();
    }

    @Override
    protected void saveAdditionalSettings() {
        cfg.set("Pages", this.getPages());
        cfg.remove("Discounts");
        this.discountConfigs.forEach(discountConfig -> VirtualDiscount.write(discountConfig, cfg, "Discounts." + UUID.randomUUID()));
    }

    @Override
    protected void saveAdditionalProducts() {
        this.getProducts()
            .stream().sorted(Comparator.comparingInt(StaticProduct::getSlot).thenComparingInt(StaticProduct::getPage))
            .forEach(product -> product.write(configProducts, "List." + product.getId()));
    }

    @Override
    @NotNull
    public VirtualShopType getType() {
        return VirtualShopType.STATIC;
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
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
