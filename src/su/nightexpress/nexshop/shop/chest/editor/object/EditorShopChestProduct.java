package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

import java.time.DayOfWeek;

public class EditorShopChestProduct extends AbstractMenu<ExcellentShop> {

    private final IShopChestProduct product;

    public EditorShopChestProduct(@NotNull ExcellentShop plugin, @NotNull IShopChestProduct product) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP_PRODUCT, "");
        this.product = product;

        IShopChest shop = product.getShop();
        IMenuClick click = (player, type, e) -> {

            ChestShop chestShop = plugin.getChestShop();
            if (chestShop == null) return;

            if (type instanceof MenuItemType type2) {
                switch (type2) {
                    case RETURN -> shop.getEditor().getEditorProducts().open(player, 1);
                    case CLOSE -> player.closeInventory();
                    default -> {}
                }
            }
            else if (type instanceof ChestEditorType type2) {
                switch (type2) {
                    /*case PRODUCT_CHANGE_COMMANDS -> {
                        if (e.isLeftClick()) {
                            EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Command.getMsg());
                            EditorUtils.sendCommandTips(player);
                            chestShop.getEditorHandler().startEdit(player, product, type2);
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                    }*/
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Currency.getMsg());
                        EditorUtils.sendClickableTips(player, ChestShopConfig.ALLOWED_CURRENCIES);
                        chestShop.getEditorHandler().startEdit(player, product, type2);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_BUY -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            if (!player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorUtils.errorCustom(player, plugin.lang().Chest_Shop_Editor_Error_Negative.getMsg());
                                return;
                            }
                            product.getPricer().setPriceMin(TradeType.BUY, -1);
                            product.getPricer().setPriceMax(TradeType.BUY, -1);
                            break;
                        }

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Price.getMsg());
                        chestShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            if (!player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorUtils.errorCustom(player, plugin.lang().Chest_Shop_Editor_Error_Negative.getMsg());
                                return;
                            }
                            product.getPricer().setPriceMin(TradeType.SELL, -1);
                            product.getPricer().setPriceMax(TradeType.SELL, -1);
                            break;
                        }

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Price.getMsg());
                        chestShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_RND -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setRandomizerEnabled(!product.getPricer().isRandomizerEnabled());
                            break;
                        }

                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                product.getPricer().getDays().clear();
                            }
                            else product.getPricer().getTimes().clear();
                            break;
                        }

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_DAY;
                            EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                            EditorUtils.sendClickableTips(player, CollectionsUT.getEnumsList(DayOfWeek.class));
                        }
                        else {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_TIME;
                            EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Time_Full.getMsg());
                        }

                        chestShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    default -> {
                        return;
                    }
                }

                shop.save();
                this.open(player, 1);
            }
        };

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

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        // TODO
                /*if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_RND) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_RND);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_COMMANDS) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_COMMANDS);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_CURRENCY) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_CURRENCY);
                }*/

        ItemUT.replace(item, product.replacePlaceholders());
        ItemUT.replace(item, product.getCurrency().replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
