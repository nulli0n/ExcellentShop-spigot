package su.nightexpress.nexshop.api.shop.chest;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.shop.IProductPrepared;

public interface IProductChestPrepared extends IProductPrepared {

    @Override
    @NotNull
    default IShopChest getShop() {
        return this.getProduct().getShop();
    }

    @Override
    @NotNull
    IProductChest getProduct();
}
