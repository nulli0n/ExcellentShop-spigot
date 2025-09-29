package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.List;
import java.util.UUID;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class BankMenu extends LinkedMenu<ShopPlugin, BankMenu.Data> implements Filled<Currency>, ConfigBased {

    private static final String BANK_BALANCE   = "%bank_balance%";
    private static final String PLAYER_BALANCE = "%player_balance%";

    private final ChestShopModule module;

    private int[] currencySlots;
    private int[] highlightSlots;

    private NightItem highlightIcon;

    private String       currencyName;
    private List<String> currencyLore;

    public record Data(@NotNull UUID bankHolder, @Nullable ChestShop shop, @Nullable Currency currency, int index) {}

    public BankMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Shop Bank"));
        this.module = module;
    }

    public void open(@NotNull Player player, @NotNull ChestShop holder) {
        this.open(player, holder.getOwnerId(), holder, null, -1);
    }

    public void open(@NotNull Player player, @NotNull UUID holder) {
        this.open(player, holder, null, null, -1);
    }

    private void open(@NotNull Player player, @NotNull UUID holder, @Nullable ChestShop shop, @Nullable Currency currency, int index) {
        this.open(player, new Data(holder, shop, currency, index));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        int slotIndex = data.index;

        if (slotIndex >= 0 && slotIndex < this.highlightSlots.length) {
            int highlightSlot = this.highlightSlots[slotIndex];
            this.addItem(viewer, this.highlightIcon.copy().toMenuItem().setSlots(highlightSlot).setPriority(10));
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        
    }

    @Override
    @NotNull
    public MenuFiller<Currency> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        return MenuFiller.builder(this)
            .setSlots(this.currencySlots)
            .setItems(this.module.getAvailableCurrencies(player))
            .setItemCreator(currency -> {
                return NightItem.fromItemStack(currency.getIcon())
                    .hideAllComponents()
                    .setDisplayName(this.currencyName)
                    .setLore(this.currencyLore)
                    .replacement(replacer -> replacer
                        .replace(currency.replacePlaceholders())
                        .replace(PLAYER_BALANCE, () -> currency.format(currency.getBalance(player)))
                        .replace(BANK_BALANCE, () -> currency.format(this.module.getPlayerBank(data.bankHolder).getBalance(currency)))
                    );
            })
            .setItemClick(currency -> (viewer1, event) -> {
                int index = Lists.indexOf(this.currencySlots, event.getRawSlot());
                this.runNextTick(() -> this.open(player, data.bankHolder, data.shop, currency, index));
            })
            .build();
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        Currency currency = data.currency;
        if (currency == null) return;

        item.replacement(replacer -> replacer
            .replace(PLAYER_BALANCE, () -> currency.format(currency.getBalance(player)))
            .replace(BANK_BALANCE, () -> currency.format(this.module.getPlayerBank(data.bankHolder).getBalance(currency)))
        );
    }

    private void handleReturn(@NotNull MenuViewer viewer) {
        Data data = this.getLink(viewer);
        if (data.shop != null && data.shop.isActive()) {
            this.runNextTick(() -> module.openShopSettings(viewer.getPlayer(), data.shop));
        }
    }

    private void handleDeposit(@NotNull MenuViewer viewer, boolean all) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        UUID holder = data.bankHolder;
        ChestBank bank = this.module.getPlayerBank(holder);
        Currency currency = data.currency;
        if (currency == null) return;

        if (all) {
            this.module.depositToBank(player, holder, currency, currency.getBalance(player));
            this.module.savePlayerBank(bank);
            this.runNextTick(() -> this.flush(viewer));
            return;
        }

        this.handleInput(Dialog.builder(player, input -> {
            double amount = input.asDoubleAbs();
            this.module.depositToBank(player, holder, currency, amount);
            return true;
        }).setPrompt(Lang.EDITOR_GENERIC_ENTER_AMOUNT.text()));
    }

    private void handleWithdraw(@NotNull MenuViewer viewer, boolean all) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        UUID holder = data.bankHolder;
        ChestBank bank = this.module.getPlayerBank(holder);
        Currency currency = data.currency;
        if (currency == null) return;

        if (all) {
            this.module.withdrawFromBank(player, holder, currency, bank.getBalance(currency));
            this.module.savePlayerBank(bank);
            this.runNextTick(() -> this.flush(viewer));
            return;
        }

        this.handleInput(Dialog.builder(player, input -> {
            double amount = input.asDoubleAbs();
            this.module.withdrawFromBank(player, holder, currency, amount);
            return true;
        }).setPrompt(Lang.EDITOR_GENERIC_ENTER_AMOUNT.text()));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.currencySlots = ConfigValue.create("Currency.ItemSlots", new int[]{0,9,18,27,36}).read(config);
        this.highlightSlots = ConfigValue.create("Currency.HighlightSlots", new int[]{1,10,19,28,37}).read(config);

        this.highlightIcon = ConfigValue.create("Currency.Icon.Highlight", NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
            .setDisplayName(GREEN.wrap("← " + BOLD.wrap("Selected Currency")))
            .hideAllComponents()
        ).read(config);

        this.currencyName = ConfigValue.create("Currency.Name",
            Placeholders.CURRENCY_NAME
        ).read(config);

        this.currencyLore = ConfigValue.create("Currency.Lore", Lists.newList(
            LIGHT_YELLOW.wrap("➥ " + GRAY.wrap("In Bank: ") + BANK_BALANCE),
            LIGHT_YELLOW.wrap("➥ " + GRAY.wrap("On Hand: ") + PLAYER_BALANCE)
        )).read(config);

        loader.addDefaultItem(MenuItem.buildExit(this, 49).setPriority(1));

        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> this.handleReturn(viewer),
            ItemOptions.builder()
                .setVisibilityPolicy(viewer -> this.getLink(viewer).shop != null)
                .build()
        ).setPriority(10).build());

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(1,10,19,28,37,45,46,47,48,49,50,51,52,53));
        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(0,9,18,27,36));

        loader.addDefaultItem(NightItem.fromType(Version.isAtLeast(Version.MC_1_21_4) ? Material.LIGHT_BLUE_BUNDLE : Material.BUNDLE)
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Deposit")) + " " + GRAY.wrap("[" + WHITE.wrap("Custom Amount") + "]"))
            .setLore(Lists.newList(
                LIGHT_BLUE.wrap("➥ " + GRAY.wrap("In Bank: ") + BANK_BALANCE),
                LIGHT_BLUE.wrap("➥ " + GRAY.wrap("On Hand: ") + PLAYER_BALANCE),
                "",
                GRAY.wrap("Deposit " + LIGHT_BLUE.wrap("desired amount")),
                GRAY.wrap("to the shop bank."),
                "",
                LIGHT_BLUE.wrap("→ " + UNDERLINED.wrap("Click to deposit"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(13)
            .setHandler(new ItemHandler("deposit_custom", (viewer, event) -> this.handleDeposit(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).currency != null)
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.WATER_BUCKET)
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Deposit")) + " " + GRAY.wrap("[" + WHITE.wrap("All") + "]"))
            .setLore(Lists.newList(
                LIGHT_BLUE.wrap("➥ " + GRAY.wrap("In Bank: ") + BANK_BALANCE),
                LIGHT_BLUE.wrap("➥ " + GRAY.wrap("On Hand: ") + PLAYER_BALANCE),
                "",
                GRAY.wrap("Deposit " + LIGHT_BLUE.wrap("all your") + " currency"),
                GRAY.wrap("to the shop bank."),
                "",
                LIGHT_BLUE.wrap("→ " + UNDERLINED.wrap("Click to deposit"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(14)
            .setHandler(new ItemHandler("deposit_all", (viewer, event) -> this.handleDeposit(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).currency != null)
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Version.isAtLeast(Version.MC_1_21_4) ? Material.ORANGE_BUNDLE : Material.BUNDLE)
            .setDisplayName(LIGHT_ORANGE.wrap(BOLD.wrap("Withdraw")) + " " + GRAY.wrap("[" + WHITE.wrap("Custom Amount") + "]"))
            .setLore(Lists.newList(
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("In Bank: ") + BANK_BALANCE),
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("On Hand: ") + PLAYER_BALANCE),
                "",
                GRAY.wrap("Withdraw " + LIGHT_ORANGE.wrap("desired amount") + " of the"),
                GRAY.wrap("currency to your balance."),
                "",
                LIGHT_ORANGE.wrap("→ " + UNDERLINED.wrap("Click to withdraw"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(31)
            .setHandler(new ItemHandler("withdraw_custom", (viewer, event) -> this.handleWithdraw(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).currency != null)
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.LAVA_BUCKET)
            .setDisplayName(LIGHT_ORANGE.wrap(BOLD.wrap("Withdraw")) + " " + GRAY.wrap("[" + WHITE.wrap("All") + "]"))
            .setLore(Lists.newList(
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("In Bank: ") + BANK_BALANCE),
                LIGHT_ORANGE.wrap("➥ " + GRAY.wrap("On Hand: ") + PLAYER_BALANCE),
                "",
                GRAY.wrap("Withdraw " + LIGHT_ORANGE.wrap("all") + " currency"),
                GRAY.wrap("from the bank."),
                "",
                LIGHT_ORANGE.wrap("→ " + UNDERLINED.wrap("Click to withdraw"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(32)
            .setHandler(new ItemHandler("withdraw_all", (viewer, event) -> this.handleWithdraw(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).currency != null)
                    .build()
            )));
    }
}
