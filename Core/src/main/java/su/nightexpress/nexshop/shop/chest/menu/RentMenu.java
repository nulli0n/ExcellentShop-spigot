package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class RentMenu extends LinkedMenu<ShopPlugin, ChestShop> implements ConfigBased {

    public static final String FILE = "shop_rent.yml";

    private final ChestShopModule module;

    public RentMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X4, BLACK.wrap("Rent Settings"));
        this.module = module;

        this.load(FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE));
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

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(MenuItem.buildReturn(this, 31, (viewer, event) -> {
            this.runNextTick(() -> module.openShopSettings(viewer.getPlayer(), this.getLink(viewer)));
        }));

        loader.addDefaultItem(NightItem.fromType(Material.LIME_DYE)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Enabled")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("● " + LIGHT_GRAY.wrap("Current: ") + RENT_ENABLED),
                "",
                LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap("➥") + " Click to " + LIGHT_YELLOW.wrap("toggle") + ".")
            ))
            .toMenuItem().setSlots(10).setPriority(10).setHandler(new ItemHandler("rent_enabled", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                shop.getRentSettings().setEnabled(!shop.getRentSettings().isEnabled());
                shop.saveSettings();
                this.module.remakeDisplay(shop);
                this.runNextTick(() -> this.flush(viewer));
            })));

        loader.addDefaultItem(NightItem.fromType(Material.CLOCK)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Duration")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("● " + LIGHT_GRAY.wrap("Current: ") + RENT_DURATION),
                "",
                LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap("➥") + " Click to " + LIGHT_YELLOW.wrap("change") + ".")
            ))
            .toMenuItem().setSlots(12).setPriority(10).setHandler(new ItemHandler("rent_duration", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                this.handleInput(Dialog.builder(viewer, ChestLang.RENT_PROMPT_DURATION, input -> {
                    int days = input.asInt(1);
                    shop.getRentSettings().setDuration(days, true);
                    shop.saveSettings();
                    return true;
                }));
            })));

        loader.addDefaultItem(NightItem.fromType(Material.MAP)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Currency")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("● " + LIGHT_GRAY.wrap("Current: ") + RENT_CURRENCY_NAME),
                "",
                LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap("➥") + " Click to " + LIGHT_YELLOW.wrap("change") + ".")
            ))
            .toMenuItem().setSlots(14).setPriority(10).setHandler(new ItemHandler("rent_currency", (viewer, event) -> {
                Player player = viewer.getPlayer();
                ChestShop shop = this.getLink(player);

                this.handleInput(Dialog.builder(player, ChestLang.RENT_PROMPT_CURRENCY, input -> {
                    Currency currency = EconomyBridge.getCurrency(input.getTextRaw());
                    if (currency == null) return false;

                    if (!this.module.isAllowedCurrency(currency, player)) {
                        ChestLang.ERROR_NO_PERMISSION.getMessage().send(player);
                        return false;
                    }

                    shop.getRentSettings().setCurrencyId(currency.getInternalId());
                    shop.getRentSettings().setPrice(shop.getRentSettings().getPrice(), true); // Limit price when currency changed.
                    shop.saveSettings();
                    return true;
                }).setSuggestions(module.getAllowedCurrencies(viewer.getPlayer()).stream().map(Currency::getInternalId).toList(), true));
            })));

        loader.addDefaultItem(NightItem.fromType(Material.DIAMOND)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Price")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.wrap("● " + LIGHT_GRAY.wrap("Current: ") + RENT_PRICE),
                "",
                LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap("➥") + " Click to " + LIGHT_YELLOW.wrap("change") + ".")
            ))
            .toMenuItem().setSlots(16).setPriority(10).setHandler(new ItemHandler("rent_price", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                this.handleInput(Dialog.builder(viewer, ChestLang.RENT_PROMPT_PRICE, input -> {
                    double price = input.asDoubleAbs(0D);
                    shop.getRentSettings().setPrice(price, true);
                    shop.saveSettings();
                    return true;
                }));
            })));


        loader.addDefaultItem(NightItem.fromType(Material.REDSTONE)
            .setDisplayName(RED.wrap(BOLD.wrap("Cancel Rent")))
            .setLore(Lists.newList(
                RED.wrap("● " + LIGHT_GRAY.wrap("Rented by: ") + CHEST_SHOP_RENTER_NAME),
                "",
                LIGHT_GRAY.wrap("Cancels current rent and revokes"),
                LIGHT_GRAY.wrap("renter's access to the shop."),
                "",
                LIGHT_GRAY.wrap(RED.wrap("➥") + " Click to " + RED.wrap("cancel") + ".")
            ))
            .toMenuItem().setSlots(4).setPriority(10).setHandler(new ItemHandler("rent_cancel", (viewer, event) -> {
                Player player = viewer.getPlayer();
                ChestShop shop = this.getLink(player);

                this.plugin.getShopManager().openConfirmation(player, Confirmation.create(
                    (viewer1, event1) -> {
                        shop.cancelRent();
                        shop.saveSettings();
                        this.module.remakeDisplay(shop);
                        this.runNextTick(() -> this.module.openRentSettings(player, shop));
                    },
                    (viewer1, event1) -> {
                        this.runNextTick(() -> this.module.openRentSettings(player, shop));
                    }
                ));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).isRented()).build())));
    }
}
