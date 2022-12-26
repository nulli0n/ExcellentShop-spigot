package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenu;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.editor.menu.ProductPriceEditor;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.Map;

public class EditorShopProduct extends AbstractEditorMenu<ExcellentShop, VirtualProduct> {

    private ProductPriceEditor editorPrice;

    public EditorShopProduct(@NotNull ExcellentShop plugin, @NotNull VirtualProduct product) {
        super(plugin, product, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        VirtualShop shop = product.getShop();
        EditorInput<VirtualProduct, VirtualEditorType> input = (player, product2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case PRODUCT_CHANGE_COMMANDS -> product2.getCommands().add(StringUtil.colorRaw(msg));
                case PRODUCT_CHANGE_CURRENCY -> {
                    String id = StringUtil.colorOff(msg);
                    ICurrency currency = plugin.getCurrencyManager().getCurrency(id);
                    if (currency == null) {
                        EditorManager.error(player, plugin.getMessage(Lang.EDITOR_GENERIC_ERROR_CURRENCY).getLocalized());
                        return false;
                    }

                    product2.setCurrency(currency);
                }
                case PRODUCT_CHANGE_STOCK_GLOBAL_BUY_INITIAL_AMOUNT -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setInitialAmount(StockType.GLOBAL, TradeType.BUY, value);
                }
                case PRODUCT_CHANGE_STOCK_GLOBAL_BUY_RESTOCK_TIME -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setRestockCooldown(StockType.GLOBAL, TradeType.BUY, value);
                }
                case PRODUCT_CHANGE_STOCK_GLOBAL_SELL_INITIAL_AMOUNT -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setInitialAmount(StockType.GLOBAL, TradeType.SELL, value);
                }
                case PRODUCT_CHANGE_STOCK_GLOBAL_SELL_RESTOCK_TIME -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setRestockCooldown(StockType.GLOBAL, TradeType.SELL, value);
                }
                case PRODUCT_CHANGE_STOCK_PLAYER_BUY_INITIAL_AMOUNT -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setInitialAmount(StockType.PLAYER, TradeType.BUY, value);
                }
                case PRODUCT_CHANGE_STOCK_PLAYER_BUY_RESTOCK_TIME -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setRestockCooldown(StockType.PLAYER, TradeType.BUY, value);
                }
                case PRODUCT_CHANGE_STOCK_PLAYER_SELL_INITIAL_AMOUNT -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setInitialAmount(StockType.PLAYER, TradeType.SELL, value);
                }
                case PRODUCT_CHANGE_STOCK_PLAYER_SELL_RESTOCK_TIME -> {
                    int value = StringUtil.getInteger(StringUtil.colorOff(msg), -1, true);
                    product2.getStock().setRestockCooldown(StockType.PLAYER, TradeType.SELL, value);
                }
                default -> {}
            }

            product2.getShop().save();
            return true;
        };
        
        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().getEditorProducts().open(player, this.object.getPage());
                }
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_PRICE -> {
                        if (e.getClick() == ClickType.DROP) {
                            product.getPricer().update();
                            this.open(player, 1);
                            return;
                        }
                        this.getEditorPrice().open(player, 1);
                        return;
                    }
                    case PRODUCT_CHANGE_COMMANDS -> {
                        if (e.isLeftClick()) {
                            EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_PRODUCT_ENTER_COMMAND).getLocalized());
                            EditorManager.sendCommandTips(player);
                            EditorManager.startEdit(player, product, type2, input);
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                    }
                    case PRODUCT_CHANGE_ITEM -> {
                        if (e.isShiftClick() && e.isLeftClick()) {
                            ItemStack buyItem = product.getItem();
                            PlayerUtil.addItem(player, buyItem);
                            return;
                        }

                        ItemStack cursor = e.getCursor();
                        if (cursor != null && !cursor.getType().isAir()) {
                            product.setItem(cursor);
                            e.getView().setCursor(null);
                        }
                        else if (e.isRightClick()) {
                            product.setItem(new ItemStack(Material.AIR));
                        }
                    }
                    case PRODUCT_CHANGE_PREVIEW -> {
                        if (e.isShiftClick() && e.isLeftClick()) {
                            ItemStack buyItem = product.getPreview();
                            PlayerUtil.addItem(player, buyItem);
                            return;
                        }

                        ItemStack item = e.getCursor();
                        if (item != null && !item.getType().isAir()) {
                            product.setPreview(item);
                            e.getView().setCursor(null);
                        }
                    }
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_CURRENCY).getLocalized());
                        EditorManager.suggestValues(player, plugin.getCurrencyManager()
                                .getCurrencies().stream().map(ICurrency::getId).toList(), true);
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_DISCOUNT -> product.setDiscountAllowed(!product.isDiscountAllowed());
                    case PRODUCT_CHANGE_ITEM_META -> product.setItemMetaEnabled(!product.isItemMetaEnabled());
                    case PRODUCT_CHANGE_STOCK_GLOBAL -> {
                        VirtualEditorType type3;
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_GLOBAL_SELL_INITIAL_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_AMOUNT).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_GLOBAL_SELL_RESTOCK_TIME;
                                EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS).getLocalized());
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_GLOBAL_BUY_INITIAL_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_AMOUNT).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_GLOBAL_BUY_RESTOCK_TIME;
                                EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS).getLocalized());
                            }
                        }

                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_STOCK_PLAYER -> {
                        VirtualEditorType type3;
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_PLAYER_SELL_INITIAL_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_AMOUNT).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_PLAYER_SELL_RESTOCK_TIME;
                                EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS).getLocalized());
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_PLAYER_BUY_INITIAL_AMOUNT;
                                EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_AMOUNT).getLocalized());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_STOCK_PLAYER_BUY_RESTOCK_TIME;
                                EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS).getLocalized());
                            }
                        }

                        EditorManager.startEdit(player, product, type3, input);
                        player.closeInventory();
                        return;
                    }
                    default -> { return; }
                }

                shop.save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
    }

    @Override
    public void clear() {
        super.clear();
        if (this.editorPrice != null) {
            this.editorPrice.clear();
            this.editorPrice = null;
        }
    }

    @NotNull
    public ProductPriceEditor getEditorPrice() {
        if (this.editorPrice == null) {
            this.editorPrice = new ProductPriceEditor(this.object);
        }
        return editorPrice;
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(VirtualEditorType.PRODUCT_CHANGE_COMMANDS, 10);
        map.put(VirtualEditorType.PRODUCT_CHANGE_CURRENCY, 13);
        map.put(VirtualEditorType.PRODUCT_CHANGE_DISCOUNT, 16);

        map.put(VirtualEditorType.PRODUCT_CHANGE_ITEM, 2);
        map.put(VirtualEditorType.PRODUCT_CHANGE_ITEM_META, 4);
        map.put(VirtualEditorType.PRODUCT_CHANGE_PREVIEW, 6);

        map.put(VirtualEditorType.PRODUCT_CHANGE_PRICE, 22);
        map.put(VirtualEditorType.PRODUCT_CHANGE_STOCK_GLOBAL, 29);
        map.put(VirtualEditorType.PRODUCT_CHANGE_STOCK_PLAYER, 33);

        map.put(MenuItemType.RETURN, 40);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        Enum<?> type = menuItem.getType();
        if (type != null) {
            if (type == VirtualEditorType.PRODUCT_CHANGE_ITEM_META) {
                item.setType(object.isItemMetaEnabled() ? Material.WRITABLE_BOOK : Material.BOOK);
            }
            else if (type == VirtualEditorType.PRODUCT_CHANGE_PREVIEW) {
                item.setType(this.object.getPreview().getType());
            }
            else if (type == VirtualEditorType.PRODUCT_CHANGE_ITEM) {
                ItemStack buyItem = object.getItem();
                if (!buyItem.getType().isAir()) {
                    item.setType(buyItem.getType());
                }
            }
        }

        ItemUtil.replace(item, object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e,  @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
