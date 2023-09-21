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
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.util.TransactionResult;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualPreparedProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;

import java.util.*;

public class ShopSellMenu extends ConfigMenu<ExcellentShop> {

    private final VirtualShopModule module;
    private final String       itemName;
    private final List<String> itemLore;
    private final int[] itemSlots;

    private static final Map<Player, Pair<List<ItemStack>, Set<StaticProduct>>> USER_ITEMS = new WeakHashMap<>();

    public ShopSellMenu(@NotNull VirtualShopModule module, @NotNull JYML cfg) {
        super(module.plugin(), cfg);
        this.module = module;
        this.itemName = Colorizer.apply(cfg.getString("Item.Name", "%item_name%"));
        this.itemLore = Colorizer.apply(cfg.getStringList("Item.Lore"));
        this.itemSlots = cfg.getIntArray("Item.Slots");

        this.registerHandler(ItemType.class)
            .addClick(ItemType.SELL, (viewer, e) -> {
                Player player = viewer.getPlayer();
                Pair<List<ItemStack>, Set<StaticProduct>> userItems = USER_ITEMS.remove(player);
                if (userItems == null) return;

                plugin.runTask(task -> {
                    sellAll(player, userItems);
                });
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
            StaticProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) return;

            int firtSlot = Arrays.stream(this.itemSlots)
                .filter(slot2 -> { // Idea tells me there is possible NPE for a single line :/
                    ItemStack has = inventory.getItem(slot2);
                    return has == null || has.getType().isAir();
                })
                .findFirst().orElse(-1);
            if (firtSlot < 0) return;

            ItemStack icon = new ItemStack(item);
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) return;

            double price = product.getPricer().getPriceSell() * (item.getAmount() / (double) product.getUnitAmount());

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

            Pair<List<ItemStack>, Set<StaticProduct>> pair = USER_ITEMS.computeIfAbsent(player, k -> Pair.of(new ArrayList<>(), new HashSet<>()));
            pair.getFirst().add(new ItemStack(item));
            pair.getSecond().add(product);
            item.setAmount(0);
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);

        Player player = viewer.getPlayer();

        if (VirtualConfig.SELL_MENU_SIMPLIFIED.get()) {
            Inventory inventory = event.getInventory();
            this.sellInventory(player, inventory);

            for (ItemStack left : inventory.getContents()) {
                if (left == null || left.getType().isAir() || left.getAmount() < 1) continue;

                PlayerUtil.addItem(player, left);
            }
        }
        else {
            Pair<List<ItemStack>, Set<StaticProduct>> userItems = USER_ITEMS.remove(player);
            if (userItems != null) {
                userItems.getFirst().forEach(item -> PlayerUtil.addItem(player, item));
            }
        }
    }

    public void sellInventory(@NotNull Player player, @NotNull Inventory inventory) {
        Pair<List<ItemStack>, Set<StaticProduct>> userItems = Pair.of(new ArrayList<>(), new HashSet<>());

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;

            StaticProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) continue;

            userItems.getFirst().add(new ItemStack(item));
            userItems.getSecond().add(product);
            item.setAmount(0);
        }

        ShopSellMenu.sellAll(player, userItems);
    }

    public static void sellAll(@NotNull Player player, @NotNull Pair<List<ItemStack>, Set<StaticProduct>> userItems) {
        if (userItems.getFirst().isEmpty() || userItems.getSecond().isEmpty()) return;

        ItemStack[] original = player.getInventory().getContents();
        player.getInventory().clear();

        List<TransactionResult> profits = new ArrayList<>();
        userItems.getFirst().forEach(item -> PlayerUtil.addItem(player, item));
        userItems.getSecond().forEach(product -> {
            VirtualPreparedProduct<?> preparedProduct = product.getPrepared(player, TradeType.SELL, true);
            TransactionResult result = preparedProduct.trade();

            if (result.getResult() == TransactionResult.Result.SUCCESS) {
                profits.add(result);
            }
        });

        ItemStack[] left = player.getInventory().getContents();
        player.getInventory().setContents(original);
        Arrays.asList(left).forEach(item -> {
            if (item != null && !item.getType().isAir()) PlayerUtil.addItem(player, item);
        });

        //player.updateInventory();
        player.closeInventory();
        if (profits.isEmpty()) return;

        ShopAPI.PLUGIN.getMessage(VirtualLang.SELL_MENU_SOLD)
            .replace(str -> str.contains(Placeholders.GENERIC_ITEM), (line, list) -> {
                profits.forEach(result -> {
                    list.add(result.replacePlaceholders().apply(line));
                });
            })
            .send(player);
    }
}
