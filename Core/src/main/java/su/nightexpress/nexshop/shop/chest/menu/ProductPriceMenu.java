package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.values.UniDouble;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.shop.impl.price.RangedPricer;
import su.nightexpress.nexshop.shop.util.ShopUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

        // TODO Button to force flush prices

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.RETURN, (viewer, event) -> plugin.runTask(task -> product.getShop().openProductsMenu(viewer.getPlayer())));

        this.registerHandler(Type.class)
            .addClick(Type.PRODUCT_CHANGE_CURRENCY, (viewer, event) -> {
                List<Currency> currencies = new ArrayList<>(ChestUtils.getAllowedCurrencies());
                int index = currencies.indexOf(product.getCurrency()) + 1;
                if (index >= currencies.size()) index = 0;
                product.setCurrency(currencies.get(index));
                this.save(viewer);
            })
            .addClick(Type.PRODUCT_CHANGE_PRICE_TYPE, (viewer, event) -> {
                Predicate<PriceType> predicate = priceType -> viewer.getPlayer().hasPermission(ChestPerms.PREFIX_PRICE_TYPE + priceType.name().toLowerCase());

                double sell = product.getPricer().getPrice(TradeType.SELL);
                double buy = product.getPricer().getPrice(TradeType.BUY);

                PriceType priceType = CollectionsUtil.next(product.getPricer().getType(), predicate);
                product.setPricer(AbstractProductPricer.from(priceType));

                if (product.getPricer() instanceof RangedPricer pricer) {
                    pricer.setPrice(TradeType.BUY, UniDouble.of(buy, buy));
                    pricer.setPrice(TradeType.SELL, UniDouble.of(sell, sell));
                }
                product.setPrice(TradeType.BUY, buy);
                product.setPrice(TradeType.SELL, sell);

                this.save(viewer);
            })
        .addClick(Type.PRODUCT_CHANGE_PRICE_BUY, (viewer, event) -> {
            AbstractProductPricer pricer = this.product.getPricer();
            if (event.getClick() == ClickType.DROP) {
                if (pricer instanceof RangedPricer ranged) {
                    ranged.setPrice(TradeType.BUY, UniDouble.of(-1D, -1D));
                }
                product.setPrice(TradeType.BUY, -1D);
                this.save(viewer);
                return;
            }

            RangedPricer ranged = pricer instanceof RangedPricer rp ? rp : null;
            LangKey key = ranged != null ? Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE : Lang.EDITOR_PRODUCT_ENTER_PRICE;

            this.handleInput(viewer, key, wrapper -> {
                if (pricer.getType() == PriceType.FLAT) {
                    product.setPrice(TradeType.BUY, wrapper.asDouble());
                }
                else if (ranged != null) {
                    ranged.setPrice(TradeType.BUY, wrapper.asUniDouble());
                }
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_SELL, (viewer, event) -> {
            AbstractProductPricer pricer = this.product.getPricer();
            if (event.getClick() == ClickType.DROP) {
                if (pricer instanceof RangedPricer ranged) {
                    ranged.setPrice(TradeType.SELL, UniDouble.of(-1D, -1D));
                }
                product.setPrice(TradeType.SELL, -1D);
                this.save(viewer);
                return;
            }

            RangedPricer ranged = pricer instanceof RangedPricer rp ? rp : null;
            LangKey key = ranged != null ? Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE : Lang.EDITOR_PRODUCT_ENTER_PRICE;

            this.handleInput(viewer, key, wrapper -> {
                if (pricer.getType() == PriceType.FLAT) {
                    product.setPrice(TradeType.SELL, wrapper.asDouble());
                }
                else if (ranged != null) {
                    ranged.setPrice(TradeType.SELL, wrapper.asUniDouble());
                }
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_INITIAL, (viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                DynamicPricer pricer = (DynamicPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, wrapper.asDouble());
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_STEP, (viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, wrapper -> {
                DynamicPricer pricer = (DynamicPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setStep(tradeType, wrapper.asDouble());
                product.getShop().save();
                return true;
            });
        })
        .addClick(Type.PRODUCT_CHANGE_PRICE_REFRESH, (viewer, event) -> {
            FloatPricer pricer = (FloatPricer) product.getPricer();
            if (event.getClick() == ClickType.DROP) {
                pricer.setRoundDecimals(!pricer.isRoundDecimals());
                this.save(viewer);
                return;
            }

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
                        pricer.getTimes().add(LocalTime.parse(wrapper.getTextRaw(), ShopUtils.TIME_FORMATTER));
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
        PRODUCT_CHANGE_CURRENCY,
        PRODUCT_CHANGE_PRICE_TYPE,
        PRODUCT_CHANGE_PRICE_BUY,
        PRODUCT_CHANGE_PRICE_SELL,
        PRODUCT_CHANGE_PRICE_REFRESH,
        PRODUCT_CHANGE_PRICE_INITIAL,
        PRODUCT_CHANGE_PRICE_STEP,
    }
}
