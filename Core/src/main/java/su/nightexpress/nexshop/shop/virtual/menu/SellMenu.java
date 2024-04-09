package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;

import java.util.*;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;

public class SellMenu extends ConfigMenu<ExcellentShop> implements Linked<List<ItemStack>> {

    private final VirtualShopModule module;
    private final ViewLink<List<ItemStack>> link;
    private final boolean simplified;

    private String       itemName;
    private List<String> itemLore;
    private int[]        itemSlots;

    public SellMenu(@NotNull ExcellentShop plugin, @NotNull VirtualShopModule module, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.module = module;
        this.simplified = VirtualConfig.SELL_MENU_SIMPLIFIED.get();
        this.link = new ViewLink<>();

        this.registerHandler(ItemType.class)
            .addClick(ItemType.SELL, (viewer, event) -> {
                Player player = viewer.getPlayer();
                List<ItemStack> items = this.getLink().get(player);
                if (items == null) return;

                Inventory inventory = plugin.getServer().createInventory(null, 54, "dummy");
                inventory.addItem(items.toArray(new ItemStack[0]));
                items.clear();

                this.module.sellWithReturn(player, inventory);
                this.plugin.runTask(task -> player.closeInventory());
            });

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

    enum ItemType {
        SELL
    }

    @Override
    public boolean isCodeCreation() {
        return true;
    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        super.onReady(viewer, inventory);

        if (this.simplified) return;

        Player player = viewer.getPlayer();
        List<ItemStack> items = this.getLink().get(player);
        int index = 0;

        for (ItemStack item : items) {
            if (index >= this.itemSlots.length) break;

            int slot = this.itemSlots[index++];
            if (slot >= inventory.getSize()) continue;

            VirtualProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
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
                .replace(Colorizer::apply)
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
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        if (this.simplified) {
            event.setCancelled(false);
            return;
        }

        Player player = viewer.getPlayer();
        if (item == null || item.getType().isAir()) return;

        if (slotType == SlotType.PLAYER) {
            VirtualProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) return;

            List<ItemStack> items = this.getLink().get(player);
            if (items.size() >= this.itemSlots.length) return;

            items.add(new ItemStack(item));
            item.setAmount(0);
            this.openNextTick(viewer, 1);
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
                userItems.forEach(item -> PlayerUtil.addItem(player, item));
            }
        }

        super.onClose(viewer, event);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions("Sell Menu", 54, InventoryType.CHEST);
    }

    @Override
    protected void loadAdditional() {
        this.itemName = JOption.create("Item.Name", Colors.WHITE + ITEM_NAME).read(cfg);

        this.itemLore = JOption.create("Item.Lore", List.of(
            ITEM_LORE,
            "",
            Colors.GREEN + Colors.BOLD + "Details:",
            Colors.GREEN + "▪ " + Colors.GRAY + "Approx Price: " + Colors.GREEN + GENERIC_PRICE,
            Colors.GREEN + "▪ " + Colors.GRAY + "Found In: " + Colors.GREEN + SHOP_NAME
        )).read(cfg);

        this.itemSlots = new JOption<int[]>("Item.Slots",
            JYML::getIntArray,
            () -> IntStream.range(0, 45).toArray()
        ).setWriter(JYML::setIntArray).read(cfg);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack sellItem = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=");
        ItemUtil.mapMeta(sellItem, meta -> {
            meta.setDisplayName(Colors.GREEN + Colors.BOLD + "SELL ALL!");
        });
        list.add(new MenuItem(sellItem).setPriority(10).setSlots(49).setType(ItemType.SELL));

        return list;
    }
}
