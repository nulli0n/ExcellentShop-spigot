package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

public class ShopSettingsMenu extends ConfigEditorMenu {

    private final ChestShop shop;

    private ShopProductsMenu productsMenu;

    public ShopSettingsMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getLocalPath() + "/menu/", "shop_settings.yml"));
        this.shop = shop;

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.RETURN, (viewer, event) -> shop.getModule().getListMenu().openNextTick(viewer, 1));

        this.registerHandler(Type.class)
            .addClick(Type.SHOP_CHANGE_TYPE, (viewer, event) -> {
                shop.setType(CollectionsUtil.next(shop.getType(), shopType -> shopType.hasPermission(viewer.getPlayer())));
                this.save(viewer);
            })
            .addClick(Type.SHOP_CHANGE_NAME, (viewer, event) -> {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, wrapper -> {
                    shop.setName(wrapper.getText());
                    shop.updateDisplayText();
                    shop.save();
                    return true;
                });
            })
            .addClick(Type.SHOP_BANK, (viewer, event) -> {
                this.shop.getModule().getBankMenu().openNextTick(viewer, 1);
            })
            .addClick(Type.SHOP_CHANGE_TRANSACTIONS, (viewer, event) -> {
                if (PlayerUtil.isBedrockPlayer(viewer.getPlayer())) {
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
                    return;
                }

                if (event.isLeftClick()) {
                    shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
                }
                else if (event.isRightClick()) {
                    shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
                }
                this.save(viewer);
            })
            .addClick(Type.SHOP_CHANGE_PRODUCTS, (viewer, event) -> {
                this.getProductsMenu().openNextTick(viewer, 1);
            })
            .addClick(Type.SHOP_DELETE, (viewer, event) -> {
                Player player = viewer.getPlayer();
                if (!event.isShiftClick() && !PlayerUtil.isBedrockPlayer(player)) return;
                if (!player.hasPermission(ChestPerms.REMOVE)
                    || (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS))) {
                    plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                    return;
                }
                this.plugin.runTask(task -> player.closeInventory());
                shop.getModule().deleteShop(player, shop.getLocation().getBlock());
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> ItemUtil.replace(item, this.shop.replacePlaceholders()));

            if (menuItem.getType() == Type.SHOP_BANK) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> {
                    if (viewer.getPlayer().hasPermission(ChestPerms.COMMAND_BANK_OTHERS)) return true;

                    return !ChestConfig.SHOP_AUTO_BANK.get();
                });
            }
        });
    }

    private void save(@NotNull MenuViewer viewer) {
        this.shop.save();
        this.openNextTick(viewer, viewer.getPage());
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
        super.clear();
    }

    @NotNull
    public ShopProductsMenu getProductsMenu() {
        if (this.productsMenu == null) {
            this.productsMenu = new ShopProductsMenu(this.shop);
        }
        return this.productsMenu;
    }
}
