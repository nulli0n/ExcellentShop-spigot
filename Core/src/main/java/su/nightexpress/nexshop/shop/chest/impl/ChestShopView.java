package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.config.Config;

import java.util.ArrayList;
import java.util.List;

public class ChestShopView extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestProduct> {

    private static int[]        PRODUCT_SLOTS;
    private static List<String> PRODUCT_FORMAT_LORE;

    private final ChestShop shop;

    public ChestShopView(@NotNull ExcellentShop plugin, @NotNull ChestShop shop) {
        super(plugin, JYML.loadOrExtract(shop.plugin(), shop.getModule().getLocalPath(), "view.yml"));
        this.shop = shop;

        PRODUCT_SLOTS = cfg.getIntArray("Product_Slots");
        PRODUCT_FORMAT_LORE = Colorizer.apply(cfg.getStringList("Product_Format.Lore.Text"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.CLOSE, (viewer, event) -> this.plugin.runTask(task -> viewer.getPlayer().closeInventory()));

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.shop.replacePlaceholders());
        }));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        options.setTitle(this.shop.getName());

        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return PRODUCT_SLOTS;
    }

    @Override
    @NotNull
    public List<ChestProduct> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.shop.getProducts());
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

            this.plugin.runTask(task -> {
                product.prepareTrade(viewer.getPlayer(), clickType);
            });
        };
    }
}
