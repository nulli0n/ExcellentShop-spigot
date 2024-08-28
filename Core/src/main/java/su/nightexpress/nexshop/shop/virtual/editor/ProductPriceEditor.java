package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.price.impl.DynamicPricer;
import su.nightexpress.nexshop.product.price.impl.FloatPricer;
import su.nightexpress.nexshop.product.price.impl.PlayersPricer;
import su.nightexpress.nexshop.product.price.impl.RangedPricer;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.text.tag.Tags;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

public class ProductPriceEditor extends EditorMenu<ShopPlugin, VirtualProduct> implements ShopEditor {

    private static final String TEXTURE_BUY  = "33658f9ed2145ef8323ec8dc2688197c58964510f3d29939238ce1b6e45af0ff";
    private static final String TEXTURE_SELL = "574e65e2f1625b0b2102d6bf3df8264ac43d9d679437a3a42e262d24c4fc";

    private final VirtualShopModule module;

    public ProductPriceEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Price Settings [" + Placeholders.PRODUCT_ID + "]"), MenuSize.CHEST_54);
        this.module = module;

        this.addReturn(49, (viewer, event, product) -> {
            this.runNextTick(() -> this.module.openProductEditor(viewer.getPlayer(), product));
        });

        this.addItem(Material.BEACON, VirtualLocales.PRODUCT_PRICE_INFO, 4, (viewer, event, product) -> {
            if (event.isLeftClick()) {
                product.getShop().getPricer().updatePrice(product);
            }
            else if (event.isRightClick()) {
                product.getShop().getPricer().deleteData(product);
            }
            else return;

            this.doFlush(viewer);
        });

        this.addItem(Material.MAP, VirtualLocales.PRODUCT_PRICE_TYPE, 12, (viewer, event, product) -> {
            PriceType priceType = Lists.next(product.getPricer().getType());

            double sell = product.getPricer().getPrice(TradeType.SELL);
            double buy = product.getPricer().getPrice(TradeType.BUY);

            product.setPricer(AbstractProductPricer.from(priceType));
            product.getShop().getPricer().deleteData(product);

            if (product.getPricer() instanceof RangedPricer pricer) {
                pricer.setPriceRange(TradeType.BUY, UniDouble.of(buy, buy));
                pricer.setPriceRange(TradeType.SELL, UniDouble.of(sell, sell));
            }
            product.setPrice(TradeType.BUY, buy);
            product.setPrice(TradeType.SELL, sell);

            this.saveProductAndFlush(viewer, product);
        });

        this.addItem(Material.EMERALD, VirtualLocales.PRODUCT_PRICE_CURRENCY, 13, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_CURRENCY, (dialog, input) -> {
                Currency currency = this.plugin.getCurrencyManager().getCurrency(input.getTextRaw());
                if (currency != null) {
                    product.setCurrency(currency);
                    this.saveProduct(viewer, product);
                }
                return true;
            }).setSuggestions(plugin.getCurrencyManager().getCurrencyIds(), true);
        }).getOptions().setDisplayModifier((viewer, itemStack) -> {
            ItemStack icon = this.getLink(viewer).getCurrency().getIcon();
            itemStack.setType(icon.getType());
            itemStack.setItemMeta(icon.getItemMeta());
            ItemReplacer.create(itemStack).readLocale(VirtualLocales.PRODUCT_PRICE_CURRENCY).trimmed().hideFlags().writeMeta();
        });

        this.addItem(Material.GRAY_DYE, VirtualLocales.PRODUCT_DISCOUNT, 14, (viewer, event, product) -> {
            product.setDiscountAllowed(!product.isDiscountAllowed());
            this.saveProductAndFlush(viewer, product);
        }).getOptions().setDisplayModifier((viewer, itemStack) -> {
            itemStack.setType(this.getLink(viewer).isDiscountAllowed() ? Material.LIME_DYE : Material.GRAY_DYE);
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_BUY), VirtualLocales.PRODUCT_PRICE_FLAT_BUY, 10, (viewer, event, product) -> {
            this.onPriceClick(viewer, event, product, TradeType.BUY);

        }).getOptions().setDisplayModifier((viewer, item) -> {
            Product product = this.getLink(viewer);
            if (product.getPricer() instanceof RangedPricer) {
                ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_PRICE_FLOAT_BUY).hideFlags().trimmed()
                    .replace(product.getPlaceholders())
                    .writeMeta();
            }
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_SELL), VirtualLocales.PRODUCT_PRICE_FLAT_SELL, 16, (viewer, event, product) -> {
                this.onPriceClick(viewer, event, product, TradeType.SELL);

        }).getOptions()
            .addVisibilityPolicy(viewer -> this.getLink(viewer).getPacker() instanceof ItemPacker)
            .setDisplayModifier((viewer, item) -> {
                Product product = this.getLink(viewer);
                if (product.getPricer() instanceof RangedPricer) {
                    ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_PRICE_FLOAT_SELL).hideFlags().trimmed()
                        .replace(product.getPlaceholders())
                        .writeMeta();
                }
            });



        // =============================================
        // Float Price stuff
        // =============================================
        Predicate<MenuViewer> isFloat = viewer -> this.getLink(viewer).getPricer().getType() == PriceType.FLOAT;

        this.addItem(Material.IRON_NUGGET, VirtualLocales.PRODUCT_PRICE_FLOAT_DECIMALS, 29, (viewer, event, product) -> {
            FloatPricer pricer = (FloatPricer) product.getPricer();
            pricer.setRoundDecimals(!pricer.isRoundDecimals());
            this.saveProductAndFlush(viewer, product);
        }).getOptions().setVisibilityPolicy(isFloat);

        this.addItem(Material.SUNFLOWER, VirtualLocales.PRODUCT_PRICE_FLOAT_REFRESH_DAYS, 31, (viewer, event, product) -> {
            FloatPricer pricer = (FloatPricer) product.getPricer();
            if (event.isRightClick()) {
                pricer.getDays().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_DAY, (dialog, input) -> {
                DayOfWeek day = StringUtil.getEnum(input.getTextRaw(), DayOfWeek.class).orElse(null);
                if (day == null) return true;

                pricer.getDays().add(day);
                this.saveProduct(viewer, product);
                return true;
            }).setSuggestions(Lists.getEnums(DayOfWeek.class), true);
        }).getOptions().setVisibilityPolicy(isFloat);

        this.addItem(Material.CLOCK, VirtualLocales.PRODUCT_PRICE_FLOAT_REFRESH_TIMES, 33, (viewer, event, product) -> {
            FloatPricer pricer = (FloatPricer) product.getPricer();
            if (event.isRightClick()) {
                pricer.getTimes().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, (dialog, input) -> {
                try {
                    pricer.getTimes().add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                    this.saveProduct(viewer, product);
                }
                catch (DateTimeParseException ignored) {}
                return true;
            });
        }).getOptions().setVisibilityPolicy(isFloat);



        // =============================================
        // Dynamic Price stuff
        // =============================================
        Predicate<MenuViewer> isDynamic = viewer -> this.getLink(viewer).getPricer().getType() == PriceType.DYNAMIC;

        this.addItem(Material.OAK_SLAB, VirtualLocales.PRODUCT_PRICE_DYNAMIC_INITIAL, 30, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, (dialog, input) -> {
                DynamicPricer pricer = (DynamicPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, input.asDouble());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isDynamic);

        this.addItem(Material.OAK_STAIRS, VirtualLocales.PRODUCT_PRICE_DYNAMIC_STEP, 32, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, (dialog, input) -> {
                DynamicPricer pricer = (DynamicPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setStep(tradeType, input.asDouble());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isDynamic);



        // =============================================
        // Players Price stuff
        // =============================================
        Predicate<MenuViewer> isPlayers = viewer -> this.getLink(viewer).getPricer().getType() == PriceType.PLAYER_AMOUNT;

        this.addItem(Material.OAK_SLAB, VirtualLocales.PRODUCT_PRICE_PLAYERS_INITIAL, 29, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, (dialog, input) -> {
                PlayersPricer pricer = (PlayersPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setInitial(tradeType, input.asDouble());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isPlayers);

        this.addItem(Material.OAK_STAIRS, VirtualLocales.PRODUCT_PRICE_PLAYERS_ADJUST, 31, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE, (dialog, input) -> {
                PlayersPricer pricer = (PlayersPricer) product.getPricer();
                TradeType tradeType = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;

                pricer.setAdjustAmount(tradeType, input.asAnyDouble(0));
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isPlayers);

        this.addItem(Material.COMPARATOR, VirtualLocales.PRODUCT_PRICE_PLAYERS_STEP, 33, (viewer, event, product) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                PlayersPricer pricer = (PlayersPricer) product.getPricer();
                pricer.setAdjustStep(input.asInt());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isPlayers);



        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).getPlaceholders());
        }));
    }

    private void onPriceClick(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product, @NotNull TradeType tradeType) {
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
        LangString string = ranged != null ? Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE : Lang.EDITOR_PRODUCT_ENTER_PRICE;

        this.handleInput(viewer, string, (dialog, input) -> {
            if (pricer.getType() == PriceType.FLAT) {
                product.setPrice(tradeType, input.asDouble());
            }
            else if (ranged != null) {
                ranged.setPriceRange(tradeType, input.asUniDouble());
            }
            this.saveProduct(viewer, product);
            return true;
        });
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.setTitle(this.getLink(viewer).replacePlaceholders().apply(options.getTitle()));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
