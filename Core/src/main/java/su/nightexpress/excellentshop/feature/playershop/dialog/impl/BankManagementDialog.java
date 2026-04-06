package su.nightexpress.excellentshop.feature.playershop.dialog.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.bank.Bank;
import su.nightexpress.excellentshop.feature.playershop.bank.BankManager;
import su.nightexpress.excellentshop.feature.playershop.core.ChestLang;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BankManagementDialog extends Dialog<Bank> {

    private static final int MODE_DEPOSIT  = 0;
    private static final int MODE_WITHDRAW = 1;

    private static final TextLocale          TITLE = ChestLang.builder("Dialog.Bank.Management.Title").text(title("Bank", "Management"));
    private static final DialogElementLocale BODY  = ChestLang.builder("Dialog.Bank.Management.Body").dialogElement(
        400,
        "Here you can deposit or withdraw funds from your bank account.",
        "",
        TagWrappers.YELLOW.and(TagWrappers.BOLD).wrap("BANK BALANCE:"),
        ShopPlaceholders.GENERIC_BALANCE
    );

    private static final TextLocale ENTRY_CURRENCY = ChestLang.builder("Dialog.Bank.Management.Entry.Currency")
        .text(TagWrappers.SOFT_YELLOW.wrap("• ") + TagWrappers.WHITE.wrap(ShopPlaceholders.CURRENCY_NAME + ": ") + TagWrappers.SOFT_YELLOW.wrap(ShopPlaceholders.GENERIC_VALUE));

    private static final TextLocale INPUT_AMOUNT = ChestLang.builder("Dialog.Bank.Management.Input.Amount")
        .text("Amount to Deposit/Withdraw");

    private static final TextLocale INPUT_CURRENCY = ChestLang.builder("Dialog.Bank.Management.Input.Currency")
        .text(TagWrappers.SOFT_YELLOW.wrap("→") + " Currency");

    private static final ButtonLocale BUTTON_DEPOSIT_SPECIFIED = ChestLang.builder("Dialog.Bank.Management.Button.DepositSpecified")
        .button(TagWrappers.GREEN.wrap("↑") + " Deposit " + TagWrappers.GREEN.wrap("Specified"),
            "Click to " + TagWrappers.GREEN.wrap("deposit") + " specified amount of selected currency."
        );

    private static final ButtonLocale BUTTON_DEPOSIT_ALL = ChestLang.builder("Dialog.Bank.Management.Button.DepositAll")
        .button(TagWrappers.GREEN.wrap("↑") + " Deposit " + TagWrappers.GREEN.wrap("All"),
            "Click to " + TagWrappers.GREEN.wrap("deposit") + " all your balance of selected currency."
        );

    private static final ButtonLocale BUTTON_WITHDRAW_SPECIFIED = ChestLang.builder("Dialog.Bank.Management.Button.WithdrawSpecified")
        .button(TagWrappers.RED.wrap("↓") + " Withdraw " + TagWrappers.RED.wrap("Specified"),
            "Click to " + TagWrappers.GREEN.wrap("withdraw") + " specified amount of selected currency."
        );

    private static final ButtonLocale BUTTON_WITHDRAW_ALL = ChestLang.builder("Dialog.Bank.Management.Button.WithdrawAll")
        .button(TagWrappers.RED.wrap("↓") + " Withdraw " + TagWrappers.RED.wrap("All"),
            "Click to " + TagWrappers.GREEN.wrap("withdraw") + " all bank balance of selected currency."
        );

    private static final String JSON_AMOUNT   = "amount";
    private static final String JSON_CURRENCY = "currency";

    private static final String ACTION_DEPOSIT_ALL        = "deposit_all";
    private static final String ACTION_DEPOSIT_SPECIFIED  = "deposit_specified";
    private static final String ACTION_WITHDRAW_ALL       = "withdraw_all";
    private static final String ACTION_WITHDRAW_SPECIFIED = "withdraw_specified";

    private final ChestShopModule module;
    private final BankManager     bankManager;

    public BankManagementDialog(@NonNull ChestShopModule module, @NonNull BankManager bankManager) {
        this.module = module;
        this.bankManager = bankManager;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull Bank bank) {
        List<Currency> currencies = this.module.getAvailableCurrencies(player).stream().sorted(Comparator.comparing(Currency::getInternalId)).toList();
        List<WrappedSingleOptionEntry> entries = new ArrayList<>();

        currencies.forEach(currency -> {
            entries.add(new WrappedSingleOptionEntry(currency.getInternalId(), currency.getName(), this.module.isDefaultCurrency(currency)));
        });

        PlaceholderContext bodyContext = PlaceholderContext.builder()
            .with(ShopPlaceholders.GENERIC_BALANCE, () -> currencies.stream().map(currency -> {
                return PlaceholderContext.builder()
                    .with(ShopPlaceholders.GENERIC_VALUE, () -> currency.format(bank.getAccount().query(currency)))
                    .andThen(currency.replacePlaceholders())
                    .build()
                    .apply(ENTRY_CURRENCY.text());
            }).collect(Collectors.joining("\n")))
            .build();

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY.replace(bodyContext)))
                .inputs(
                    DialogInputs.text(JSON_AMOUNT, INPUT_AMOUNT).initial(String.valueOf(0)).maxLength(10).build(),
                    DialogInputs.singleOption(JSON_CURRENCY, INPUT_CURRENCY, entries).build()
                )
                .build()
            )
            .type(
                DialogTypes.multiAction(
                        DialogButtons.action(BUTTON_DEPOSIT_SPECIFIED).action(DialogActions.customClick(ACTION_DEPOSIT_SPECIFIED)).build(),
                        DialogButtons.action(BUTTON_WITHDRAW_SPECIFIED).action(DialogActions.customClick(ACTION_WITHDRAW_SPECIFIED)).build(),
                        DialogButtons.action(BUTTON_DEPOSIT_ALL).action(DialogActions.customClick(ACTION_DEPOSIT_ALL)).build(),
                        DialogButtons.action(BUTTON_WITHDRAW_ALL).action(DialogActions.customClick(ACTION_WITHDRAW_ALL)).build()
                    )
                    .exitAction(DialogButtons.back())
                    .columns(2)
                    .build()
            )
            .handleResponse(ACTION_DEPOSIT_ALL, (viewer, identifier, nbtHolder) -> {
                this.depositOrWithdraw(player, bank, nbtHolder, MODE_DEPOSIT, true);
                this.show(player, bank, viewer.getCallback());
            })
            .handleResponse(ACTION_DEPOSIT_SPECIFIED, (viewer, identifier, nbtHolder) -> {
                this.depositOrWithdraw(player, bank, nbtHolder, MODE_DEPOSIT, false);
                this.show(player, bank, viewer.getCallback());
            })
            .handleResponse(ACTION_WITHDRAW_ALL, (viewer, identifier, nbtHolder) -> {
                this.depositOrWithdraw(player, bank, nbtHolder, MODE_WITHDRAW, true);
                this.show(player, bank, viewer.getCallback());
            })
            .handleResponse(ACTION_WITHDRAW_SPECIFIED, (viewer, identifier, nbtHolder) -> {
                this.depositOrWithdraw(player, bank, nbtHolder, MODE_WITHDRAW, false);
                this.show(player, bank, viewer.getCallback());
            })
        );
    }

    private void depositOrWithdraw(@NonNull Player player, @NonNull Bank bank, @Nullable NightNbtHolder nbtHolder, int mode, boolean all) {
        if (nbtHolder == null) return;

        String currencyId = nbtHolder.getText(JSON_CURRENCY).orElse(null);
        if (currencyId == null) return;

        Currency currency = EconomyBridge.api().getCurrency(currencyId);
        if (currency == null) return;

        double amount = all ? -1D : Math.abs(nbtHolder.getDouble(JSON_AMOUNT, 0D));

        if (mode == MODE_DEPOSIT) {
            this.bankManager.depositToBank(player, bank, currency, amount);
        }
        else if (mode == MODE_WITHDRAW) {
            this.bankManager.withdrawFromBank(player, bank, currency, amount);
        }
    }
}
