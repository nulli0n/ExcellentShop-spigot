package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;

import java.util.*;

public class SellMenu extends ConfigMenu<ExcellentShop> {

    private final VirtualShopModule module;
    private final String            itemName;
    private final List<String>      itemLore;
    private final int[]             itemSlots;

    private static final Map<Player, List<ItemStack>> USER_ITEMS = new WeakHashMap<>();

    public SellMenu(@NotNull VirtualShopModule module, @NotNull JYML cfg) {
        super(module.plugin(), cfg);
        this.module = module;
        this.itemName = Colorizer.apply(cfg.getString("Item.Name", "%item_name%"));
        this.itemLore = Colorizer.apply(cfg.getStringList("Item.Lore"));
        this.itemSlots = cfg.getIntArray("Item.Slots");

        this.registerHandler(ItemType.class)
            .addClick(ItemType.SELL, (viewer, event) -> {
                Player player = viewer.getPlayer();
                List<ItemStack> userItems = USER_ITEMS.remove(player);
                if (userItems == null) return;

                Inventory inventory = plugin.getServer().createInventory(null, 54, "dummy");
                inventory.addItem(userItems.toArray(new ItemStack[0]));

                this.module.sellWithReturn(player, inventory);
                this.plugin.runTask(task -> player.closeInventory());
            });

        this.load();
    }

    enum ItemType {
        SELL
    }

    @Override
    public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
        super.onDrag(viewer, event);
        if (VirtualConfig.SELL_MENU_SIMPLIFIED.get()) {
            event.setCancelled(false);
        }
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        if (VirtualConfig.SELL_MENU_SIMPLIFIED.get()) {
            event.setCancelled(false);
            return;
        }

        Player player = viewer.getPlayer();
        Inventory inventory = event.getInventory();
        if (item == null || item.getType().isAir()) return;

        if (slotType == SlotType.PLAYER) {
            VirtualProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) return;

            int firtSlot = Arrays.stream(this.itemSlots)
                .filter(slot2 -> {
                    ItemStack has = inventory.getItem(slot2);
                    return has == null || has.getType().isAir();
                })
                .findFirst().orElse(-1);
            if (firtSlot < 0) return;

            ItemStack icon = new ItemStack(item);
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) return;

            double price = product.getPricer().getSellPrice() * (item.getAmount() / (double) product.getUnitAmount());

            List<String> lore = new ArrayList<>(this.itemLore);
            lore.replaceAll(line -> {
                line = product.getShop().replacePlaceholders().apply(line);
                line = product.replacePlaceholders().apply(line);
                return line
                    .replace(Placeholders.GENERIC_PRICE, product.getCurrency().format(price));
            });
            lore = StringUtil.replaceInList(lore, "%item_lore%", ItemUtil.getLore(item));
            lore = StringUtil.stripEmpty(lore);

            meta.setDisplayName(this.itemName.replace("%item_name%", ItemUtil.getItemName(item)));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(firtSlot, icon);

            List<ItemStack> userItems = USER_ITEMS.computeIfAbsent(player, k -> new ArrayList<>());
            userItems.add(new ItemStack(item));
            item.setAmount(0);
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);

        Player player = viewer.getPlayer();

        if (VirtualConfig.SELL_MENU_SIMPLIFIED.get()) {
            this.module.sellWithReturn(player, event.getInventory());
        }
        else {
            List<ItemStack> userItems = USER_ITEMS.remove(player);
            if (userItems != null) {
                userItems.forEach(item -> PlayerUtil.addItem(player, item));
            }
        }
    }

    @Deprecated
    public void sellWithReturn(@NotNull Player player, @NotNull Inventory inventory) {
        this.module.sellWithReturn(player, inventory);
    }

    @Deprecated
    public void sellSlots(@NotNull Player player, int... slots) {
        this.module.sellSlots(player, slots);
    }

    @Deprecated
    public void sellAll(@NotNull Player player) {
        this.module.sellAll(player);
    }

    @Deprecated
    public void sellAll(@NotNull Player player, @NotNull Inventory inventory) {
        this.module.sellAll(player, inventory);
    }

    @Deprecated
    public void sellAll(@NotNull Player player, @NotNull Inventory inventory, @Nullable VirtualShop shop) {
        this.module.sellAll(player, inventory, shop);
    }
}
