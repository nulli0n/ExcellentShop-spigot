package su.nightexpress.nexshop.shop.virtual.editor.menu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.shop.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.shop.StaticShop;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.*;

public class ShopViewEditor extends EditorMenu<ExcellentShop, VirtualShop<?, ?>> {

    private final NamespacedKey keyItemType;
    private final NamespacedKey keyReserved;

    private static final String TYPE_PREFIX = ChatColor.AQUA + "Type" + ChatColor.GRAY + " [Q/Drop]: " + ChatColor.GREEN;

    public ShopViewEditor(@NotNull ExcellentShop plugin, @NotNull VirtualShop<?, ?> shop) {
        super(plugin, shop, shop.getName(), 54);
        this.keyItemType = new NamespacedKey(plugin, "menu_item_type");
        this.keyReserved = new NamespacedKey(plugin, "reserved_slot");
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        int pages;
        int[] slots;

        if (this.object instanceof StaticShop staticShop) {
            pages = staticShop.getPages();
            slots = staticShop.getProducts().stream().mapToInt(StaticProduct::getSlot).toArray();
        }
        else if (this.object instanceof RotatingShop rotatingShop) {
            pages = 1;
            slots = rotatingShop.getProductSlots();
        }
        else return;

        viewer.setPages(pages);
        options.setTitle(this.object.getView().getOptions().getTitle());
        options.setSize(this.object.getView().getOptions().getSize());

        ItemStack reserved = new ItemStack(Material.BARRIER);
        PDCUtil.set(reserved, this.keyReserved, true);

        this.addItem(reserved, VirtualLocales.PRODUCT_RESERVED_SLOT, slots).setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        super.onReady(viewer, inventory);

        for (MenuItem menuItem : this.object.getView().getItems()) {
            Enum<?> type = menuItem.getType();
            ItemStack item = menuItem.getItem();
            PDCUtil.set(item, this.keyItemType, type.name());
            this.updateItem(item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Player player = viewer.getPlayer();

        this.plugin.runTask(task -> {
            this.save(event.getInventory());
            this.object.getEditor().open(player, 1);
        });

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) this.removeTypeLore(item);
        }

        super.onClose(viewer, event);
    }

    @NotNull
    private MenuItemType getType(@NotNull ItemStack item) {
        String typeRaw = PDCUtil.getString(item, this.keyItemType).orElse(null);
        return typeRaw == null ? MenuItemType.NONE : StringUtil.getEnum(typeRaw, MenuItemType.class).orElse(MenuItemType.NONE);
    }

    private void updateItem(@NotNull ItemStack item) {
        this.removeTypeLore(item);
        this.addTypeLore(item, this.getType(item));
    }

    private void removeTypeLore(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        lore.removeIf(line -> line.startsWith(TYPE_PREFIX));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void addTypeLore(@NotNull ItemStack item, @NotNull MenuItemType type) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        lore.add(0, TYPE_PREFIX + type.name());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void save(@NotNull Inventory inventory) {
        Map<MenuItemType, Map<ItemStack, List<Integer>>> items = new HashMap<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            if (PDCUtil.getBoolean(item, this.keyReserved).isPresent()) continue;

            MenuItemType type = this.getType(item);
            Map<ItemStack, List<Integer>> map = items.computeIfAbsent(type, k -> new HashMap<>());
            map.computeIfAbsent(item, k -> new ArrayList<>()).add(slot);
        }

        JYML cfg = this.object.getView().getConfig();
        cfg.set("Content", null);

        items.forEach((type, map) -> {
            map.forEach((itemStack, slots) -> {
                this.removeTypeLore(itemStack);

                MenuItemType itemType = this.getType(itemStack);

                String id = UUID.randomUUID().toString();
                String path = "Content." + id + ".";
                String typeRaw = itemType.name();

                cfg.set(path + "Priority", itemType != MenuItemType.NONE ? 100 : 0);
                cfg.setItem(path + "Item.", itemStack);
                cfg.setIntArray(path + "Slots", slots.stream().mapToInt(Number::intValue).toArray());
                cfg.set(path + "Type", typeRaw);
            });
        });

        cfg.saveChanges();
        this.object.getView().reload();
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        event.setCancelled(false);

        if (item == null || item.getType().isAir()) return;

        if (PDCUtil.getBoolean(item, this.keyReserved).isPresent()) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.DROP && slot < event.getInventory().getSize()) {
            PDCUtil.set(item, this.keyItemType, CollectionsUtil.next(this.getType(item)).name());
            event.setCancelled(true);
        }
        this.updateItem(item);
    }

    @Override
    public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
        super.onDrag(viewer, event);
        event.setCancelled(false);
    }
}
