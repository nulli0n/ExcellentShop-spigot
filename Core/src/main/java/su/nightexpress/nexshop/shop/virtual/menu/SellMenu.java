package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class SellMenu extends ConfigMenu<ShopPlugin> implements Linked<List<ItemStack>> {

    public static final String FILE_NAME = "sell.menu.yml";

    private final VirtualShopModule         module;
    private final ViewLink<List<ItemStack>> link;
    private final ItemHandler               sellHandler;
    private final boolean                   simplified;

    private String       itemName;
    private List<String> itemLore;
    private int[]        itemSlots;

    public SellMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
        this.module = module;
        this.simplified = VirtualConfig.SELL_MENU_SIMPLIFIED.get();
        this.link = new ViewLink<>();

        this.addHandler(this.sellHandler = new ItemHandler("sell", (viewer, event) -> {
            Player player = viewer.getPlayer();
            List<ItemStack> items = this.getLink().get(player);
            if (items == null) return;

            Inventory inventory = plugin.getServer().createInventory(null, 54, "dummy");
            inventory.addItem(items.toArray(new ItemStack[0]));
            items.clear();

            this.module.sellWithReturn(player, inventory);
            this.plugin.runTask(task -> player.closeInventory());
        }));

        this.load();

        // We don't need decorative items in simplified menu without click event restrictions.
        if (this.simplified) {
            this.getItems().clear();
        }
    }

    @NotNull
    @Override
    public ViewLink<List<ItemStack>> getLink() {
        return link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        if (this.simplified) return;

        Player player = viewer.getPlayer();
        List<ItemStack> items = this.getLink().get(player);
        int index = 0;

        for (ItemStack item : items) {
            if (index >= this.itemSlots.length) break;

            int slot = this.itemSlots[index++];
            if (slot >= inventory.getSize()) continue;

            VirtualProduct product = this.module.getBestProductFor(item, TradeType.SELL, player);
            if (product == null) continue;

            ItemStack icon = new ItemStack(item);
            double price = product.getPriceSell(player) * (item.getAmount() / (double) product.getUnitAmount());

            ItemReplacer.create(icon)
                .trimmed()
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replaceLoreExact(ITEM_LORE, ItemUtil.getLore(item))
                .replace(ITEM_NAME, ItemUtil.getItemName(item))
                .replace(product.getShop().getPlaceholders())
                .replace(product.getPlaceholders(player))
                .replace(GENERIC_PRICE, () -> product.getCurrency().format(price))
                .writeMeta();

            inventory.setItem(slot, icon);
        }
    }

    @Override
    public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
        super.onDrag(viewer, event);
        if (this.simplified) {
            event.setCancelled(false);
        }
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (this.simplified) {
            event.setCancelled(false);
            return;
        }

        Player player = viewer.getPlayer();
        ItemStack item = result.getItemStack();
        if (item == null || item.getType().isAir()) return;

        if (result.isInventory()) {
            VirtualProduct product = this.module.getBestProductFor(item, TradeType.SELL, player);
            if (product == null) return;

            List<ItemStack> items = this.getLink().get(player);
            if (items.size() >= this.itemSlots.length) return;

            items.add(new ItemStack(item));
            item.setAmount(0);
            this.runNextTick(() -> this.flush(viewer));
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Player player = viewer.getPlayer();

        if (this.simplified) {
            this.module.sellWithReturn(player, event.getInventory());
        }
        else {
            List<ItemStack> userItems = this.getLink().get(player);
            if (userItems != null) {
                userItems.forEach(item -> Players.addItem(player, item));
            }
        }

        super.onClose(viewer, event);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Put items to sell"), MenuSize.CHEST_54);
    }

    @Override
    protected void loadAdditional() {
        this.itemName = ConfigValue.create("Item.Name", WHITE.enclose(ITEM_NAME)).read(cfg);

        this.itemLore = ConfigValue.create("Item.Lore", List.of(
            ITEM_LORE,
            "",
            LIGHT_GREEN.enclose(BOLD.enclose("Details:")),
            LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Approx Price: ") + GENERIC_PRICE),
            LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Found In: ") + SHOP_NAME)
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Item.Slots", IntStream.range(0, 45).toArray()).read(cfg);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack sellItem = ItemUtil.getSkinHead(SKIN_CHECK_MARK);
        ItemUtil.editMeta(sellItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("SELL ALL!")));
        });
        list.add(new MenuItem(sellItem).setPriority(10).setSlots(49).setHandler(this.sellHandler));

        return list;
    }
}
