package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.Price;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.*;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class SellMenu extends LinkedMenu<ShopPlugin, SellMenu.Content> implements ConfigBased {

    private final VirtualShopModule module;
    private final boolean           simplified;

    private String priceEntry;

    private String       itemName;
    private List<String> itemLore;
    private int[]        itemSlots;

    public record Content(@NotNull List<ContentItem> items) {

        public void addItem(@NotNull ItemStack itemStack, @NotNull Price price) {
            this.items.add(new ContentItem(new ItemStack(itemStack), price));
        }

        @NotNull
        public ContentItem removeItem(int index) {
            return this.items.remove(index);
        }

        @NotNull
        public ContentItem getItem(int index) {
            return this.items.get(index);
        }

        @NotNull
        public Price price() {
            Price price = Price.create();
            this.items.forEach(contentItem -> price.add(contentItem.price));
            return price;
        }
    }

    public record ContentItem(@NotNull ItemStack itemStack, @NotNull Price price){}

    public SellMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Put items to sell"));
        this.module = module;
        this.simplified = VirtualConfig.SELL_MENU_SIMPLIFIED.get();

        this.load(FileConfig.loadOrExtract(plugin, module.getLocalPath(), "sell.menu.yml"));

        // We don't need decorative items in simplified menu without click event restrictions.
        if (this.simplified) {
            this.getItems().clear();
        }
    }

    public void open(@NotNull Player player) {
        this.open(player, new Content(new ArrayList<>()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        if (this.simplified) return;

        Player player = viewer.getPlayer();
        Content content = this.getLink(player);

        for (int index = 0; index < content.items().size(); index++) {
            if (index >= this.itemSlots.length) break;

            int slot = this.itemSlots[index];
            int finalIndex = index;

            ContentItem item = content.getItem(index);
            ItemStack itemStack = item.itemStack();

            VirtualProduct product = this.module.getBestProductFor(itemStack, TradeType.SELL, player);
            if (product == null) continue;

            viewer.addItem(NightItem.fromItemStack(itemStack)
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replacement(replacer -> replacer
                    .replace(ITEM_LORE, ItemUtil.getLoreSerialized(itemStack))
                    .replace(ITEM_NAME, ItemUtil.getNameSerialized(itemStack))
                    .replace(product.getShop().replacePlaceholders())
                    .replace(product.replacePlaceholders(player))
                    .replace(GENERIC_PRICE, () -> item.price().formatValues())
                )
                .toMenuItem()
                .setSlots(slot)
                .setHandler((viewer1, event) -> {
                    Players.addItem(player, itemStack);
                    content.removeItem(finalIndex);
                    this.runNextTick(() -> this.flush(viewer1));
                })
                .build()
            );
        }
    }

    @Override
    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
        if (this.simplified) return;

        super.onDrag(viewer, event);
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        if (this.simplified) return;

        super.onClick(viewer, result, event);

        Player player = viewer.getPlayer();
        ItemStack itemStack = result.getItemStack();
        if (itemStack == null || itemStack.getType().isAir()) return;

        if (result.isInventory()) {
            VirtualProduct product = this.module.getBestProductFor(itemStack, TradeType.SELL, player);
            if (product == null) return;

            Content content = this.getLink(player);
            if (content.items().size() >= this.itemSlots.length) return;

            int units = UnitUtils.amountToUnits(product, itemStack.getAmount());
            int sellAmount = UnitUtils.unitsToAmount(product, units);

            ItemStack sellStack = new ItemStack(itemStack);
            sellStack.setAmount(sellAmount);
            content.addItem(sellStack, Price.create().add(product.getCurrencyId(), product.getFinalSellPrice(player, units)));

            itemStack.setAmount(itemStack.getAmount() - sellAmount);

            this.runNextTick(() -> this.flush(viewer));
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Player player = viewer.getPlayer();

        if (this.simplified) {
            this.module.sellWithReturn(player, event.getInventory());
        }
        else {
            Content content = this.getLink(player);
            if (content != null) {
                content.items().forEach(item -> Players.addItem(player, item.itemStack()));
            }
        }

        super.onClose(viewer, event);
    }

    private void handleSell(@NotNull Player player) {
        Content content = this.getLink(player);

        Inventory inventory = this.plugin.getServer().createInventory(null, 54);
        inventory.addItem(content.items().stream().map(ContentItem::itemStack).toArray(ItemStack[]::new));
        content.items().clear();

        this.module.sellWithReturn(player, inventory);
        this.runNextTick(player::closeInventory);
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.itemName = ConfigValue.create("Item.Name", ITEM_NAME).read(config);

        this.itemLore = ConfigValue.create("Item.Lore", Lists.newList(
            ITEM_LORE,
            EMPTY_IF_ABOVE,
            GREEN.wrap(BOLD.wrap("Details:")),
            GREEN.wrap("➥ " + GRAY.wrap("Sell For: ") + GENERIC_PRICE),
            GREEN.wrap("➥ " + GRAY.wrap("Found In: ") + SHOP_NAME)
        )).read(config);

        this.itemSlots = ConfigValue.create("Item.Slots", IntStream.range(0, 45).toArray()).read(config);

        this.priceEntry = ConfigValue.create("Price.Entry",
            DARK_GRAY.wrap("» ") + WHITE.wrap(CURRENCY_NAME + ": ") + GOLD.wrap(GENERIC_AMOUNT)
        ).read(config);

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE)
            .setHideTooltip(true)
            .toMenuItem()
            .setSlots(IntStream.range(45, 54).toArray())
            .setPriority(-1)
        );

        loader.addDefaultItem(NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
            .setDisplayName(GREEN.and(BOLD).wrap("Sell All"))
            .setLore(Lists.newList(
                GRAY.wrap("Sell everything you put here!"),
                "",
                DARK_GRAY.wrap("[Total Income]"),
                GENERIC_PRICE,
                EMPTY_IF_ABOVE,
                GREEN.wrap("→ " + UNDERLINED.wrap("Click to sell"))
            ))
            .toMenuItem()
            .setPriority(10)
            .setSlots(49)
            .setHandler(new ItemHandler("sell", (viewer, event) -> this.handleSell(viewer.getPlayer()), ItemOptions.builder()
                .setVisibilityPolicy(viewer -> !this.getLink(viewer).items().isEmpty())
                .setDisplayModifier((viewer, nightItem) -> nightItem.replacement(replacer -> replacer
                    .replace(GENERIC_PRICE, this.getLink(viewer).price().formatted(this.priceEntry))
                ))
                .build()))
        );

        loader.addDefaultItem(NightItem.fromType(Material.RED_STAINED_GLASS_PANE)
            .setDisplayName(RED.and(BOLD).wrap("Sell All"))
            .setLore(Lists.newList(
                GRAY.wrap("Nothing to sell...")
            ))
            .toMenuItem()
            .setPriority(1)
            .setSlots(49)
        );
    }
}
