package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopConfig;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EditorShopProduct extends AbstractMenu<ExcellentShop> {

    private final IProductVirtual product;

    public EditorShopProduct(@NotNull ExcellentShop plugin, @NotNull IProductVirtual product) {
        super(plugin, VirtualShopConfig.SHOP_PRODUCT_MAIN_YML, "");
        this.product = product;

        IShopVirtual shop = product.getShop();

        EditorInput<IProductVirtual, VirtualEditorType> input = (player, product2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case PRODUCT_CHANGE_COMMANDS -> product2.getCommands().add(StringUtil.colorRaw(msg));
                case PRODUCT_CHANGE_CURRENCY -> {
                    String id = StringUtil.colorOff(msg);
                    ICurrency currency = plugin.getCurrencyManager().getCurrency(id);
                    if (currency == null) {
                        EditorManager.error(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Product_Error_Currency).getLocalized());
                        return false;
                    }

                    product2.setCurrency(currency);
                }
                case PRODUCT_CHANGE_PRICE_SELL_MIN, PRODUCT_CHANGE_PRICE_SELL_MAX, PRODUCT_CHANGE_PRICE_BUY_MAX, PRODUCT_CHANGE_PRICE_BUY_MIN -> {
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), -99, true);
                    if (price == -99) {
                        EditorManager.error(player, EditorManager.ERROR_NUM_INVALID);
                        return false;
                    }

                    if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                        product2.getPricer().setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                        product2.getPricer().setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX) {
                        product2.getPricer().setPriceMax(TradeType.SELL, price);
                    }
                    else {
                        product2.getPricer().setPriceMin(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_LIMIT_BUY_AMOUNT -> {
                    double value = StringUtil.getDouble(StringUtil.colorOff(msg), -1, true);
                    product2.setLimitAmount(TradeType.BUY, (int) value);
                }
                case PRODUCT_CHANGE_LIMIT_BUY_RESET -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.setLimitCooldown(TradeType.BUY, value);
                }
                case PRODUCT_CHANGE_LIMIT_SELL_AMOUNT -> {
                    double value = StringUtil.getDouble(StringUtil.colorOff(msg), -1, true);
                    product2.setLimitAmount(TradeType.SELL, (int) value);
                }
                case PRODUCT_CHANGE_LIMIT_SELL_RESET -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.setLimitCooldown(TradeType.SELL, value);
                }
                case PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_DAY -> {
                    DayOfWeek day = CollectionsUtil.getEnum(msg, DayOfWeek.class);
                    if (day == null) {
                        EditorManager.error(player, EditorManager.ERROR_ENUM);
                        return false;
                    }
                    product2.getPricer().getDays().add(day);
                }
                case PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_TIME -> {
                    String[] raw = msg.split(" ");
                    LocalTime[] times = new LocalTime[raw.length];

                    for (int count = 0; count < raw.length; count++) {
                        String[] split = raw[count].split(":");
                        int hour = StringUtil.getInteger(split[0], 0);
                        int minute = StringUtil.getInteger(split.length >= 2 ? split[1] : "0", 0);
                        times[count] = LocalTime.of(hour, minute);
                    }
                    if (times.length < 2) return false;

                    product2.getPricer().getTimes().add(times);
                }
                default -> {}
            }

            product2.getShop().save();
            return true;
        };
        
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
                            EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Command).getLocalized());
                            EditorManager.sendCommandTips(player);
                            EditorManager.startEdit(player, product, type2, input);
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                    }
                    case PRODUCT_CHANGE_ITEM -> {
                        if (e.isShiftClick() && e.isLeftClick()) {
                            ItemStack buyItem = product.getItem();
                            PlayerUtil.addItem(player, buyItem);
                            return;
                        }

                        ItemStack cursor = e.getCursor();
                        if (cursor != null && !cursor.getType().isAir()) {
                            product.setItem(cursor);
                            e.getView().setCursor(null);
                        }
                        else if (e.isRightClick()) {
                            product.setItem(new ItemStack(Material.AIR));
                        }
                    }
                    case PRODUCT_CHANGE_PREVIEW -> {
                        if (e.isShiftClick() && e.isLeftClick()) {
                            ItemStack buyItem = product.getPreview();
                            PlayerUtil.addItem(player, buyItem);
                            return;
                        }

                        ItemStack item = e.getCursor();
                        if (item != null && !item.getType().isAir()) {
                            product.setPreview(item);
                            e.getView().setCursor(null);
                        }
                    }
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Currency).getLocalized());
                        EditorManager.suggestValues(player, plugin.getCurrencyManager()
                                .getCurrencies().stream().map(ICurrency::getId).toList(), true);
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_DISCOUNT -> product.setDiscountAllowed(!product.isDiscountAllowed());
                    case PRODUCT_CHANGE_ITEM_META -> product.setItemMetaEnabled(!product.isItemMetaEnabled());
                    case PRODUCT_CHANGE_PRICE_BUY -> {
                        if (e.isShiftClick() && e.isRightClick()) {
                            product.getPricer().setPriceMin(TradeType.BUY, -1);
                            product.getPricer().setPriceMax(TradeType.BUY, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Price).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL -> {
                        if (e.isShiftClick() && e.isRightClick()) {
                            product.getPricer().setPriceMin(TradeType.SELL, -1);
                            product.getPricer().setPriceMax(TradeType.SELL, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Price).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_LIMIT -> {
                        VirtualEditorType type3;
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Amount).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_RESET;
                                EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Time_Seconds).getLocalized());
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Amount).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_RESET;
                                EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Time_Seconds).getLocalized());
                            }
                        }

                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_RANDOMIZER_TOGGLE -> product.getPricer().setRandomizerEnabled(!product.getPricer().isRandomizerEnabled());
                    case PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES -> {
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                product.getPricer().getDays().clear();
                            }
                            else product.getPricer().getTimes().clear();
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_DAY;
                            EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Day).getLocalized());
                            EditorManager.suggestValues(player, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                        }
                        else {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_TIME;
                            EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Time_Full).getLocalized());
                        }

                        EditorManager.startEdit(player, product, type3, input);
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
                if (!buyItem.getType().isAir()) {
                    item.setType(buyItem.getType());
                }
            }
        }

        ItemUtil.replace(item, product.replacePlaceholders());
        ItemUtil.replace(item, product.getCurrency().replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e,  @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
