package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.AbstractEditorHandler;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerDiscount;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerProduct;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerShop;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;

public class VirtualEditorHandler extends AbstractEditorHandler<ExcellentShop, VirtualEditorType> {

    public static JYML SHOP_LIST_YML;
    public static JYML SHOP_MAIN_YML;
    public static JYML SHOP_DISCOUNTS_YML;
    public static JYML SHOP_PRODUCT_LIST_YML;
    public static JYML SHOP_PRODUCT_MAIN_YML;

    private final VirtualShop virtualShop;

    public VirtualEditorHandler(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin());
        this.virtualShop = virtualShop;

        SHOP_LIST_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_list.yml");
        SHOP_MAIN_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_main.yml");
        SHOP_PRODUCT_LIST_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_product_list.yml");
        SHOP_PRODUCT_MAIN_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_product.yml");
        SHOP_DISCOUNTS_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_discounts.yml");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.addInputHandler(IShopVirtual.class, new EditorHandlerShop(this.plugin));
        this.addInputHandler(IShopVirtualProduct.class, new EditorHandlerProduct(this.plugin));
        this.addInputHandler(IShopDiscount.class, new EditorHandlerDiscount(this.virtualShop));
    }

    @Override
    protected boolean onType(@NotNull Player player, @NotNull Object object,
                             @NotNull VirtualEditorType type, @NotNull String input) {

        if (type == VirtualEditorType.SHOP_CREATE) {
            String id = EditorUtils.fineId(input);
            if (this.virtualShop.getShopById(id) != null) {
                EditorUtils.errorCustom(player, plugin.lang().Virtual_Shop_Editor_Create_Error_Exist.getMsg());
                return false;
            }
            IShopVirtual shop = new ShopVirtual(this.virtualShop, this.virtualShop.getFullPath() + VirtualShop.DIR_SHOPS + id + "/" + id + ".yml");
            this.virtualShop.getShopsMap().put(shop.getId(), shop);
            return true;
        }

        return super.onType(player, object, type, input);
    }
}
