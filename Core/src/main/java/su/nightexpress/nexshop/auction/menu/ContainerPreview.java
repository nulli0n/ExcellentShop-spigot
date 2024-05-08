package su.nightexpress.nexshop.auction.menu;

import org.bukkit.block.Container;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;

public class ContainerPreview extends AbstractMenu<ShopPlugin> {

    private final AuctionManager manager;
    private final Container   container;
    private final int page;

    public ContainerPreview(@NotNull ShopPlugin plugin, @NotNull AuctionManager manager, @NotNull Container container, int page) {
        super(plugin, AuctionConfig.MENU_CONTAINER_PREVIEW_TITLE.get(), container.getInventory().getSize());
        this.manager = manager;
        this.container = container;
        this.page = page;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        inventory.setContents(this.container.getInventory().getContents());
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        this.runNextTick(() -> this.manager.openAuction(viewer.getPlayer(), this.page));
        super.onClose(viewer, event);
    }
}
