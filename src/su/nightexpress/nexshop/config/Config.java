package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.menu.ShopCartMenu;

import java.util.HashMap;
import java.util.Map;

public class Config extends IConfigTemplate {

    public static boolean GENERAL_BUY_WITH_FULL_INVENTORY;

    private static Map<TradeType, ShopCartMenu> CART_MENU;

    public static Sound SOUND_PURCHASE_SUCCESS;
    public static Sound SOUND_PURCHASE_FAILURE;
    public static Sound SOUND_CART_ADDITEM;

    public Config(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        String path = "General.";
        GENERAL_BUY_WITH_FULL_INVENTORY = cfg.getBoolean(path + "Buy_With_Full_Inventory");

        CART_MENU = new HashMap<>();
        for (TradeType tradeType : TradeType.values()) {
            JYML cartConfig = JYML.loadOrExtract(plugin, "cart." + tradeType.name().toLowerCase() + ".menu.yml");
            CART_MENU.put(tradeType, new ShopCartMenu((ExcellentShop) plugin, cartConfig, tradeType));
        }

        path = "GUI.";
        for (String typeRaw : cfg.getSection(path + "Click_Actions")) {
            ShopClickType clickShop = CollectionsUT.getEnum(typeRaw, ShopClickType.class);
            ClickType clickDef = cfg.getEnum(path + "Click_Actions." + typeRaw, ClickType.class);
            if (clickShop == null || clickDef == null) continue;

            clickShop.setClickType(clickDef);
        }

        path = "Sounds.";
        SOUND_PURCHASE_SUCCESS = CollectionsUT
                .getEnum(cfg.getString(path + "Purchase.Success", Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name()), Sound.class);
        SOUND_PURCHASE_FAILURE = CollectionsUT.getEnum(cfg.getString(path + "Purchase.Failure", Sound.BLOCK_ANVIL_PLACE.name()),
                Sound.class);
        SOUND_CART_ADDITEM = CollectionsUT.getEnum(cfg.getString(path + "Cart.Item_Add", Sound.ENTITY_ITEM_PICKUP.name()),
                Sound.class);
    }

    @NotNull
    public static ShopCartMenu getCartMenu(@NotNull TradeType buyType) {
        return CART_MENU.get(buyType);
    }
}
