package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestPlayerBank;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.*;

public class BankMenu extends ConfigEditorMenu implements AutoPaged<Currency>, Linked<BankMenu.Info> {

    private static final String PLACEHOLDER_BANK_BALANCE = "%bank_balance%";
    private static final String PLACEHOLDER_PLAYER_BALANCE = "%player_balance%";

    private final ChestShopModule module;
    private final int[]        objectSlots;
    private final String       objectName;
    private final List<String> objectLore;

    private final ViewLink<Info> link;

    public static class Info {

        public Info(@Nullable ChestShop shop, @Nullable UUID playerId) {
            this.holder = shop;
            this.playerId = playerId;
        }

        public ChestShop holder;
        public UUID playerId;

    }

    public BankMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getLocalPath() + "/menu/", "shop_bank.yml"));
        this.module = module;
        this.link = new ViewLink<>();
        //this.others = new WeakHashMap<>();

        this.objectSlots = cfg.getIntArray("Currency.Slots");
        this.objectName = Colorizer.apply(cfg.getString("Currency.Name", ""));
        this.objectLore = Colorizer.apply(cfg.getStringList("Currency.Lore"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.RETURN, (viewer, event) -> {
                Info info = this.getInfo(viewer.getPlayer());
                if (info.holder != null) {
                    info.holder.openMenu(viewer.getPlayer());
                }
            })
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getType() == MenuItemType.RETURN) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> this.getInfo(viewer.getPlayer()).holder != null);
            }
        });
    }

    @NotNull
    @Override
    public ViewLink<Info> getLink() {
        return link;
    }

    public void open(@NotNull Player player, @NotNull ChestShop holder) {
        Info info = new Info(holder, holder.getOwnerId());
        this.open(player, info, 1);
    }

    public void open(@NotNull Player player, @NotNull UUID holder) {
        Info info = new Info(null, holder);
        this.open(player, info, 1);

        //this.others.put(player, holder);
        //this.open(player, 1);
    }

    @NotNull
    public Info getInfo(@NotNull Player player) {
        return this.getLink().get(player);
    }

    /*@NotNull
    public UUID getHolder(@NotNull Player player) {
        return this.others.getOrDefault(player, player.getUniqueId());
    }*/

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    private enum Type {
        UNSPECIFIED,
        DEPOSIT,
        WITHDRAW,
    }

    @Override
    public int[] getObjectSlots() {
        return this.objectSlots;
    }

    @Override
    @NotNull
    public List<Currency> getObjects(@NotNull Player player) {
        return new ArrayList<>(ChestUtils.getAllowedCurrencies());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull Currency currency) {
        Info info = this.getInfo(player);
        ItemStack icon = currency.getIcon();
        ItemReplacer.create(icon).hideFlags().trimmed()
            .setDisplayName(this.objectName)
            .setLore(this.objectLore)
            .replace(currency.getPlaceholders())
            .replace(PLACEHOLDER_PLAYER_BALANCE, currency.format(currency.getHandler().getBalance(player)))
            .replace(PLACEHOLDER_BANK_BALANCE, currency.format(module.getPlayerBank(info.playerId).getBalance(currency)))
            .writeMeta();
        return icon;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull Currency currency) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            Info info = this.getInfo(player);
            UUID holder = info.playerId;//this.getHolder(player);
            ChestPlayerBank bank = this.module.getPlayerBank(holder);

            if (event.getClick() == ClickType.DROP) {
                this.module.depositToBank(player, holder, currency, currency.getHandler().getBalance(player));
                this.module.savePlayerBank(bank);
                this.openNextTick(player, viewer.getPage());
                return;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                this.module.withdrawFromBank(player, holder, currency, bank.getBalance(currency));
                this.module.savePlayerBank(bank);
                this.openNextTick(player, viewer.getPage());
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, wrapper -> {
                String msg = wrapper.getTextRaw();
                Type type = Type.UNSPECIFIED;
                if (!PlayerUtil.isBedrockPlayer(player)) {
                    if (event.isLeftClick()) type = Type.DEPOSIT;
                    else if (event.isRightClick()) type = Type.WITHDRAW;
                }

                if (type == Type.UNSPECIFIED) {
                    if (msg.startsWith("+")) type = Type.DEPOSIT;
                    else if (msg.startsWith("-")) type = Type.WITHDRAW;
                    else return false;

                    msg = msg.substring(1);
                }

                double amount = StringUtil.getDouble(msg, 0, false);
                if (amount == 0D) {
                    EditorManager.error(player, plugin.getMessage(Lang.EDITOR_ERROR_NUMBER_GENERIC).getLocalized());
                    return false;
                }

                boolean result;
                if (type == Type.DEPOSIT) {
                    result = this.module.depositToBank(player, holder, currency, amount);
                }
                else {
                    result = this.module.withdrawFromBank(player, holder, currency, amount);
                }

                this.module.savePlayerBank(bank);
                return result;
            });
        };
    }
}
