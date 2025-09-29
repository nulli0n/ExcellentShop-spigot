package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class PurchaseOptionMenu extends LinkedMenu<ShopPlugin, Breadcumb<Product>> implements ConfigBased {

    public static final String FILE_NAME = "purchase_option.yml";

    private int productSlot;

    public record Data(Product product, int page) {}

    public PurchaseOptionMenu(@NotNull ShopPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X1, BLACK.enclose("What you would like to do?"));

        this.load(FileConfig.loadOrExtract(plugin, Config.DIR_MENU, FILE_NAME));
    }

    private void onOptionClick(@NotNull MenuViewer viewer, @NotNull TradeType type) {
        Player player = viewer.getPlayer();
        Product product = this.getLink(player).source();

        if (!product.isTradeable(type)) return;

        this.runNextTick(() -> plugin.getShopManager().startTrade(player, product, type, null, null));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        Product product = this.getLink(viewer).source();

        item.replacement(replacer -> replacer.replace(product.replacePlaceholders(viewer.getPlayer())));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Inventory inventory = view.getTopInventory();

        if (this.productSlot >= 0 && this.productSlot <= inventory.getSize()) {
            Product product = this.getLink(viewer).source();

            this.addItem(viewer, NightItem.fromItemStack(product.getPreviewOrPlaceholder()).toMenuItem().setSlots(this.productSlot));
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addHandler(ItemHandler.forReturn(this, (viewer, event) -> {
            Player player = viewer.getPlayer();
            var breadcumb = this.getLink(player);
            Product product = breadcumb.source();

            this.runNextTick(() -> product.getShop().open(player, breadcumb.page(), true));
        }));

        loader.addDefaultItem(NightItem.fromType(Material.LIME_DYE)
            .setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("BUY")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Unit Price: " + LIGHT_GREEN.enclose(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)))
            ))
            .toMenuItem().setPriority(10).setSlots(8).setHandler(new ItemHandler("buy", (viewer, event) -> {
                this.onOptionClick(viewer, TradeType.BUY);
            })));

        loader.addDefaultItem(NightItem.fromType(Material.RED_DYE)
            .setDisplayName(LIGHT_RED.enclose(BOLD.enclose("SELL")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Unit Price: " + LIGHT_RED.enclose(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)))
            ))
            .toMenuItem().setPriority(10).setSlots(0).setHandler(new ItemHandler("sell", (viewer, event) -> {
                this.onOptionClick(viewer, TradeType.SELL);
            })));

        this.productSlot = ConfigValue.create("Product.Slot", 4).read(config);
    }
}
