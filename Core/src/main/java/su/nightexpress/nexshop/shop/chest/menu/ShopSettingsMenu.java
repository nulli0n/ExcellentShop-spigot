package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

public class ShopSettingsMenu extends ConfigEditorMenu implements Linked<ChestShop> {

    public static final String FILE = "shop_settings.yml";

    private final ViewLink<ChestShop> link;

    public ShopSettingsMenu(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        super(plugin, JYML.loadOrExtract(plugin, module.getMenusPath(), FILE));
        this.link = new ViewLink<>();

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.RETURN, (viewer, event) -> plugin.runTask(task -> module.listShops(viewer.getPlayer())));

        this.registerHandler(Type.class)
            .addClick(Type.SHOP_CHANGE_TYPE, (viewer, event) -> {
                ChestShop shop = this.getShop(viewer);
                shop.setType(CollectionsUtil.next(shop.getType(), shopType -> shopType.hasPermission(viewer.getPlayer())));
                this.save(viewer);
            })
            .addClick(Type.SHOP_CHANGE_NAME, (viewer, event) -> {
                ChestShop shop = this.getShop(viewer);
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, wrapper -> {
                    shop.setName(wrapper.getText());
                    shop.updateDisplayText();
                    shop.save();
                    return true;
                });
            })
            .addClick(Type.SHOP_BANK, (viewer, event) -> {
                module.getBankMenu().openNextTick(viewer, 1);
            })
            .addClick(Type.SHOP_CHANGE_TRANSACTIONS, (viewer, event) -> {
                ChestShop shop = this.getShop(viewer);
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
                this.plugin.runTask(task -> this.getShop(viewer).openProductsMenu(viewer.getPlayer()));
            })
            .addClick(Type.SHOP_DELETE, (viewer, event) -> {
                Player player = viewer.getPlayer();
                if (!event.isShiftClick() && !PlayerUtil.isBedrockPlayer(player)) return;

                this.plugin.runTask(task -> player.closeInventory());
                module.deleteShop(player, this.getShop(viewer));
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ItemReplacer.replace(item, this.getShop(viewer).replacePlaceholders());
            });

            if (menuItem.getType() == Type.SHOP_BANK) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> {
                    if (viewer.getPlayer().hasPermission(ChestPerms.COMMAND_BANK_OTHERS)) return true;

                    return !ChestConfig.SHOP_AUTO_BANK.get();
                });
            }
        });
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @NotNull
    private ChestShop getShop(@NotNull MenuViewer viewer) {
        return this.getLink().get(viewer);
    }

    private void save(@NotNull MenuViewer viewer) {
        this.getShop(viewer).save();
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
}
