package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ProductSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.shop.StaticShop;

import java.util.UUID;

public class StaticProduct extends VirtualProduct<StaticProduct, StaticShop> {

    private int shopSlot;
    private int shopPage;

    public StaticProduct(@NotNull ProductSpecific spec, @NotNull Currency currency) {
        this(UUID.randomUUID().toString(), spec, currency);
    }

    public StaticProduct(@NotNull String id, @NotNull ProductSpecific spec, @NotNull Currency currency) {
        super(id, spec, currency);
    }

    @Override
    protected void writeAdditionalData(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Discount.Allowed", this.isDiscountAllowed());
        cfg.set(path + ".Shop_View.Slot", this.getSlot());
        cfg.set(path + ".Shop_View.Page", this.getPage());
    }

    @NotNull
    protected StaticProduct get() {
        return this;
    }

    public int getSlot() {
        return this.shopSlot;
    }

    public void setSlot(int slot) {
        this.shopSlot = slot;
    }

    public int getPage() {
        return this.shopPage;
    }

    public void setPage(int page) {
        this.shopPage = page;
    }
}
