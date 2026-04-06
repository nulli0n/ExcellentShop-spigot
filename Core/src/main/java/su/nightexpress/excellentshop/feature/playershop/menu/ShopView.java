package su.nightexpress.excellentshop.feature.playershop.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestProduct;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.Numbers;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class ShopView extends AbstractObjectMenu<ChestShop> {

    private final ChestShopModule module;

    private TreeMap<Integer, int[]> productSlotsByCount;

    public ShopView(@NonNull ShopPlugin plugin, @NonNull ChestShopModule module) {
        super(plugin, MenuType.HOPPER, ShopPlaceholders.SHOP_NAME, ChestShop.class);
        this.module = module;
    }

    @Override
    @NonNull
    protected String getRawTitle(@NonNull ViewerContext context) {
        return PlaceholderContext.builder().with(this.getObject(context).placeholders()).build().apply(super.getRawTitle(context));
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(0, 5).toArray());
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {
        this.productSlotsByCount = new TreeMap<>(config.get(ConfigTypes.forMap(Numbers::getIntegerAbs, String::valueOf, ConfigTypes.INT_ARRAY), "Product.SlotsByCount",
            Map.of(
                1, new int[]{2},
                2, new int[]{1,3},
                3, new int[]{1,2,3},
                4, new int[]{0,1,3,4},
                5, new int[]{0,1,2,3,4}
            )));
    }

    @Override
    protected void onClick(@NonNull ViewerContext context, @NonNull InventoryClickEvent event) {

    }

    @Override
    protected void onDrag(@NonNull ViewerContext context, @NonNull InventoryDragEvent event) {

    }

    @Override
    protected void onClose(@NonNull ViewerContext context, @NonNull InventoryCloseEvent event) {

    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory, @NonNull List<MenuItem> items) {
        Player player = context.getPlayer();
        ChestShop shop = this.getObject(context);

        List<ChestProduct> products = shop.getValidProducts();
        if (products.isEmpty()) return;

        int totalItems = products.size();
        var entry = this.productSlotsByCount.floorEntry(totalItems);
        if (entry == null) return;

        int[] slots = entry.getValue();

        for (int index = 0; index < products.size(); index++) {
            ChestProduct product = products.get(index);
            int slot = slots[index];

            items.add(MenuItem.custom()
                .defaultState(ItemState.builder()
                    .icon(NightItem.fromItemStack(product.getEffectivePreview())
                        .setLore(this.module.formatProductLore(product, player))
                        .replace(builder -> builder
                            .with(product.placeholders())
                            .with(shop.placeholders())
                            .andThen(product.getCurrency().replacePlaceholders())
                        )
                    )
                    .action(ctx -> this.handleProduct(ctx, product))
                    .build()
                )
                .slots(slot)
                .build()
            );
        }
    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    private void handleProduct(@NonNull ActionContext context, @NonNull ChestProduct product) {
        Player player = context.getPlayer();
        int page = context.getViewer().getCurrentPage();
        InventoryClickEvent event = context.getEvent();

        this.module.handleProductClick(player, product, page, event);
    }
}
