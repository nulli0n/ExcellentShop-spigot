package su.nightexpress.nexshop.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.menu.api.Menu;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopManager extends AbstractManager<ShopPlugin> {

    //private CartMenu cartMenu;

    private final Map<String, CartMenu> cartMenuMap;

    public ShopManager(@NotNull ShopPlugin plugin) {
        super(plugin);
        this.cartMenuMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        //this.cartMenu = new CartMenu(this.plugin);
        this.loadCartUIs();

        this.addTask(this.plugin.createAsyncTask(this::updateShops).setSecondsInterval(Config.SHOP_UPDATE_INTERVAL.get()));
    }

    @Override
    protected void onShutdown() {
        //if (this.cartMenu != null) this.cartMenu.clear();
        this.cartMenuMap.values().forEach(Menu::clear);
        this.cartMenuMap.clear();
    }

    private void loadCartUIs() {
        File dir = new File(this.plugin.getDataFolder() + Config.DIR_CARTS);
        if (!dir.exists()) {
            dir.mkdirs();
            new CartMenu(this.plugin, FileConfig.loadOrExtract(plugin, Config.DIR_CARTS, Placeholders.DEFAULT + ".yml"));
        }

        for (FileConfig config : FileConfig.loadAll(plugin.getDataFolder() + Config.DIR_CARTS)) {
            CartMenu cartMenu = new CartMenu(this.plugin, config);
            this.cartMenuMap.put(FileConfig.getName(config.getFile()), cartMenu);
        }
        this.plugin.info("Loaded " + this.cartMenuMap.size() + " product cart UIs!");
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

    @Nullable
    public CartMenu getCartUI(@NotNull String id) {
        return this.cartMenuMap.getOrDefault(id.toLowerCase(), this.cartMenuMap.get(Placeholders.DEFAULT));
    }

    public boolean openProductCart(@NotNull Player player, @NotNull PreparedProduct product) {
        CartMenu cartMenu = this.getCartUI(product.getShop().getModule().getDefaultCartUI());
        if (cartMenu == null) {
            Lang.SHOP_PRODUCT_ERROR_INVALID_CART_UI.getMessage().send(player);
            return false;
        }

        cartMenu.open(player, product);
        return true;
    }
}
