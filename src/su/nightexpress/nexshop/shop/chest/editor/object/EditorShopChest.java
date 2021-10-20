package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

public class EditorShopChest extends AbstractMenu<ExcellentShop> {

    private final IShopChest shop;

    private EditorShopChestProducts products;

    public EditorShopChest(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP, "");
        this.shop = shop;

        IMenuClick click = (player, type, e) -> {

            ChestShop chestShop = plugin.getChestShop();
            if (chestShop == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    chestShop.getListOwnGUI().open(player, 1);
                }
                else if (type2 == MenuItemType.CLOSE) {
                    player.closeInventory();
                }
            }
            else if (type instanceof ChestEditorType type2) {
                switch (type2) {
                    case SHOP_CHANGE_NAME -> {
                        chestShop.getEditorHandler().startEdit(player, shop, type2);
                        EditorUtils.tipCustom(player, plugin.lang().Chest_Shop_Editor_Tip_Name.getMsg());
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_ADMIN -> {
                        if (!player.hasPermission(Perms.CHEST_EDITOR_ADMINSHOP)) {
                            plugin.lang().Error_NoPerm.send(player);
                            return;
                        }
                        shop.setAdminShop(!shop.isAdminShop());
                    }
                    case SHOP_CHANGE_TRANSACTIONS -> {
                        if (e.isLeftClick()) {
                            shop.setPurchaseAllowed(TradeType.BUY, !shop.isPurchaseAllowed(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setPurchaseAllowed(TradeType.SELL, !shop.isPurchaseAllowed(TradeType.SELL));
                        }
                    }
                    case SHOP_CHANGE_PRODUCTS -> {
                        this.getEditorProducts().open(player, 1);
                        return;
                    }
                    case SHOP_DELETE -> {
                        if (!e.isShiftClick()) return;
                        if (!player.hasPermission(Perms.CHEST_REMOVE)
                                || (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_REMOVE_OTHERS))) {
                            plugin.lang().Error_NoPerm.send(player);
                            return;
                        }
                        chestShop.deleteShop(player, shop.getLocation().getBlock());
                        player.closeInventory();
                        return;
                    }
                    default -> {
                        return;
                    }
                }
                this.shop.save();
                this.open(player, 1);
            }
        };

        JYML cfg = ChestEditorHandler.CONFIG_SHOP;

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ChestEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @NotNull
    public EditorShopChestProducts getEditorProducts() {
        if (this.products == null) {
            this.products = new EditorShopChestProducts(this.plugin, this.shop);
        }
        return this.products;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUT.replace(item, shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
