package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nightexpress.nexshop.api.shop.AbstractShopView;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopConfig;

import java.util.ArrayList;
import java.util.List;

public class ShopVirtualView extends AbstractShopView<IShopVirtual> {

    public ShopVirtualView(@NotNull IShopVirtual shop, @NotNull JYML cfg) {
        super(shop, cfg);

        IMenuClick click = (p, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    VirtualShop virtualShop = plugin.getVirtualShop();
                    if (virtualShop != null && virtualShop.hasMainMenu()) {
                        virtualShop.openMainMenu(p);
                    }
                    else p.closeInventory();
                }
                else this.onItemClickDefault(p, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void displayProducts(@NotNull Player player, @NotNull Inventory inventory, int page) {
        for (IProductVirtual product : shop.getProducts()) {
            if (product.getPage() != page) continue;

            ItemStack preview = product.getPreview();
            ItemMeta meta = preview.getItemMeta();

            List<String> loreFormat = VirtualShopConfig.PRODUCT_FORMAT_LORE_PRICE_ALL;
            if (!product.isBuyable() || !shop.isPurchaseAllowed(TradeType.BUY)) {
                loreFormat = VirtualShopConfig.PRODUCT_FORMAT_LORE_PRICE_SELL;
            }
            if (!product.isSellable() || !shop.isPurchaseAllowed(TradeType.SELL)) {
                loreFormat = VirtualShopConfig.PRODUCT_FORMAT_LORE_PRICE_BUY;
            }

            if (meta != null) {
                List<String> lore = new ArrayList<>();

                for (String lineFormat : loreFormat) {
                    if (lineFormat.contains("product_limit_buy") && !product.isLimited(TradeType.BUY)) {
                        continue;
                    }
                    if (lineFormat.contains("product_limit_sell") && !product.isLimited(TradeType.SELL)) {
                        continue;
                    }
                    if (lineFormat.contains("%lore%")) {
                        List<String> list2 = meta.getLore();
                        if (list2 != null) lore.addAll(list2);
                        continue;
                    }
                    lore.add(lineFormat);
                }

                lore.replaceAll(product.replacePlaceholders(player));
                lore.replaceAll(product.getCurrency().replacePlaceholders());
                meta.setLore(lore);
                preview.setItemMeta(meta);
            }

            IMenuItem menuItem = new MenuItem(preview);
            menuItem.setSlots(product.getSlot());
            menuItem.setClick((player1, type, e) -> {
                ShopClickType clickType = ShopClickType.getByDefault(e.getClick());
                if (clickType == null) return;

                product.prepareTrade(player1, clickType);
            });
            this.addItem(player, menuItem);
        }

        this.setPage(player, page, this.shop.getPages());
    }
}
