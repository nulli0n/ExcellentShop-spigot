package su.nightexpress.nexshop.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Shop;

public abstract class EditorProductList<S extends Shop<S, ?>> extends AbstractMenu<ExcellentShop> {

    protected final S   shop;

    public EditorProductList(@NotNull S shop) {
        super(shop.plugin(), shop.getView().getTitle(), shop.getView().getSize());
        this.shop = shop;
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            AbstractMenu<?> menu = getMenu(player);
            if (menu != null) return;

            shop.getEditor().open(player, 1);
        }, false);

        super.onClose(player, e);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryDragEvent e) {
        return e.getRawSlots().stream().anyMatch(slot -> slot < e.getInventory().getSize());
    }
}
