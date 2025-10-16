package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.impl.Showcase;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.LangUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShowcaseMenu extends LinkedMenu<ShopPlugin, ChestShop> implements Filled<Showcase>, ConfigBased {

    private final ChestShopModule     module;

    private NightItem showcaseDisabled;
    private List<String> showcaseLore;
    private int[]        showcaseSlots;

    public ShowcaseMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Display Settings"));
        this.module = module;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    public MenuFiller<Showcase> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);
        String id = shop.getShowcaseId();

        return MenuFiller.builder(this)
            .setSlots(this.showcaseSlots)
            .setItems(ChestConfig.SHOWCASE_CATALOG.get().values().stream()
                .filter(showcase -> !showcase.getId().equalsIgnoreCase(id))
                .sorted(Comparator.comparing(showcase -> BukkitThing.getValue(showcase.getDisplayItem().getMaterial())))
                .toList())
            .setItemCreator(showcase -> {
                NightItem item = shop.isShowcaseEnabled() ? showcase.getDisplayItem().setLore(this.showcaseLore).setDisplayName(showcase.getName()) : this.showcaseDisabled.copy();

                return item
                    .hideAllComponents()
                    .replacement(replacer -> replacer
                        .replace(Placeholders.GENERIC_TYPE, LangUtil.getSerializedName(showcase.getDisplayItem().getMaterial()))
                    );
            })
            .setItemClick(showcase -> (viewer1, event) -> {
                if (!shop.isShowcaseEnabled()) return;

                this.handleChange(viewer1, shop1 -> shop1.setShowcaseId(showcase.getId()));
                this.runNextTick(() -> this.flush(viewer1));
            })
            .build();
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    private void handleHologram(@NotNull MenuViewer viewer) {
        this.handleChange(viewer, shop -> shop.setHologramEnabled(!shop.isHologramEnabled()));
    }

    private void handleShowcaseToggle(@NotNull MenuViewer viewer) {
        this.handleChange(viewer, shop -> shop.setShowcaseEnabled(!shop.isShowcaseEnabled()));
    }

    private void handleShowcaseReset(@NotNull MenuViewer viewer) {
        this.handleChange(viewer, shop -> shop.setShowcaseId(null));
    }

    private void handleChange(@NotNull MenuViewer viewer, @NotNull Consumer<ChestShop> consumer) {
        ChestShop shop = this.getLink(viewer);
        consumer.accept(shop);
        this.module.getDisplayManager().remake(shop);

        shop.markDirty();
        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleReturn(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openShopSettings(viewer.getPlayer(), this.getLink(viewer)));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.showcaseDisabled = ConfigValue.create("Showcase.Disabled", NightItem.fromType(Material.GRAY_DYE)
            .setDisplayName(DARK_GRAY.wrap(GENERIC_TYPE))
            .setLore(Lists.newList(
                GRAY.wrap("Enable showcase display to"),
                GRAY.wrap("select showcase variant.")
            ))
        ).read(config);

        this.showcaseLore = ConfigValue.create("Showcase.Lore", Lists.newList(
            WHITE.wrap("→ " + UNDERLINED.wrap("Click to select"))
        )).read(config);

        this.showcaseSlots = ConfigValue.create("Showcase.Slots", new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34}).read(config);


        loader.addDefaultItem(NightItem.fromType(Material.ARMOR_STAND)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Hologram")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("➥ " + GRAY.wrap("Enabled: ") + CHEST_SHOP_HOLOGRAM_ENABLED),
                "",
                GRAY.wrap("Adds hologram above the shop"),
                GRAY.wrap("displaying shop name and prices"),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to enable"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(47)
            .setPriority(10)
            .setHandler(new ItemHandler("hologram_toggle", (viewer, event) -> this.handleHologram(viewer))));

        loader.addDefaultItem(NightItem.fromType(Material.BEACON)
            .setDisplayName(LIGHT_CYAN.wrap(BOLD.wrap("Showcase")))
            .setLore(Lists.newList(
                LIGHT_CYAN.wrap("➥ " + GRAY.wrap("Enabled: ") + CHEST_SHOP_SHOWCASE_ENABLED),
                "",
                GRAY.wrap("Adds showcase for shop items"),
                GRAY.wrap("displayed on top of the shop."),
                "",
                LIGHT_CYAN.wrap("→ " + UNDERLINED.wrap("Click to enable"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(51)
            .setPriority(10)
            .setHandler(new ItemHandler("showcase_toggle", (viewer, event) -> this.handleShowcaseToggle(viewer))));

        loader.addDefaultItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(RED.wrap(BOLD.wrap("Reset Showcase")))
            .setLore(Lists.newList(
                GRAY.wrap("Reset showcase to its default"),
                GRAY.wrap("model based on shop type."),
                "",
                RED.wrap("→ " + UNDERLINED.wrap("Click to reset"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(53)
            .setPriority(10)
            .setHandler(new ItemHandler("showcase_reset", (viewer, event) -> this.handleShowcaseReset(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasCustomShowcase())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(45, 54).toArray()));

        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> this.handleReturn(viewer)).setPriority(10));

        loader.addDefaultItem(MenuItem.buildNextPage(this, 26));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 18));
    }
}
