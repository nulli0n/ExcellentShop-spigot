package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.menu.CentralMenu;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopMenuSlotsDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String ACTION_SLOT = "slot";
    private static final String JSON_SLOT = "slot";

    public static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopSlots.Title").text(TITLE_PREFIX + "Menu Slots");
    public static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopSlots.Body").dialogElement(
        300,
        "Sets shop slots for the " + SOFT_YELLOW.wrap("Central Shop GUI") + ".",
        "",
        "You can assign " + SOFT_YELLOW.wrap("multiple slots") + " for the same shop " + GRAY.wrap("(useful for GUIs with custom textures)"),
        "",
        GREEN.wrap("✔ Green") + " = current shop slot(s).",
        RED.wrap("✘ Red") + " = occupied by other shop(s)."
    );

    public ShopMenuSlotsDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        CentralMenu menu = this.module.getMainMenu();
        if (menu == null) return;

        InventoryView view = Software.instance().createView(menu.getMenuType(), NightComponent.empty(), player);
        int size = view.getTopInventory().getSize();
        Set<Integer> allowedSlots = IntStream.range(0, size).boxed().collect(Collectors.toSet());
        Set<Integer> occupiedSlots = new HashSet<>();
        this.module.getShops().stream().filter(other -> other != shop).forEach(other -> occupiedSlots.addAll(other.getMenuSlots()));

        List<WrappedActionButton> actionButtons = new ArrayList<>();
        allowedSlots.forEach(slot -> {
            String label = "#" + slot;

            if (occupiedSlots.contains(slot)) {
                label = TagWrappers.RED.wrap(label);
            }
            else if (shop.isMenuSlot(slot)) {
                label = TagWrappers.GREEN.wrap(label);
            }

            actionButtons.add(DialogButtons.action(label).width(30).action(DialogActions.customClick(ACTION_SLOT, NightNbtHolder.builder().put(JSON_SLOT, slot).build())).build());
        });

        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(actionButtons)
                .columns(9)
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).action(DialogActions.customClick(ACTION_BACK)).build())
                .build()
            )
            .handleResponse(ACTION_BACK, (user, identifier, nbtHolder) -> {
                this.closeAndThen(user, shop, this.module::openShopOptions);
            })
            .handleResponse(ACTION_SLOT, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                int slot = nbtHolder.getInt(JSON_SLOT).orElse(-1);
                if (!occupiedSlots.contains(slot)) {
                    if (shop.isMenuSlot(slot)) {
                        shop.removeMenuSlot(slot);
                    }
                    else {
                        shop.addMenuSlot(slot);
                    }
                }

                shop.setSaveRequired(true);
                this.showNextTick(player, shop);
            })
        );
    }
}
