package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.type.RefreshType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.price.impl.*;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.language.entry.LangItem;
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
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@SuppressWarnings("UnstableApiUsage")
public class ProductPriceMenu extends LinkedMenu<ShopPlugin, VirtualProduct> {

    private static final String TEXTURE_BUY  = "33658f9ed2145ef8323ec8dc2688197c58964510f3d29939238ce1b6e45af0ff";
    private static final String TEXTURE_SELL = "574e65e2f1625b0b2102d6bf3df8264ac43d9d679437a3a42e262d24c4fc";
    private static final String SKULL_CLOCK = "cbbc06a8d6b1492e40f0e7c3b632b6fd8e66dc45c15234990caa5410ac3ac3fd";
    private static final String SKULL_DAYS = "ffe96fc80becc0df08be5fbbd3696bfadf75a35e98ac623a475ef6e141d8fb6b";
    private static final String SKULL_RESET = "802246ff8b6c617168edaec39660612e72a54ab2eacc27c5e815e4ac70239e3a";
    private static final String SKULL_INITIAL = "271c2b4782646b380408ab4489c98531b30ed42a3a9f6d33269efc5edf5dc0e8";
    private static final String SKULL_STEP = "7b14317e3aa4bea88027d5457a6b13e1bea7b54f5ae1486491063ec0186ed662";
    private static final String SKULL_PLAYER = "97e6e5657b8f85f3af2c835b3533856607682f8571a4548967e2bdb535ac56b7";

    public ProductPriceMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_PRODUCT_PRICE.getString());

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> module.openProductOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(NightItem.asCustomHead(SKULL_RESET), VirtualLocales.PRODUCT_PRICE_RESET, 28, (viewer, event, product) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    plugin.getDataManager().resetPriceData(product);
                    product.updatePrice(true);
                    module.openPriceOptions(viewer1.getPlayer(), product);
                },
                (viewer1, event1) -> {
                    module.openPriceOptions(viewer1.getPlayer(), product);
                }
            )));
        });

        this.addItem(Material.NAME_TAG, VirtualLocales.PRODUCT_EDIT_PRICE_TYPE, 10, (viewer, event, product) -> {
            PriceType priceType = Lists.next(product.getPricer().getType());

            double sell = product.getPricer().getPrice(TradeType.SELL);
            double buy = product.getPricer().getPrice(TradeType.BUY);

            product.setPricer(AbstractProductPricer.from(priceType));
            plugin.getDataManager().resetPriceData(product);

            if (product.getPricer() instanceof RangedPricer pricer) {
                pricer.setPriceRange(TradeType.BUY, UniDouble.of(buy, buy));
                pricer.setPriceRange(TradeType.SELL, UniDouble.of(sell, sell));
            }
            product.setPrice(TradeType.BUY, buy);
            product.setPrice(TradeType.SELL, sell);

            this.saveAndFlush(viewer, product);
        });

        this.addItem(Material.EMERALD, VirtualLocales.PRODUCT_EDIT_PRICE_CURRENCY, 19, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_CURRENCY, input -> {
                Currency currency = EconomyBridge.getCurrency(input.getTextRaw());
                if (currency != null) {
                    product.setCurrency(currency);
                    product.save();
                }
                return true;
            }).setSuggestions(EconomyBridge.getCurrencyIds(), true));
        }, ItemOptions.builder().setDisplayModifier((viewer, item) -> {
            item.inherit(NightItem.fromItemStack(this.getLink(viewer).getCurrency().getIcon()))
                .localized(VirtualLocales.PRODUCT_EDIT_PRICE_CURRENCY)
                .setHideComponents(true);
        }).build());

//        this.addItem(Material.GRAY_DYE, VirtualLocales.PRODUCT_EDIT_DISCOUNT, 28, (viewer, event, product) -> {
//            product.setDiscountAllowed(!product.isDiscountAllowed());
//            this.saveProductAndFlush(viewer, product);
//        }, ItemOptions.builder().setDisplayModifier((viewer, itemStack) -> {
//            itemStack.setMaterial(this.getLink(viewer).isDiscountAllowed() ? Material.LIME_DYE : Material.GRAY_DYE);
//        }).build());
    }

    private void handleFlatPrice(@NotNull MenuViewer viewer,
                                 @NotNull InventoryClickEvent event,
                                 @NotNull VirtualProduct product,
                                 @NotNull TradeType tradeType) {
        if (event.getClick() == ClickType.DROP) {
            product.setPrice(tradeType, -1D);
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, input -> {
            product.setPrice(tradeType, input.asDouble(0));
            product.save();
            return true;
        }));
    }

    private void handleRangedPrice(@NotNull MenuViewer viewer,
                                   @NotNull InventoryClickEvent event,
                                   @NotNull VirtualProduct product,
                                   @NotNull RangedPricer pricer,
                                   @NotNull TradeType tradeType) {
        if (event.getClick() == ClickType.DROP) {
            pricer.setPriceRange(tradeType, UniDouble.of(-1D, -1D));
            product.setPrice(tradeType, -1D);
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE, input -> {
            String[] split = input.getTextRaw().split(" ");
            double min = NumberUtil.getDoubleAbs(split[0]);
            double max = split.length >= 2 ? NumberUtil.getDoubleAbs(split[1]) : min;

            pricer.setPriceRange(tradeType, UniDouble.of(min, max));
            product.save();
            return true;
        }));
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull VirtualProduct product) {
        product.save();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        VirtualProduct product = this.getLink(viewer);
        AbstractProductPricer pricer = product.getPricer();
        switch (pricer) {
            case FlatPricer ignored -> this.addFlatButtons(viewer, product);
            case FloatPricer floatPricer -> {
                this.addRangedButtons(viewer, product, floatPricer);
                this.addFloatButtons(viewer, floatPricer);
            }
            case DynamicPricer dynamicPricer -> {
                this.addRangedButtons(viewer, product, dynamicPricer);
                this.addDynamicButtons(viewer, dynamicPricer);
            }
            case PlayersPricer playersPricer -> {
                this.addRangedButtons(viewer, product, playersPricer);
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
                         @NotNull LangItem locale,
                         int slot,
                         @NotNull LinkHandler<VirtualProduct> handler) {
        this.addItem(viewer, item, locale, slot, handler, null);
    }

    private void addItem(@NotNull MenuViewer viewer,
                         @NotNull NightItem item,
                         @NotNull LangItem locale,
                         int slot,
                         @NotNull LinkHandler<VirtualProduct> handler,
                         @Nullable ItemOptions options) {
        viewer.addItem(item.localized(locale).toMenuItem().setSlots(slot).setHandler(ItemHandler.forLink(this, handler, options)).build());
    }

    private void addFlatButtons(@NotNull MenuViewer menuViewer, @NotNull VirtualProduct shopItem) {
        this.addItem(menuViewer, NightItem.asCustomHead(TEXTURE_BUY), VirtualLocales.PRODUCT_EDIT_PRICE_FLAT_BUY, 13, (viewer, event, product) -> {
            this.handleFlatPrice(viewer, event, product, TradeType.BUY);
        });

        if (shopItem.getType() instanceof PhysicalTyping) {
            this.addItem(menuViewer, NightItem.asCustomHead(TEXTURE_SELL), VirtualLocales.PRODUCT_EDIT_PRICE_FLAT_SELL, 15, (viewer, event, product) -> {
                this.handleFlatPrice(viewer, event, product, TradeType.SELL);
            });
        }
    }

    private void addRangedButtons(@NotNull MenuViewer menuViewer, @NotNull VirtualProduct shopItem, @NotNull RangedPricer pricer) {
        this.addItem(menuViewer, NightItem.asCustomHead(TEXTURE_BUY), VirtualLocales.PRODUCT_EDIT_PRICE_BOUNDS_BUY, 13, (viewer, event, product) -> {
            this.handleRangedPrice(viewer, event, product, pricer, TradeType.BUY);
        });

        if (shopItem.getType() instanceof PhysicalTyping) {
            this.addItem(menuViewer, NightItem.asCustomHead(TEXTURE_SELL), VirtualLocales.PRODUCT_EDIT_PRICE_BOUNDS_SELL, 15, (viewer, event, product) -> {
                this.handleRangedPrice(viewer, event, product, pricer, TradeType.SELL);
            });
        }
    }

    private void addFloatButtons(@NotNull MenuViewer menuViewer, @NotNull FloatPricer pricer) {
        this.addItem(menuViewer, NightItem.fromType(Material.SHEARS), VirtualLocales.PRODUCT_EDIT_PRICE_FLOAT_DECIMALS, 40, (viewer, event, product) -> {
            pricer.setRoundDecimals(!pricer.isRoundDecimals());
            this.saveAndFlush(viewer, product);
        });

        this.addItem(menuViewer, NightItem.fromType(Material.OAK_HANGING_SIGN), VirtualLocales.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TYPE, 31, (viewer, event, product) -> {
            pricer.setRefreshType(Lists.next(pricer.getRefreshType()));
            this.saveAndFlush(viewer, product);
        });

        if (pricer.getRefreshType() == RefreshType.INTERVAL) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_INTERVAL, 33, (viewer, event, product) -> {
                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS, input -> {
                    pricer.setRefreshInterval(input.asIntAbs(0));
                    product.save();
                    return true;
                }));
            });
        }

        if (pricer.getRefreshType() == RefreshType.FIXED) {
            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_DAYS), VirtualLocales.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_DAYS, 33, (viewer, event, product) -> {
                if (event.isRightClick()) {
                    pricer.getDays().clear();
                    this.saveAndFlush(viewer, product);
                    return;
                }

                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_DAY, input -> {
                    DayOfWeek day = StringUtil.getEnum(input.getTextRaw(), DayOfWeek.class).orElse(null);
                    if (day == null) return true;

                    pricer.getDays().add(day);
                    product.save();
                    return true;
                }).setSuggestions(Lists.getEnums(DayOfWeek.class), true));
            });

            this.addItem(menuViewer, NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TIMES, 42, (viewer, event, product) -> {
                if (event.isRightClick()) {
                    pricer.getTimes().clear();
                    this.saveAndFlush(viewer, product);
                    return;
                }

                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, input -> {
                    try {
                        pricer.getTimes().add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                        product.save();
                    }
                    catch (DateTimeParseException ignored) {
                    }
                    return true;
                }));
            });
        }
    }

    private void addDynamicButtons(@NotNull MenuViewer menuViewer, @NotNull DynamicPricer pricer) {
        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_INITIAL), VirtualLocales.PRODUCT_EDIT_PRICE_DYNAMIC_INITIAL, 31, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, input.asDouble(0));
                product.save();
                return true;
            }));
        });

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_STEP), VirtualLocales.PRODUCT_EDIT_PRICE_DYNAMIC_STEP, 33, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setStep(tradeType, input.asDouble(0));
                product.save();
                return true;
            }));
        });
    }

    private void addPlayersButtons(@NotNull MenuViewer menuViewer, @NotNull PlayersPricer pricer) {
        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_INITIAL), VirtualLocales.PRODUCT_EDIT_PRICE_PLAYERS_INITIAL, 31, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, input.asDouble(0));
                product.save();
                return true;
            }));
        });

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_STEP), VirtualLocales.PRODUCT_EDIT_PRICE_PLAYERS_ADJUST, 32, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, input -> {
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setAdjustAmount(tradeType, input.asDouble(0));
                product.save();
                return true;
            }));
        });

        this.addItem(menuViewer, NightItem.asCustomHead(SKULL_PLAYER), VirtualLocales.PRODUCT_EDIT_PRICE_PLAYERS_STEP, 33, (viewer, event, product) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, input -> {
                pricer.setAdjustStep(input.asInt(0));
                product.save();
                return true;
            }));
        });
    }
}
