package su.nightexpress.nexshop.shop.virtual.editor.menu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.*;

public class EditorShopViewDesign extends AbstractMenu<ExcellentShop> {

    private final VirtualShop shop;

    private final NamespacedKey keyItemType;
    private final NamespacedKey keyReserved;

    private static final String TYPE_PREFIX = ChatColor.AQUA + "Type: " + ChatColor.GREEN;

    public EditorShopViewDesign(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(plugin, shop.getView().getTitle(), shop.getView().getSize());
        this.keyItemType = new NamespacedKey(plugin, "menu_item_type");
        this.keyReserved = new NamespacedKey(plugin, "reserved_slot");
        this.shop = shop;
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        this.setPage(player, 1, this.shop.getPages()); // Hack for page items display.

        for (MenuItem menuItem : this.shop.getView().getItemsMap().values()) {
            Enum<?> type = menuItem.getType();
            ItemStack item = menuItem.getItem();
            PDCUtil.set(item, this.keyItemType, type != null ? type.name() : "null");
            this.updateItem(item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }

        ItemStack reserved = VirtualEditorType.PRODUCT_RESERVED_SLOT.getItem();
        PDCUtil.set(reserved, this.keyReserved, true);
        for (VirtualProduct product : this.shop.getProducts()) {
            int slot = product.getSlot();
            if (slot >= inventory.getSize()) continue;
            inventory.setItem(slot, reserved);
        }

        return true;
    }

    @Override
    public void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (item == null || item.getType().isAir()) return;

        if (PDCUtil.getBoolean(item, this.keyReserved).orElse(false)) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick() == ClickType.DROP && slot < this.getSize()) {
            PDCUtil.set(item, this.keyItemType, CollectionsUtil.next(this.getType(item)).name());
            e.setCancelled(true);
        }
        this.updateItem(item);
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.plugin.runTask(task -> {
            this.save(e.getInventory());
            this.shop.getEditor().open(player, 1);
        });

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) this.removeTypeLore(item);
        }

        super.onClose(player, e);
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
            if (PDCUtil.getBoolean(item, this.keyReserved).orElse(false)) continue;

            MenuItemType type = this.getType(item);
            Map<ItemStack, List<Integer>> map = items.computeIfAbsent(type, k -> new HashMap<>());
            map.computeIfAbsent(item, k -> new ArrayList<>()).add(slot);
        }

        JYML cfg = shop.getConfigView();
        cfg.set("Content", null);

        items.forEach((type, map) -> {
            map.forEach((item2, slots) -> {
                this.removeTypeLore(item2);

                String id = UUID.randomUUID().toString();
                String path = "Content." + id + ".";
                String typeRaw = this.getType(item2).name();

                cfg.setItem(path + "Item.", item2);
                cfg.setIntArray(path + "Slots", slots.stream().mapToInt(i -> i).toArray());
                cfg.set(path + "Type", typeRaw);
            });
        });

        cfg.saveChanges();
        shop.setupView();
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return false;
    }
}
