package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.GENERIC_BALANCE;
import static su.nightexpress.nexshop.Placeholders.GENERIC_SELL_MULTIPLIER;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class CentralMenu extends NormalMenu<ShopPlugin> implements ConfigBased {

    public static final String FILE_NAME = "main.menu.yml";
    private static final String TITLE_COLOR = "#3E3E3E";

    private final VirtualShopModule module;
    private final Currency currency;

    public CentralMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, HEX_COLOR.wrap("Shop → Main", TITLE_COLOR));
        this.module = module;
        this.currency = module.getDefaultCurrency();
        this.setApplyPlaceholderAPI(true);

        this.load(FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        // Do not apply on shop buttons.
        if (viewer.hasItem(menuItem)) return;

        item.replacement(replacer -> replacer
            .replace(GENERIC_BALANCE, () -> currency.format(currency.getBalance(viewer.getPlayer())))
            .replace(GENERIC_SELL_MULTIPLIER, () -> NumberUtil.format(VirtualShopModule.getSellMultiplier(viewer.getPlayer())))
            .replacePlaceholderAPI(viewer.getPlayer()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        int size = view.getTopInventory().getSize();

        this.module.getShops().forEach(shop -> {
            int slot = shop.getMainMenuSlot();
            if (slot < 0 || slot >= size) return;

            this.addItem(viewer, shop.getIcon()
                .setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get())
                .setLore(VirtualConfig.SHOP_FORMAT_LORE.get())
                .setHideComponents(true)
                .replacement(replacer -> replacer.replace(shop.replacePlaceholders()))
                .toMenuItem()
                .setSlots(slot)
                .setPriority(Integer.MAX_VALUE)
                .setHandler(ItemHandler.forClick((viewer1, event) -> {
                    this.runNextTick(() -> shop.open(viewer1.getPlayer()));
                }, ItemOptions.builder().setVisibilityPolicy(viewer1 -> {
                    return !VirtualConfig.MAIN_MENU_HIDE_NO_PERM_SHOPS.get() || shop.canAccess(viewer1.getPlayer(), false);
                }).build()))
            );
        });
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(0, 54).toArray()));

        loader.addDefaultItem(NightItem.asCustomHead("3324a7d61ccd44b031744b517f911a5c461614b953b17f648282e147b29d10e")
            .setDisplayName(HEX_COLOR.wrap(BOLD.wrap("BALANCE"), "#7cf1de"))
            .setLore(Lists.newList(
                LIGHT_GRAY.wrap("Here's displayed how much"),
                LIGHT_GRAY.wrap("money you have."),
                "",
                HEX_COLOR.wrap("➥", "#7cf1de") + " " + WHITE.wrap(GENERIC_BALANCE))
            )
            .toMenuItem().setSlots(37).setPriority(10)
        );

        loader.addDefaultItem(NightItem.asCustomHead("9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d")
            .setDisplayName(HEX_COLOR.wrap(BOLD.wrap("SELL ALL"), "#ebd12a"))
            .setLore(Lists.newList(
                LIGHT_GRAY.wrap("Sells everything from your"),
                LIGHT_GRAY.wrap("inventory to all available shops."),
                "",
                LIGHT_GRAY.wrap(HEX_COLOR.wrap("➥", "#ebd12a") + " Sell Multiplier: " + HEX_COLOR.wrap("x" + GENERIC_SELL_MULTIPLIER, "#ebd12a")),
                "",
                //LIGHT_GRAY.wrap(HEX_COLOR.wrap("[▶]", "#ebd12a") + " Click to " + HEX_COLOR.wrap("sell all", "#ebd12a") + ".")
                HEX_COLOR.wrap("→ " + BOLD.wrap(UNDERLINED.wrap("CLICK")) + " to sell", "#ebd12a")
            ))
            .toMenuItem().setSlots(43).setPriority(10).setHandler(new ItemHandler("sell_all", (viewer, event) -> {
                Player player = viewer.getPlayer();
                this.module.sellAll(player, player.getInventory());
            }))
        );
    }
}
