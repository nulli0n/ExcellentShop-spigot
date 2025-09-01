package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class PlayerBrowserMenu extends LinkedMenu<ShopPlugin, PlayerBrowserMenu.Data> implements Filled<OfflinePlayer>, ConfigBased {

    private static final LangEnum<SortType> SORT_LOCALE = LangEnum.of("Player.SortLocale", SortType.class);

    private final ChestShopModule module;

    private String       playerTitle;
    private String       playerName;
    private List<String> playerLore;
    private int[]        playerSlots;

    public record Data(String playerSearch, SortType sortType) {}

    private enum SortType {

        A_Z(Comparator.comparing(owner -> String.valueOf(owner.getName()))),
        Z_A(Comparator.comparing((OfflinePlayer owner) -> String.valueOf(owner.getName())).reversed())
        ;

        private final Comparator<OfflinePlayer> comparator;

        SortType(@NotNull Comparator<OfflinePlayer> comparator) {
            this.comparator = comparator;
        }

        @NotNull
        public Comparator<OfflinePlayer> getComparator() {
            return this.comparator;
        }
    }

    public PlayerBrowserMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Player Shops"));
        this.module = module;
    }

    public void open(@NotNull Player player) {
        this.open(player, (String) null);
    }

    public void open(@NotNull Player player, @Nullable String ownerName) {
        this.open(player, ownerName, SortType.A_Z);
    }

    private void open(@NotNull Player player, @Nullable String ownerName, @Nullable SortType sortType) {
        if (ownerName != null) ownerName = ownerName.toLowerCase();

        this.open(player, new Data(ownerName, sortType));
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        String search = data.playerSearch;

        return search != null ? this.playerTitle.replace(PLAYER_NAME, search) : super.getTitle(viewer);
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        item.replacement(replacer -> replacer.replace(Placeholders.GENERIC_TYPE, SORT_LOCALE.getLocalized(data.sortType)));
    }

    @Override
    @NotNull
    public MenuFiller<OfflinePlayer> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        List<OfflinePlayer> players = this.module.lookup().getAll().stream()
            .filter(ChestShop::isActive)
            .filter(ChestShop::hasProducts)
            .filter(shop -> data.playerSearch == null || shop.getOwnerName().toLowerCase().contains(data.playerSearch))
            .map(ChestShop::getOwner)
            .distinct()
            .sorted(data.sortType.getComparator())
            .toList();

        return MenuFiller.builder(this)
            .setSlots(this.playerSlots)
            .setItems(players)
            .setItemCreator(owner -> {
                return NightItem.fromType(Material.PLAYER_HEAD)
                    .setDisplayName(this.playerName)
                    .setLore(this.playerLore)
                    .setPlayerProfile(owner)
                    .replacement(replacer -> replacer
                        .replace(PLAYER_NAME, () -> String.valueOf(owner.getName()))
                        .replace(GENERIC_AMOUNT, () -> NumberUtil.format(this.module.lookup().getOwnedBy(owner.getUniqueId()).stream().filter(ChestShop::isActive).filter(ChestShop::hasProducts).count()))
                    );
            })
            .setItemClick(owner -> (viewer1, event) -> {
                String ownerName = owner.getName();
                if (ownerName == null) return;

                this.runNextTick(() -> this.module.browsePlayerShops(player, ownerName));
            })
            .build();
    }

    private void handleSearch(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        this.handleInput(Dialog.builder(player, input -> {
            // .runNextTick hack to override dialog GUI restoration.
            this.runNextTick(() -> this.open(player, input.getTextRaw(), data.sortType));
            return true;
        }));

        ChestLang.SEARCH_PROMPT_PLAYER_NAME.message().send(player);
    }

    private void handleSorting(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        SortType sortType = Lists.next(data.sortType);

        this.runNextTick(() -> this.open(player, data.playerSearch, sortType));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        SORT_LOCALE.load(config);

        this.playerTitle = ConfigValue.create("Title.PlayerSearch",
            BLACK.wrap("Shops found for '" + PLAYER_NAME + "'")
        ).read(config);

        this.playerName = ConfigValue.create("Player.Name",
            LIGHT_YELLOW.wrap(BOLD.wrap(PLAYER_NAME))
        ).read(config);

        this.playerLore = ConfigValue.create("Player.Lore", Lists.newList(
            LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + GRAY.wrap(" shops."),
            "",
            LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to browse"))
        )).read(config);

        this.playerSlots = ConfigValue.create("Player.Slots", new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34}).read(config);

        loader.addDefaultItem(NightItem.fromType(Material.RECOVERY_COMPASS)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Search Player")))
            .setLore(Lists.newList(
                GRAY.wrap("Search for shops owned by"),
                GRAY.wrap("specific player(s)."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to search"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(46)
            .setHandler(new ItemHandler("search_player", (viewer, event) -> this.handleSearch(viewer)))
        );

        loader.addDefaultItem(NightItem.fromType(Material.COMPARATOR)
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("Sorting Mode")))
            .setLore(Lists.newList(
                LIGHT_RED.wrap("➥ " + GRAY.wrap("Current: ") + GENERIC_TYPE),
                "",
                GRAY.wrap("Changes player display order."),
                "",
                LIGHT_RED.wrap("→ " + UNDERLINED.wrap("Click to toggle"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(52)
            .setHandler(new ItemHandler("sorting", (viewer, event) -> this.handleSorting(viewer)))
        );

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(45, 54).toArray()));

        loader.addDefaultItem(MenuItem.buildExit(this, 49).setPriority(10));
        loader.addDefaultItem(MenuItem.buildNextPage(this, 26));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 18));
    }
}
