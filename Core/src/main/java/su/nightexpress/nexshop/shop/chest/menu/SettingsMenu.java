package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class SettingsMenu extends LinkedMenu<ShopPlugin, ChestShop> implements ConfigBased {

    private final ChestShopModule module;

    public SettingsMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap(SHOP_NAME));
        this.module = module;
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        return this.getLink(viewer).replacePlaceholders().apply(this.title);
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        Player player = viewer.getPlayer();

        item.replacement(replacer -> replacer
            .replace(this.getLink(viewer).replacePlaceholders())
            .replace(GENERIC_MAX_PRODUCTS, () -> ShopUtils.formatOrInfinite(ChestUtils.getProductLimit(player)))
            .replace(GENERIC_MAX_SHOPS, () -> ShopUtils.formatOrInfinite(ChestUtils.getShopLimit(player)))
            .replace(GENERIC_SHOPS_AMOUNT, () -> ShopUtils.formatOrInfinite(this.module.countShops(player)))
        );
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    private void handleName(@NotNull MenuViewer viewer) {
        ChestShop shop = this.getLink(viewer);
        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, input -> {
            return this.module.renameShop(viewer.getPlayer(), shop, input.getText());
        }));
    }

    private void handleProducts(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openProductsMenu(viewer.getPlayer(), this.getLink(viewer)));
    }

    private void handleShops(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.browseShopsByShop(viewer.getPlayer(), this.getLink(viewer)));
    }

    private void handleBank(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openBank(viewer.getPlayer(), this.getLink(viewer)));
    }

    private void handleDisplay(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openShowcaseMenu(viewer.getPlayer(), this.getLink(viewer)));
    }

    private void handleRentSettings(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openRentSettings(viewer.getPlayer(), this.getLink(viewer)));
    }

    private void handleRentExtend(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);

        this.runNextTick(() -> UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer1, event1) -> this.module.rentShopOrExtend(viewer.getPlayer(), shop))
            .onReturn((viewer1, event1) -> this.runNextTick(() -> this.module.openShopSettings(player, shop)))
            .returnOnAccept(true)
            .build()
        ));
    }

    private void handleAdminShop(@NotNull MenuViewer viewer, boolean toAdmin) {
        ChestShop shop = this.getLink(viewer);
        shop.setAdminShop(toAdmin);
        shop.updateStockCache();
        shop.setSaveRequired(true);
        this.module.getDisplayManager().remake(shop);
        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleRemove(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);

        UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer1, event) -> {
                this.module.deleteShop(player, shop);
                this.plugin.runTask(task -> player.closeInventory());
            })
            .onReturn((viewer1, event) -> {
                this.plugin.runTask(task -> this.module.openShopSettings(player, shop));
            })
            .returnOnAccept(false)
            .build());
    }

    private void handleRentCancel(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);

        UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer1, event) -> {
                this.module.cancelRent(player, shop);
                this.plugin.runTask(task -> player.closeInventory());
            })
            .onReturn((viewer1, event) -> {
                this.plugin.runTask(task -> this.module.openShopSettings(player, shop));
            })
            .returnOnAccept(false)
            .build());
    }

    private void handleBuyingMode(@NotNull MenuViewer viewer, @NotNull TradeType type) {
        ChestShop shop = this.getLink(viewer);
        if (type == TradeType.SELL) {
            shop.setSellingAllowed(!shop.isSellingAllowed());
        }
        else {
            shop.setBuyingAllowed(!shop.isBuyingAllowed());
        }
        shop.setSaveRequired(true);
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(NightItem.fromType(Material.NAME_TAG)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Shop Name")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("➥ " + GRAY.wrap("Current: ") + SHOP_NAME),
                "",
                GRAY.wrap("The name the players see"),
                GRAY.wrap("above the shop and in GUI."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(10)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_change_name", (viewer, event) -> this.handleName(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).canRename(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.ITEM_FRAME)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Products")))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "You have " + LIGHT_YELLOW.wrap(SHOP_PRODUCTS) + "/" + LIGHT_YELLOW.wrap(GENERIC_MAX_PRODUCTS) + " products."),
                "",
                GRAY.wrap("Edit prices, add items and"),
                GRAY.wrap("restock them here."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(13)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_change_products", (viewer, event) -> this.handleProducts(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).canManageProducts(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.CHEST)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("My Shops")))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "You own " + LIGHT_YELLOW.wrap(GENERIC_SHOPS_AMOUNT) + "/" + LIGHT_YELLOW.wrap(GENERIC_MAX_SHOPS) + " shops."),
                "",
                GRAY.wrap("View all shops you rented/created."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(16)
            .setPriority(10)
            .setHandler(new ItemHandler("list_shops", (viewer, event) -> this.handleShops(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).isOwner(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.CHEST)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap(CHEST_SHOP_OWNER + "'s Shops")))
            .setLore(Lists.newList(
                GRAY.wrap("View all shops created"),
                GRAY.wrap("by " + LIGHT_YELLOW.wrap(CHEST_SHOP_OWNER) + "."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(16)
            .setPriority(10)
            .setHandler(new ItemHandler("list_owner_shops", (viewer, event) -> this.handleShops(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> !this.getLink(viewer).isOwner(viewer.getPlayer()))
                    .build()
            )));



        loader.addDefaultItem(NightItem.fromType(Material.EMERALD)
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("Bank")))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_GREEN.wrap("➥ ") + "Balance: " + LIGHT_GREEN.wrap(CHEST_SHOP_BANK_BALANCE)),
                "",
                GRAY.wrap("Deposit funds to bank to"),
                GRAY.wrap(LIGHT_RED.wrap("purchase items") + " from players."),
                "",
                GRAY.wrap("Withdraw funds from bank gained"),
                GRAY.wrap("from " + LIGHT_GREEN.wrap("items sold") + " to players."),
                "",
                LIGHT_GREEN.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(28)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_bank", (viewer, event) -> this.handleBank(viewer),
                ItemOptions.builder()
                .setVisibilityPolicy(viewer -> !ChestConfig.isAutoBankEnabled() && this.getLink(viewer).canManageBank(viewer.getPlayer()))
                .build()
            )));


        loader.addDefaultItem(NightItem.fromType(Material.BEACON)
            .setDisplayName(LIGHT_CYAN.wrap(BOLD.wrap("Shop Display")))
            .setLore(Lists.newList(
                LIGHT_CYAN.wrap("➥ " + GRAY.wrap("Hologram: ") + CHEST_SHOP_HOLOGRAM_ENABLED),
                LIGHT_CYAN.wrap("➥ " + GRAY.wrap("Showcase: ") + CHEST_SHOP_SHOWCASE_ENABLED),
                "",
                GRAY.wrap("Toggle shop hologram and"),
                GRAY.wrap("customize showcase color."),
                "",
                LIGHT_CYAN.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(31)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_display", (viewer, event) -> this.handleDisplay(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).canManageDisplay(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.JUNGLE_SIGN)
            .setDisplayName(LIGHT_ORANGE.wrap(BOLD.wrap("Rent Settings")))
            .setLore(Lists.newList(
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("Renter: ") + CHEST_SHOP_RENTER_NAME),
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("Expires in: ") + CHEST_SHOP_RENT_EXPIRES_IN),
                "",
                GRAY.wrap("Make your shop rentable by other"),
                GRAY.wrap("players, set rent price and duration."),
                "",
                LIGHT_ORANGE.wrap("→ " + UNDERLINED.wrap("Click to open"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(34)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_rent_settings", (viewer, event) -> this.handleRentSettings(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> ChestConfig.isRentEnabled() && this.getLink(viewer).canManageRent(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.GOLD_NUGGET)
            .setDisplayName(YELLOW.wrap(BOLD.wrap("Extend Rent")))
            .setLore(Lists.newList(
                YELLOW.wrap("➥ " + GRAY.wrap("Expires in: ") + CHEST_SHOP_RENT_EXPIRES_IN),
                YELLOW.wrap("➥ " + GRAY.wrap("Period: ") + CHEST_SHOP_RENT_DURATION),
                YELLOW.wrap("➥ " + GRAY.wrap("Price: ") + CHEST_SHOP_RENT_PRICE),
                "",
                GRAY.wrap("Extend shop rent to continue"),
                GRAY.wrap("your business here."),
                "",
                YELLOW.wrap("→ " + UNDERLINED.wrap("Click to extend"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(34)
            .setPriority(15)
            .setHandler(new ItemHandler("shop_rent_extend", (viewer, event) -> this.handleRentExtend(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> ChestConfig.isRentEnabled() && this.getLink(viewer).isRenter(viewer.getPlayer()))
                    .build()
            )));



        loader.addDefaultItem(NightItem.fromType(Material.CAMPFIRE)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Player Shop")))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_YELLOW.wrap("✔ ") + "This is " + LIGHT_YELLOW.wrap("player shop") + "."),
                "",
                GRAY.wrap("Player shops have limited funds"),
                GRAY.wrap("and resources, and have to be"),
                GRAY.wrap("managed by player(s)."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to make admin shop"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(45)
            .setPriority(10)
            .setHandler(new ItemHandler("set_admin_shop", (viewer, event) -> this.handleAdminShop(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.ADMIN_SHOP) && !this.getLink(viewer).isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.SOUL_CAMPFIRE)
            .setDisplayName(CYAN.wrap(BOLD.wrap("Admin Shop")))
            .setLore(Lists.newList(
                GRAY.wrap(CYAN.wrap("✔ ") + "This is " + CYAN.wrap("admin shop") + "."),
                "",
                GRAY.wrap("Admin shops have unlimited funds"),
                GRAY.wrap("and resources, and can function"),
                GRAY.wrap("on its own."),
                "",
                CYAN.wrap("→ " + UNDERLINED.wrap("Click to make player shop"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(45)
            .setPriority(10)
            .setHandler(new ItemHandler("set_player_shop", (viewer, event) -> this.handleAdminShop(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.ADMIN_SHOP) && this.getLink(viewer).isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(RED.wrap(BOLD.wrap("Remove Shop")))
            .setLore(Lists.newList(
                GRAY.wrap("Permanently removes the shop."),
                "",
                RED.wrap("→ " + UNDERLINED.wrap("Click to remove"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(53)
            .setPriority(10)
            .setHandler(new ItemHandler("shop_delete", (viewer, event) -> this.handleRemove(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).canRemove(viewer.getPlayer()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(RED.wrap(BOLD.wrap("Cancel Rent")))
            .setLore(Lists.newList(
                RED.wrap("➥ " + GRAY.wrap("Expires in: ") + CHEST_SHOP_RENT_EXPIRES_IN),
                "",
                GRAY.wrap("Cancel your rent of this shop."),
                "",
                RED.wrap("→ " + UNDERLINED.wrap("Click to cancel rent"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(53)
            .setPriority(15)
            .setHandler(new ItemHandler("rent_cancel", (viewer, event) -> this.handleRentCancel(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).isRenter(viewer.getPlayer()))
                    .build()
            )));


        loader.addDefaultItem(MenuItem.buildExit(this, 49).setPriority(10));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(45, 54).toArray()));

        // Compatibility for previous version configs.
        loader.addHandler(new ItemHandler("shop_buying", (viewer, event) -> this.handleBuyingMode(viewer, TradeType.BUY)));
        loader.addHandler(new ItemHandler("shop_selling", (viewer, event) -> this.handleBuyingMode(viewer, TradeType.SELL)));
        loader.addHandler(new ItemHandler("shop_change_type", (viewer, event) -> this.handleAdminShop(viewer, !this.getLink(viewer).isAdminShop()),
            ItemOptions.builder()
                .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.ADMIN_SHOP))
                .build()
        ));

        loader.addHandler(new ItemHandler("shop_storage", (viewer, event) -> {
            this.runNextTick(() -> module.openProductsMenu(viewer.getPlayer(), this.getLink(viewer)));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> ChestUtils.isInfiniteStorage() && !this.getLink(viewer).isAdminShop()).build()));
    }
}
