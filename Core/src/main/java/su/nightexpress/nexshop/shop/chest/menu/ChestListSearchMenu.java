package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.chest.IProductChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;

import java.util.*;

public class ChestListSearchMenu extends AbstractMenuAuto<ExcellentShop, IProductChest> {

    private final ChestShop                     chestShop;
    private final Map<String, Set<IProductChest>> searchCache;

    private final int[]        productSlots;
    private final String       productName;
    private final List<String> productLore;

    public ChestListSearchMenu(@NotNull ChestShop chestShop) {
        super(chestShop.plugin(), JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "menu/search.yml"), "");
        this.chestShop = chestShop;
        this.searchCache = new HashMap<>();

        this.productSlots = cfg.getIntArray("Product.Slots");
        this.productName = StringUtil.color(cfg.getString("Product.Name", ""));
        this.productLore = StringUtil.color(cfg.getStringList("Product.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    public void searchProduct(@NotNull Player player, @NotNull Material material) {
        Set<IProductChest> products = new HashSet<>();
        this.chestShop.getShops().forEach(shop -> {
            products.addAll(shop.getProducts().stream().filter(product -> product.getItem().getType() == material).toList());
        });
        this.searchCache.put(player.getName(), products);
    }

    @NotNull
    public Collection<IProductChest> getSearchResult(@NotNull Player player) {
        return this.searchCache.getOrDefault(player.getName(), Collections.emptySet());
    }

    @Override
    protected int[] getObjectSlots() {
        return this.productSlots;
    }

    @Override
    @NotNull
    protected List<IProductChest> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.getSearchResult(player).stream()
            .sorted((p1, p2) -> (int) (p1.getPricer().getPriceBuy() - p2.getPricer().getPriceBuy())).toList());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IProductChest product) {
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
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IProductChest product) {
        return (player1, type, e) -> {
            product.getShop().teleport(player1);
        };
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.searchCache.remove(player.getName());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
