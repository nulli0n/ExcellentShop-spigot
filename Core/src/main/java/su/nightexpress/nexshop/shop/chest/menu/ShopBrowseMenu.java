package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.dialog.Dialog;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.shop.chest.Placeholders.*;

public class ShopBrowseMenu extends ConfigMenu<ShopPlugin> implements AutoFilled<OfflinePlayer> {

    public static final String FILE_NAME = "shop_browse.yml";

    private final ChestShopModule module;
    private final ItemHandler     searchHandler;

    private String       objectName;
    private List<String> objectLore;
    private int[]        objectSlots;

    public ShopBrowseMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;

        this.addHandler(this.searchHandler = new ItemHandler("search", (viewer, event) -> {
            Player player = viewer.getPlayer();

            ChestLang.SEARCH_ENTER_ITEM.getMessage().send(player);
            this.runNextTick(player::closeInventory);
            Dialog.create(player, (dialog, input) -> {
                this.module.searchShops(player, input.getTextRaw());
                return true;
            }).setLastMenu(null);
        }));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<OfflinePlayer> autoFill) {
        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(this.module.getActiveShops().stream().map(ChestShop::getOwner).distinct()
                .sorted(Comparator.comparing(owner -> String.valueOf(owner.getName()))).toList());
        autoFill.setItemCreator(owner -> {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);

            ItemUtil.editMeta(item, meta -> {
                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(owner);
                }
            });

            ItemReplacer.create(item).hideFlags().trimmed()
                .setDisplayName(this.objectName).setLore(this.objectLore)
                .replace(PLAYER_NAME, String.valueOf(owner.getName()))
                .replace(GENERIC_AMOUNT, NumberUtil.format(this.module.getShops(owner.getUniqueId()).size()))
                .writeMeta();

            return item;
        });
        autoFill.setClickAction(owner -> (viewer1, event) -> {
            String name = owner.getName();
            if (name == null) return;

            this.runNextTick(() -> this.module.listShops(viewer1.getPlayer(), name));
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Player Shops"), MenuSize.CHEST_45);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack searchItem = ItemUtil.getSkinHead("2e17702e859a8b23c85636287bf33e18aa1e283906aef1374fc65ed6d276c197");
        ItemUtil.editMeta(searchItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Search")));
            meta.setLore(Lists.newList(LIGHT_GRAY.enclose("Click to search shops by item.")));
        });
        list.add(new MenuItem(searchItem).setSlots(40).setPriority(10).setHandler(this.searchHandler));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(39).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(41).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.objectName = ConfigValue.create("Player.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(PLAYER_NAME)) + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_AMOUNT) + " shops)")
        ).read(cfg);

        this.objectLore = ConfigValue.create("Player.Lore", Lists.newList(
            LIGHT_GRAY.enclose("Click to browse all"),
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(PLAYER_NAME) + "'s shops.")
        )).read(cfg);

        this.objectSlots = ConfigValue.create("Player.Slots", IntStream.range(0, 36).toArray()).read(cfg);
    }
}
