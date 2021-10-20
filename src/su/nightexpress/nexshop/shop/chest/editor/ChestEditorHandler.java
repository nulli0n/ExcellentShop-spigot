package su.nightexpress.nexshop.shop.chest.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.AbstractEditorHandler;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.editor.handler.EditorHandlerProduct;
import su.nightexpress.nexshop.shop.chest.editor.handler.EditorHandlerShop;

public class ChestEditorHandler extends AbstractEditorHandler<ExcellentShop, ChestEditorType> {

    public static JYML CONFIG_SHOP;
    public static JYML CONFIG_SHOP_PRODUCTS;
    public static JYML CONFIG_SHOP_PRODUCT;

    public ChestEditorHandler(@NotNull ChestShop chestShop) {
        super(chestShop.plugin());

        CONFIG_SHOP = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/shop_main.yml");
        CONFIG_SHOP_PRODUCTS = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/product_list.yml");
        CONFIG_SHOP_PRODUCT = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/product_main.yml");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.addInputHandler(IShopChest.class, new EditorHandlerShop(this.plugin));
        this.addInputHandler(IShopChestProduct.class, new EditorHandlerProduct(this.plugin));
    }
}
