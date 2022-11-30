package su.nightexpress.nexshop.shop.chest.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenu;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;

import java.util.Map;
import java.util.stream.Stream;

public class ShopEditor extends AbstractEditorMenu<ExcellentShop, ChestShop> {

    private ShopProductsEditor productsEditor;

    public ShopEditor(@NotNull ChestShop shop) {
        super(shop.plugin(), shop, ChestConfig.EDITOR_TITLE.get(), 9);

        EditorInput<ChestShop, ChestEditorType> input = (player, shop2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case SHOP_CHANGE_NAME -> {
                    shop2.setName(msg);
                    shop2.updateDisplayText();
                }
                case SHOP_BANK_DEPOSIT, SHOP_BANK_WITHDRAW -> {
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
                        EditorManager.error(player, EditorManager.ERROR_NUM_INVALID);
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
        
        IMenuClick click = (player, type, e) -> {
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
                        if (e.isLeftClick()) type2 = ChestEditorType.SHOP_BANK_DEPOSIT;
                        else if (e.isRightClick()) type2 = ChestEditorType.SHOP_BANK_WITHDRAW;

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
                        if (!e.isShiftClick()) return;
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
                this.object.updateDisplay();
                this.object.save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
    }

    @NotNull
    public ShopProductsEditor getProductsEditor() {
        if (this.productsEditor == null) {
            this.productsEditor = new ShopProductsEditor(this.object);
        }
        return this.productsEditor;
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(ChestEditorType.SHOP_CHANGE_NAME, 0);
        map.put(ChestEditorType.SHOP_CHANGE_TRANSACTIONS, 1);
        map.put(ChestEditorType.SHOP_CHANGE_TYPE, 2);
        map.put(ChestEditorType.SHOP_CHANGE_PRODUCTS, 3);
        map.put(ChestEditorType.SHOP_BANK, 4);
        map.put(ChestEditorType.SHOP_DELETE, 8);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
