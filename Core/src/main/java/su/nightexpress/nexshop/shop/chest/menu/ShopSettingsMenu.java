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
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

public class ShopSettingsMenu extends AbstractMenu<ExcellentShop> {

    private final ChestShop shop;

    private ShopProductsMenu productsMenu;
    private ShopBankMenu     bankMenu;

    public ShopSettingsMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getPath() + "menu/shop_settings.yml"), "");
        this.shop = shop;

        EditorInput<ChestShop, Type> input = (player, shop2, type, e) -> {
            String msg = e.getMessage();
            switch (type) {
                case SHOP_CHANGE_NAME -> {
                    shop2.setName(msg);
                    shop2.updateDisplayText();
                }
            }

            shop2.save();
            return true;
        };
        
        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getModule().getListMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof Type type2) {
                switch (type2) {
                    case SHOP_CHANGE_NAME -> {
                        EditorManager.startEdit(player, shop, type2, input);
                        EditorManager.prompt(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_NAME).getLocalized());
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_TYPE -> shop.setType(CollectionsUtil.next(shop.getType(), shopType -> shopType.hasPermission(player)));
                    case SHOP_BANK -> {
                        this.getBankMenu().open(player, 1);
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
                        this.getProductsMenu().open(player, 1);
                        return;
                    }
                    case SHOP_DELETE -> {
                        if (!e.isShiftClick() && !PlayerUtil.isBedrockPlayer(player)) return;
                        if (!player.hasPermission(ChestPerms.REMOVE)
                                || (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS))) {
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
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, Type.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum Type {
        SHOP_CHANGE_NAME,
        SHOP_CHANGE_TYPE,
        SHOP_CHANGE_TRANSACTIONS,
        SHOP_CHANGE_PRODUCTS,
        SHOP_BANK,
        SHOP_DELETE,
    }

    @Override
    public void clear() {
        if (this.productsMenu != null) {
            this.productsMenu.clear();
            this.productsMenu = null;
        }
        if (this.bankMenu != null) {
            this.bankMenu.clear();
            this.bankMenu = null;
        }
        super.clear();
    }

    @NotNull
    public ShopProductsMenu getProductsMenu() {
        if (this.productsMenu == null) {
            this.productsMenu = new ShopProductsMenu(this.shop);
        }
        return this.productsMenu;
    }

    @NotNull
    public ShopBankMenu getBankMenu() {
        if (this.bankMenu == null) {
            this.bankMenu = new ShopBankMenu(this.shop);
        }
        return bankMenu;
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
}
