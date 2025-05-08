package su.nightexpress.nexshop.shop.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.ItemClick;

@Deprecated
public interface Confirmation {

    void onAccept(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event);

    void returnBack(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event);

    @NotNull
    static Confirmation create(@NotNull ItemClick accept, @NotNull ItemClick decline) {
        return new Confirmation() {

            @Override
            public void onAccept(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event) {
                accept.onClick(viewer, event);
            }

            @Override
            public void returnBack(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event) {
                decline.onClick(viewer, event);
            }
        };
    }
}
