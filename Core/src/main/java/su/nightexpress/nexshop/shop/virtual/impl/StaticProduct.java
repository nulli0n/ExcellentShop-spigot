package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.UUID;

public class StaticProduct extends AbstractVirtualProduct<StaticShop> {

    private int shopSlot;
    private int shopPage;

    public StaticProduct(@NotNull ShopPlugin plugin,
                         @NotNull StaticShop shop, @NotNull Currency currency,
                         @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this(plugin, UUID.randomUUID().toString(), shop, currency, handler, packer);
    }

    public StaticProduct(@NotNull ShopPlugin plugin,
                         @NotNull String id, @NotNull StaticShop shop, @NotNull Currency currency,
                         @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(plugin, id, shop, currency, handler, packer);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.setSlot(config.getInt(path + ".Shop_View.Slot", -1));
        this.setPage(config.getInt(path + ".Shop_View.Page", -1));
        this.setDiscountAllowed(config.getBoolean(path + ".Discount.Allowed"));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Discount.Allowed", this.isDiscountAllowed());
        config.set(path + ".Shop_View.Slot", this.getSlot());
        config.set(path + ".Shop_View.Page", this.getPage());
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
