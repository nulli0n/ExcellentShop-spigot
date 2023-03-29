package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.DynamicProductPricer;
import su.nightexpress.nexshop.shop.FloatProductPricer;
import su.nightexpress.nexshop.shop.RangedProductPricer;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ProductPriceMenu extends AbstractMenu<ExcellentShop> {

    private final ChestProduct product;
    private final Map<TradeType, Map<PriceType, List<String>>> formatLorePrice;

    public ProductPriceMenu(@NotNull ChestProduct product) {
        super(product.getShop().plugin(), JYML.loadOrExtract(product.getShop().plugin(), product.getShop().getModule().getPath() + "menu/product_price.yml"), "");
        this.product = product;
        this.formatLorePrice = new HashMap<>();

        for (TradeType tradeType : TradeType.values()) {
            for (PriceType priceType : PriceType.values()) {
                List<String> lore = Colorizer.apply(cfg.getStringList("Format.Lore.Price." + tradeType.name() + "." + priceType.name()));
                this.formatLorePrice.computeIfAbsent(tradeType, k -> new HashMap<>()).put(priceType, lore);
            }
        }

        EditorInput<ChestProduct, Type> input = (player, product2, type, e) -> {
            String msg = Colorizer.strip(e.getMessage());
            switch (type) {
                case PRODUCT_CHANGE_PRICE_SELL, PRODUCT_CHANGE_PRICE_BUY -> {
                    double price = StringUtil.getDouble(msg, -1D, true);
                    TradeType tradeType = type == Type.PRODUCT_CHANGE_PRICE_BUY ? TradeType.BUY : TradeType.SELL;

                    product2.getPricer().setPrice(tradeType, price);
                }
                case PRODUCT_CHANGE_PRICE_BUY_MIN, PRODUCT_CHANGE_PRICE_BUY_MAX, PRODUCT_CHANGE_PRICE_SELL_MIN, PRODUCT_CHANGE_PRICE_SELL_MAX -> {
                    RangedProductPricer pricer = (RangedProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    if (type == Type.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                        pricer.setPriceMin(TradeType.BUY, price);
                    }
                    else if (type == Type.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                        pricer.setPriceMax(TradeType.BUY, price);
                    }
                    else if (type == Type.PRODUCT_CHANGE_PRICE_SELL_MIN) {
                        pricer.setPriceMin(TradeType.SELL, price);
                    }
                    else {
                        pricer.setPriceMax(TradeType.SELL, price);
                    }
                }
                case PRODUCT_CHANGE_PRICE_REFRESH_DAYS -> {
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
                case PRODUCT_CHANGE_PRICE_REFRESH_TIMES -> {
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
                case PRODUCT_CHANGE_PRICE_INITIAL_BUY, PRODUCT_CHANGE_PRICE_INITIAL_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    TradeType tradeType = type == Type.PRODUCT_CHANGE_PRICE_INITIAL_BUY ? TradeType.BUY : TradeType.SELL;

                    pricer.setInitial(tradeType, price);
                }
                case PRODUCT_CHANGE_PRICE_STEP_BUY, PRODUCT_CHANGE_PRICE_STEP_SELL -> {
                    DynamicProductPricer pricer = (DynamicProductPricer) product2.getPricer();
                    double price = StringUtil.getDouble(msg, 0D);
                    if (type == Type.PRODUCT_CHANGE_PRICE_STEP_BUY) {
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
                    this.product.getShop().getEditor().getProductsMenu().open(player, 1);
                }
            }
            else if (type instanceof Type type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_PRICE_TYPE -> {
                        Predicate<PriceType> predicate = priceType -> player.hasPermission(ChestPerms.PRICE_TYPE + priceType.name().toLowerCase());
                        PriceType priceType = CollectionsUtil.next(product.getPricer().getType(), predicate);

                        String path = "Products." + product.getId() + ".Price";
                        product.setPricer(ProductPricer.read(priceType, this.product.getShop().getConfig(), path));
                    }
                    case PRODUCT_CHANGE_PRICE_BUY -> {
                        if (this.product.getPricer().getType() == PriceType.FLAT) {
                            if (e.isRightClick()) {
                                this.product.getPricer().setPrice(TradeType.BUY, -1);
                                break;
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type2 = Type.PRODUCT_CHANGE_PRICE_BUY_MIN;
                            }
                            else type2 = Type.PRODUCT_CHANGE_PRICE_BUY_MAX;
                        }
                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL -> {
                        if (this.product.getPricer().getType() == PriceType.FLAT) {
                            if (e.isRightClick()) {
                                this.product.getPricer().setPrice(TradeType.SELL, -1);
                                break;
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type2 = Type.PRODUCT_CHANGE_PRICE_SELL_MIN;
                            }
                            else type2 = Type.PRODUCT_CHANGE_PRICE_SELL_MAX;
                        }
                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_REFRESH -> {
                        FloatProductPricer pricer = (FloatProductPricer) product.getPricer();
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                pricer.getDays().clear();
                            }
                            else pricer.getTimes().clear();
                            break;
                        }

                        if (e.isLeftClick()) {
                            type2 = Type.PRODUCT_CHANGE_PRICE_REFRESH_DAYS;
                            EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_DAY).getLocalized());
                            EditorManager.suggestValues(player, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                        }
                        else {
                            type2 = Type.PRODUCT_CHANGE_PRICE_REFRESH_TIMES;
                            EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_TIME).getLocalized());
                        }
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_INITIAL -> {
                        if (e.isLeftClick()) {
                            type2 = Type.PRODUCT_CHANGE_PRICE_INITIAL_BUY;
                        }
                        else type2 = Type.PRODUCT_CHANGE_PRICE_INITIAL_SELL;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_STEP -> {
                        if (e.isLeftClick()) {
                            type2 = Type.PRODUCT_CHANGE_PRICE_STEP_BUY;
                        }
                        else type2 = Type.PRODUCT_CHANGE_PRICE_STEP_SELL;

                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_PRICE).getLocalized());
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                }
                this.product.getShop().save();
                this.open(player, 1);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            MenuItem menuItem = cfg.getMenuItem("Editor." + sId, Type.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            if (menuItem.getType() instanceof Type type) {
                if (type == Type.PRODUCT_CHANGE_PRICE_REFRESH) {
                    menuItem.setVisibilityPolicy(player1 -> this.product.getPricer().getType() == PriceType.FLOAT);
                }
                else if (type == Type.PRODUCT_CHANGE_PRICE_INITIAL || type == Type.PRODUCT_CHANGE_PRICE_STEP) {
                    menuItem.setVisibilityPolicy(player1 -> this.product.getPricer().getType() == PriceType.DYNAMIC);
                }
            }
            this.addItem(menuItem);
        }
    }

    private enum Type {
        PRODUCT_CHANGE_PRICE_TYPE,
        PRODUCT_CHANGE_PRICE_BUY,
        PRODUCT_CHANGE_PRICE_BUY_MIN,
        PRODUCT_CHANGE_PRICE_BUY_MAX,
        PRODUCT_CHANGE_PRICE_SELL,
        PRODUCT_CHANGE_PRICE_SELL_MIN,
        PRODUCT_CHANGE_PRICE_SELL_MAX,
        PRODUCT_CHANGE_PRICE_REFRESH,
        PRODUCT_CHANGE_PRICE_REFRESH_DAYS,
        PRODUCT_CHANGE_PRICE_REFRESH_TIMES,
        PRODUCT_CHANGE_PRICE_INITIAL,
        PRODUCT_CHANGE_PRICE_INITIAL_BUY,
        PRODUCT_CHANGE_PRICE_INITIAL_SELL,
        PRODUCT_CHANGE_PRICE_STEP,
        PRODUCT_CHANGE_PRICE_STEP_BUY,
        PRODUCT_CHANGE_PRICE_STEP_SELL,
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        if (menuItem.getType() instanceof Type type) {
            if (type == Type.PRODUCT_CHANGE_PRICE_BUY) {
                ItemUtil.replaceLore(item, "%price_buy%", this.formatLorePrice.get(TradeType.BUY).get(this.product.getPricer().getType()));
            }
            else if (type == Type.PRODUCT_CHANGE_PRICE_SELL) {
                ItemUtil.replaceLore(item, "%price_sell%", this.formatLorePrice.get(TradeType.SELL).get(this.product.getPricer().getType()));
            }
        }
        ItemUtil.replace(item, this.product.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
