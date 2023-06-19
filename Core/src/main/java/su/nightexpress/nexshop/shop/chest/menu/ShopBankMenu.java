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
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.List;

public class ShopBankMenu extends PlayerEditorMenu implements AutoPaged<Currency> {

    private static final String PLACEHOLDER_BANK_BALANCE = "%bank_balance%";
    private static final String PLACEHOLDER_PLAYER_BALANCE = "%player_balance%";

    private final ChestShop shop;

    private final int[]        objectSlots;
    private final String       objectName;
    private final List<String> objectLore;

    public ShopBankMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getLocalPath() + "/menu/", "shop_bank.yml"));
        this.shop = shop;

        this.objectSlots = cfg.getIntArray("Currency.Slots");
        this.objectName = Colorizer.apply(cfg.getString("Currency.Name", ""));
        this.objectLore = Colorizer.apply(cfg.getStringList("Currency.Lore"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.RETURN, (viewer, event) -> this.shop.getEditor().openNextTick(viewer, 1))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
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
        return ChestShopModule.ALLOWED_CURRENCIES.stream().toList();
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
                .replace(PLACEHOLDER_BANK_BALANCE, currency.format(shop.getBank().getBalance(currency))));
        });
        return icon;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull Currency currency) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();

            if (event.getClick() == ClickType.DROP) {
                this.shop.getModule().depositToShop(player, shop, currency, currency.getHandler().getBalance(player));
                this.shop.save();
                this.openNextTick(player, viewer.getPage());
                return;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                this.shop.getModule().withdrawFromShop(player, shop, currency, this.shop.getBank().getBalance(currency));
                this.shop.save();
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
                    result = shop.getModule().depositToShop(player, shop, currency, amount);
                }
                else {
                    result = shop.getModule().withdrawFromShop(player, shop, currency, amount);
                }

                this.shop.save();
                return result;
            });
        };
    }
}
