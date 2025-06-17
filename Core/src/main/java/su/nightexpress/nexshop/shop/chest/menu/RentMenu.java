package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class RentMenu extends LinkedMenu<ShopPlugin, ChestShop> implements ConfigBased {

    private final ChestShopModule module;

    public RentMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X4, BLACK.wrap("Rent Settings"));
        this.module = module;
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        ChestShop shop = this.getLink(viewer);

        item.replacement(replacer -> replacer.replace(shop.replacePlaceholders()).replace(shop.getRentSettings().replacePlaceholder()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    private void handleToggle(@NotNull MenuViewer viewer, boolean state) {
        ChestShop shop = this.getLink(viewer);
        shop.getRentSettings().setEnabled(state);
        shop.setSaveRequired(true);
        this.module.getDisplayManager().remake(shop);
        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleDuration(@NotNull MenuViewer viewer) {
        ChestShop shop = this.getLink(viewer);
        this.handleInput(Dialog.builder(viewer, ChestLang.RENT_PROMPT_DURATION, input -> {
            int days = input.asInt(1);
            shop.getRentSettings().setDuration(days, true);
            shop.setSaveRequired(true);
            return true;
        }));
    }

    private void handlePrice(@NotNull MenuViewer viewer) {
        ChestShop shop = this.getLink(viewer);
        this.handleInput(Dialog.builder(viewer, ChestLang.RENT_PROMPT_PRICE, input -> {
            double price = input.asDoubleAbs(0D);
            shop.getRentSettings().setPrice(price, true);
            shop.setSaveRequired(true);
            return true;
        }));
    }

    private void handleCurrency(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);
        List<Currency> currencies = new ArrayList<>(this.module.getAvailableCurrencies(player));
        if (currencies.size() <= 1) return;

        int index = currencies.indexOf(shop.getRentSettings().getCurrency()) + 1;
        if (index >= currencies.size()) index = 0;

        shop.getRentSettings().setCurrencyId(currencies.get(index).getInternalId());
        shop.getRentSettings().setPrice(shop.getRentSettings().getPrice(), true); // Limit price when currency changed.
        shop.setSaveRequired(true);

        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleCancel(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);

        UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer1, event1) -> this.module.cancelRent(viewer.getPlayer(), shop))
            .onReturn((viewer1, event1) -> this.runNextTick(() -> this.module.openRentSettings(player, shop)))
            .returnOnAccept(true)
            .build()
        );
    }

    private void handleReturn(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openShopSettings(viewer.getPlayer(), this.getLink(viewer)));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(NightItem.fromType(Material.LIME_DYE)
            .setDisplayName(GREEN.wrap(BOLD.wrap("Rent Enabled")))
            .setLore(Lists.newList(
                GRAY.wrap("Other players " + GREEN.wrap("can") + " rent this shop."),
                "",
                GREEN.wrap("→ " + UNDERLINED.wrap("Click to disable"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(11)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_off", (viewer, event) -> this.handleToggle(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).getRentSettings().isEnabled())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.GRAY_DYE)
            .setDisplayName(RED.wrap(BOLD.wrap("Rent Disabled")))
            .setLore(Lists.newList(
                GRAY.wrap("Other players " + RED.wrap("can't") + " rent this shop."),
                "",
                RED.wrap("→ " + UNDERLINED.wrap("Click to enable"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(11)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_on", (viewer, event) -> this.handleToggle(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> !this.getLink(viewer).getRentSettings().isEnabled())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.CLOCK)
            .setDisplayName(YELLOW.wrap(BOLD.wrap("Duration")))
            .setLore(Lists.newList(
                YELLOW.wrap("➥ " + GRAY.wrap("Current: ") + RENT_DURATION),
                "",
                GRAY.wrap("Initial rent duration when"),
                GRAY.wrap("player rents a shop."),
                "",
                YELLOW.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(13)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_duration", (viewer, event) -> this.handleDuration(viewer))));

        loader.addDefaultItem(NightItem.fromType(Material.GOLD_NUGGET)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Price")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("➥ " + GRAY.wrap("Current: ") + RENT_PRICE),
                "",
                GRAY.wrap("Price for players to rent"),
                GRAY.wrap("this shop."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(15)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_price", (viewer, event) -> this.handlePrice(viewer))));

        loader.addDefaultItem(NightItem.fromType(Material.EMERALD)
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("Currency")))
            .setLore(Lists.newList(
                LIGHT_GREEN.wrap("➥ " + GRAY.wrap("Current: ") + RENT_CURRENCY_NAME),
                "",
                GRAY.wrap("Set currency used to"),
                GRAY.wrap("rent this shop."),
                "",
                LIGHT_GREEN.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(16)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_currency", (viewer, event) -> this.handleCurrency(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.module.getAvailableCurrencies(viewer.getPlayer()).size() > 1)
                    .build())));


        loader.addDefaultItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(RED.wrap(BOLD.wrap("Cancel Rent")))
            .setLore(Lists.newList(
                RED.wrap("➥ " + GRAY.wrap("Rented by: ") + CHEST_SHOP_RENTER_NAME),
                RED.wrap("➥ " + GRAY.wrap("Expires in: ") + CHEST_SHOP_RENT_EXPIRES_IN),
                "",
                GRAY.wrap("Cancel current rent and revoke"),
                GRAY.wrap("renter's access to the shop."),
                "",
                RED.wrap("→ " + UNDERLINED.wrap("Click to cancel"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setSlots(35)
            .setPriority(10)
            .setHandler(new ItemHandler("rent_cancel", (viewer, event) -> this.handleCancel(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).isRented())
                    .build()
            )));

        loader.addDefaultItem(MenuItem.buildReturn(this, 31, (viewer, event) -> this.handleReturn(viewer)).setPriority(10));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(27, 36).toArray()));

        // Compatibility with old configs
        loader.addHandler(new ItemHandler("rent_enabled", (viewer, event) -> {
            this.handleToggle(viewer, !this.getLink(viewer).getRentSettings().isEnabled());
        }));
    }
}
