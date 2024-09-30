package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
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
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.shop.chest.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class BankMenu extends ShopEditorMenu implements AutoFilled<Currency>, Linked<BankMenu.Info> {

    public static final  String FILE_NAME                  = "shop_bank.yml";
    private static final String PLACEHOLDER_BANK_BALANCE   = "%bank_balance%";
    private static final String PLACEHOLDER_PLAYER_BALANCE = "%player_balance%";

    private final ChestShopModule module;
    private final ItemHandler     returnHandler;
    private final ViewLink<Info>  link;

    private int[]        objectSlots;
    private String       objectName;
    private List<String> objectLore;

    public record Info(@Nullable ChestShop shop, @NotNull UUID playerId) {}

    public BankMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            Info info = this.getLink(viewer);
            if (info.shop != null && info.shop.isActive()) {
                this.runNextTick(() -> module.openShopSettings(viewer.getPlayer(), info.shop));
            }
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getHandler() == this.returnHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).shop != null);
            }
            // Added for currency PAPI support.
            menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                ItemReplacer.replacePlaceholderAPI(itemStack, viewer.getPlayer());
            });
        });
    }

    @NotNull
    @Override
    public ViewLink<Info> getLink() {
        return link;
    }

    public void open(@NotNull Player player, @NotNull ChestShop holder) {
        Info info = new Info(holder, holder.getOwnerId());
        this.open(player, info);
    }

    public void open(@NotNull Player player, @NotNull UUID holder) {
        Info info = new Info(null, holder);
        this.open(player, info);
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        
    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<Currency> autoFill) {
        Player player = viewer.getPlayer();
        Info info = this.getLink(player);

        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(this.module.getAllowedCurrencies(player));
        autoFill.setItemCreator(currency -> {
            ItemStack icon = currency.getIcon();
            ItemReplacer.create(icon).hideFlags().trimmed()
                .setDisplayName(this.objectName)
                .setLore(this.objectLore)
                .replace(currency.getPlaceholders())
                .replace(PLACEHOLDER_PLAYER_BALANCE, currency.format(currency.getHandler().getBalance(player)))
                .replace(PLACEHOLDER_BANK_BALANCE, currency.format(module.getPlayerBank(info.playerId).getBalance(currency)))
                .replacePlaceholderAPI(player)
                .writeMeta();
            return icon;
        });
        autoFill.setClickAction(currency -> (viewer1, event) -> {
            UUID holder = info.playerId;
            ChestBank bank = this.module.getPlayerBank(holder);

            if (event.getClick() == ClickType.DROP) {
                this.module.depositToBank(player, holder, currency, currency.getHandler().getBalance(player));
                this.module.savePlayerBank(bank);
                this.runNextTick(() -> this.flush(viewer));
                return;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                this.module.withdrawFromBank(player, holder, currency, bank.getBalance(currency));
                this.module.savePlayerBank(bank);
                this.runNextTick(() -> this.flush(viewer));
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                String msg = input.getTextRaw();
                Type type = Type.UNSPECIFIED;
                if (!Players.isBedrock(player)) {
                    if (event.isLeftClick()) type = Type.DEPOSIT;
                    else if (event.isRightClick()) type = Type.WITHDRAW;
                }

                if (type == Type.UNSPECIFIED) {
                    if (msg.startsWith("+")) type = Type.DEPOSIT;
                    else if (msg.startsWith("-")) type = Type.WITHDRAW;
                    else return false;

                    msg = msg.substring(1);
                }

                double amount = NumberUtil.getDouble(msg, 0D);
                if (amount == 0D) {
                    dialog.error(Lang.EDITOR_INPUT_ERROR_GENERIC.getMessage());
                    return false;
                }

                if (type == Type.DEPOSIT) {
                    this.module.depositToBank(player, holder, currency, amount);
                }
                else {
                    this.module.withdrawFromBank(player, holder, currency, amount);
                }
                return true;
            });
        });
    }

    private enum Type {
        UNSPECIFIED,
        DEPOSIT,
        WITHDRAW,
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Shop Bank"), MenuSize.CHEST_18);
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
        list.add(new MenuItem(prevPage).setSlots(9).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(17).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    // TODO Add extra menu for all 4 actions (bedrock players)
    @Override
    protected void loadAdditional() {
        this.objectSlots = ConfigValue.create("Currency.Slots", IntStream.range(0, 9).toArray()).read(cfg);

        this.objectName = ConfigValue.create("Currency.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(CURRENCY_NAME)) + " " + LIGHT_GRAY.enclose("(ID: " + WHITE.enclose(CURRENCY_ID) + ")")
        ).read(cfg);

        this.objectLore = ConfigValue.create("Currency.Lore", Lists.newList(
            " ",
            LIGHT_YELLOW.enclose(BOLD.enclose("Details:")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Bank Balance: ") + PLACEHOLDER_BANK_BALANCE),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Your Balance: ") + PLACEHOLDER_PLAYER_BALANCE),
            "",
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Left-Click to " + LIGHT_GREEN.enclose("deposit") + "."),
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Right-Click to " + LIGHT_GREEN.enclose("withdraw") + "."),
            LIGHT_GRAY.enclose(LIGHT_RED.enclose("[▶]") + " [Q/Drop] Key to " + LIGHT_RED.enclose("deposit all") + "."),
            LIGHT_GRAY.enclose(LIGHT_RED.enclose("[▶]") + " [F/Swap] Key to " + LIGHT_RED.enclose("withdraw all") + ".")
        )).read(cfg);
    }
}
