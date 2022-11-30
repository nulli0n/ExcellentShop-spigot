package su.nightexpress.nexshop.shop.chest.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.editor.menu.ProductPriceEditor;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

import java.util.Map;

public class ShopProductEditor extends AbstractEditorMenu<ExcellentShop, ChestProduct> {

    private ProductPriceEditor priceEditor;

    public ShopProductEditor(@NotNull ChestProduct product) {
        super(product.getShop().plugin(), product, ChestConfig.EDITOR_TITLE.get(), 36);

        EditorInput<ChestProduct, ChestEditorType> input = (player, product2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            if (type == ChestEditorType.PRODUCT_CHANGE_CURRENCY) {
                String id = StringUtil.colorOff(msg);
                ICurrency currency = plugin.getCurrencyManager().getCurrency(id);
                if (currency == null) {
                    EditorManager.error(player, plugin.getMessage(Lang.EDITOR_GENERIC_ERROR_CURRENCY).getLocalized());
                    return false;
                }
                if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
                    EditorManager.error(player, plugin.getMessage(ChestLang.SHOP_ERROR_CURRENCY_INVALID).getLocalized());
                    return false;
                }

                product2.setCurrency(currency);
            }

            product2.getShop().save();
            return true;
        };
        
        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    product.getShop().getEditor().getProductsEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ChestEditorType type2) {
                switch (type2) {
                    case PRODUCT_CHANGE_PRICE -> {
                        if (e.getClick() == ClickType.DROP) {
                            product.getPricer().update();
                            return;
                        }
                        this.getPriceEditor().open(player, 1);
                    }
                    case PRODUCT_CHANGE_CURRENCY -> {
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_PRODUCT_ENTER_CURRENCY).getLocalized());
                        plugin.getMessage(ChestLang.EDITOR_TIP_PRODUCT_CURRENCY).asList().forEach(line -> {
                            if (line.contains(Placeholders.CURRENCY_ID)) {
                                for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
                                    MessageUtil.sendWithJSON(player, currency.replacePlaceholders().apply(line));
                                }
                            }
                            else MessageUtil.sendWithJSON(player, line);
                        });
                        EditorManager.startEdit(player, product, type2, input);
                        player.closeInventory();
                    }
                }
            }
        };

        this.loadItems(click);
    }

    @Override
    public void clear() {
        if (this.priceEditor != null) {
            this.priceEditor.clear();
            this.priceEditor = null;
        }
        super.clear();
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(MenuItemType.RETURN, 31);

        map.put(ChestEditorType.PRODUCT_CHANGE_CURRENCY, 11);
        map.put(ChestEditorType.PRODUCT_CHANGE_PRICE, 15);
    }

    @NotNull
    public ProductPriceEditor getPriceEditor() {
        if (this.priceEditor == null) {
            this.priceEditor = new ProductPriceEditor(this.object);
        }
        return priceEditor;
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        ItemUtil.replace(item, this.object.replacePlaceholders());
        ItemUtil.replace(item, this.object.getCurrency().replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
