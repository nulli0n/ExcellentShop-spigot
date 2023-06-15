package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.List;

public class ShopBankMenu extends AbstractMenuAuto<ExcellentShop, ICurrency> {

    private static final String PLACEHOLDER_BANK_BALANCE = "%bank_balance%";
    private static final String PLACEHOLDER_PLAYER_BALANCE = "%player_balance%";

    private final ChestShop shop;

    private final int[]        objectSlots;
    private final String       objectName;
    private final List<String> objectLore;

    public ShopBankMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getPath() + "menu/shop_bank.yml"), "");
        this.shop = shop;

        this.objectSlots = cfg.getIntArray("Currency.Slots");
        this.objectName = Colorizer.apply(cfg.getString("Currency.Name", ""));
        this.objectLore = Colorizer.apply(cfg.getStringList("Currency.Lore"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.shop.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum Type {
        UNSPECIFIED,
        DEPOSIT,
        WITHDRAW,
    }

    @Override
    protected int[] getObjectSlots() {
        return this.objectSlots;
    }

    @Override
    @NotNull
    protected List<ICurrency> getObjects(@NotNull Player player) {
        return ChestShopModule.ALLOWED_CURRENCIES.stream().toList();
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ICurrency currency) {
        ItemStack icon = currency.getIcon();
        ItemUtil.mapMeta(icon, meta -> {
            meta.setDisplayName(this.objectName);
            meta.setLore(this.objectLore);
            ItemUtil.replace(meta, currency.replacePlaceholders());
            ItemUtil.replace(meta, str -> str
                .replace(PLACEHOLDER_PLAYER_BALANCE, currency.format(currency.getBalance(player)))
                .replace(PLACEHOLDER_BANK_BALANCE, currency.format(shop.getBank().getBalance(currency))));
        });
        return icon;
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ICurrency currency) {
        return (player2, type, e) -> {

            Type type2 = Type.UNSPECIFIED;
            if (!PlayerUtil.isBedrockPlayer(player)) {
                if (e.isLeftClick()) type2 = Type.DEPOSIT;
                else if (e.isRightClick()) type2 = Type.WITHDRAW;
            }

            if (e.getClick() == ClickType.DROP) {
                this.shop.getModule().depositToShop(player2, shop, currency, currency.getBalance(player2));
                this.shop.save();
                this.open(player2, this.getPage(player2));
                return;
            }
            if (e.getClick() == ClickType.SWAP_OFFHAND) {
                this.shop.getModule().withdrawFromShop(player2, shop, currency, this.shop.getBank().getBalance(currency));
                this.shop.save();
                this.open(player2, this.getPage(player2));
                return;
            }

            EditorInput<ICurrency, Type> input = (player3, shop2, type3, e2) -> {
                String msg = Colorizer.strip(e2.getMessage());
                if (type3 == Type.UNSPECIFIED && PlayerUtil.isBedrockPlayer(player3)) {
                    if (msg.startsWith("+")) type3 = Type.DEPOSIT;
                    else if (msg.startsWith("-")) type3 = Type.WITHDRAW;
                    else return false;

                    msg = msg.substring(1);
                }

                double amount = StringUtil.getDouble(msg, 0, false);
                if (amount == 0D) {
                    EditorManager.error(player3, plugin.getMessage(Lang.EDITOR_ERROR_NUMBER_GENERIC).getLocalized());
                    return false;
                }

                boolean result;
                if (type3 == Type.DEPOSIT) {
                    result = shop.getModule().depositToShop(player3, shop, currency, amount);
                }
                else {
                    result = shop.getModule().withdrawFromShop(player3, shop, currency, amount);
                }

                this.shop.save();
                return result;
            };

            EditorManager.startEdit(player, currency, type2, input);
            EditorManager.prompt(player2, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_AMOUNT).getLocalized());
            player.closeInventory();
        };
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
