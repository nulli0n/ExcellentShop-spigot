package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.block.Container;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.impl.Menu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;

public class ContainerPreview extends Menu<ExcellentShop> {

    private final AuctionMainMenu mainMenu;
    private final Container container;
    private final int page;

    public ContainerPreview(@NotNull AuctionMainMenu mainMenu, @NotNull Container container, int page) {
        super(mainMenu.plugin(), AuctionConfig.MENU_CONTAINER_PREVIEW_TITLE.get(), container.getInventory().getSize());
        this.mainMenu = mainMenu;
        this.container = container;
        this.page = page;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        super.onReady(viewer, inventory);
        inventory.setContents(this.container.getInventory().getContents());
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.mainMenu.openNextTick(viewer, this.page);
    }
}
