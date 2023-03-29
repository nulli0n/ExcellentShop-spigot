package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenu;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.DynamicProductPricer;
import su.nightexpress.nexshop.shop.FloatProductPricer;
import su.nightexpress.nexshop.shop.RangedProductPricer;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class ProductPriceEditor extends AbstractEditorMenu<ExcellentShop, VirtualProduct> {

    public ProductPriceEditor(@NotNull VirtualProduct product) {
        super(product.getShop().plugin(), product, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        EditorInput<VirtualProduct, VirtualEditorType> input = (player, product2, type, e) -> {
            String msg = Colorizer.strip(e.getMessage());
            switch (type) {
                case PRODUCT_CHANGE_PRICE_FLAT_SELL, PRODUCT_CHANGE_PRICE_FLAT_BUY -> {
                    double price = StringUtil.getDouble(msg, -1D, true);
                    if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY) {
                        product2.getPricer().setPrice(TradeType.BUY, price);
                    }
                    else {
                        product2.getPricer().setPrice(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_BUY_MIN, PRODUCT_CHANGE_PRICE_BUY_MAX, PRODUCT_CHANGE_PRICE_SELL_MIN, PRODUCT_CHANGE_PRICE_SELL_MAX -> {
                    RangedProductPricer pricer = (RangedProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                        pricer.setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                        pricer.setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN) {
                        pricer.setPriceMin(TradeType.SELL, price);
                    }
                    else {
                        pricer.setPriceMax(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_DAYS -> {
                    FloatProductPricer pricer = (FloatProductPricer) product2.getPricer();
                    DayOfWeek day = StringUtil.getEnum(msg, DayOfWeek.class).orElse(null);
                    if (day == null) {
                        EditorManager.error(player, plugin.getMessage(Lang.EDITOR_ERROR_ENUM).getLocalized());
                        return false;
                    }
                    pricer.getDays().add(day);
                    pricer.stopScheduler();
                    pricer.startScheduler();
                }
                case PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_TIMES -> {
                    FloatProductPricer pricer = (FloatProductPricer) product2.getPricer();
                    try {
                        pricer.getTimes().add(LocalTime.parse(msg, IScheduled.TIME_FORMATTER));
                        pricer.stopScheduler();
                        pricer.startScheduler();
                    }
                    catch (DateTimeParseException ex) {
                        return false;
                    }
                }
                case PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY, PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY) {
                        pricer.setInitial(TradeType.BUY, price);
                    }
                    else {
                        pricer.setInitial(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY, PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY) {
                        pricer.setStep(TradeType.BUY, price);
                    }
                    else {
                        pricer.setStep(TradeType.SELL, price);
                    }
                }
            }
            product2.getShop().save();
            return true;
        };

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.object.getEditor().open(player, 1);
                }
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_PRICE_TYPE -> {
                        PriceType priceType = CollectionsUtil.next(product.getPricer().getType());
                        String path = "List." + product.getId() + ".Price";// + priceType.name();
                        product.setPricer(ProductPricer.read(priceType, product.getShop().getConfigProducts(), path));
                    }
                    case PRODUCT_CHANGE_PRICE_FLAT_BUY -> {
                        if (e.isRightClick()) {
                            this.object.getPricer().setPrice(TradeType.BUY, -1);
                            break;
                        }
                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLAT_SELL -> {
                        if (e.isRightClick()) {
                            this.object.getPricer().setPrice(TradeType.SELL, -1);
                            break;
                        }
                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLOAT_BUY, PRODUCT_CHANGE_PRICE_DYNAMIC_BUY -> {
                        if (e.isLeftClick()) {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLOAT_SELL, PRODUCT_CHANGE_PRICE_DYNAMIC_SELL -> {
                        if (e.isLeftClick()) {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLOAT_REFRESH -> {
                        FloatProductPricer pricer = (FloatProductPricer) product.getPricer();
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                pricer.getDays().clear();
                            }
                            else pricer.getTimes().clear();
                            break;
                        }

                        if (e.isLeftClick()) {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_DAYS;
                            EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_DAY).getLocalized());
                            EditorManager.suggestValues(player, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                        }
                        else {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_TIMES;
                            EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_TIME).getLocalized());
                        }
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL -> {
                        if (e.isLeftClick()) {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY;
                        }
                        else type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_SELL;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_STEP -> {
                        if (e.isLeftClick()) {
                            type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY;
                        }
                        else type2 = VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_SELL;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                }
                this.object.getShop().save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
        this.getItemsMap().values().forEach(menuItem -> {
            if (menuItem.getType() instanceof VirtualEditorType type) {
                if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLAT_SELL) {
                    menuItem.setVisibilityPolicy(player1 -> this.object.getPricer().getType() == PriceType.FLAT);
                }
                else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL
                    || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH) {
                    menuItem.setVisibilityPolicy(player1 -> this.object.getPricer().getType() == PriceType.FLOAT);
                }
                else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL
                    || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL || type == VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP) {
                    menuItem.setVisibilityPolicy(player1 -> this.object.getPricer().getType() == PriceType.DYNAMIC);
                }
            }
        });
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_TYPE, 4);

        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY, 21);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_FLAT_SELL, 23);

        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY, 20);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL, 22);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH, 24);

        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY, 19);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL, 21);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL, 23);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP, 25);

        map.put(MenuItemType.RETURN, 40);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
