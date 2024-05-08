package su.nightexpress.nexshop.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.util.HashSet;
import java.util.Set;

public class ShopManager extends AbstractManager<ShopPlugin> {

    private CartMenu cartMenu;

    public ShopManager(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.cartMenu = new CartMenu(this.plugin);

        this.addTask(this.plugin.createAsyncTask(this::updateShops).setSecondsInterval(Config.SHOP_UPDATE_INTERVAL.get()));
    }

    @Override
    protected void onShutdown() {
        if (this.cartMenu != null) this.cartMenu.clear();
    }

    private void updateShops() {
        Set<Shop> shops = new HashSet<>();

        VirtualShopModule virtualShopModule = plugin.getVirtualShop();
        if (virtualShopModule != null) {
            virtualShopModule.getShops().forEach(shop -> {
                if (!shop.isLoaded()) return;

                shops.add(shop);

                if (shop instanceof StaticShop staticShop) {
                    staticShop.getDiscountConfigs().forEach(discount -> {
                        if (discount.isDiscountTime()) {
                            discount.update();
                        }
                    });
                }
                else if (shop instanceof RotatingShop rotatingShop) {
                    rotatingShop.tryRotate();
                }
            });
        }

        ChestShopModule chestShopModule = plugin.getChestShop();
        if (chestShopModule != null) {
            shops.addAll(chestShopModule.getActiveShops());
        }

        shops.forEach(shop -> shop.getPricer().updatePrices());
    }

    public void openProductCart(@NotNull Player player, @NotNull PreparedProduct product) {
        this.cartMenu.open(player, product);
    }
}
