package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.Placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AuctionCurrencyFilterMenu extends AbstractMenuAuto<ExcellentShop, ICurrency> {

    private final AuctionManager auctionManager;
    private final int[]          objectSlots;
    private final String       itemName;
    private final List<String> itemLore;
    private final ItemStack selectedIcon;

    public AuctionCurrencyFilterMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg, "");
        this.auctionManager = auctionManager;
        this.itemName = Colorizer.apply(cfg.getString("Items.Name", Placeholders.CURRENCY_NAME));
        this.itemLore = Colorizer.apply(cfg.getStringList("Items.Lore"));
        this.objectSlots = cfg.getIntArray("Items.Slots");
        this.selectedIcon = cfg.getItem("Selected");

        MenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN || type2 == MenuItemType.CONFIRMATION_DECLINE) {
                    this.auctionManager.getMainMenu().open(player, 1);
                }
                else if (type2 == MenuItemType.CONFIRMATION_ACCEPT) {
                    this.auctionManager.getMainMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    protected int[] getObjectSlots() {
        return this.objectSlots;
    }

    @Override
    @NotNull
    protected List<ICurrency> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.auctionManager.getCurrencies());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ICurrency currency) {
        Set<ICurrency> currencies = AuctionMainMenu.getCurrencies(player);
        boolean isSelected = currencies.contains(currency);

        ItemStack item = isSelected ? new ItemStack(this.selectedIcon) : currency.getIcon();
        ItemMeta meta = item.getItemMeta();
        if (meta != null && !isSelected) {
            meta.setDisplayName(this.itemName);
            meta.setLore(this.itemLore);
            item.setItemMeta(meta);
        }

        ItemUtil.replace(item, currency.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ICurrency currency) {
        return (player2, type, e) -> {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            Set<ICurrency> categories = AuctionMainMenu.getCurrencies(player);
            if (categories.add(currency) || categories.remove(currency)) {
                this.open(player2, this.getPage(player2));
            }
        };
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
