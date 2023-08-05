package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

import java.util.*;

public class ShopSearchMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestProduct> {

    public static final String FILE = "shops_search.yml";

    private final ChestShopModule                module;
    private final Map<Player, List<ChestProduct>> searchCache;

    private final int[]        productSlots;
    private final String       productName;
    private final List<String> productLore;

    public ShopSearchMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getMenusPath(), FILE));
        this.module = module;
        this.searchCache = new WeakHashMap<>();

        this.productSlots = cfg.getIntArray("Product.Slots");
        this.productName = Colorizer.apply(cfg.getString("Product.Name", Placeholders.PRODUCT_PREVIEW_NAME));
        this.productLore = Colorizer.apply(cfg.getStringList("Product.Lore"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    public void open(@NotNull Player player, @NotNull String input) {
        String input2 = input.toLowerCase();

        List<ChestProduct> products = new ArrayList<>();
        this.module.getShops().forEach(shop -> {
            products.addAll(shop.getProducts().stream().filter(product -> {
                ItemStack item = product.getItem();
                if (item.getType().name().toLowerCase().contains(input2)) return true;

                return ItemUtil.getItemName(item).toLowerCase().contains(input);
            }).toList());
        });

        this.searchCache.put(player, products);
        this.open(player, 1);
    }

    @NotNull
    private Collection<ChestProduct> getSearchResult(@NotNull Player player) {
        return this.searchCache.getOrDefault(player, Collections.emptyList());
    }

    @Override
    public int[] getObjectSlots() {
        return this.productSlots;
    }

    @Override
    @NotNull
    public List<ChestProduct> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.getSearchResult(player).stream()
            .sorted((p1, p2) -> (int) (p1.getPricer().getPriceBuy() - p2.getPricer().getPriceBuy())).toList());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ChestProduct product) {
        ItemStack item = new ItemStack(product.getItem());
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(this.productName);
            meta.setLore(this.productLore);
            item.setItemMeta(meta);
            ItemUtil.replace(meta, product.replacePlaceholders());
            ItemUtil.replace(meta, product.getShop().replacePlaceholders());
        });
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestProduct product) {
        return (viewer, event) -> {
            product.getShop().teleport(viewer.getPlayer());
        };
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.searchCache.remove(viewer.getPlayer());
    }
}
