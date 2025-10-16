package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.DialogActions;
import su.nightexpress.nightcore.ui.dialog.build.DialogBases;
import su.nightexpress.nightcore.ui.dialog.build.DialogButtons;
import su.nightexpress.nightcore.ui.dialog.build.DialogTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductCurrencyDialog extends VirtualDialogProvider<VirtualProduct> {

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.ProductCurrency.Title").text(TITLE_PREFIX + "Item Currency");

    private static final String ACTION_CURRENCY = "currency";

    private static final String JSON_ID = "id";

    public ProductCurrencyDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualProduct product) {
        List<WrappedActionButton> buttons = new ArrayList<>();

        this.module.getEnabledCurrencies().stream().sorted(Comparator.comparing(Currency::getInternalId)).forEach(currency -> {
            buttons.add(DialogButtons.action(currency.getName())
                .action(DialogActions.customClick(ACTION_CURRENCY, NightNbtHolder.builder().put(JSON_ID, currency.getInternalId()).build()))
                .build());
        });

        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(buttons)
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).action(DialogActions.customClick(ACTION_BACK)).build())
                .columns(2)
                .build()
            )
            .handleResponse(ACTION_BACK, (user, identifier, nbtHolder) -> {
                this.closeAndThen(user.getPlayer(), product, () -> this.module.handleDialogs(dialogs -> dialogs.openProductPrice(user.getPlayer(), product)));
            })
            .handleResponse(ACTION_CURRENCY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                Currency currency = nbtHolder.getText(JSON_ID).map(EconomyBridge::getCurrency).orElse(null);
                if (currency == null) return;

                product.setCurrencyId(currency.getInternalId());
                product.getShop().markDirty();
                this.closeAndThen(user.getPlayer(), product, () -> this.module.handleDialogs(dialogs -> dialogs.openProductPrice(user.getPlayer(), product)));
            })
        );
    }
}
