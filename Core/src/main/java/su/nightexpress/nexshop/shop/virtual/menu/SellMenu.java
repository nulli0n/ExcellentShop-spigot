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
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;

import java.util.*;
import java.util.stream.Collectors;

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

                this.sellWithReturn(player, inventory);
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
            StaticProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
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
            this.sellWithReturn(player, event.getInventory());
        }
        else {
            List<ItemStack> userItems = USER_ITEMS.remove(player);
            if (userItems != null) {
                userItems.forEach(item -> PlayerUtil.addItem(player, item));
            }
        }
    }

    public void sellWithReturn(@NotNull Player player, @NotNull Inventory inventory) {
        this.sellAll(player, inventory);

        for (ItemStack left : inventory.getContents()) {
            if (left == null || left.getType().isAir() || left.getAmount() < 1) continue;

            PlayerUtil.addItem(player, left);
        }
    }

    public void sellAll(@NotNull Player player) {
        this.sellAll(player, player.getInventory());
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory) {
        Map<Currency, Double> profitMap = new HashMap<>();
        Map<ItemStack, Transaction> resultMap = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;

            StaticProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) continue;

            VirtualPreparedProduct preparedProduct = product.getPrepared(player, TradeType.SELL, true);
            preparedProduct.setInventory(inventory);

            Transaction result = preparedProduct.trade();
            if (result.getResult() == Transaction.Result.SUCCESS) {
                Currency currency = result.getProduct().getCurrency();
                double has = profitMap.getOrDefault(currency, 0D) + result.getPrice();
                profitMap.put(currency, has);
                resultMap.put(item, result);
            }
        }
        if (profitMap.isEmpty()) return;

        String total = profitMap.entrySet().stream()
            .map(entry -> entry.getKey().format(entry.getValue()))
            .collect(Collectors.joining(", "));

        this.plugin.getMessage(VirtualLang.SELL_MENU_SALE_RESULT)
            .replace(Placeholders.GENERIC_TOTAL, total)
            .replace(str -> str.contains(Placeholders.GENERIC_ITEM), (str, list) -> {
                resultMap.forEach((item, result) -> {
                    list.add(str
                        .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                        .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(item.getAmount()))
                        .replace(Placeholders.GENERIC_PRICE, result.getProduct().getCurrency().format(result.getPrice()))
                    );
                });
            })
            .send(player);
    }
}
