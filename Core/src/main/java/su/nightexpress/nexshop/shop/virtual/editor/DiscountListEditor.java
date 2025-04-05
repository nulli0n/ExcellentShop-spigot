package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class DiscountListEditor extends EditorMenu<ShopPlugin, VirtualShop> implements AutoFilled<VirtualDiscount> {

    private final VirtualShopModule module;

    public DiscountListEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Discounts Editor"), MenuSize.CHEST_45);
        this.module = module;

        this.addReturn(39, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopOptions(viewer.getPlayer(), shop));
        });
        this.addNextPage(44);
        this.addPreviousPage(36);

        this.addCreation(VirtualLocales.DISCOUNT_CREATE, 41, (viewer, event, shop) -> {
            // TODO shop.addDiscountConfig(new VirtualDiscount());
            //this.saveAndFlush(viewer, shop);
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<VirtualDiscount> autoFill) {
        // TODO
//        autoFill.setSlots(IntStream.range(0, 36).toArray());
//        autoFill.setItems(this.getLink(viewer).getDiscountConfigs());
//        autoFill.setItemCreator(discount -> {
//            ItemStack item = new ItemStack(Material.GOLD_NUGGET);
//            ItemReplacer.create(item).hideFlags().trimmed()
//                .readLocale(VirtualLocales.DISCOUNT_OBJECT)
//                .replace(discount.getPlaceholders())
//                .writeMeta();
//            return item;
//        });
//        autoFill.setClickAction(discount -> (viewer1, event) -> {
//            Player player = viewer1.getPlayer();
//            if (event.isShiftClick()) {
//                if (event.isRightClick()) {
//                    StaticShop shop = this.getLink(viewer);
//                    shop.removeDiscountConfig(discount);
//                    this.saveAndFlush(viewer, shop);
//                }
//            }
//            else {
//                this.runNextTick(() -> this.module.openDiscountEditor(player, discount));
//            }
//        });
    }
}
