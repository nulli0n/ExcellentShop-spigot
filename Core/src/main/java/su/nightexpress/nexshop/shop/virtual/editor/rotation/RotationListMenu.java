package su.nightexpress.nexshop.shop.virtual.editor.rotation;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.Comparator;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class RotationListMenu extends LinkedMenu<ShopPlugin, VirtualShop> implements Filled<Rotation> {

    private final VirtualShopModule module;

    public RotationListMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_SHOP_ROTATIONS.getString());
        this.module = module;

        this.addItem(MenuItem.buildReturn(this, 39, (viewer, event) -> {
            this.runNextTick(() -> module.openShopOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));

        this.addItem(Material.ANVIL, VirtualLocales.ROTATION_CREATE, 41, (viewer, event, shop) -> {
            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_ROTATION_ID, input -> {
                String id = StringUtil.transformForID(input.getTextRaw());
                if (id.isBlank()) return false;

                Rotation exist = shop.getRotationById(id);
                if (exist != null) {
                    VirtualLang.ERROR_EDITOR_ROTATION_EXISTS.getMessage().send(viewer.getPlayer());
                    return false;
                }

                Rotation rotation = new Rotation(id, shop);
                shop.addRotation(rotation);
                shop.saveRotations();
                return true;
            }));
        });
    }

    @Override
    @NotNull
    public MenuFiller<Rotation> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        VirtualShop shop = this.getLink(player);

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(0, 36).toArray())
            .setItems(shop.getRotations().stream().sorted(Comparator.comparing(Rotation::getId)).toList())
            .setItemCreator(rotation -> {
                return rotation.getIcon()
                    .localized(VirtualLocales.ROTATION_OBJECT)
                    .setHideComponents(true)
                    .replacement(replacer -> replacer.replace(rotation.replacePlaceholders()));
            })
            .setItemClick(rotation -> (viewer1, event) -> {
                this.runNextTick(() -> this.module.openRotationOptions(player, rotation));
            })
            .build();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
