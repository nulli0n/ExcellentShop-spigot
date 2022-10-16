package su.nightexpress.nexshop.api.shop.virtual;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.shop.IProductPrepared;

public interface IProductVirtualPrepared extends IProductPrepared {

    @Override
    @NotNull
    default IShopVirtual getShop() {
        return this.getProduct().getShop();
    }

    @Override
    @NotNull
    IProductVirtual getProduct();
}
