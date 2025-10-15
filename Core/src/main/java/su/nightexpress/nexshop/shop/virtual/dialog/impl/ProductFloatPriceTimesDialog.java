package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.type.RefreshType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.impl.FloatPricing;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;

import java.time.DayOfWeek;
import java.util.*;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ProductFloatPriceTimesDialog extends VirtualDialogProvider<VirtualProduct> {

    private static final String JSON_INTERVAL = "interval";
    private static final String JSON_TIMES    = "times";
    private static final String JSON_DAY      = "day";

    private static final String ACTION_DAY  = "day";
    private static final String ACTION_MODE = "mode";

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.ProductPrice.FloatTimes.Title").text(TITLE_PREFIX + "Float Pricing Times");

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


    public ProductFloatPriceTimesDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualProduct product) {
        if (!(product.getPricing() instanceof FloatPricing pricing)) return;

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

        buttons.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build());
        buttons.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_RESET).action(DialogActions.customClick(ACTION_RESET)).build());

        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(refreshType == RefreshType.INTERVAL ? BODY_INTERVAL : BODY_SCHEDULED))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(buttons)
                .columns(refreshType == RefreshType.INTERVAL ? 1 : 2)
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).action(DialogActions.customClick(ACTION_BACK)).build())
                .build())
            .handleResponse(ACTION_BACK, (user, identifier, nbtHolder) -> {
                this.closeAndThen(user.getPlayer(), product, () -> this.module.handleDialogs(dialogs -> dialogs.openProductPrice(user.getPlayer(), product)));
            })
            .handleResponse(ACTION_MODE, (user, identifier, nbtHolder) -> {
                pricing.setRefreshType(Lists.next(refreshType));
                this.showNextTick(user.getPlayer(), product);
            })
            .handleResponse(ACTION_DAY, (user, identifier, nbtHolder) -> {
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
                this.showNextTick(user.getPlayer(), product);
            })
            .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                this.applyTimes(pricing, nbtHolder);
                product.updatePrice(false);
                product.getShop().markDirty();
                this.closeAndThen(user.getPlayer(), product, () -> this.module.handleDialogs(dialogs -> dialogs.openProductPrice(user.getPlayer(), product)));
            })
            .handleResponse(ACTION_RESET, (user, identifier, nbtHolder) -> {
                pricing.getDays().clear();
                pricing.getTimes().clear();
                pricing.setRefreshInterval(0L);
                product.getShop().markDirty();
                this.plugin.getDataManager().resetPriceData(product);
                this.showNextTick(user.getPlayer(), product);
            })
        );
    }

    private void applyTimes(@NotNull FloatPricing pricing, @NotNull NightNbtHolder nbtHolder) {
        nbtHolder.getInt(JSON_INTERVAL).map(Integer::longValue).ifPresent(pricing::setRefreshInterval);
        nbtHolder.getText(JSON_TIMES).map(s -> Arrays.asList(s.split("\n"))).map(ShopUtils::parseTimes).ifPresent(pricing::setTimes);
    }
}
