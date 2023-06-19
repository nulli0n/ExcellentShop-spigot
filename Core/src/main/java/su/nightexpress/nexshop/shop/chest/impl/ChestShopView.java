package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.ShopView;
import su.nightexpress.nexshop.api.type.ShopClickAction;
import su.nightexpress.nexshop.config.Config;

import java.util.ArrayList;
import java.util.List;

public class ChestShopView extends ShopView<ChestShop, ChestProduct> implements AutoPaged<ChestProduct> {

    private static int[]        PRODUCT_SLOTS;
    private static List<String> PRODUCT_FORMAT_LORE;

    public ChestShopView(@NotNull ChestShop shop) {
        super(shop, JYML.loadOrExtract(shop.plugin(), shop.getModule().getLocalPath(), "view.yml"));

        PRODUCT_SLOTS = cfg.getIntArray("Product_Slots");
        PRODUCT_FORMAT_LORE = Colorizer.apply(cfg.getStringList("Product_Format.Lore.Text"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.CLOSE, (viewer, event) -> this.plugin.runTask(task -> viewer.getPlayer().closeInventory()));

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getOptions().getDisplayModifier() == null) {
                menuItem.getOptions().setDisplayModifier((viewer, item) -> ItemUtil.replace(item, this.shop.replacePlaceholders()));
            }
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        options.setTitle(this.getShop().getName());

        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return PRODUCT_SLOTS;
    }

    @Override
    @NotNull
    public List<ChestProduct> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.getShop().getProducts());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ChestProduct product) {
        ItemStack preview = product.getPreview();

        ItemUtil.mapMeta(preview, meta -> {
            List<String> lore = new ArrayList<>();

            for (String lineFormat : PRODUCT_FORMAT_LORE) {
                if (lineFormat.contains(Placeholders.GENERIC_LORE)) {
                    List<String> list2 = meta.getLore();
                    if (list2 != null) lore.addAll(list2);
                    continue;
                }
                lore.add(lineFormat);
            }

            PlaceholderMap placeholderMap = new PlaceholderMap();
            placeholderMap.getKeys().addAll(product.getPlaceholders(player).getKeys());
            placeholderMap.getKeys().addAll(product.getCurrency().getPlaceholders().getKeys());
            placeholderMap.getKeys().addAll(shop.getPlaceholders().getKeys());
            lore.replaceAll(placeholderMap.replacer());
            meta.setLore(lore);
        });

        return preview;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestProduct product) {
        return (viewer, event) -> {
            ShopClickAction clickType = Config.GUI_CLICK_ACTIONS.get().get(event.getClick());
            if (clickType == null) return;

            product.prepareTrade(viewer.getPlayer(), clickType);
        };
    }
}
