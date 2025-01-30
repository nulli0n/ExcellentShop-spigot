package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.virtual.menu.LegacyShopEditor;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Pair;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ShopShowcaseMenu extends ShopEditorMenu implements Linked<ChestShop>, AutoFilled<Pair<String, ItemStack>>, LegacyShopEditor {

    public static final String FILE_NAME = "shop_showcase.yml";

    private final ChestShopModule     module;
    private final ViewLink<ChestShop> link;
    private final ItemHandler         returnHandler;

    private String       itemName;
    private List<String> itemLore;
    private int[]        itemSlots;

    public ShopShowcaseMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> module.openDisplayMenu(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.load();

        if (Config.GUI_PLACEHOLDER_API.get()) {
            this.getItems().forEach(menuItem -> {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    ItemReplacer.replacePlaceholderAPI(itemStack, viewer.getPlayer());
                });
            });
        }
    }

    @Override
    @NotNull
    public ViewLink<ChestShop> getLink() {
        return this.link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<Pair<String, ItemStack>> autoFill) {
        var map = ChestConfig.DISPLAY_PLAYER_CUSTOMIZATION_SHOWCASE_LIST.get();

        autoFill.setSlots(this.itemSlots);
        autoFill.setItems(map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList());
        autoFill.setItemCreator(pair -> {
            ItemStack icon = new ItemStack(pair.getSecond());
            ItemReplacer.create(icon).hideFlags().trimmed()
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replace(GENERIC_NAME, ItemUtil.getItemName(pair.getSecond()))
                .replacePlaceholderAPI(viewer.getPlayer())
                .writeMeta();
            return icon;
        });
        autoFill.setClickAction(pair -> (viewer1, event) -> {
            ChestShop shop = this.getLink(viewer1);
            shop.setShowcaseType(pair.getFirst());
            this.save(viewer1, shop);
            this.runNextTick(() -> this.module.openDisplayMenu(viewer1.getPlayer(), shop));
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Showcase Selection"), MenuSize.CHEST_36);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(13).setPriority(10).setHandler(this.returnHandler));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(27).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(35).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.itemName = ConfigValue.create("Item.Name", LIGHT_YELLOW.enclose(BOLD.enclose(GENERIC_NAME))).read(cfg);

        this.itemLore = ConfigValue.create("Item.Lore", Lists.newList(
            "",
            LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Click to " + LIGHT_YELLOW.enclose("select") + ".")
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Item.Slots", IntStream.range(0, 27).toArray()).read(cfg);
    }
}
