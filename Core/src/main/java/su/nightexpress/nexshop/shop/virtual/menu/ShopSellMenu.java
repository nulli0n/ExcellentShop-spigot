package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualPreparedProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.*;

public class ShopSellMenu extends AbstractMenu<ExcellentShop> {

    private final VirtualShopModule module;
    private final String       itemName;
    private final List<String> itemLore;
    private final int[] itemSlots;

    private static final Map<Player, List<Pair<ItemStack, VirtualProduct>>> USER_ITEMS = new WeakHashMap<>();

    public ShopSellMenu(@NotNull VirtualShopModule module, @NotNull JYML cfg) {
        super(module.plugin(), cfg, "");
        this.module = module;
        this.itemName = Colorizer.apply(cfg.getString("Item.Name", "%item_name%"));
        this.itemLore = Colorizer.apply(cfg.getStringList("Item.Lore"));
        this.itemSlots = cfg.getIntArray("Item.Slots");

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type1) {
                this.onItemClickDefault(player, type1);
            }
            else if (type instanceof ItemType type1) {
                if (type1 == ItemType.SELL) {
                    List<Pair<ItemStack, VirtualProduct>> userItems = USER_ITEMS.remove(player);
                    if (userItems != null && !userItems.isEmpty()) {
                        userItems.forEach(pair -> {
                            Collection<ItemStack> left = player.getInventory().addItem(pair.getFirst()).values();
                            if (left.isEmpty()) {
                                VirtualPreparedProduct preparedProduct = pair.getSecond().getPrepared(TradeType.SELL);
                                preparedProduct.setAmount(pair.getFirst().getAmount());
                                preparedProduct.sell(player, false);
                            }
                            else {
                                left.forEach(item -> PlayerUtil.addItem(player, item));
                            }
                        });
                        player.updateInventory();
                        plugin.getMessage(VirtualLang.SELL_MENU_SOLD).send(player);
                    }
                    player.closeInventory();
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, ItemType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    enum ItemType {
        SELL
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return true;

        if (slotType == SlotType.PLAYER) {
            VirtualProduct product = this.module.getBestProductFor(player, item, item.getAmount(), TradeType.SELL);
            if (product == null) return true;

            int firtSlot = Arrays.stream(this.itemSlots)
                .filter(slot -> { // Idea tells me there is possible NPE for a single line :/
                    ItemStack has = inventory.getItem(slot);
                    return has == null || has.getType().isAir();
                })
                .findFirst().orElse(-1);
            if (firtSlot < 0) return true;

            //int toSell = product.getStock().getPossibleAmount(TradeType.SELL, player);
            //if (toSell == 0) return true;
            //if (toSell > 0) toSell = item.getAmount();

            ItemStack icon = new ItemStack(item);
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) return true;

            double price = product.getPricer().getPriceSell() * item.getAmount();// toSell;

            List<String> lore = new ArrayList<>(this.itemLore);
            lore.replaceAll(line -> {
                line = product.getShop().replacePlaceholders().apply(line);
                line = product.replacePlaceholders().apply(line);
                return line
                    .replace(Placeholders.GENERIC_PRICE, product.getCurrency().format(price));
            });
            lore = StringUtil.replace(lore, "%item_lore%", false, ItemUtil.getLore(item));
            lore = StringUtil.stripEmpty(lore);

            meta.setDisplayName(this.itemName.replace("%item_name%", ItemUtil.getItemName(item)));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(firtSlot, icon);

            USER_ITEMS.computeIfAbsent(player, k -> new ArrayList<>()).add(Pair.of(new ItemStack(item), product));
            item.setAmount(0);
        }
        return true;
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        List<Pair<ItemStack, VirtualProduct>> userItems = USER_ITEMS.remove(player);
        if (userItems != null) {
            userItems.forEach(pair -> PlayerUtil.addItem(player, pair.getFirst()));
        }
    }
}
