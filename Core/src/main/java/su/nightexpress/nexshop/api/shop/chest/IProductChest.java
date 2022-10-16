package su.nightexpress.nexshop.api.shop.chest;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.shop.IProduct;

public interface IProductChest extends IProduct {

    @Override
    @NotNull
    IShopChest getShop();
}
