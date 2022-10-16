package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;
import su.nightexpress.nexshop.shop.chest.type.ChestType;

import java.util.stream.Stream;

public class EditorShopChest extends AbstractMenu<ExcellentShop> {

    private final IShopChest shop;

    private EditorShopChestProducts products;

    public EditorShopChest(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestConfig.CONFIG_SHOP, "");
        this.shop = shop;

        EditorInput<IShopChest, ChestEditorType> input = (player, shop2, type, e) -> {
            ChestShop chestShop = plugin.getChestShop();
            if (chestShop == null) return true;
            
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case SHOP_CHANGE_NAME -> {
                    shop2.setName(msg);
                    shop2.updateDisplayText();
                }
                case SHOP_BANK_DEPOSIT, SHOP_BANK_WITHDRAW -> {
                    String[] split = msg.split(" ");
                    if (split.length != 2) {
                        EditorManager.error(player, plugin.getMessage(Lang.Editor_Error_Bank_InvalidSyntax).getLocalized());
                        return false;
                    }

                    ICurrency currency = plugin.getCurrencyManager().getCurrency(split[0]);
                    if (currency == null) {
                        EditorManager.error(player, plugin.getMessage(Lang.Shop_Error_Currency_Invalid).getLocalized());
                        return false;
                    }

                    double amount = StringUtil.getDouble(split[1], 0, true);
                    if (amount == 0D) {
                        EditorManager.error(player, EditorManager.ERROR_NUM_INVALID);
                        return false;
                    }

                    if (type == ChestEditorType.SHOP_BANK_DEPOSIT) {
                        return chestShop.depositToShop(player, shop2, currency, amount);
                    }
                    else {
                        return chestShop.withdrawFromShop(player, shop2, currency, amount);
                    }
                }
                default -> { }
            }

            shop2.save();
            return true;
        };
        
        IMenuClick click = (player, type, e) -> {

            ChestShop chestShop = plugin.getChestShop();
            if (chestShop == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    chestShop.getListOwnMenu().open(player, 1);
                }
                else if (type2 == MenuItemType.CLOSE) {
                    player.closeInventory();
                }
            }
            else if (type instanceof ChestEditorType type2) {
                switch (type2) {
                    case SHOP_CHANGE_NAME -> {
                        EditorManager.startEdit(player, shop, type2, input);
                        EditorManager.tip(player, plugin.getMessage(Lang.Editor_Tip_Name).getLocalized());
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_TYPE -> {
                        if (!player.hasPermission(Perms.CHEST_EDITOR_TYPE) || Stream.of(ChestType.values()).noneMatch(t -> t.hasPermission(player))) {
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
                        plugin.getMessage(Lang.Editor_Tip_Bank_Currency).asList().forEach(line -> {
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
                            shop.setPurchaseAllowed(TradeType.BUY, !shop.isPurchaseAllowed(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setPurchaseAllowed(TradeType.SELL, !shop.isPurchaseAllowed(TradeType.SELL));
                        }
                    }
                    case SHOP_CHANGE_PRODUCTS -> {
                        this.getEditorProducts().open(player, 1);
                        return;
                    }
                    case SHOP_DELETE -> {
                        if (!e.isShiftClick()) return;
                        if (!player.hasPermission(Perms.CHEST_REMOVE)
                                || (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_REMOVE_OTHERS))) {
                            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                            return;
                        }
                        chestShop.deleteShop(player, shop.getLocation().getBlock());
                        player.closeInventory();
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
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ChestEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @NotNull
    public EditorShopChestProducts getEditorProducts() {
        if (this.products == null) {
            this.products = new EditorShopChestProducts(this.plugin, this.shop);
        }
        return this.products;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
