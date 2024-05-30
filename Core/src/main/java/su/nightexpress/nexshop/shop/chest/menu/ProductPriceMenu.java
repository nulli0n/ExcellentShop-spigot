package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.shop.impl.price.PlayersPricer;
import su.nightexpress.nexshop.shop.impl.price.RangedPricer;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickAction;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static su.nightexpress.nexshop.shop.chest.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ProductPriceMenu extends ShopEditorMenu implements Linked<ChestProduct>, ShopEditor {

    public static final  String FILE_NAME  = "product_price.yml";
    private static final String PRICE_BUY  = "%price_buy%";
    private static final String PRICE_SELL = "%price_sell%";

    private Map<TradeType, Map<PriceType, List<String>>> formatLorePrice;

    private final ItemHandler returnHandler;
    private final ItemHandler currencyHandler;
    private final ItemHandler priceTypeHandler;
    private final ItemHandler buyPriceHandler;
    private final ItemHandler sellPriceHandler;

    private final ItemHandler refreshPriceHandler;

    private final ItemHandler dynInitialHandler;
    private final ItemHandler dynStepHandler;

    private final ItemHandler playerInitialHandler;
    private final ItemHandler playerAdjustHandler;
    private final ItemHandler playerStepHandler;

    private final ViewLink<ChestProduct> link;

    public ProductPriceMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.link = new ViewLink<>();
        this.formatLorePrice = new HashMap<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            this.runNextTick(() -> module.openProductsMenu(viewer.getPlayer(), product.getShop()));
        }));

        this.addHandler(this.currencyHandler = new ItemHandler("product_change_currency", (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            List<Currency> currencies = new ArrayList<>(module.getAllowedCurrencies());
            int index = currencies.indexOf(product.getCurrency()) + 1;
            if (index >= currencies.size()) index = 0;
            product.setCurrency(currencies.get(index));
            this.saveProductAndFlush(viewer, product);
        }));

        this.addHandler(this.priceTypeHandler = new ItemHandler("product_change_price_type", (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            Predicate<PriceType> predicate = priceType -> viewer.getPlayer().hasPermission(ChestPerms.PREFIX_PRICE_TYPE + priceType.name().toLowerCase());

            double sell = product.getPricer().getPrice(TradeType.SELL);
            double buy = product.getPricer().getPrice(TradeType.BUY);

            PriceType priceType = Lists.next(product.getPricer().getType(), predicate);
            product.setPricer(AbstractProductPricer.from(priceType));
            product.getShop().getPricer().deleteData(product);

            if (product.getPricer() instanceof RangedPricer pricer) {
                pricer.setPriceRange(TradeType.BUY, UniDouble.of(buy, buy));
                pricer.setPriceRange(TradeType.SELL, UniDouble.of(sell, sell));
            }
            product.setPrice(TradeType.BUY, buy);
            product.setPrice(TradeType.SELL, sell);

            this.saveProductAndFlush(viewer, product);
        }));

        this.addHandler(this.buyPriceHandler = new ItemHandler("product_change_price_buy", this.forPrice(TradeType.BUY)));

        this.addHandler(this.sellPriceHandler = new ItemHandler("product_change_price_sell", this.forPrice(TradeType.SELL)));

        this.addHandler(this.refreshPriceHandler = new ItemHandler("product_change_price_refresh", (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            FloatPricer pricer = (FloatPricer) product.getPricer();
            if (event.getClick() == ClickType.DROP) {
                pricer.setRoundDecimals(!pricer.isRoundDecimals());
                this.saveProductAndFlush(viewer, product);
                return;
            }

            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    pricer.getDays().clear();
                }
                else pricer.getTimes().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            if (event.isLeftClick()) {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_DAY, (dialog, input) -> {
                    DayOfWeek day = StringUtil.getEnum(input.getTextRaw(), DayOfWeek.class).orElse(null);
                    if (day == null) return false;

                    pricer.getDays().add(day);
                    this.saveProduct(viewer, product);
                    return true;
                }).setSuggestions(Lists.getEnums(DayOfWeek.class), true);
            }
            else {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, (dialog, input) -> {
                    try {
                        pricer.getTimes().add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                        this.saveProduct(viewer, product);
                        return true;
                    }
                    catch (DateTimeParseException exception) {
                        return false;
                    }
                });
            }
        }));

        this.addHandler(this.dynStepHandler = new ItemHandler("product_change_price_step", this.forDynamicPricer(0)));

        this.addHandler(this.dynInitialHandler = new ItemHandler("product_change_price_initial", this.forDynamicPricer(1)));



        this.addHandler(this.playerAdjustHandler = new ItemHandler("product_change_price_player_adjust", this.forDynamicPricer(0)));

        this.addHandler(this.playerInitialHandler = new ItemHandler("product_change_price_player_initial", this.forDynamicPricer(1)));

        this.addHandler(this.playerStepHandler = new ItemHandler("product_change_price_player_step", (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            PlayersPricer pricer = (PlayersPricer) product.getPricer();

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                pricer.setAdjustStep(input.asInt());
                this.saveProduct(viewer, product);
                return true;
            });
        }));



        this.load();

        this.getItems().forEach(menuItem -> {
            ItemHandler handler = menuItem.getHandler();

            if (handler == this.refreshPriceHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getPricer().getType() == PriceType.FLOAT);
            }
            else if (handler == this.dynInitialHandler || handler == this.dynStepHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getPricer().getType() == PriceType.DYNAMIC);
            }
            else if (handler == this.playerInitialHandler || handler == this.playerAdjustHandler || handler == this.playerStepHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getPricer().getType() == PriceType.PLAYER_AMOUNT);
            }
            else if (handler == this.buyPriceHandler || handler == this.sellPriceHandler) {
                TradeType tradeType = handler == this.buyPriceHandler ? TradeType.BUY : TradeType.SELL;
                String placeholder = tradeType == TradeType.BUY ? PRICE_BUY : PRICE_SELL;

                menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                    ItemReplacer.create(item).readMeta()
                        .replaceLoreExact(placeholder, this.formatLorePrice.get(tradeType).get(this.getLink(viewer).getPricer().getType()))
                        .writeMeta();
                });
            }
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ItemReplacer.create(item).readMeta().trimmed()
                    .replace(this.getLink(viewer).getPlaceholders())
                    .replacePlaceholderAPI(viewer.getPlayer())
                    .writeMeta();
            });
        });
    }

    @NotNull
    @Override
    public ViewLink<ChestProduct> getLink() {
        return link;
    }

    @NotNull
    private ClickAction forPrice(@NotNull TradeType tradeType) {
        return (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            AbstractProductPricer pricer = product.getPricer();
            if (event.getClick() == ClickType.DROP) {
                if (pricer instanceof RangedPricer ranged) {
                    ranged.setPriceRange(tradeType, UniDouble.of(-1D, -1D));
                }
                product.setPrice(tradeType, -1D);
                this.saveProductAndFlush(viewer, product);
                return;
            }

            RangedPricer ranged = pricer instanceof RangedPricer rp ? rp : null;
            LangString key = ranged != null ? Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE : Lang.EDITOR_PRODUCT_ENTER_PRICE;

            this.handleInput(viewer, key, (dialog, input) -> {
                if (pricer.getType() == PriceType.FLAT) {
                    product.setPrice(tradeType, input.asDouble());
                }
                else if (ranged != null) {
                    ranged.setPriceRange(tradeType, input.asUniDouble());
                }
                this.saveProduct(viewer, product);
                return true;
            });
        };
    }

    @NotNull
    private ClickAction forDynamicPricer(int index) {
        return (viewer, event) -> {
            ChestProduct product = this.getLink(viewer);
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, (dialog, input) -> {
                AbstractProductPricer pricer = product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
                double value = input.asDouble();

                if (index == 0) {
                    if (pricer instanceof DynamicPricer dynamicPricer) {
                        dynamicPricer.setStep(tradeType, value);
                    }
                    else if (pricer instanceof PlayersPricer playersPricer) {
                        playersPricer.setAdjustAmount(tradeType, input.asAnyDouble(0));
                    }
                }
                else {
                    if (pricer instanceof DynamicPricer dynamicPricer) {
                        dynamicPricer.setInitial(tradeType, value);
                    }
                    else if (pricer instanceof PlayersPricer playersPricer) {
                        playersPricer.setInitial(tradeType, value);
                    }
                }
                this.saveProduct(viewer, product);
                return true;
            });
        };
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Price Settings"), MenuSize.CHEST_27);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(22).setPriority(10).setHandler(this.returnHandler));

        ItemStack currencyItem = new ItemStack(Material.GOLD_NUGGET);
        ItemUtil.editMeta(currencyItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Currency")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Current: ") + PRODUCT_CURRENCY),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ));
        });
        list.add(new MenuItem(currencyItem).setSlots(3).setPriority(10).setHandler(this.currencyHandler));

        ItemStack priceType = new ItemStack(Material.COMPARATOR);
        ItemUtil.editMeta(priceType, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Price Type")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Current: ") + PRODUCT_PRICE_TYPE),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ));
        });
        list.add(new MenuItem(priceType).setSlots(5).setPriority(10).setHandler(this.priceTypeHandler));

        ItemStack buyPriceItem = ItemUtil.getSkinHead("5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1");
        ItemUtil.editMeta(buyPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Buy Price")));
            meta.setLore(Lists.newList(PRICE_BUY));
        });
        list.add(new MenuItem(buyPriceItem).setSlots(10).setPriority(10).setHandler(this.buyPriceHandler));

        ItemStack sellPriceItem = ItemUtil.getSkinHead("4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46");
        ItemUtil.editMeta(sellPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Sell Price")));
            meta.setLore(Lists.newList(PRICE_SELL));
        });
        list.add(new MenuItem(sellPriceItem).setSlots(11).setPriority(10).setHandler(this.sellPriceHandler));

        // ========================================
        // Float Price Items
        // ========================================

        // TODO Split
        ItemStack refreshPriceItem = ItemUtil.getSkinHead("d2d99342be67156cef48b50a97192989222d569b446092899cc23347542e71aa");
        ItemUtil.editMeta(refreshPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Refresh Settings")));
            meta.setLore(Lists.newList(
                LIGHT_RED.enclose("Days and Times are required!"),
                "",
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Round Decimals: ") + PRODUCT_PRICER_FLOAT_ROUND_DECIMALS),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Days: ") ),
                PRODUCT_PRICER_FLOAT_REFRESH_DAYS,
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Times:")),
                PRODUCT_PRICER_FLOAT_REFRESH_TIMES,
                "",
                LIGHT_GRAY.enclose("Defines how often price should"),
                LIGHT_GRAY.enclose("be regenerated with new values."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " [Q/Drop] Key to " + LIGHT_YELLOW.enclose("toggle decimals") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("add day") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Shift-Left to " + LIGHT_YELLOW.enclose("clear days") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("add time") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Shift-Right to " + LIGHT_YELLOW.enclose("clear times") + ".")
            ));
        });
        list.add(new MenuItem(refreshPriceItem).setSlots(15).setPriority(10).setHandler(this.refreshPriceHandler));

        // ========================================
        // Dynamic Price Items
        // ========================================

        ItemStack stepPriceItem = ItemUtil.getSkinHead("200f58f3f7ce73d4aa6cc65f859c36cc7543a82e4f9da031784bd62c8d2bca67");
        ItemUtil.editMeta(stepPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Price Step")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy: ") + PRODUCT_PRICER_DYNAMIC_STEP_BUY),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell: ") + PRODUCT_PRICER_DYNAMIC_STEP_SELL),
                "",
                LIGHT_GRAY.enclose("Defines on how much the price will"),
                LIGHT_GRAY.enclose("go up/down on each purchase/sale."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change buy") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("change sell") + ".")
            ));
        });
        list.add(new MenuItem(stepPriceItem).setSlots(15).setPriority(10).setHandler(this.dynStepHandler));

        ItemStack initPriceItem = ItemUtil.getSkinHead("26e197928a03e65b255d094cc8723faa7d41ccec56643b44b9cccbd0519de879");
        ItemUtil.editMeta(initPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Initial Price")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy: ") + PRODUCT_PRICER_DYNAMIC_INITIAL_BUY),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell: ") + PRODUCT_PRICER_DYNAMIC_INITIAL_SELL),
                "",
                LIGHT_GRAY.enclose("Sets initial (start) price for the product."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change buy") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("change sell") + ".")
            ));
        });
        list.add(new MenuItem(initPriceItem).setSlots(16).setPriority(10).setHandler(this.dynInitialHandler));

        // ========================================
        // Players Price Items
        // ========================================

        ItemStack playerInitPriceItem = ItemUtil.getSkinHead("26e197928a03e65b255d094cc8723faa7d41ccec56643b44b9cccbd0519de879");
        ItemUtil.editMeta(playerInitPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Initial Price")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy: ") + PRODUCT_PRICER_DYNAMIC_INITIAL_BUY),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell: ") + PRODUCT_PRICER_DYNAMIC_INITIAL_SELL),
                "",
                LIGHT_GRAY.enclose("Sets initial (start) price for the product."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change buy") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("change sell") + ".")
            ));
        });
        list.add(new MenuItem(playerInitPriceItem).setSlots(14).setPriority(10).setHandler(this.playerInitialHandler));

        ItemStack playerAdjustPriceItem = ItemUtil.getSkinHead("200f58f3f7ce73d4aa6cc65f859c36cc7543a82e4f9da031784bd62c8d2bca67");
        ItemUtil.editMeta(playerAdjustPriceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Price Adjust")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy: ") + PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_BUY),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell: ") + PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_SELL),
                "",
                LIGHT_GRAY.enclose("Defines how much the price will"),
                LIGHT_GRAY.enclose("be adjusted for each X online players set"),
                LIGHT_GRAY.enclose("in the " + LIGHT_YELLOW.enclose("Adjust Step") + " option."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change buy") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("change sell") + ".")
            ));
        });
        list.add(new MenuItem(playerAdjustPriceItem).setSlots(15).setPriority(10).setHandler(this.playerAdjustHandler));

        ItemStack playerStepItem = ItemUtil.getSkinHead("60d106609f05b9830043f3351b5f3ec3cae28d08900436692320022db491e671");
        ItemUtil.editMeta(playerStepItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Adjust Step")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Current: ") + PRODUCT_PRICER_PLAYERS_ADJUST_STEP),
                "",
                LIGHT_GRAY.enclose("Sets for how much online players"),
                LIGHT_GRAY.enclose("price will be adjusted by values set"),
                LIGHT_GRAY.enclose("in the " + LIGHT_YELLOW.enclose("Price Adjust") + " option."),
                "",
                LIGHT_YELLOW.enclose(BOLD.enclose("Examples:")),
                LIGHT_YELLOW.enclose("1") + " = for " + LIGHT_YELLOW.enclose("every player") + " online.",
                LIGHT_YELLOW.enclose("5") + " = for " + LIGHT_YELLOW.enclose("every 5") + " players (5, 10, 15, etc.)",
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ));
        });
        list.add(new MenuItem(playerStepItem).setSlots(16).setPriority(10).setHandler(this.playerAdjustHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.formatLorePrice = ConfigValue.create("Format.Lore.Price",
            (cfg, path, def) -> {
                Map<TradeType, Map<PriceType, List<String>>> map = new HashMap<>();
                for (TradeType tradeType : TradeType.values()) {
                    for (PriceType priceType : PriceType.values()) {
                        List<String> lore = cfg.getStringList(path + "." + tradeType.name() + "." + priceType.name());
                        map.computeIfAbsent(tradeType, k -> new HashMap<>()).put(priceType, lore);
                    }
                }
                return map;
            },
            (cfg, path, map) -> map.forEach((tradeType, priceMap) -> priceMap.forEach((priceType, lore) -> cfg.set(path + "." + tradeType.name() + "." + priceType.name(), lore))),
            () -> {
                Map<TradeType, Map<PriceType, List<String>>> map = new HashMap<>();

                for (TradeType tradeType : TradeType.values()) {
                    Map<PriceType, List<String>> loreMap = new HashMap<>();

                    for (PriceType priceType : PriceType.values()) {
                        loreMap.put(priceType, switch (priceType) {
                            case FLAT -> Lists.newList(
                                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Current: ") + PRODUCT_PRICE.apply(tradeType)),
                                "",
                                LIGHT_GRAY.enclose("Price set from buyer/seller perspective."),
                                "",
                                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("change") + "."),
                                LIGHT_GRAY.enclose(LIGHT_RED.enclose("[▶]") + " [Q/Drop] Key to " + LIGHT_RED.enclose("disable") + ".")
                            );
                            case FLOAT, DYNAMIC, PLAYER_AMOUNT -> Lists.newList(
                                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Min: ") + PRODUCT_PRICER_RANGE_MIN.apply(tradeType)),
                                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Max: ") + PRODUCT_PRICER_RANGE_MAX.apply(tradeType)),
                                "",
                                LIGHT_GRAY.enclose("Sets price bounds."),
                                "",
                                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("set min") + "."),
                                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("set max") + ".")
                            );
                        });
                    }

                    map.put(tradeType, loreMap);
                }
                return map;
            }).read(cfg);
    }
}
