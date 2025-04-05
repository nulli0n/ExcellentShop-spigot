package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.SKIN_ARROW_DOWN;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopDisplayMenu extends ShopEditorMenu implements Linked<ChestShop> {

    public static final String FILE_NAME = "shop_display.yml";

    private final ViewLink<ChestShop> link;

    private final ItemHandler returnHandler;
    private final ItemHandler hologramToggleHandler;
    private final ItemHandler showcaseToggleHandler;
    private final ItemHandler showcaseChangeHandler;

    public ShopDisplayMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> module.openShopSettings(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addHandler(this.hologramToggleHandler = new ItemHandler("hologram_toggle", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            shop.setHologramEnabled(!shop.isHologramEnabled());
            module.manageDisplay(displayHandler -> displayHandler.remake(shop));

            this.saveAndFlush(viewer, shop);
        }));

        this.addHandler(this.showcaseToggleHandler = new ItemHandler("showcase_toggle", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            shop.setShowcaseEnabled(!shop.isShowcaseEnabled());
            module.manageDisplay(displayHandler -> displayHandler.remake(shop));

            this.saveAndFlush(viewer, shop);
        }));

        this.addHandler(this.showcaseChangeHandler = new ItemHandler("showcase_change", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            if (event.isRightClick()) {
                shop.setShowcaseType(null);
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.runNextTick(() -> module.openShowcaseMenu(viewer.getPlayer(), shop));
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            ItemHandler handler = menuItem.getHandler();
            if (handler == this.showcaseChangeHandler) {
                menuItem.getOptions().addDisplayModifier(((viewer, itemStack) -> {
                    ItemStack showcase = ChestUtils.getCustomShowcaseOrDefault(this.getLink(viewer));
                    if (showcase == null) return;

                    itemStack.setType(showcase.getType());
                }));
            }

            menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                ItemReplacer.create(itemStack).readMeta().trimmed()
                    .replace(this.getLink(viewer).replacePlaceholders())
                    .replacePlaceholderAPI(viewer.getPlayer())
                    .writeMeta();
            });
        });
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        shop.saveSettings();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    @NotNull
    public ViewLink<ChestShop> getLink() {
        return this.link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Display Settings"), MenuSize.CHEST_36);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(ChestLang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(31).setPriority(10).setHandler(this.returnHandler));


        ItemStack holoToggleItem = new ItemStack(Material.ARMOR_STAND);
        ItemUtil.editMeta(holoToggleItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Toggle Hologram")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Enabled: ") + su.nightexpress.nexshop.Placeholders.CHEST_SHOP_HOLOGRAM_ENABLED),
                "",
                LIGHT_GRAY.enclose("Enables holographic text above shop."),
                "",
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Click to " + LIGHT_YELLOW.enclose("toggle") + ".")
            ));
        });
        list.add(new MenuItem(holoToggleItem).setSlots(11).setPriority(10).setHandler(this.hologramToggleHandler));


        ItemStack caseToggleItem = new ItemStack(Material.BEACON);
        ItemUtil.editMeta(caseToggleItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Toggle Showcase")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Enabled: ") + su.nightexpress.nexshop.Placeholders.CHEST_SHOP_SHOWCASE_ENABLED),
                "",
                LIGHT_GRAY.enclose("Enables product showcase."),
                "",
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Click to " + LIGHT_YELLOW.enclose("toggle") + ".")
            ));
        });
        list.add(new MenuItem(caseToggleItem).setSlots(13).setPriority(10).setHandler(this.showcaseToggleHandler));


        ItemStack caseChangeItem = new ItemStack(Material.WHITE_STAINED_GLASS);
        ItemUtil.editMeta(caseChangeItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Showcase Model")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Select a showcase for your shop!"),
                "",
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Left-Click to " + LIGHT_YELLOW.enclose("navigate") + "."),
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Right-Click to " + LIGHT_YELLOW.enclose("reset") + ".")
            ));
        });
        list.add(new MenuItem(caseChangeItem).setSlots(15).setPriority(10).setHandler(this.showcaseChangeHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {

    }
}
