package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class StaticShop extends AbstractVirtualShop<StaticProduct> {

    private final Set<VirtualDiscount> discountConfigs;

    private int pages;

    public StaticShop(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull File file, @NotNull String id) {
        super(plugin, module, file, id);
        this.discountConfigs = new HashSet<>();
        this.placeholderMap.add(Placeholders.forStaticShop(this));
    }

    @Override
    protected boolean loadAdditional(@NotNull FileConfig config) {
        this.setPages(config.getInt("Pages", 1));
        for (String sId : config.getSection("Discounts")) {
            this.addDiscountConfig(VirtualDiscount.read(config, "Discounts." + sId));
        }
        return true;
    }

    @Override
    @NotNull
    public StaticProduct createProduct(@NotNull String id, @NotNull Currency currency,
                                       @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        return new StaticProduct(this.plugin, id, this, currency, handler, packer);
    }

    @Override
    protected void saveAdditionalSettings(@NotNull FileConfig config) {
        config.set("Pages", this.getPages());
        config.remove("Discounts");
        this.discountConfigs.forEach(discountConfig -> VirtualDiscount.write(discountConfig, config, "Discounts." + UUID.randomUUID()));
    }

    @Override
    protected void saveAdditionalProducts() {
        this.getProducts()
            .stream().sorted(Comparator.comparingInt(StaticProduct::getSlot).thenComparingInt(StaticProduct::getPage))
            .forEach(this::writeProduct);
    }

    @Override
    protected void writeProduct(@NotNull StaticProduct product) {
        product.write(this.configProducts, this.getProductSavePath(product));
    }

    @Override
    public void addProduct(@NotNull Product product) {
        if (product instanceof StaticProduct staticProduct) {
            this.addProduct(staticProduct);
        }
    }

    @Override
    @NotNull
    public ShopType getType() {
        return ShopType.STATIC;
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
        this.discountConfigs.remove(config);
    }
}
