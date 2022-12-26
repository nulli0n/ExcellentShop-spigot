package su.nightexpress.nexshop.editor.menu;

import org.bukkit.Material;
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
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.editor.GenericEditorType;
import su.nightexpress.nexshop.shop.DynamicProductPricer;
import su.nightexpress.nexshop.shop.FloatProductPricer;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ProductPriceEditor extends AbstractEditorMenu<ExcellentShop, Product<?, ?, ?>> {

    public ProductPriceEditor(@NotNull Product<?, ?, ?> product) {
        super(product.getShop().plugin(), product, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        EditorInput<Product<?, ?, ?>, GenericEditorType> input = (player, product2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case PRODUCT_CHANGE_PRICE_FLAT_SELL, PRODUCT_CHANGE_PRICE_FLAT_BUY -> {
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), -1D, true);
                    if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY) {
                        product2.getPricer().setPrice(TradeType.BUY, price);
                    }
                    else {
                        product2.getPricer().setPrice(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_FLOAT_BUY_MIN, PRODUCT_CHANGE_PRICE_FLOAT_BUY_MAX, PRODUCT_CHANGE_PRICE_FLOAT_SELL_MIN, PRODUCT_CHANGE_PRICE_FLOAT_SELL_MAX -> {
                    FloatProductPricer pricer = (FloatProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), -1D, true);
                    if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY_MIN) {
                        pricer.setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY_MAX) {
                        pricer.setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL_MIN) {
                        pricer.setPriceMin(TradeType.SELL, price);
                    }
                    else {
                        pricer.setPriceMax(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_DAYS -> {
                    FloatProductPricer pricer = (FloatProductPricer) product2.getPricer();
                    DayOfWeek day = CollectionsUtil.getEnum(msg, DayOfWeek.class);
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
                case PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MIN, PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MAX, PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MIN, PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MAX -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), 0D);
                    if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MIN) {
                        pricer.setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MAX) {
                        pricer.setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MIN) {
                        pricer.setPriceMin(TradeType.SELL, price);
                    }
                    else {
                        pricer.setPriceMax(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY, PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), 0D);
                    if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY) {
                        pricer.setInitial(TradeType.BUY, price);
                    }
                    else {
                        pricer.setInitial(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY, PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(StringUtil.colorOff(msg), 0D);
                    if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY) {
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
            else if (type instanceof GenericEditorType type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_PRICE_TYPE -> {
                        PriceType priceType = CollectionsUtil.switchEnum(product.getPricer().getType());
                        if (product instanceof ChestProduct chestProduct) {
                            while (priceType != product.getPricer().getType() && !player.hasPermission(Perms.PREFIX_CHEST_PRICE + priceType.name().toLowerCase()) && !player.hasPermission(Perms.CHEST_SHOP_PRICE)) {
                                priceType = CollectionsUtil.switchEnum(product.getPricer().getType());
                            }
                        }
                        if (priceType == product.getPricer().getType()) return;

                        if (product instanceof VirtualProduct virtualProduct) {
                            String path = "List." + product.getId() + ".Price";// + priceType.name();
                            product.setPricer(ProductPricer.read(priceType, virtualProduct.getShop().getConfigProducts(), path));
                        }
                        else if (product instanceof ChestProduct chestProduct) {
                            String path = "Products." + product.getId() + ".Price";
                            product.setPricer(ProductPricer.read(priceType, chestProduct.getShop().getConfig(), path));
                        }
                    }
                    case PRODUCT_CHANGE_PRICE_FLAT_BUY -> {
                        if (e.isRightClick()) {
                            this.object.getPricer().setPrice(TradeType.BUY, -1);
                            break;
                        }
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLAT_SELL -> {
                        if (e.isRightClick()) {
                            this.object.getPricer().setPrice(TradeType.SELL, -1);
                            break;
                        }
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLOAT_BUY -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY_MIN;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_FLOAT_SELL -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL_MIN;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
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

                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_DAYS;
                            EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_DAY).getLocalized());
                            EditorManager.suggestValues(player, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                        }
                        else {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_TIMES;
                            EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_TIME).getLocalized());
                        }
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_BUY -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MIN;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_SELL -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MIN;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MAX;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_SELL;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_DYNAMIC_STEP -> {
                        GenericEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY;
                        }
                        else type3 = GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_SELL;

                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                }
                this.object.getShop().save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_TYPE, 4);

        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY, 21);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_FLAT_SELL, 23);

        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY, 20);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL, 22);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH, 24);

        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY, 19);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL, 21);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL, 23);
        map.put(GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP, 25);

        map.put(MenuItemType.RETURN, 40);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        Map<EditorButtonType, Integer> map = new HashMap<>();
        this.setTypes(map);

        PriceType priceType = this.object.getPricer().getType();
        if (menuItem.getType() instanceof GenericEditorType type) {
            if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLAT_BUY || type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLAT_SELL) {
                if (priceType != PriceType.FLAT) {
                    item.setType(Material.AIR);
                    menuItem.setSlots(new int[0]);
                }
                else menuItem.setSlots(IntStream.of(map.get(type)).toArray());
            }
            else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_BUY || type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_SELL
            || type == GenericEditorType.PRODUCT_CHANGE_PRICE_FLOAT_REFRESH) {
                if (priceType != PriceType.FLOAT) {
                    item.setType(Material.AIR);
                    menuItem.setSlots(new int[0]);
                }
                else menuItem.setSlots(IntStream.of(map.get(type)).toArray());
            }
            else if (type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_BUY || type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_SELL
            || type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL || type == GenericEditorType.PRODUCT_CHANGE_PRICE_DYNAMIC_STEP) {
                if (priceType != PriceType.DYNAMIC) {
                    item.setType(Material.AIR);
                    menuItem.setSlots(new int[0]);
                }
                else menuItem.setSlots(IntStream.of(map.get(type)).toArray());
            }
        }
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
