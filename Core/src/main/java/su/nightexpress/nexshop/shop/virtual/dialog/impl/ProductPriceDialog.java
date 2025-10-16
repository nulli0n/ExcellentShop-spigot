package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.product.price.impl.DynamicPricing;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.product.price.impl.FloatPricing;
import su.nightexpress.nexshop.product.price.impl.PlayersPricing;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.nexshop.Placeholders.*;

public class ProductPriceDialog extends VirtualDialogProvider<VirtualProduct> {

    private static final String JSON_FLAT_BUY  = "buy";
    private static final String JSON_FLAT_SELL = "sell";

    private static final String JSON_FLOAT_BUY_MIN  = "buy_min";
    private static final String JSON_FLOAT_BUY_MAX  = "buy_max";
    private static final String JSON_FLOAT_SELL_MIN = "sell_min";
    private static final String JSON_FLOAT_SELL_MAX = "sell_max";
    private static final String JSON_FLOAT_ROUNDING = "rounding";

    private static final Function<TradeType, String> JSON_DYNAMIC_START       = type -> "start_" + type.index();
    private static final Function<TradeType, String> JSON_DYNAMIC_BUY_OFFSET  = type -> "buy_offset_" + type.index();
    private static final Function<TradeType, String> JSON_DYNAMIC_SELL_OFFSET = type -> "sell_offset_" + type.index();
    private static final Function<TradeType, String> JSON_DYNAMIC_MIN_OFFSET  = type -> "min_offset_" + type.index();
    private static final Function<TradeType, String> JSON_DYNAMIC_MAX_OFFSET  = type -> "max_offset_" + type.index();

    private static final String JSON_DYNAMIC_STABILIZE_INTERVAL = "stabilize_interval";
    private static final String JSON_DYNAMIC_STABILIZE_AMOUNT = "stabilize_amount";

    private static final String ACTION_PRICING_TYPE = "pricing";
    private static final String ACTION_CURRENCY     = "currency";
    private static final String ACTION_FLOAT_TIMES  = "times";

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.ProductPrice.Title").text(TITLE_PREFIX + "Product Price");

    private static final DialogElementLocale BODY_FLAT = VirtualLang.builder("Dialog.ProductPrice.Body.Flat").dialogElement(
        400,
        "Flat pricing allows you to set a " + SOFT_YELLOW.wrap("fixed") + " price for an item, without any conditions that would change it automatically."
    );

    private static final DialogElementLocale BODY_FLOAT = VirtualLang.builder("Dialog.ProductPrice.Body.Float").dialogElement(
        400,
        "Floating pricing allows you to set an " + SOFT_YELLOW.wrap("automatically") + " generated price for an item within defined boundaries.",
        "The price can change at " + SOFT_YELLOW.wrap("specific hours") + " or at a set " + SOFT_YELLOW.wrap("interval") + ".",
        "",
        SOFT_YELLOW.wrap("→") + " The min. and max. values for buying and selling prices are set in the fields below.",
        "",
        SOFT_YELLOW.wrap("→") + " The time for automatic price updates is set using the " + SOFT_YELLOW.wrap("Refresh Times") + " button."
    );

    private static final DialogElementLocale BODY_DYNAMIC = VirtualLang.builder("Dialog.ProductPrice.Body.Dynamic").dialogElement(
        400,
        "Dynamic pricing allows the item's price to change " + SOFT_YELLOW.wrap("automatically") + " by a specified percentage with " + SOFT_YELLOW.wrap("each") + " purchase or sale, up to defined limits.",
        "",
        SOFT_YELLOW.wrap("→") + " The initial price is set in the " + SOFT_YELLOW.wrap("Initial Price") + " field.",
        "",
        SOFT_YELLOW.wrap("→") + " The percentage modifier applied on buying and selling is set in the " + SOFT_YELLOW.wrap("On Buy Offset") + " and " + SOFT_YELLOW.wrap("On Sell Offset") + " fields, respectively.",
        "",
        SOFT_YELLOW.wrap("→") + " The lower and upper bounds of the modifier are set in the " + SOFT_YELLOW.wrap("Min Offset") + " and " + SOFT_YELLOW.wrap("Max Offset") + " fields."
    );

    private static final DialogElementLocale BODY_PLAYERS = VirtualLang.builder("Dialog.ProductPrice.Body.Players").dialogElement(
        400,
        "Online-based pricing allows the item's price to change " + SOFT_YELLOW.wrap("automatically") + " by a specified percentage based on " + SOFT_YELLOW.wrap("online players") + " count.",
        "",
        SOFT_YELLOW.wrap("→") + " The initial price is set in the " + SOFT_YELLOW.wrap("Initial Price") + " field.",
        "",
        SOFT_YELLOW.wrap("→") + " The percentage modifier applied per player online is set in the " + SOFT_YELLOW.wrap("Per Player Offset") + " field.",
        "",
        SOFT_YELLOW.wrap("→") + " The lower and upper bounds of the modifier are set in the " + SOFT_YELLOW.wrap("Min Offset") + " and " + SOFT_YELLOW.wrap("Max Offset") + " fields."
    );

    private static final ButtonLocale BUTTON_PRICING_TYPE = VirtualLang.builder("Dialog.ProductPrice.Button.Pricing")
        .button(SOFT_YELLOW.wrap("→") + " Pricing: " + SOFT_YELLOW.wrap(GENERIC_TYPE),
            RED.wrap("Changing the pricing will cause the item's price data to reset!")
        );

    private static final ButtonLocale BUTTON_CURRENCY = VirtualLang.builder("Dialog.ProductPrice.Button.Currency")
        .button(SOFT_YELLOW.wrap("→") + " Currency: " + SOFT_YELLOW.wrap(CURRENCY_NAME));

    private static final ButtonLocale BUTTON_FLOAT_TIMES = VirtualLang.builder("Dialog.ProductPrice.Float.Button.Times").button(
        SOFT_YELLOW.wrap("⌛") + " Refresh Times");



    private static final DialogElementLocale INPUT_FLAT_BUY = VirtualLang.builder("Dialog.ProductPrice.Flat.Input.Buy").dialogElement(200,
        SOFT_GREEN.wrap("[$]") + " Buy Price");
    private static final DialogElementLocale INPUT_FLAT_SELL = VirtualLang.builder("Dialog.ProductPrice.Flat.Input.Sell").dialogElement(200,
        SOFT_RED.wrap("[$]") + " Sell Price");



    private static final DialogElementLocale INPUT_FLOAT_BUY_MIN = VirtualLang.builder("Dialog.ProductPrice.Float.Input.BuyMin").dialogElement(
        200, SOFT_GREEN.wrap("[$]") + " Buy Price " + GRAY.wrap("(Min)"));
    private static final DialogElementLocale INPUT_FLOAT_BUY_MAX = VirtualLang.builder("Dialog.ProductPrice.Float.Input.BuyMax").dialogElement(
        200, SOFT_GREEN.wrap("[$]") + " Buy Price " + GRAY.wrap("(Max)"));
    private static final DialogElementLocale INPUT_FLOAT_SELL_MIN = VirtualLang.builder("Dialog.ProductPrice.Float.Input.SellMin").dialogElement(
        200, SOFT_RED.wrap("[$]") + " Sell Price " + GRAY.wrap("(Min)"));
    private static final DialogElementLocale INPUT_FLOAT_SELL_MAX = VirtualLang.builder("Dialog.ProductPrice.Float.Input.SellMax").dialogElement(
        200, SOFT_RED.wrap("[$]") + " Sell Price " + GRAY.wrap("(Max)"));
    private static final TextLocale INPUT_FLOAT_ROUNDING = VirtualLang.builder("Dialog.ProductPrice.Float.Input.Rounding").text("Round Prices");



    private static final DialogElementLocale INPUT_DYNAMIC_BUY_START = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Buy.Start").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "Initial Value");

    private static final DialogElementLocale INPUT_DYNAMIC_BUY_BUY_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Buy.BuyOffset").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "On Buy Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_BUY_SELL_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Buy.SellOffset").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "On Sell Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_BUY_MIN_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Buy.MinOffset").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "Min. Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_BUY_MAX_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Buy.MaxOffset").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "Max. Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_SELL_START = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Sell.Start").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "Initial Value");

    private static final DialogElementLocale INPUT_DYNAMIC_SELL_BUY_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Sell.BuyOffset").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "On Buy Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_SELL_SELL_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Sell.SellOffset").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "On Sell Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_SELL_MIN_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Sell.MinOffset").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "Min. Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_SELL_MAX_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Sell.MaxOffset").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "Max. Offset " + GRAY.wrap("(in %)"));

    private static final DialogElementLocale INPUT_DYNAMIC_STABILIZE_INTERVAL = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Stabilize.Interval").dialogElement(200,
        SOFT_YELLOW.wrap("[⌛] Stabilization: ") + "Interval " + GRAY.wrap("(in seconds)"));

    private static final DialogElementLocale INPUT_DYNAMIC_STABILIZE_AMOUNT = VirtualLang.builder("Dialog.ProductPrice.Dynamic.Input.Stabilize.Amount").dialogElement(200,
        SOFT_YELLOW.wrap("[⌛] Stabilization: ") + "Amount " + GRAY.wrap("(in %)"));




    private static final DialogElementLocale INPUT_PLAYERS_BUY_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Players.Input.Buy.Offset").dialogElement(200,
        SOFT_GREEN.wrap("[$] Buy Price: ") + "Per Player Offset");

    private static final DialogElementLocale INPUT_PLAYERS_SELL_OFFSET = VirtualLang.builder("Dialog.ProductPrice.Players.Input.Sell.Offset").dialogElement(200,
        SOFT_RED.wrap("[$] Sell Price: ") + "Per Player Offset");

    public ProductPriceDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualProduct product) {
        ProductPricing pricing = product.getPricing();
        WrappedDialog.Builder builder = Dialogs.builder();

        List<WrappedActionButton> buttons = new ArrayList<>();

        if (pricing.getType() == PriceType.FLOAT) {
            buttons.add(DialogButtons.action(BUTTON_FLOAT_TIMES).action(DialogActions.customClick(ACTION_FLOAT_TIMES)).build());
        }

        buttons.add(DialogButtons.action(BUTTON_PRICING_TYPE.replace(str -> str.replace(GENERIC_TYPE, Lang.PRICE_TYPES.getLocalized(pricing.getType()))))
            .action(DialogActions.customClick(ACTION_PRICING_TYPE)).build());

        buttons.add(DialogButtons.action(BUTTON_CURRENCY.replace(product.getCurrency().replacePlaceholders()))
            .action(DialogActions.customClick(ACTION_CURRENCY)).build());

        buttons.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build());

        builder.type(DialogTypes.multiAction(buttons)
            .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).action(DialogActions.customClick(ACTION_BACK)).build())
            .columns(1)
            .build()
        );

        builder.handleResponse(ACTION_PRICING_TYPE, (user, identifier, nbtHolder) -> {
            PriceType type = Lists.next(pricing.getType());
            ProductPricing newPricing = ProductPricing.from(type);
            this.plugin.getDataManager().resetPriceData(product);
            product.setPricing(newPricing);
            product.getShop().markDirty();
            this.showNextTick(user.getPlayer(), product);
        });

        builder.handleResponse(ACTION_CURRENCY, (user, identifier, nbtHolder) -> {
            this.saveFields(product, pricing, nbtHolder);
            this.module.handleDialogs(dialogs -> dialogs.openProductCurrency(user.getPlayer(), product));
        });

        builder.handleResponse(ACTION_BACK, (user, identifier, nbtHolder) -> {
            this.closeAndThen(user.getPlayer(), product, this.module::openProductOptions);
        });

        builder.handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
            this.saveFields(product, pricing, nbtHolder);
            this.closeAndThen(user.getPlayer(), product, this.module::openProductOptions);
        });

        switch (pricing) {
            case FlatPricing flatPricing -> this.flatPricing(builder, product, flatPricing);
            case FloatPricing floatPricing -> this.floatPricing(builder, product, floatPricing);
            case DynamicPricing dynamicPricing -> this.dynamicPricing(builder, product, dynamicPricing);
            case PlayersPricing playersPricing -> this.playersPricing(builder, product, playersPricing);
            default -> {}
        }

        Dialogs.showDialog(player, builder.build());
    }

    private void flatPricing(@NotNull WrappedDialog.Builder builder, @NotNull VirtualProduct product, @NotNull FlatPricing pricing) {
        List<WrappedDialogInput> inputs = Lists.newList(
            DialogInputs.text(JSON_FLAT_BUY, INPUT_FLAT_BUY).maxLength(10).initial(String.valueOf(pricing.getPrice(TradeType.BUY))).build()
        );

        if (product.getContent().type() == ContentType.ITEM) {
            inputs.add(DialogInputs.text(JSON_FLAT_SELL, INPUT_FLAT_SELL).maxLength(10).initial(String.valueOf(pricing.getPrice(TradeType.SELL))).build());
        }

        builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY_FLAT))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            );
    }

    private void floatPricing(@NotNull WrappedDialog.Builder builder, @NotNull VirtualProduct product, @NotNull FloatPricing pricing) {
        List<WrappedDialogInput> inputs = Lists.newList(
            DialogInputs.text(JSON_FLOAT_BUY_MIN, INPUT_FLOAT_BUY_MIN).maxLength(10).initial(String.valueOf(pricing.getMin(TradeType.BUY))).build(),
            DialogInputs.text(JSON_FLOAT_BUY_MAX, INPUT_FLOAT_BUY_MAX).maxLength(10).initial(String.valueOf(pricing.getMax(TradeType.BUY))).build()
        );

        if (product.getContent().type() == ContentType.ITEM) {
            inputs.add(DialogInputs.text(JSON_FLOAT_SELL_MIN, INPUT_FLOAT_SELL_MIN).maxLength(10).initial(String.valueOf(pricing.getMin(TradeType.SELL))).build());
            inputs.add(DialogInputs.text(JSON_FLOAT_SELL_MAX, INPUT_FLOAT_SELL_MAX).maxLength(10).initial(String.valueOf(pricing.getMax(TradeType.SELL))).build());
        }

        inputs.add(DialogInputs.bool(JSON_FLOAT_ROUNDING, INPUT_FLOAT_ROUNDING).initial(pricing.isRoundDecimals()).build());

        builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY_FLOAT))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .handleResponse(ACTION_FLOAT_TIMES, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                this.saveFields(product, pricing, nbtHolder);
                this.module.handleDialogs(dialogs -> dialogs.openFloatPricingTimes(user.getPlayer(), product));
            });
    }

    private void dynamicPricing(@NotNull WrappedDialog.Builder builder, @NotNull VirtualProduct product, @NotNull DynamicPricing pricing) {
        List<WrappedDialogInput> inputs = Lists.newList();
        int length = 20;

        for (TradeType tradeType : TradeType.values()) {
            if (tradeType == TradeType.SELL && product.getContent().type() != ContentType.ITEM) continue;

            DynamicPricing.PriceUnit unit = pricing.getPriceUnit(tradeType);

            var localeStart = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_START : INPUT_DYNAMIC_SELL_START;
            var localeBuyOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_BUY_OFFSET : INPUT_DYNAMIC_SELL_BUY_OFFSET;
            var localeSellOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_SELL_OFFSET : INPUT_DYNAMIC_SELL_SELL_OFFSET;
            var localeMinOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_MIN_OFFSET : INPUT_DYNAMIC_SELL_MIN_OFFSET;
            var localeMaxOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_MAX_OFFSET : INPUT_DYNAMIC_SELL_MAX_OFFSET;

            inputs.add(DialogInputs.text(JSON_DYNAMIC_START.apply(tradeType), localeStart).maxLength(length).initial(String.valueOf(unit.start())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_BUY_OFFSET.apply(tradeType), localeBuyOffset).maxLength(length).initial(String.valueOf(unit.buyOffset())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_SELL_OFFSET.apply(tradeType), localeSellOffset).maxLength(length).initial(String.valueOf(unit.sellOffset())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_MIN_OFFSET.apply(tradeType), localeMinOffset).maxLength(length).initial(String.valueOf(unit.minOffset())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_MAX_OFFSET.apply(tradeType), localeMaxOffset).maxLength(length).initial(String.valueOf(unit.maxOffset())).build());
        }

        inputs.add(DialogInputs.text(JSON_DYNAMIC_STABILIZE_INTERVAL, INPUT_DYNAMIC_STABILIZE_INTERVAL).maxLength(length).initial(String.valueOf(pricing.getStabilizeInterval())).build());
        inputs.add(DialogInputs.text(JSON_DYNAMIC_STABILIZE_AMOUNT, INPUT_DYNAMIC_STABILIZE_AMOUNT).maxLength(length).initial(String.valueOf(pricing.getStabilizeAmount())).build());

        builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY_DYNAMIC))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            );
    }

    private void playersPricing(@NotNull WrappedDialog.Builder builder, @NotNull VirtualProduct product, @NotNull PlayersPricing pricing) {
        List<WrappedDialogInput> inputs = Lists.newList();

        for (TradeType tradeType : TradeType.values()) {
            if (tradeType == TradeType.SELL && product.getContent().type() != ContentType.ITEM) continue;

            PlayersPricing.PriceUnit unit = pricing.getPriceUnit(tradeType);

            var localeStart = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_START : INPUT_DYNAMIC_SELL_START;
            var localeBuyOffset = tradeType == TradeType.BUY ? INPUT_PLAYERS_BUY_OFFSET : INPUT_PLAYERS_SELL_OFFSET;
            var localeMinOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_MIN_OFFSET : INPUT_DYNAMIC_SELL_MIN_OFFSET;
            var localeMaxOffset = tradeType == TradeType.BUY ? INPUT_DYNAMIC_BUY_MAX_OFFSET : INPUT_DYNAMIC_SELL_MAX_OFFSET;

            int length = 20;

            inputs.add(DialogInputs.text(JSON_DYNAMIC_START.apply(tradeType), localeStart).maxLength(length).initial(String.valueOf(unit.start())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_BUY_OFFSET.apply(tradeType), localeBuyOffset).maxLength(length).initial(String.valueOf(unit.offset())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_MIN_OFFSET.apply(tradeType), localeMinOffset).maxLength(length).initial(String.valueOf(unit.minOffset())).build());
            inputs.add(DialogInputs.text(JSON_DYNAMIC_MAX_OFFSET.apply(tradeType), localeMaxOffset).maxLength(length).initial(String.valueOf(unit.maxOffset())).build());
        }

        builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY_PLAYERS))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            );
    }

    private void saveFields(@NotNull VirtualProduct product, @NotNull ProductPricing price, @Nullable NightNbtHolder nbtHolder) {
        if (nbtHolder == null) return;

        switch (price) {
            case FlatPricing pricing -> {
                double buyPrice = nbtHolder.getFloat(JSON_FLAT_BUY).map(Float::doubleValue).orElse(0D);
                double sellPrice = nbtHolder.getFloat(JSON_FLAT_SELL).map(Float::doubleValue).orElse(0D);

                pricing.setPrice(TradeType.BUY, buyPrice);
                pricing.setPrice(TradeType.SELL, sellPrice);
            }
            case FloatPricing floatPricing -> {
                double buyPriceMin = nbtHolder.getDouble(JSON_FLOAT_BUY_MIN).orElse(0D);
                double buyPriceMax = nbtHolder.getDouble(JSON_FLOAT_BUY_MAX).orElse(0D);
                double sellPriceMin = nbtHolder.getDouble(JSON_FLOAT_SELL_MIN).orElse(floatPricing.getMin(TradeType.SELL));
                double sellPriceMax = nbtHolder.getDouble(JSON_FLOAT_SELL_MAX).orElse(floatPricing.getMax(TradeType.SELL));
                boolean rounding = nbtHolder.getBoolean(JSON_FLOAT_ROUNDING).orElse(floatPricing.isRoundDecimals());

                floatPricing.setPriceRange(TradeType.BUY, UniDouble.of(buyPriceMin, buyPriceMax));
                floatPricing.setPriceRange(TradeType.SELL, UniDouble.of(sellPriceMin, sellPriceMax));
                floatPricing.setRoundDecimals(rounding);
            }
            case DynamicPricing pricing -> {
                for (TradeType tradeType : TradeType.values()) {
                    double start = nbtHolder.getDouble(JSON_DYNAMIC_START.apply(tradeType), 0D);
                    double buyOffset = nbtHolder.getDouble(JSON_DYNAMIC_BUY_OFFSET.apply(tradeType), 0D);
                    double sellOffset = nbtHolder.getDouble(JSON_DYNAMIC_SELL_OFFSET.apply(tradeType), 0D);
                    double minOffset = nbtHolder.getDouble(JSON_DYNAMIC_MIN_OFFSET.apply(tradeType), 0D);
                    double maxOffset = nbtHolder.getDouble(JSON_DYNAMIC_MAX_OFFSET.apply(tradeType), 0D);

                    pricing.setPriceUnit(tradeType, start, buyOffset, sellOffset, minOffset, maxOffset);
                    pricing.setStabilizeAmount(nbtHolder.getDouble(JSON_DYNAMIC_STABILIZE_AMOUNT, pricing.getStabilizeAmount()));
                    pricing.setStabilizeInterval(nbtHolder.getInt(JSON_DYNAMIC_STABILIZE_INTERVAL, pricing.getStabilizeInterval()));
                }
            }
            case PlayersPricing pricing -> {
                for (TradeType tradeType : TradeType.values()) {
                    double start = nbtHolder.getDouble(JSON_DYNAMIC_START.apply(tradeType), 0D);
                    double offset = nbtHolder.getDouble(JSON_DYNAMIC_BUY_OFFSET.apply(tradeType), 0D);
                    double minOffset = nbtHolder.getDouble(JSON_DYNAMIC_MIN_OFFSET.apply(tradeType), 0D);
                    double maxOffset = nbtHolder.getDouble(JSON_DYNAMIC_MAX_OFFSET.apply(tradeType), 0D);

                    pricing.setPriceUnit(tradeType, start, offset, minOffset, maxOffset);
                }
            }
            default -> {return;}
        }

        product.updatePrice(false);
        product.getShop().markDirty();
    }
}
