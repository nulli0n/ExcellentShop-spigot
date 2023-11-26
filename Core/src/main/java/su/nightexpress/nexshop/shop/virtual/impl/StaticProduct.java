package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;

import java.util.UUID;

public class StaticProduct extends AbstractVirtualProduct<StaticShop> {

    private int shopSlot;
    private int shopPage;

    public StaticProduct(@NotNull StaticShop shop, @NotNull Currency currency,
                         @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this(UUID.randomUUID().toString(), shop, currency, handler, packer);
    }

    public StaticProduct(@NotNull String id, @NotNull StaticShop shop, @NotNull Currency currency,
                         @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(id, shop, currency, handler, packer);
    }

    @Override
    protected void loadAdditional(@NotNull JYML cfg, @NotNull String path) {
        this.setSlot(cfg.getInt(path + ".Shop_View.Slot", -1));
        this.setPage(cfg.getInt(path + ".Shop_View.Page", -1));
        this.setDiscountAllowed(cfg.getBoolean(path + ".Discount.Allowed"));
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Discount.Allowed", this.isDiscountAllowed());
        cfg.set(path + ".Shop_View.Slot", this.getSlot());
        cfg.set(path + ".Shop_View.Page", this.getPage());
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
