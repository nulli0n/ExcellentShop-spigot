package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopView extends LinkedMenu<ShopPlugin, ChestShop> implements Filled<ChestProduct>, ConfigBased {

    public static final String FILE_NAME = "view.yml";

    private int[] productSlots;

    public ShopView(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X3, BLACK.wrap(SHOP_NAME));

        this.load(FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        return Placeholders.forChestShop(this.getLink(viewer)).apply(this.title);
    }

    @Override
    @NotNull
    public MenuFiller<ChestProduct> createFiller(@NotNull MenuViewer viewer) {
        ChestShop shop = this.getLink(viewer);

        return MenuFiller.builder(this)
            .setSlots(this.productSlots)
            .setItems(shop.getValidProducts())
            .setItemCreator(product -> {
                List<String> loreFormat = ChestConfig.PRODUCT_FORMAT_LORE_GENERAL.get();
                List<String> buyLore = product.isBuyable() ? ChestConfig.PRODUCT_FORMAT_LORE_BUY.get() : Collections.emptyList();
                List<String> sellLore = product.isSellable() ? ChestConfig.PRODUCT_FORMAT_LORE_SELL.get() : Collections.emptyList();

                ItemStack preview = product.getPreview();
                return NightItem.fromItemStack(preview)
                    .setLore(loreFormat)
                    .replacement(replacer -> replacer
                        .replace(GENERIC_BUY, buyLore)
                        .replace(GENERIC_SELL, sellLore)
                        .replace(GENERIC_LORE, ItemUtil.getLoreSerialized(preview))
                        .replace(product.replacePlaceholders(viewer.getPlayer()))
                        .replace(product.getCurrency().replacePlaceholders())
                        .replace(shop.replacePlaceholders())
                        .replacePlaceholderAPI(viewer.getPlayer())
                    );
            })
            .setItemClick(product -> (viewer1, event) -> {
                Player player = viewer1.getPlayer();
                plugin.getShopManager().onProductClick(player, product, event.getClick(), this);
            })
            .build();
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.productSlots = ConfigValue.create("Item.Product_Slots", new int[]{10,13,16}).read(config);

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem()
            .setSlots(IntStream.range(0, 27).toArray())
            .setPriority(0)
        );

        loader.addDefaultItem(NightItem.fromType(Material.FIREWORK_STAR)
            .setDisplayName(GRAY.wrap("Out of Stock"))
            .setColor(Color.GRAY)
            .hideAllComponents()
            .toMenuItem()
            .setSlots(10,13,16)
            .setPriority(1)
        );

        loader.addDefaultItem(MenuItem.buildNextPage(this, 26).setPriority(10));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 18).setPriority(10));
    }
}
