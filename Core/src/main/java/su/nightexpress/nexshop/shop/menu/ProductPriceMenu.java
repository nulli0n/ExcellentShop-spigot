package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.type.RefreshType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.product.price.impl.DynamicPricing;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.product.price.impl.FloatPricing;
import su.nightexpress.nexshop.product.price.impl.PlayersPricing;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.language.entry.LangUIButton;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.LinkHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@Deprecated
public abstract class ProductPriceMenu<T extends AbstractProduct<?>> extends LinkedMenu<ShopPlugin, T> {

    private static final String SKULL_BUY     = "33658f9ed2145ef8323ec8dc2688197c58964510f3d29939238ce1b6e45af0ff";
    private static final String SKULL_SELL    = "574e65e2f1625b0b2102d6bf3df8264ac43d9d679437a3a42e262d24c4fc";
    private static final String SKULL_CLOCK   = "cbbc06a8d6b1492e40f0e7c3b632b6fd8e66dc45c15234990caa5410ac3ac3fd";
    private static final String SKULL_DAYS    = "ffe96fc80becc0df08be5fbbd3696bfadf75a35e98ac623a475ef6e141d8fb6b";
    private static final String SKULL_INITIAL = "271c2b4782646b380408ab4489c98531b30ed42a3a9f6d33269efc5edf5dc0e8";
    private static final String SKULL_STEP    = "7b14317e3aa4bea88027d5457a6b13e1bea7b54f5ae1486491063ec0186ed662";
    private static final String SKULL_RESET   = "802246ff8b6c617168edaec39660612e72a54ab2eacc27c5e815e4ac70239e3a";

    public ProductPriceMenu(@NotNull ShopPlugin plugin, @NotNull String title) {
        super(plugin, MenuType.GENERIC_9X6, title);

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.handleReturn(viewer, event, this.getLink(viewer));
        }));

        this.addItem(Material.NAME_TAG, Lang.PRODUCT_EDIT_PRICE_TYPE, 10, this::handlePriceType);

        this.addItem(Material.GOLD_NUGGET, Lang.PRODUCT_EDIT_PRICE_CURRENCY, 19, this::handleCurrency, ItemOptions.builder().setDisplayModifier((viewer, item) -> {
            item.inherit(NightItem.fromItemStack(this.getLink(viewer).getCurrency().getIcon()))
                .localized(Lang.PRODUCT_EDIT_PRICE_CURRENCY)
                .setHideComponents(true);
        }).build());

        this.addItem(NightItem.asCustomHead(SKULL_RESET), Lang.PRODUCT_PRICE_RESET, 28, (viewer, event, product) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    plugin.getDataManager().resetPriceData(product);
                    product.updatePrice(true);
                    this.open(viewer1.getPlayer(), product);
                },
                (viewer1, event1) -> {
                    this.open(viewer1.getPlayer(), product);
                }
            )));
        }, ItemOptions.builder().setVisibilityPolicy(this::canResetPriceData).build());
    }

    protected void saveAndFlush(@NotNull MenuViewer viewer, @NotNull T product) {
        this.save(viewer, product);
        this.runNextTick(() -> this.flush(viewer));
    }

    protected abstract void save(@NotNull MenuViewer viewer, @NotNull T product);

    protected abstract boolean canResetPriceData(@NotNull MenuViewer viewer);

    protected abstract void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull T product);

    protected abstract void handleCurrency(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull T product);

    protected abstract void handlePriceType(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull T product);

    private void handleFlatPrice(@NotNull MenuViewer viewer,
                                 @NotNull InventoryClickEvent event,
                                 @NotNull T product,
                                 @NotNull TradeType tradeType) {
        if (event.getClick() == ClickType.DROP) {
            if (product.getPricing() instanceof FlatPricing pricing) {
                pricing.setPrice(tradeType, ProductPricing.DISABLED);
                product.updatePrice(false);
            }
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
            if (product.getPricing() instanceof FlatPricing pricing) {
                pricing.setPrice(tradeType, input.asDoubleAbs());
                product.updatePrice(false);
            }
            this.save(viewer, product);
            return true;
        }));
    }

    private void handleRangedPrice(@NotNull MenuViewer viewer,
                                   @NotNull InventoryClickEvent event,
                                   @NotNull T product,
                                   @NotNull FloatPricing pricer,
                                   @NotNull TradeType tradeType) {
        if (event.getClick() == ClickType.DROP) {
            pricer.setPriceRange(tradeType, UniDouble.of(-1D, -1D));
            product.setPrice(tradeType, -1D);
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE.text(), input -> {
            String[] split = input.getTextRaw().split(" ");
            double min = NumberUtil.getDoubleAbs(split[0]);
            double max = split.length >= 2 ? NumberUtil.getDoubleAbs(split[1]) : min;

            pricer.setPriceRange(tradeType, UniDouble.of(min, max));
            this.save(viewer, product);
            return true;
        }));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        AbstractProduct<?> product = this.getLink(viewer);
        ProductPricing pricing = product.getPricing();

        item.replacement(replacer -> {
            replacer.replace(this.getLink(viewer).replacePlaceholders());

            switch (pricing) {
                case DynamicPricing dynamicPricing -> replacer
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.BUY), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.BUY).minOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.BUY), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.BUY).maxOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.SELL), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.SELL).minOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.SELL), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.SELL).maxOffset()))
                    .replace(Placeholders.PRICER_DYNAMIC_INITIAL_BUY, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.BUY).start()))
                    .replace(Placeholders.PRICER_DYNAMIC_INITIAL_SELL, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.SELL).start()))
                    .replace(Placeholders.PRICER_DYNAMIC_STEP_BUY, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.BUY).buyOffset()))
                    .replace(Placeholders.PRICER_DYNAMIC_STEP_SELL, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.SELL).sellOffset()));
                case PlayersPricing dynamicPricing -> replacer
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.BUY), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.BUY).minOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.BUY), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.BUY).maxOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.SELL), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.SELL).minOffset()))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.SELL), () -> String.valueOf(dynamicPricing.getPriceUnit(TradeType.SELL).maxOffset()))
                    .replace(Placeholders.PRICER_DYNAMIC_INITIAL_BUY, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.BUY).start()))
                    .replace(Placeholders.PRICER_DYNAMIC_INITIAL_SELL, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.SELL).start()))
                    .replace(Placeholders.PRICER_PLAYERS_ADJUST_AMOUNT_BUY, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.BUY).offset()))
                    .replace(Placeholders.PRICER_PLAYERS_ADJUST_AMOUNT_SELL, () -> NumberUtil.format(dynamicPricing.getPriceUnit(TradeType.SELL).offset()));
                case FloatPricing pricer -> replacer
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.BUY), () -> String.valueOf(pricer.getMin(TradeType.BUY)))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.BUY), () -> String.valueOf(pricer.getMax(TradeType.BUY)))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MIN.apply(TradeType.SELL), () -> String.valueOf(pricer.getMin(TradeType.SELL)))
                    .replace(Placeholders.PRICER_RANGED_BOUNDS_MAX.apply(TradeType.SELL), () -> String.valueOf(pricer.getMax(TradeType.SELL)))
                    .replace(Placeholders.PRICER_FLOAT_REFRESH_TYPE, () -> pricer.getRefreshType().name())
                    .replace(Placeholders.PRICER_FLOAT_REFRESH_INTERVAL, () -> TimeFormats.toLiteral(pricer.getRefreshIntervalMillis()))
                    .replace(Placeholders.PRICER_FLOAT_REFRESH_DAYS, () -> {
                        if (pricer.getDays().isEmpty()) {
                            return CoreLang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_DAYS.text());
                        }
                        return pricer.getDays().stream().map(day -> CoreLang.goodEntry(Lang.DAYS.getLocalized(day))).collect(Collectors.joining("\n"));
                    })
                    .replace(Placeholders.PRICER_FLOAT_REFRESH_TIMES, () -> {
                        if (pricer.getTimes().isEmpty()) {
                            return CoreLang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_TIMES.text());
                        }
                        return pricer.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).map(CoreLang::goodEntry).collect(Collectors.joining("\n"));
                    })
                    .replace(Placeholders.PRICER_FLOAT_ROUND_DECIMALS, () -> CoreLang.STATE_YES_NO.get(pricer.isRoundDecimals()));
                default -> replacer
                    .replace(Placeholders.PRODUCT_PRICE.apply(TradeType.BUY), () -> String.valueOf(product.getPrice(TradeType.BUY)))
                    .replace(Placeholders.PRODUCT_PRICE.apply(TradeType.SELL), () -> String.valueOf(product.getPrice(TradeType.SELL)));
            }
        });
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        T product = this.getLink(viewer);
        ProductPricing pricer = product.getPricing();
        switch (pricer) {
            case FlatPricing ignored -> this.addFlatButtons(viewer, product);
            case FloatPricing floatPricer -> {
                this.addRangedButtons(viewer, product, floatPricer);
                this.addFloatButtons(viewer, floatPricer);
            }
            case DynamicPricing dynamicPricer -> {
                this.addDynamicButtons(viewer, dynamicPricer);
            }
            case PlayersPricing playersPricer -> {
                this.addPlayersButtons(viewer, playersPricer);
            }
            default -> {}
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    private void addItem(@NotNull MenuViewer viewer,
                         @NotNull NightItem item,
                         @NotNull LangUIButton locale,
                         int slot,
                         @NotNull LinkHandler<T> handler) {
        this.addItem(viewer, item, locale, slot, handler, null);
    }

    private void addItem(@NotNull MenuViewer viewer,
                         @NotNull NightItem item,
                         @NotNull LangUIButton locale,
                         int slot,
                         @NotNull LinkHandler<T> handler,
                         @Nullable ItemOptions options) {
        viewer.addItem(item.localized(locale).toMenuItem().setSlots(slot).setHandler(ItemHandler.forLink(this, handler, options)).build());
    }

    private void addFlatButtons(@NotNull MenuViewer menuViewer, @NotNull T shopItem) {
        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_BUY), Lang.PRODUCT_EDIT_PRICE_FLAT_BUY, 13, (viewer, event, product) -> {
            this.handleFlatPrice(viewer, event, product, TradeType.BUY);
        });

        if (shopItem.getContent() instanceof ItemContent) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_SELL), Lang.PRODUCT_EDIT_PRICE_FLAT_SELL, 15, (viewer, event, product) -> {
                this.handleFlatPrice(viewer, event, product, TradeType.SELL);
            });
        }
    }

    private void addRangedButtons(@NotNull MenuViewer menuViewer, @NotNull T shopItem, @NotNull FloatPricing pricer) {
        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_BUY), Lang.PRODUCT_EDIT_PRICE_BOUNDS_BUY, 13, (viewer, event, product) -> {
            this.handleRangedPrice(viewer, event, product, pricer, TradeType.BUY);
        });

        if (shopItem.getContent() instanceof ItemContent) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_SELL), Lang.PRODUCT_EDIT_PRICE_BOUNDS_SELL, 15, (viewer, event, product) -> {
                this.handleRangedPrice(viewer, event, product, pricer, TradeType.SELL);
            });
        }
    }

    private void addFloatButtons(@NotNull MenuViewer menuViewer, @NotNull FloatPricing pricer) {
        this.addItem(menuViewer, NightItem.fromType(Material.SHEARS), Lang.PRODUCT_EDIT_PRICE_FLOAT_DECIMALS, 40, (viewer, event, product) -> {
            pricer.setRoundDecimals(!pricer.isRoundDecimals());
            this.saveAndFlush(viewer, product);
        });

        this.addItem(menuViewer, NightItem.fromType(Material.OAK_HANGING_SIGN), Lang.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TYPE, 31, (viewer, event, product) -> {
            pricer.setRefreshType(Lists.next(pricer.getRefreshType()));
            this.saveAndFlush(viewer, product);
        });

        if (pricer.getRefreshType() == RefreshType.INTERVAL) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_CLOCK), Lang.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_INTERVAL, 33, (viewer, event, product) -> {
                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS.text(), input -> {
                    pricer.setRefreshInterval(input.asIntAbs(0));
                    this.save(viewer, product);
                    return true;
                }));
            });
        }

        if (pricer.getRefreshType() == RefreshType.FIXED) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_DAYS), Lang.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_DAYS, 33, (viewer, event, product) -> {
                if (event.isRightClick()) {
                    pricer.getDays().clear();
                    this.saveAndFlush(viewer, product);
                    return;
                }

                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_DAY.text(), input -> {
                    DayOfWeek day = StringUtil.getEnum(input.getTextRaw(), DayOfWeek.class).orElse(null);
                    if (day == null) return true;

                    pricer.getDays().add(day);
                    this.save(viewer, product);
                    return true;
                }).setSuggestions(Lists.getEnums(DayOfWeek.class), true));
            });

            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_CLOCK), Lang.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TIMES, 42, (viewer, event, product) -> {
                if (event.isRightClick()) {
                    pricer.getTimes().clear();
                    this.saveAndFlush(viewer, product);
                    return;
                }

                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_TIME.text(), input -> {
                    try {
                        pricer.getTimes().add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                        this.save(viewer, product);
                    }
                    catch (DateTimeParseException ignored) {
                    }
                    return true;
                }));
            });
        }
    }



    private void addDynamicButtons(@NotNull MenuViewer menuViewer, @NotNull DynamicPricing pricer) {
        for (TradeType tradeType : TradeType.values()) {
            String url = tradeType == TradeType.BUY ? SKULL_BUY : SKULL_SELL;
            int slot = tradeType == TradeType.BUY ? 13 : 14;
            LangUIButton button = tradeType == TradeType.BUY ? Lang.PRODUCT_EDIT_PRICE_BOUNDS_BUY : Lang.PRODUCT_EDIT_PRICE_BOUNDS_SELL;

            this.addItem(menuViewer, NightItem.asCustomHead(url), button, slot, (viewer, event, product) -> {
                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE.text(), input -> {
                    String[] split = input.getTextRaw().split(" ");
                    double min = NumberUtil.getDoubleAbs(split[0]);
                    double max = split.length >= 2 ? NumberUtil.getDoubleAbs(split[1]) : min;

                    DynamicPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                    pricer.setPriceUnit(tradeType, unit.start(), unit.buyOffset(), unit.sellOffset(), min, max);
                    this.save(viewer, product);
                    return true;
                }));
            });
        }

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_INITIAL), Lang.PRODUCT_EDIT_PRICE_DYNAMIC_INITIAL, 31, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
                DynamicPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                double start = input.asDouble(0);
                pricer.setPriceUnit(tradeType, start, unit.buyOffset(), unit.sellOffset(), unit.minOffset(), unit.maxOffset());
                this.save(viewer, product);
                return true;
            }));
        });

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_STEP), Lang.PRODUCT_EDIT_PRICE_DYNAMIC_STEP, 33, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
                DynamicPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                double value = input.asDouble(0);
                pricer.setPriceUnit(tradeType, unit.start(), value, value, unit.minOffset(), unit.maxOffset());
                this.save(viewer, product);
                return true;
            }));
        });
    }

    private void addPlayersButtons(@NotNull MenuViewer menuViewer, @NotNull PlayersPricing pricer) {
        for (TradeType tradeType : TradeType.values()) {
            String url = tradeType == TradeType.BUY ? SKULL_BUY : SKULL_SELL;
            int slot = tradeType == TradeType.BUY ? 13 : 14;
            LangUIButton button = tradeType == TradeType.BUY ? Lang.PRODUCT_EDIT_PRICE_BOUNDS_BUY : Lang.PRODUCT_EDIT_PRICE_BOUNDS_SELL;

            this.addItem(menuViewer, NightItem.asCustomHead(url), button, slot, (viewer, event, product) -> {
                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE.text(), input -> {
                    String[] split = input.getTextRaw().split(" ");
                    double min = NumberUtil.getDoubleAbs(split[0]);
                    double max = split.length >= 2 ? NumberUtil.getDoubleAbs(split[1]) : min;

                    PlayersPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                    pricer.setPriceUnit(tradeType, unit.start(), unit.offset(), min, max);
                    this.save(viewer, product);
                    return true;
                }));
            });
        }

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_INITIAL), Lang.PRODUCT_EDIT_PRICE_PLAYERS_INITIAL, 31, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
                PlayersPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                double value = input.asDouble(0);
                pricer.setPriceUnit(tradeType, value, unit.offset(), unit.minOffset(), unit.maxOffset());
                this.save(viewer, product);
                return true;
            }));
        });

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_STEP), Lang.PRODUCT_EDIT_PRICE_PLAYERS_ADJUST, 32, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                PlayersPricing.PriceUnit unit = pricer.getPriceUnit(tradeType);

                double value = input.asDouble(0);
                pricer.setPriceUnit(tradeType, unit.start(), value, unit.minOffset(), unit.maxOffset());
                this.save(viewer, product);
                return true;
            }));
        });
    }
}
