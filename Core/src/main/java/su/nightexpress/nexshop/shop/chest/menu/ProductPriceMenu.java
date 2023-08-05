package su.nightexpress.nexshop.shop.chest.menu;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.price.DynamicProductPricer;
import su.nightexpress.nexshop.shop.price.FloatProductPricer;
import su.nightexpress.nexshop.shop.price.RangedProductPricer;
import su.nightexpress.nexshop.shop.util.TimeUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ProductPriceMenu extends ConfigEditorMenu {

    private final ChestProduct product;
    private final Map<TradeType, Map<PriceType, List<String>>> formatLorePrice;

    public ProductPriceMenu(@NotNull ExcellentShop plugin, @NotNull ChestProduct product) {
        super(plugin, JYML.loadOrExtract(plugin, product.getShop().getModule().getLocalPath() + "/menu/", "product_price.yml"));
        this.product = product;
        this.formatLorePrice = new HashMap<>();

        for (TradeType tradeType : TradeType.values()) {
            for (PriceType priceType : PriceType.values()) {
                List<String> lore = Colorizer.apply(cfg.getStringList("Format.Lore.Price." + tradeType.name() + "." + priceType.name()));
                this.formatLorePrice.computeIfAbsent(tradeType, k -> new HashMap<>()).put(priceType, lore);
            }
        }

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.RETURN, (viewer, event) -> product.getShop().getEditor().getProductsMenu().openNextTick(viewer, 1));

        this.registerHandler(Type.class)
            .addClick(Type.PRODUCT_CHANGE_PRICE_TYPE, (viewer, event) -> {
                Predicate<PriceType> predicate = priceType -> viewer.getPlayer().hasPermission(ChestPerms.PRICE_TYPE + priceType.name().toLowerCase());

                double sell = product.getPricer().getPrice(TradeType.SELL);
                double buy = product.getPricer().getPrice(TradeType.BUY);

                PriceType priceType = CollectionsUtil.next(product.getPricer().getType(), predicate);
                product.setPricer(ProductPricer.from(priceType));

                if (product.getPricer() instanceof RangedProductPricer pricer) {
                    pricer.setPriceMin(TradeType.BUY, buy);
                    pricer.setPriceMax(TradeType.BUY, buy);
                    pricer.setPriceMin(TradeType.SELL, sell);
                    pricer.setPriceMax(TradeType.SELL, sell);
                }
                product.getPricer().setPrice(TradeType.BUY, buy);
                product.getPricer().setPrice(TradeType.SELL, sell);

                this.save(viewer);
            })
        .addClick(Type.PRODUCT_CHANGE_PRICE_BUY, (viewer, event) -> {
            if (this.product.getPricer().getType() == PriceType.FLAT) {
                if (event.isRightClick()) {
                    this.product.getPricer().setPrice(TradeType.BUY, -1);
                    this.save(viewer);
                    return;
                }
            }

            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                if (this.product.getPricer().getType() == PriceType.FLAT) {
                    product.getPricer().setPrice(TradeType.BUY, wrapper.asDouble());
                }
                else {
                    RangedProductPricer pricer = (RangedProductPricer) product.getPricer();
                    if (event.isLeftClick()) {
                        pricer.setPriceMin(TradeType.BUY, wrapper.asDouble());
                    }
                    else {
                        pricer.setPriceMax(TradeType.BUY, wrapper.asDouble());
                    }
                }
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_SELL, (viewer, event) -> {
            if (this.product.getPricer().getType() == PriceType.FLAT) {
                if (event.isRightClick()) {
                    this.product.getPricer().setPrice(TradeType.SELL, -1);
                    this.save(viewer);
                    return;
                }
            }

            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                if (this.product.getPricer().getType() == PriceType.FLAT) {
                    product.getPricer().setPrice(TradeType.SELL, wrapper.asDouble());
                }
                else {
                    RangedProductPricer pricer = (RangedProductPricer) product.getPricer();
                    if (event.isLeftClick()) {
                        pricer.setPriceMin(TradeType.SELL, wrapper.asDouble());
                    }
                    else {
                        pricer.setPriceMax(TradeType.SELL, wrapper.asDouble());
                    }
                }
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_INITIAL, (viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                DynamicProductPricer pricer = (DynamicProductPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, wrapper.asDouble());
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_STEP, (viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                DynamicProductPricer pricer = (DynamicProductPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setStep(tradeType, wrapper.asDouble());
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_REFRESH, (viewer, event) -> {
            FloatProductPricer pricer = (FloatProductPricer) product.getPricer();
            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    pricer.getDays().clear();
                }
                else pricer.getTimes().clear();
                this.save(viewer);
                return;
            }

            if (event.isLeftClick()) {
                EditorManager.suggestValues(viewer.getPlayer(), CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_DAY, wrapper -> {
                    DayOfWeek day = StringUtil.getEnum(wrapper.getTextRaw(), DayOfWeek.class).orElse(null);
                    if (day == null) {
                        EditorManager.error(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_ERROR_ENUM).getLocalized());
                        return false;
                    }
                    pricer.getDays().add(day);
                    product.getShop().save();
                    return true;
                });
            }
            else {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, wrapper -> {
                    try {
                        pricer.getTimes().add(LocalTime.parse(wrapper.getTextRaw(), TimeUtils.TIME_FORMATTER));
                        product.getShop().save();
                        return true;
                    }
                    catch (DateTimeParseException ex) {
                        return false;
                    }
                });
            }
        });

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getType() == Type.PRODUCT_CHANGE_PRICE_REFRESH) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.product.getPricer().getType() == PriceType.FLOAT);
            }
            else if (menuItem.getType() == Type.PRODUCT_CHANGE_PRICE_INITIAL || menuItem.getType() == Type.PRODUCT_CHANGE_PRICE_STEP) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.product.getPricer().getType() == PriceType.DYNAMIC);
            }
            else if (menuItem.getType() == Type.PRODUCT_CHANGE_PRICE_BUY) {
                menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                    ItemUtil.replaceLore(item, "%price_buy%", this.formatLorePrice.get(TradeType.BUY).get(this.product.getPricer().getType()));
                });
            }
            else if (menuItem.getType() == Type.PRODUCT_CHANGE_PRICE_SELL) {
                menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                    ItemUtil.replaceLore(item, "%price_sell%", this.formatLorePrice.get(TradeType.SELL).get(this.product.getPricer().getType()));
                });
            }
            menuItem.getOptions().addDisplayModifier((viewer, item) -> ItemUtil.replace(item, this.product.replacePlaceholders()));
        });
    }

    private void save(@NotNull MenuViewer viewer) {
        this.product.getShop().save();
        this.openNextTick(viewer, viewer.getPage());
    }

    private enum Type {
        PRODUCT_CHANGE_PRICE_TYPE,
        PRODUCT_CHANGE_PRICE_BUY,
        PRODUCT_CHANGE_PRICE_SELL,
        PRODUCT_CHANGE_PRICE_REFRESH,
        PRODUCT_CHANGE_PRICE_INITIAL,
        PRODUCT_CHANGE_PRICE_STEP,
    }
}
