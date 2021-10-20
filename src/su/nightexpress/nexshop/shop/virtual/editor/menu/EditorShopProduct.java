package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;

public class EditorShopProduct extends AbstractMenu<ExcellentShop> {

    private final IShopVirtualProduct product;

    public EditorShopProduct(@NotNull ExcellentShop plugin, @NotNull IShopVirtualProduct product) {
        super(plugin, VirtualEditorHandler.SHOP_PRODUCT_MAIN_YML, "");
        this.product = product;

        IShopVirtual shop = product.getShop();

        IMenuClick click = (player, type, e) -> {
            VirtualShop virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().getEditorProducts().open(player, this.product.getPage());
                }
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_COMMANDS -> {
                        if (e.isLeftClick()) {
                            EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Command.getMsg());
                            EditorUtils.sendCommandTips(player);
                            virtualShop.getEditorHandler().startEdit(player, product, type2);
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                    }
                    case PRODUCT_CHANGE_ITEM -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            ItemStack buyItem = product.getItem();
                            ItemUT.addItem(player, buyItem);
                            return;
                        }

                        ItemStack cursor = e.getCursor();
                        if (cursor != null && !ItemUT.isAir(cursor)) {
                            product.setItem(cursor);
                            e.getView().setCursor(null);
                        }
                        else if (e.isRightClick()) {
                            product.setItem(new ItemStack(Material.AIR));
                        }
                    }
                    case PRODUCT_CHANGE_PREVIEW -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            ItemStack buyItem = product.getPreview();
                            ItemUT.addItem(player, buyItem);
                            return;
                        }

                        ItemStack item = e.getCursor();
                        if (item != null && !ItemUT.isAir(item)) {
                            product.setPreview(item);
                            e.getView().setCursor(null);
                        }
                    }
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Currency.getMsg());
                        EditorUtils.sendClickableTips(player, plugin.getCurrencyManager()
                                .getCurrencies().stream().map(IShopCurrency::getId).toList());
                        virtualShop.getEditorHandler().startEdit(player, product, type2);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_DISCOUNT -> product.setDiscountAllowed(!product.isDiscountAllowed());
                    case PRODUCT_CHANGE_ITEM_META -> product.setItemMetaEnabled(!product.isItemMetaEnabled());
                    case PRODUCT_CHANGE_PRICE_BUY -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setPriceMin(TradeType.BUY, -1);
                            product.getPricer().setPriceMax(TradeType.BUY, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Price.getMsg());
                        virtualShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setPriceMin(TradeType.SELL, -1);
                            product.getPricer().setPriceMax(TradeType.SELL, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Price.getMsg());
                        virtualShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_LIMIT -> {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.setLimitAmount(TradeType.BUY, -1);
                            product.setLimitCooldown(TradeType.BUY, 0);
                            product.setLimitAmount(TradeType.SELL, -1);
                            product.setLimitCooldown(TradeType.SELL, 0);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_AMOUNT;
                                EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_RESET;
                                EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Time_Seconds.getMsg());
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_AMOUNT;
                                EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_RESET;
                                EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Time_Seconds.getMsg());
                            }
                        }

                        virtualShop.getEditorHandler().startEdit(player, product, type3);
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

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_DAY;
                            EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                            EditorUtils.sendClickableTips(player, CollectionsUT.getEnumsList(DayOfWeek.class));
                        }
                        else {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_TIME;
                            EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Time_Full.getMsg());
                        }

                        virtualShop.getEditorHandler().startEdit(player, product, type3);
                        player.closeInventory();
                        return;
                    }
                    default -> { return; }
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
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, VirtualEditorType.class);

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
    @Nullable
    public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
        if (menuItem.getType() instanceof VirtualEditorType type) {
            if (type == VirtualEditorType.PRODUCT_CHANGE_ITEM_META) {
                return menuItem.getDisplay(String.valueOf(product.isItemMetaEnabled() ? 1 : 0));
            }
        }
        return super.onItemDisplayPrepare(player, menuItem);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        Enum<?> type = menuItem.getType();
        if (type != null) {
            if (type == VirtualEditorType.PRODUCT_CHANGE_PREVIEW) {
                item.setType(this.product.getPreview().getType());
            }
            else if (type == VirtualEditorType.PRODUCT_CHANGE_ITEM) {
                ItemStack buyItem = product.getItem();
                if (!ItemUT.isAir(buyItem)) {
                    item.setType(buyItem.getType());
                }
            }
        }

        ItemUT.replace(item, product.replacePlaceholders());
        ItemUT.replace(item, product.getCurrency().replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
