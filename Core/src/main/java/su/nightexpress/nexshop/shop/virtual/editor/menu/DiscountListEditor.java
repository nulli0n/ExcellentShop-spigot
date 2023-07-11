package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DiscountListEditor extends EditorMenu<ExcellentShop, VirtualShop> implements AutoPaged<VirtualDiscount> {

    public DiscountListEditor(@NotNull VirtualShop shop) {
        super(shop.plugin(), shop, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        this.addReturn(39).setClick((viewer, event) -> {
            this.plugin.runTask(task -> shop.getEditor().open(viewer.getPlayer(), 1));
        });
        this.addNextPage(44);
        this.addPreviousPage(36);

        this.addCreation(VirtualLocales.DISCOUNT_CREATE, 41).setClick((viewer, event) -> {
            shop.addDiscountConfig(new VirtualDiscount());
            shop.saveSettings();
            this.plugin.runTask(task -> this.open(viewer.getPlayer(), viewer.getPage()));
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    @NotNull
    public List<VirtualDiscount> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.object.getDiscountConfigs());
    }

    @Override
    public int[] getObjectSlots() {
        return IntStream.range(0, 36).toArray();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull VirtualDiscount discount) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(VirtualLocales.DISCOUNT_OBJECT.getLocalizedName());
            meta.setLore(VirtualLocales.DISCOUNT_OBJECT.getLocalizedLore());
            ItemUtil.replace(meta, discount.replacePlaceholders());
        });
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull VirtualDiscount discount) {
        return (viewer, e) -> {
            Player player = viewer.getPlayer();
            if (e.isShiftClick()) {
                if (e.isRightClick()) {
                    this.object.removeDiscountConfig(discount);
                    this.object.save();
                    this.plugin.runTask(task -> this.open(player, viewer.getPage()));
                }
            }
            else {
                discount.getEditor().open(player, 1);
            }
        };
    }
}
