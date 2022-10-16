package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.data.Pair;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.AuctionUtils;
import su.nightexpress.nexshop.shop.auction.Placeholders;

import java.util.*;

public class AuctionCurrencySelectorMenu extends AbstractMenuAuto<ExcellentShop, ICurrency> {

    private final AuctionManager auctionManager;
    private final int[]          objectSlots;
    private final String         itemName;
    private final List<String>   itemLore;

    private static final Map<Player, Pair<ItemStack, Double>> PREPARED_ITEM = new WeakHashMap<>();

    public AuctionCurrencySelectorMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg, "");
        this.auctionManager = auctionManager;
        this.itemName = StringUtil.color(cfg.getString("Items.Name", Placeholders.LISTING_ITEM_NAME));
        this.itemLore = StringUtil.color(cfg.getStringList("Items.Lore"));
        this.objectSlots = cfg.getIntArray("Items.Slots");

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

    public void open(@NotNull Player player, @NotNull ItemStack item, double price) {
        PREPARED_ITEM.put(player, Pair.of(item, price));
        this.open(player, 1);
    }

    @Nullable
    private Pair<ItemStack, Double> getPrepared(@NotNull Player player) {
        return PREPARED_ITEM.get(player);
    }

    @Override
    protected int[] getObjectSlots() {
        return this.objectSlots;
    }

    @Override
    @NotNull
    protected List<ICurrency> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.auctionManager.getCurrencies(player));
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ICurrency currency) {
        ItemStack item = currency.getIcon();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Pair<ItemStack, Double> prepared = this.getPrepared(player);
        if (prepared == null) return item;

        double price = prepared.getSecond();
        double tax = AuctionUtils.calculateTax(price, AuctionConfig.LISTINGS_TAX_ON_LISTING_ADD);

        meta.setDisplayName(this.itemName);
        meta.setLore(this.itemLore);
        item.setItemMeta(meta);

        ItemUtil.replace(item, currency.replacePlaceholders());
        ItemUtil.replace(item, str -> str
            .replace(Placeholders.GENERIC_PRICE, currency.format(price))
            .replace(Placeholders.GENERIC_TAX, currency.format(tax))
        );
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull ICurrency currency) {
        return (player2, type, e) -> {
            Pair<ItemStack, Double> prepared = this.getPrepared(player2);
            if (prepared == null) {
                player2.closeInventory();
                return;
            }

            if (this.auctionManager.add(player2, prepared.getFirst(), currency, prepared.getSecond())) {
                PREPARED_ITEM.remove(player2);
            }
            player2.closeInventory();
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        Pair<ItemStack, Double> prepared = PREPARED_ITEM.remove(player);
        if (prepared != null) {
            PlayerUtil.addItem(player, prepared.getFirst());
        }
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
