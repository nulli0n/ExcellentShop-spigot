package su.nightexpress.nexshop.shop.virtual.editor.rotation;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class RotationSlotsMenu extends LinkedMenu<ShopPlugin, Rotation> {

    private final VirtualShopModule module;

    public RotationSlotsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_ROTATION_SLOT_SELECTION.text());
        this.module = module;
    }

    @Override
    public boolean open(@NotNull Player player, @NotNull Rotation rotation) {
        return this.open(player, rotation, viewer -> viewer.setPages(rotation.getShop().getPages()));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        //Player player = viewer.getPlayer();
        Rotation rotation = this.getLink(viewer);
        VirtualShop shop = rotation.getShop();

        int size = view.getTopInventory().getSize();
        int page = viewer.getPage();
        Set<Integer> freeSlots = IntStream.range(0, size).boxed().collect(Collectors.toSet());

        // ====================================
        // Build Shop Layout
        // ====================================
        ShopLayout layout = this.module.getLayout(shop, page);
        if (layout != null) {
            for (MenuItem layoutItem : layout.getItems()) {
                if (layoutItem.getPriority() < 0) continue;

                MenuItem.Builder builder = MenuItem.builder(layoutItem.getItem().copy())
                    .setPriority(layoutItem.getPriority())
                    .setSlots(layoutItem.getSlots());

                ItemHandler handler = layoutItem.getHandler();
                if (handler != null) {
                    String handlerName = handler.getName();
                    if (handlerName.equalsIgnoreCase(ItemHandler.RETURN)) {
                        builder.setHandler((viewer1, event) -> {
                            this.runNextTick(() -> this.module.openRotationOptions(viewer1.getPlayer(), rotation));
                        });
                    }
                    else if (handlerName.equalsIgnoreCase(ItemHandler.NEXT_PAGE)) {
                        builder.setHandler(ItemHandler.forNextPage(this));
                    }
                    else if (handlerName.equalsIgnoreCase(ItemHandler.PREVIOUS_PAGE)) {
                        builder.setHandler(ItemHandler.forPreviousPage(this));
                    }
                }

                this.addItem(viewer, builder.build());

                IntStream.of(layoutItem.getSlots()).forEach(freeSlots::remove);
            }
        }

        for (VirtualProduct product : shop.getProducts()) {
            if (product.isRotating()) continue;
            if (product.getPage() != page) continue;

            int slot = product.getSlot();

            this.addItem(viewer, NightItem.fromItemStack(product.getPreviewOrPlaceholder())
                .setHideComponents(true)
                .setDisplayName(Placeholders.PRODUCT_PREVIEW_NAME)
                .replacement(replacer -> replacer.replace(product.replacePlaceholders()))
                .toMenuItem()
                .setSlots(slot)
                .setPriority(MenuItem.HIGH_PRIORITY));

            freeSlots.remove(slot);
        }

        shop.getRotations().forEach(other -> {
            boolean isCurrent = rotation == other;
            Material material = isCurrent ? Material.CYAN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            LangItem locale = isCurrent ? VirtualLocales.ROTATION_SELECTED_SLOT : VirtualLocales.ROTATION_OTHER_SLOT;

            other.getSlots(page).forEach(slot -> {
                this.addItem(viewer, NightItem.fromType(material)
                    .setHideComponents(true)
                    .localized(locale)
                    .toMenuItem()
                    .setSlots(slot)
                    .setPriority(MenuItem.HIGH_PRIORITY)
                    .setHandler((viewer1, event) -> {
                        if (!isCurrent) return;

                        rotation.removeSlot(page, slot);
                        rotation.getShop().markDirty();
                        this.runNextTick(() -> this.flush(viewer1));
                    }));

                freeSlots.remove(slot);
            });
        });

//        rotation.getSlots(page).forEach(slot -> {
//            this.addItem(viewer, NightItem.fromType(Material.CYAN_STAINED_GLASS_PANE)
//                .setHideComponents(true)
//                .localized(VirtualLocales.ROTATION_USED_SLOT)
//                .toMenuItem()
//                .setSlots(slot)
//                .setPriority(MenuItem.HIGH_PRIORITY)
//                .setHandler((viewer1, event) -> {
//                    rotation.removeSlot(page, slot);
//                    shop.saveRotations();
//                    this.runNextTick(() -> this.flush(viewer));
//                }));
//
//            freeSlots.remove(slot);
//        });

        this.addItem(viewer, NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
            .setHideComponents(true)
            .localized(VirtualLocales.ROTATION_FREE_SLOT)
            .toMenuItem()
            .setSlots(freeSlots.stream().mapToInt(Number::intValue).toArray())
            .setHandler((viewer1, event) -> {
                rotation.addSlot(page, event.getRawSlot());
                rotation.getShop().markDirty();
                this.runNextTick(() -> this.flush(viewer));
            }));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Rotation rotation = this.getLink(viewer);
        Player player = viewer.getPlayer();

        this.runNextTick(() -> {
            Menu menu = MenuRegistry.getMenu(player);
            if (menu != null) return;

            this.module.openRotationOptions(player, rotation);
        });

        super.onClose(viewer, event);
    }
}
