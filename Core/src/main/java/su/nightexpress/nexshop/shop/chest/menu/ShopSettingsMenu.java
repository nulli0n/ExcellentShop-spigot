package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.editor.menu.ShopProductsEditor;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;

import java.util.stream.Stream;

public class ShopSettingsMenu extends AbstractMenu<ExcellentShop> {

    private final ChestShop          shop;
    private       ShopProductsEditor productsEditor;

    public ShopSettingsMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getPath() + "menu/shop_settings.yml"), "");
        this.shop = shop;

        EditorInput<ChestShop, ChestEditorType> input = (player, shop2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case SHOP_CHANGE_NAME -> {
                    shop2.setName(msg);
                    shop2.updateDisplayText();
                }
                case SHOP_BANK, SHOP_BANK_DEPOSIT, SHOP_BANK_WITHDRAW -> {
                    if (type == ChestEditorType.SHOP_BANK && PlayerUtil.isBedrockPlayer(player)) {
                        if (msg.startsWith("+")) type = ChestEditorType.SHOP_BANK_DEPOSIT;
                        else if (msg.startsWith("-")) type = ChestEditorType.SHOP_BANK_WITHDRAW;
                        else return false;
                        
                        msg = msg.substring(1);
                    }

                    String[] split = msg.split(" ");
                    if (split.length != 2) {
                        EditorManager.error(player, plugin.getMessage(ChestLang.EDITOR_ERROR_BANK_INVALID_SYNTAX).getLocalized());
                        return false;
                    }

                    ICurrency currency = plugin.getCurrencyManager().getCurrency(split[0]);
                    if (currency == null) {
                        EditorManager.error(player, plugin.getMessage(ChestLang.SHOP_ERROR_CURRENCY_INVALID).getLocalized());
                        return false;
                    }

                    double amount = StringUtil.getDouble(split[1], 0, true);
                    if (amount == 0D) {
                        EditorManager.error(player, plugin.getMessage(Lang.EDITOR_ERROR_NUMBER_GENERIC).getLocalized());
                        return false;
                    }

                    if (type == ChestEditorType.SHOP_BANK_DEPOSIT) {
                        return shop.getModule().depositToShop(player, shop2, currency, amount);
                    }
                    else {
                        return shop.getModule().withdrawFromShop(player, shop2, currency, amount);
                    }
                }
                default -> { }
            }

            shop2.save();
            return true;
        };
        
        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getModule().getListOwnMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ChestEditorType type2) {
                switch (type2) {
                    case SHOP_CHANGE_NAME -> {
                        EditorManager.startEdit(player, shop, type2, input);
                        EditorManager.tip(player, plugin.getMessage(ChestLang.EDITOR_TIP_NAME).getLocalized());
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_TYPE -> {
                        if (Stream.of(ChestShopType.values()).noneMatch(t -> t.hasPermission(player))) {
                            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                            return;
                        }

                        shop.setType(CollectionsUtil.switchEnum(shop.getType()));
                        while (!shop.getType().hasPermission(player)) {
                            shop.setType(CollectionsUtil.switchEnum(shop.getType()));
                        }
                    }
                    case SHOP_BANK -> {
                        if (!PlayerUtil.isBedrockPlayer(player)) {
                            if (e.isLeftClick()) type2 = ChestEditorType.SHOP_BANK_DEPOSIT;
                            else if (e.isRightClick()) type2 = ChestEditorType.SHOP_BANK_WITHDRAW;
                        }

                        EditorManager.startEdit(player, shop, type2, input);
                        plugin.getMessage(ChestLang.EDITOR_TIP_BANK_CURRENCY).asList().forEach(line -> {
                            if (line.contains(Placeholders.CURRENCY_ID)) {
                                for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
                                    MessageUtil.sendWithJSON(player, currency.replacePlaceholders().apply(line));
                                }
                            }
                            else MessageUtil.sendWithJSON(player, line);
                        });
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_TRANSACTIONS -> {
                        if (PlayerUtil.isBedrockPlayer(player)) {
                            boolean isBuy = shop.isTransactionEnabled(TradeType.BUY);
                            boolean isSell = shop.isTransactionEnabled(TradeType.SELL);
                            if (isBuy && isSell) {
                                shop.setTransactionEnabled(TradeType.BUY, false);
                            }
                            else if (!isBuy && isSell) {
                                shop.setTransactionEnabled(TradeType.SELL, false);
                            }
                            else if (!isBuy) {
                                shop.setTransactionEnabled(TradeType.BUY, true);
                            }
                            else {
                                shop.setTransactionEnabled(TradeType.SELL, true);
                            }
                            break;
                        }

                        if (e.isLeftClick()) {
                            shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
                        }
                    }
                    case SHOP_CHANGE_PRODUCTS -> {
                        this.getProductsEditor().open(player, 1);
                        return;
                    }
                    case SHOP_DELETE -> {
                        if (!e.isShiftClick() && !PlayerUtil.isBedrockPlayer(player)) return;
                        if (!player.hasPermission(Perms.CHEST_SHOP_REMOVE)
                                || (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_SHOP_REMOVE_OTHERS))) {
                            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                            return;
                        }
                        player.closeInventory();
                        shop.getModule().deleteShop(player, shop.getLocation().getBlock());
                        return;
                    }
                    default -> {
                        return;
                    }
                }
                this.shop.updateDisplay();
                this.shop.save();
                this.open(player, 1);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, ChestEditorType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void clear() {
        if (this.productsEditor != null) {
            this.productsEditor.clear();
            this.productsEditor = null;
        }
        super.clear();
    }

    @NotNull
    public ShopProductsEditor getProductsEditor() {
        if (this.productsEditor == null) {
            this.productsEditor = new ShopProductsEditor(this.shop);
        }
        return this.productsEditor;
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }

    enum ChestEditorType {

        SHOP_CHANGE_NAME,
        SHOP_CHANGE_TYPE,
        SHOP_CHANGE_TRANSACTIONS,
        SHOP_CHANGE_PRODUCTS,
        SHOP_BANK,
        SHOP_BANK_DEPOSIT,
        SHOP_BANK_WITHDRAW,
        SHOP_DELETE,
    }
}
