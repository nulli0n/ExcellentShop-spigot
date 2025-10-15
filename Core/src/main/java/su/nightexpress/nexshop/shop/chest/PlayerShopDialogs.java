package su.nightexpress.nexshop.shop.chest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

public class PlayerShopDialogs extends SimpleManager<ShopPlugin> {

    private final ChestShopModule module;

    public PlayerShopDialogs(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onShutdown() {

    }

    public void openShopNameDialog(@NotNull Player player, @NotNull ChestShop shop) {
        Dialogs.showDialog(player, this.createShopNameDialog(shop));
    }

    @NotNull
    private WrappedDialog createShopNameDialog(@NotNull ChestShop shop) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder("Shop Name")
                .body(DialogBodies.plainMessage("Enter the name for your shop.<br>Max length is " + ChestConfig.SHOP_MAX_NAME_LENGTH.get()))
                .inputs(DialogInputs.text("name", "Shop Name")
                    .maxLength(ChestConfig.SHOP_MAX_NAME_LENGTH.get())
                    .initial(shop.getName())
                    .build()
                )
                .build()
            )
            .type(DialogTypes.confirmation(
                DialogButtons.action("Yes").action(DialogActions.customClick("confirm")).build(),
                DialogButtons.action("No").build()
            ))
            .handleResponse("confirm", (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String name = nbtHolder.getText("name").orElse(null);
                if (name == null) return;

                // TODO Colors permission?

                shop.setName(name);
                shop.markDirty();
                plugin.runTask(task -> module.openShopSettings(user.getPlayer(), shop));
            })
        );
    }
}
