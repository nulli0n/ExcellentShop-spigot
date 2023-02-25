package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

import java.util.*;

public class ShopsSearchMenu extends AbstractMenuAuto<ExcellentShop, ChestProduct> {

    private final ChestShopModule                module;
    private final Map<Player, List<ChestProduct>> searchCache;

    private final int[]        productSlots;
    private final String       productName;
    private final List<String> productLore;

    public ShopsSearchMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getPath() + "menu/shops_search.yml"), "");
        this.module = module;
        this.searchCache = new WeakHashMap<>();

        this.productSlots = cfg.getIntArray("Product.Slots");
        this.productName = Colorizer.apply(cfg.getString("Product.Name", Placeholders.PRODUCT_ITEM_NAME));
        this.productLore = Colorizer.apply(cfg.getStringList("Product.Lore"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    public void open(@NotNull Player player, @NotNull Material material) {
        List<ChestProduct> products = new ArrayList<>();
        this.module.getShops().forEach(shop -> {
            products.addAll(shop.getProducts().stream().filter(product -> product.getItem().getType() == material).toList());
        });
        this.searchCache.put(player, products);
        this.open(player, 1);
    }

    @NotNull
    private Collection<ChestProduct> getSearchResult(@NotNull Player player) {
        return this.searchCache.getOrDefault(player, Collections.emptyList());
    }

    @Override
    protected int[] getObjectSlots() {
        return this.productSlots;
    }

    @Override
    @NotNull
    protected List<ChestProduct> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.getSearchResult(player).stream()
            .sorted((p1, p2) -> (int) (p1.getPricer().getPriceBuy() - p2.getPricer().getPriceBuy())).toList());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ChestProduct product) {
        ItemStack item = new ItemStack(product.getItem());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.productName);
        meta.setLore(this.productLore);
        item.setItemMeta(meta);

        ItemUtil.replace(item, product.replacePlaceholders());
        ItemUtil.replace(item, product.getShop().replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ChestProduct product) {
        return (player1, type, e) -> {
            product.getShop().teleport(player1);
        };
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.searchCache.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
