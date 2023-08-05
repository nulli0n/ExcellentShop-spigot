package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestPlayerBank;
import su.nightexpress.nexshop.shop.chest.util.ShopUtils;

import java.util.*;

public class BankMenu extends ConfigEditorMenu implements AutoPaged<Currency> {

    private static final String PLACEHOLDER_BANK_BALANCE = "%bank_balance%";
    private static final String PLACEHOLDER_PLAYER_BALANCE = "%player_balance%";

    private final ChestShopModule module;
    private final int[]        objectSlots;
    private final String       objectName;
    private final List<String> objectLore;

    private final Map<Player, UUID> others;

    public BankMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getLocalPath() + "/menu/", "shop_bank.yml"));
        this.module = module;
        this.others = new WeakHashMap<>();

        this.objectSlots = cfg.getIntArray("Currency.Slots");
        this.objectName = Colorizer.apply(cfg.getString("Currency.Name", ""));
        this.objectLore = Colorizer.apply(cfg.getStringList("Currency.Lore"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    public void open(@NotNull Player player, @NotNull UUID holder) {
        this.others.put(player, holder);
        this.open(player, 1);
    }

    @NotNull
    public UUID getHolder(@NotNull Player player) {
        return this.others.getOrDefault(player, player.getUniqueId());
    }

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
        return new ArrayList<>(ShopUtils.getAllowedCurrencies());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull Currency currency) {
        ItemStack icon = currency.getIcon();
        ItemUtil.mapMeta(icon, meta -> {
            meta.setDisplayName(this.objectName);
            meta.setLore(this.objectLore);
            ItemUtil.replace(meta, currency.replacePlaceholders());
            ItemUtil.replace(meta, str -> str
                .replace(PLACEHOLDER_PLAYER_BALANCE, currency.format(currency.getHandler().getBalance(player)))
                .replace(PLACEHOLDER_BANK_BALANCE, currency.format(module.getPlayerBank(this.getHolder(player)).getBalance(currency))));
        });
        return icon;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull Currency currency) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            UUID holder = this.getHolder(player);
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
