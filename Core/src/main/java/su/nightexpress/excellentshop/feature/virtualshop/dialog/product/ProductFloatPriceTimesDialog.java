package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.RefreshType;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.product.price.FloatPricing;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ProductFloatPriceTimesDialog extends Dialog<VirtualProduct> {

    private static final String JSON_INTERVAL = "interval";
    private static final String JSON_TIMES    = "times";
    private static final String JSON_DAY      = "day";

    private static final String ACTION_DAY  = "day";
    private static final String ACTION_MODE = "mode";

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Title").text(title("Float Price", "Refresh Times"));

    private static final DialogElementLocale BODY_INTERVAL = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Body.Interval").dialogElement(
        400,
        "In " + SOFT_YELLOW.wrap("Interval") + " mode, the item’s price is updated at a specified interval.",
        "",
        SOFT_YELLOW.wrap("→") + " To set the interval, enter the desired value in the field below."
    );

    private static final DialogElementLocale BODY_SCHEDULED = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Body.Scheduled").dialogElement(
        400,
        "In " + SOFT_YELLOW.wrap("Scheduled") + " mode, the item’s price is updated strictly at the specified " + SOFT_YELLOW.wrap("hours") + " on selected " + SOFT_YELLOW.wrap("days") + " of the week.",
        "",
        SOFT_YELLOW.wrap("→") + " To set the " + SOFT_YELLOW.wrap("hours") + ", enter the desired times in the " + SOFT_YELLOW.wrap("Times") + " field " + GRAY.wrap("(one time per line)") + ".",
        "",
        SOFT_YELLOW.wrap("→") + " To choose the " + SOFT_YELLOW.wrap("days") + ", click the weekday buttons. The selected days are highlighted in " + GREEN.wrap("green") + "."
    );

    private static final DialogElementLocale INPUT_INTERVAL = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Input.Interval")
        .dialogElement(120, "Interval " + SOFT_YELLOW.wrap("(in seconds)"));

    private static final DialogElementLocale INPUT_TIMES = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Input.Times")
        .dialogElement(150, "Times " + SOFT_YELLOW.wrap("(in 24-hour HH:MM format)"));

    private static final ButtonLocale BUTTON_DAY_ACTIVE = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Button.DayActive")
        .button(GREEN.wrap("[X]") + " " + GENERIC_NAME, 100);

    private static final ButtonLocale BUTTON_DAY_INACTIVE = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Button.DayInactive")
        .button(GRAY.wrap("[ ] " + GENERIC_NAME), 100);

    public static final ButtonLocale BUTTON_MODE_INTERVAL = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Button.ModeInterval")
        .button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Interval") + " mode");

    public static final ButtonLocale BUTTON_MODE_TIMES = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Button.ModeScheduled")
        .button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Scheduled") + " mode");


    private final VirtualShopModule module;

    public ProductFloatPriceTimesDialog(@NonNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualProduct product) {
        if (!(product.getPricing() instanceof FloatPricing pricing)) throw new IllegalArgumentException("Product does not have a Floating Price");

        RefreshType refreshType = pricing.getRefreshType();
        List<WrappedDialogInput> inputs = new ArrayList<>();
        List<WrappedActionButton> buttons = new ArrayList<>();

        if (refreshType == RefreshType.INTERVAL) {
            inputs.add(DialogInputs.text(JSON_INTERVAL, INPUT_INTERVAL).initial(String.valueOf(pricing.getRefreshInterval())).build());

            buttons.add(DialogButtons.action(BUTTON_MODE_TIMES).action(DialogActions.customClick(ACTION_MODE)).build());
        }
        else {
            inputs.add(DialogInputs.text(JSON_TIMES, INPUT_TIMES)
                .initial(String.join("\n", ShopUtils.serializeTimes(pricing.getTimes())))
                .maxLength(80)
                .multiline(new WrappedMultilineOptions(10, 100))
                .build());

            for (DayOfWeek day : DayOfWeek.values()) {
                ButtonLocale locale = (pricing.hasDay(day) ? BUTTON_DAY_ACTIVE : BUTTON_DAY_INACTIVE).replace(s -> s.replace(GENERIC_NAME, Lang.DAYS.getLocalized(day)));

                buttons.add(DialogButtons.action(locale).action(DialogActions.customClick(ACTION_DAY, NightNbtHolder.builder().put(JSON_DAY, day.name()).build())).build());
            }

            buttons.add(DialogButtons.action(BUTTON_MODE_INTERVAL).action(DialogActions.customClick(ACTION_MODE)).build());
        }

        buttons.add(DialogButtons.apply());
        buttons.add(DialogButtons.reset());

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(refreshType == RefreshType.INTERVAL ? BODY_INTERVAL : BODY_SCHEDULED))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(buttons)
                .columns(refreshType == RefreshType.INTERVAL ? 1 : 2)
                .exitAction(DialogButtons.back())
                .build())
            .handleResponse(DialogActions.BACK, (viewer, identifier, nbtHolder) -> {
                this.module.openProductPriceDialog(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(ACTION_MODE, (viewer, identifier, nbtHolder) -> {
                pricing.setRefreshType(Lists.next(refreshType));
                this.show(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(ACTION_DAY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                DayOfWeek day = nbtHolder.getText(JSON_DAY).map(str -> Enums.get(str, DayOfWeek.class)).orElse(null);
                if (day != null) {
                    if (pricing.hasDay(day)) {
                        pricing.removeDay(day);
                    }
                    else {
                        pricing.addDay(day);
                    }
                }

                product.getShop().markDirty();
                this.applyTimes(pricing, nbtHolder);
                this.show(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                this.applyTimes(pricing, nbtHolder);
                product.updatePrice(false);
                product.getShop().markDirty();
                this.module.openProductPriceDialog(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(DialogActions.RESET, (viewer, identifier, nbtHolder) -> {
                pricing.getDays().clear();
                pricing.getTimes().clear();
                pricing.setRefreshInterval(0L);
                product.getShop().markDirty();
                product.getPriceData().reset();
                this.show(viewer.getPlayer(), product, viewer.getCallback());
            })
        );
    }

    private void applyTimes(@NonNull FloatPricing pricing, @NonNull NightNbtHolder nbtHolder) {
        nbtHolder.getInt(JSON_INTERVAL).map(Integer::longValue).ifPresent(pricing::setRefreshInterval);
        nbtHolder.getText(JSON_TIMES).map(s -> Arrays.asList(s.split("\n"))).map(ShopUtils::parseTimes).ifPresent(pricing::setTimes);
    }
}
