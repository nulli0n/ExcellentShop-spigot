package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.DialogActions;
import su.nightexpress.nightcore.ui.dialog.build.DialogBases;
import su.nightexpress.nightcore.ui.dialog.build.DialogButtons;
import su.nightexpress.nightcore.ui.dialog.build.DialogTypes;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductCurrencyDialog extends Dialog<VirtualProduct> {

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.ProductCurrency.Title").text(title("Product", "Currency"));

    private static final String ACTION_CURRENCY = "currency";

    private static final String JSON_CURRENCY_ID = "currency_id";

    private final VirtualShopModule module;

    public ProductCurrencyDialog(@NonNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualProduct product) {
        List<WrappedActionButton> buttons = new ArrayList<>();

        this.module.getEnabledCurrencies().stream().sorted(Comparator.comparing(Currency::getInternalId)).forEach(currency -> {
            buttons.add(DialogButtons.action(currency.getName())
                .action(DialogActions.customClick(ACTION_CURRENCY, NightNbtHolder.builder().put(JSON_CURRENCY_ID, currency.getInternalId()).build()))
                .build());
        });

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(buttons)
                .exitAction(DialogButtons.back())
                .columns(2)
                .build()
            )
            .handleResponse(DialogActions.BACK, (viewer, identifier, nbtHolder) -> {
                this.module.openProductPriceDialog(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(ACTION_CURRENCY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                Currency currency = nbtHolder.getText(JSON_CURRENCY_ID).map(EconomyBridge.api()::getCurrency).orElse(null);
                if (currency == null) return;

                product.setCurrencyId(currency.getInternalId());
                product.getShop().markDirty();
                this.module.openProductPriceDialog(viewer.getPlayer(), product, viewer.getCallback());
            })
        );
    }
}
