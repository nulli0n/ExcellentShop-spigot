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
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionCategory;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.Placeholders;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AuctionCategoryFilterMenu extends AbstractMenuAuto<ExcellentShop, AuctionCategory> {

    private final AuctionManager auctionManager;
    private final int[]          objectSlots;
    private final String       itemName;
    private final List<String> itemLore;
    private final ItemStack selectedIcon;

    public AuctionCategoryFilterMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg, "");
        this.auctionManager = auctionManager;
        this.itemName = StringUtil.color(cfg.getString("Items.Name", Placeholders.LISTING_ITEM_NAME));
        this.itemLore = StringUtil.color(cfg.getStringList("Items.Lore"));
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
    protected List<AuctionCategory> getObjects(@NotNull Player player) {
        return new ArrayList<>(AuctionConfig.getCategories());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull AuctionCategory category) {
        Set<AuctionCategory> categories = AuctionMainMenu.getCategories(player);
        boolean isSelected = categories.contains(category);

        ItemStack item = isSelected ? new ItemStack(this.selectedIcon) : category.getIcon();
        ItemMeta meta = item.getItemMeta();
        if (meta != null && !isSelected) {
            meta.setDisplayName(this.itemName);
            meta.setLore(this.itemLore);
            item.setItemMeta(meta);
        }

        ItemUtil.replace(item, category.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull AuctionCategory category) {
        return (player2, type, e) -> {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            Set<AuctionCategory> categories = AuctionMainMenu.getCategories(player2);
            if (categories.add(category) || categories.remove(category)) {
                this.open(player2, this.getPage(player2));
            }
        };
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
