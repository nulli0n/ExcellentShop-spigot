package su.nightexpress.nexshop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.auction.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class CurrencySelectMenu extends ConfigMenu<ShopPlugin> implements AutoFilled<Currency>, Linked<Pair<ItemStack, Double>> {

    public static final String FILE_NAME = "currency_selector.yml";

    private final AuctionManager auctionManager;
    private final ViewLink<Pair<ItemStack, Double>> link;

    private String       itemName;
    private List<String> itemLore;
    private int[]        itemSlots;

    public CurrencySelectMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin, FileConfig.loadOrExtract(plugin, auctionManager.getMenusPath(), FILE_NAME));
        this.auctionManager = auctionManager;
        this.link = new ViewLink<>();

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<Pair<ItemStack, Double>> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<Currency> autoFill) {
        Player player = viewer.getPlayer();

        autoFill.setSlots(this.itemSlots);
        autoFill.setItems(this.auctionManager.getAllowedCurrencies(player).stream().sorted(Comparator.comparing(Currency::getInternalId)).toList());
        autoFill.setItemCreator(currency -> {
            ItemStack item = currency.getIcon();
            Pair<ItemStack, Double> prepared = this.getLink(player);
            if (prepared == null) return item;

            double price = prepared.getSecond();
            double taxAmount = AuctionUtils.getSellTax(player);
            double taxPay = AuctionUtils.getTax(currency, price, taxAmount);

            ItemReplacer.create(item).trimmed().hideFlags()
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replace(currency.replacePlaceholders())
                .replace(GENERIC_PRICE, currency.format(price))
                .replace(GENERIC_TAX, currency.format(taxPay))
                .replacePlaceholderAPI(player)
                .writeMeta();
            return item;
        });
        autoFill.setClickAction(currency -> (viewer1, event) -> {
            Pair<ItemStack, Double> prepared = this.getLink(player);
            if (prepared != null && this.auctionManager.add(player, prepared.getFirst(), currency, prepared.getSecond()) != null) {
                this.getLink().clear(viewer);
            }
            this.runNextTick(player::closeInventory);
        });
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Pair<ItemStack, Double> prepared = this.getLink(viewer);
        if (prepared != null) {
            Players.addItem(viewer.getPlayer(), prepared.getFirst());
        }

        super.onClose(viewer, event);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Select a currency"), MenuSize.CHEST_27);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_WRONG_MARK);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("Cancel")));
        });
        list.add(new MenuItem(backItem).setSlots(22).setPriority(10).setHandler(ItemHandler.forClose(this)));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(9).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(17).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.itemName = ConfigValue.create("Items.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(Placeholders.CURRENCY_NAME))
        ).read(cfg);

        this.itemLore = ConfigValue.create("Items.Lore", Lists.newList(
            LIGHT_GRAY.enclose("Sell item for " + LIGHT_YELLOW.enclose(GENERIC_PRICE) + "."),
            "",
            LIGHT_GRAY.enclose(LIGHT_RED.enclose("[❗]") + " Tax: " + LIGHT_RED.enclose(GENERIC_TAX))
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Items.Slots", IntStream.range(0, 18).toArray()).read(cfg);
    }
}
