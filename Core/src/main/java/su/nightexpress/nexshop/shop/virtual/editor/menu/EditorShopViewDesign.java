package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemDisplay;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;

import java.util.*;

public class EditorShopViewDesign extends AbstractMenu<ExcellentShop> {

    private final IShopVirtual shop;

    private final NamespacedKey keyItemType;
    private final NamespacedKey keyReserved;

    private static final String TAG_LORE = "menu_type";
    private static final String TIP_LORE_TYPE = StringUtil.color("&eItem Type &7(Middle-Click): &6");

    public EditorShopViewDesign(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, shop.getView().getTitle(), shop.getView().getSize());
        this.keyItemType = new NamespacedKey(plugin, "menu_item_type");
        this.keyReserved = new NamespacedKey(plugin, "reserved_slot");
        this.shop = shop;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        this.setPage(player, 1, this.shop.getPages()); // Hack for page items display.

        for (IMenuItem menuItem : this.shop.getView().getItemsMap().values()) {
            MenuItemDisplay display = menuItem.getDisplay(player);
            if (display == null) continue;

            Enum<?> type = menuItem.getType();
            ItemStack item = display.getItem();
            PDCUtil.setData(item, this.keyItemType, type != null ? type.name() : "null");
            this.updateItem(item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }

        ItemStack reserved = new ItemStack(Material.BARRIER);
        PDCUtil.setData(reserved, this.keyReserved, true);
        for (IProductVirtual product : this.shop.getProducts()) {
            int slot = product.getSlot();
            if (slot >= inventory.getSize()) continue;
            inventory.setItem(slot, reserved);
        }
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (item == null || item.getType().isAir()) return;

        if (PDCUtil.getBooleanData(item, this.keyReserved)) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick() == ClickType.MIDDLE && slot < this.getSize()) {
            PDCUtil.setData(item, this.keyItemType, CollectionsUtil.switchEnum(this.getType(item)).name());
            e.setCancelled(true);
        }
        this.updateItem(item);
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask(c -> {
            this.save(e.getInventory());
            this.shop.getEditor().open(player, 1);
        }, false);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) ItemUtil.delLore(item, TAG_LORE);
        }

        super.onClose(player, e);
    }

    @NotNull
    private MenuItemType getType(@NotNull ItemStack item) {
        String typeRaw = PDCUtil.getStringData(item, this.keyItemType);
        MenuItemType type = typeRaw == null ? MenuItemType.NONE : CollectionsUtil.getEnum(typeRaw, MenuItemType.class);
        return type == null ? MenuItemType.NONE : type;
    }

    private void updateItem(@NotNull ItemStack item) {
        ItemUtil.delLore(item, TAG_LORE);
        String str = StringUtil.color(TIP_LORE_TYPE + this.getType(item).name());
        ItemUtil.addLore(item, TAG_LORE, str, -1);
    }

    private void save(@NotNull Inventory inventory) {
        Map<MenuItemType, Map<ItemStack, List<Integer>>> items = new HashMap<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            if (PDCUtil.getBooleanData(item, this.keyReserved)) continue;

            MenuItemType type = this.getType(item);
            Map<ItemStack, List<Integer>> map = items.computeIfAbsent(type, k -> new HashMap<>());
            map.computeIfAbsent(item, k -> new ArrayList<>()).add(slot);
        }

        ShopVirtual shop = (ShopVirtual) this.shop;
        JYML cfg = shop.getConfigView();
        cfg.set("Content", null);

        items.forEach((type, map) -> {
            map.forEach((item2, slots) -> {
                ItemUtil.delLore(item2, TAG_LORE);

                String id = UUID.randomUUID().toString();
                String path = "Content." + id + ".";
                String typeRaw = this.getType(item2).name();

                cfg.setItem(path + "Display.default.Item.", item2);
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
