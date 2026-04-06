package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.feature.virtualshop.shop.menu.CentralMenu;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
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

public class ShopMenuSlotsDialog extends Dialog<VirtualShop> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopSlots.Title").text(title("Shop", "Menu Slots"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopSlots.Body").dialogElement(
        300,
        "Sets shop slots for the " + SOFT_YELLOW.wrap("Central Shop GUI") + ".",
        "",
        "You can assign " + SOFT_YELLOW.wrap("multiple slots") + " for the same shop " + GRAY.wrap("(useful for GUIs with custom textures)"),
        "",
        GREEN.wrap("✔ Green") + " = current shop slot(s).",
        RED.wrap("✘ Red") + " = occupied by other shop(s)."
    );

    private static final String ACTION_SLOT = "slot";
    private static final String JSON_SLOT   = "slot";

    private final VirtualShopModule module;
    private final CentralMenu menu;

    public ShopMenuSlotsDialog(@NonNull VirtualShopModule module, @NonNull CentralMenu menu) {
        this.module = module;
        this.menu = menu;
    }

    @Override
    @NotNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualShop shop) {
        InventoryView view = Software.get().createView(menu.getMenuType(), NightComponent.empty(), player);
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

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(DialogTypes.multiAction(actionButtons)
                .columns(9)
                .exitAction(DialogButtons.back())
                .build()
            )
            .handleResponse(DialogActions.BACK, (viewer, identifier, nbtHolder) -> {
                viewer.closeFully();
            })
            .handleResponse(ACTION_SLOT, (viewer, identifier, nbtHolder) -> {
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

                shop.markDirty();
                this.show(viewer.getPlayer(), shop, viewer.getCallback());
            })
        );
    }
}
