package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.chest.IProductChest;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class EditorShopChestProduct extends AbstractMenu<ExcellentShop> {

    private final IProductChest product;

    public EditorShopChestProduct(@NotNull ExcellentShop plugin, @NotNull IProductChest product) {
        super(plugin, ChestConfig.CONFIG_SHOP_PRODUCT, "");
        this.product = product;

        IShopChest shop = product.getShop();

        EditorInput<IProductChest, ChestEditorType> input = (player, product2, type, e) -> {
            ChestShop chestShop = plugin.getChestShop();
            if (chestShop == null) return true;

            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                //case PRODUCT_CHANGE_COMMANDS -> product.getCommands().add(StringUT.colorRaw(msg));
                case PRODUCT_CHANGE_CURRENCY -> {
                    String id = StringUtil.colorOff(msg);
                    ICurrency currency = plugin.getCurrencyManager().getCurrency(id);
                    if (currency == null) {
                        EditorManager.error(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Product_Error_Currency).getLocalized());
                        return false;
                    }
                    if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
                        EditorManager.error(player, plugin.getMessage(Lang.Shop_Error_Currency_Invalid).getLocalized());
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

                    if (price < 0 && !player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                        EditorManager.error(player, plugin.getMessage(Lang.Editor_Error_Negative).getLocalized());
                        return false;
                    }

                    if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                        product2.getPricer().setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                        product2.getPricer().setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX) {
                        product2.getPricer().setPriceMax(TradeType.SELL, price);
                    }
                    else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN) {
                        product2.getPricer().setPriceMin(TradeType.SELL, price);
                    }
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
                            EditorManager.tipCustom(player, plugin.lang().Editor_Enter_Command.getMsg());
                            EditorManager.sendCommandTips(player);
                            chestShop.getEditorHandler().startEdit(player, product, type2);
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                    }*/
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Currency).getLocalized());
                        plugin.getMessage(Lang.Editor_Tip_Product_Currency).asList().forEach(line -> {
                            if (line.contains(Placeholders.CURRENCY_ID)) {
                                for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
                                    MessageUtil.sendWithJSON(player, currency.replacePlaceholders().apply(line));
                                }
                            }
                            else MessageUtil.sendWithJSON(player, line);
                        });
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_BUY -> {
                        if (e.isShiftClick() && e.isRightClick()) {
                            if (!player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorManager.error(player, plugin.getMessage(Lang.Editor_Error_Negative).getLocalized());
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

                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Price).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL -> {
                        if (e.isShiftClick() && e.isRightClick()) {
                            if (!player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorManager.error(player, plugin.getMessage(Lang.Editor_Error_Negative).getLocalized());
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

                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Enter_Price).getLocalized());
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

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_DAY;
                            EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Day).getLocalized());
                            EditorManager.suggestValues(player, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                        }
                        else {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES_TIME;
                            EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Time_Full).getLocalized());
                        }

                        EditorManager.startEdit(player, product, type3, input);
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
    public void onItemSet(@NotNull Player player, @NotNull List<IMenuItem> items) {
        super.onItemSet(player, items);
        items.removeIf(menuItem -> {
            if (menuItem.getType() == ChestEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TOGGLE
            || menuItem.getType() == ChestEditorType.PRODUCT_CHANGE_PRICE_RANDOMIZER_TIMES) {
                return !player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_RND);
            }
            else if (menuItem.getType() == ChestEditorType.PRODUCT_CHANGE_CURRENCY) {
                return !player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_CURRENCY);
            }
            return false;
        });
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        ItemUtil.replace(item, product.replacePlaceholders());
        ItemUtil.replace(item, product.getCurrency().replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
