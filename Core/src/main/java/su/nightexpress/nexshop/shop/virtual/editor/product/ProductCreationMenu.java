package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.stream.IntStream;

public class ProductCreationMenu extends LinkedMenu<ShopPlugin, ProductCreationMenu.Data> {

    private static final int ITEM_SLOT = 22;

    private final VirtualShopModule module;

    public record Data(VirtualShop shop, boolean rotating, int page, int slot) {}

    public ProductCreationMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_PRODUCT_CREATION.text());
        this.module = module;

        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).toMenuItem().setPriority(-1).setSlots(IntStream.range(0, 45).toArray()));

        this.addItem(NightItem.fromType(Material.LIGHT_BLUE_STAINED_GLASS_PANE).toMenuItem().setPriority(1).setSlots(10,11,12,19,21,28,29,30));

        this.addItem(NightItem.fromType(Material.MAGENTA_STAINED_GLASS_PANE).toMenuItem().setPriority(1).setSlots(14,15,16,23,25,32,33,34));

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.comeback(viewer);
        }));

        this.addItem(NightItem.fromType(Material.OAK_SIGN).localized(VirtualLocales.PRODUCT_CREATION_INFO).toMenuItem().setSlots(4));

        this.addItem(Material.COMMAND_BLOCK, VirtualLocales.PRODUCT_CREATION_COMMAND, 24, (viewer, event, shop) -> this.tryCreate(viewer, ContentType.COMMAND));
        this.addItem(Material.DIAMOND, VirtualLocales.PRODUCT_CREATION_ITEM, 20, (viewer, event, shop) -> this.tryCreate(viewer, ContentType.ITEM));
    }

    public void open(@NotNull Player player, @NotNull VirtualShop shop, boolean rotating, int page, int slot) {
        this.open(player, new ProductCreationMenu.Data(shop, rotating, page, slot));
    }

    private void comeback(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        this.runNextTick(() -> {
            if (data.rotating) {
                module.openRotatingsProducts(player, data.shop);
            }
            else {
                module.openNormalProducts(player, data.shop);
            }
        });
    }

    private void tryCreate(@NotNull MenuViewer viewer, @NotNull ContentType type) {
        Inventory inventory = viewer.getInventory();
        if (inventory == null) return;

        ItemStack source = inventory.getItem(ITEM_SLOT);
        if (source == null || source.getType().isAir()) return;

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        VirtualShop shop = data.shop;

        VirtualProduct product = shop.createProduct(type, source);

        product.setRotating(this.getLink(viewer).rotating);
        product.setPage(data.page);
        product.setSlot(data.slot);

        shop.addProduct(product);
        shop.markDirty();

        this.comeback(viewer);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        inventory.setItem(ITEM_SLOT, null);
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);

        if (!result.isInventory()) return;

        ItemStack clicked = result.getItemStack();
        if (clicked == null || clicked.getType().isAir()) return;

        Inventory inventory = event.getInventory();
        inventory.setItem(ITEM_SLOT, new ItemStack(clicked));
    }
}
