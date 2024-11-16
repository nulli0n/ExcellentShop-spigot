package su.nightexpress.nexshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.product.ProductDataManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.menu.ShopView;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nexshop.shop.menu.PurchaseOptionMenu;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;
import su.nightexpress.nightcore.util.Plugins;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopManager extends AbstractManager<ShopPlugin> {

    private final Map<String, CartMenu> cartMenuMap;
    private final ProductDataManager    productDataManager;

    private PurchaseOptionMenu purchaseOptionMenu;

    public ShopManager(@NotNull ShopPlugin plugin) {
        super(plugin);
        this.cartMenuMap = new HashMap<>();
        this.productDataManager = new ProductDataManager(plugin);
    }

    @Override
    protected void onLoad() {
        this.loadUI();
        this.loadCartUIs();
        this.loadProductData();

        this.addTask(this.plugin.createAsyncTask(this::updateShops).setSecondsInterval(Config.SHOP_UPDATE_INTERVAL.get()));
    }

    @Override
    protected void onShutdown() {
        if (this.purchaseOptionMenu != null) this.purchaseOptionMenu.clear();

        this.cartMenuMap.values().forEach(Menu::clear);
        this.cartMenuMap.clear();

        this.productDataManager.shutdown();
    }

    private void loadProductData() {
        long delay = Plugins.isInstalled(HookId.ITEMS_ADDER) ? 110L : 1L; // костыль FIXME
        this.plugin.runTaskLaterAsync(task -> {
            this.productDataManager.setup(); // Load price & stock datas for all products in both, virtual and chest shops.
            this.productDataManager.cleanUp(); // Remove datas for non-existent shops or products (shops are already loaded).
            this.getShops().forEach(shop -> shop.getPricer().updatePrices()); // Update product prices with loaded datas.
        }, delay);
    }

    private void loadUI() {
        this.purchaseOptionMenu = new PurchaseOptionMenu(this.plugin);
    }

    private void loadCartUIs() {
        File dir = new File(this.plugin.getDataFolder() + Config.DIR_CARTS);
        if (!dir.exists()) {
            dir.mkdirs();
            new CartMenu(this.plugin, FileConfig.loadOrExtract(plugin, Config.DIR_CARTS, Placeholders.DEFAULT + ".yml"));
        }

        for (FileConfig config : FileConfig.loadAll(plugin.getDataFolder() + Config.DIR_CARTS, true)) {
            CartMenu cartMenu = new CartMenu(this.plugin, config);
            this.cartMenuMap.put(FileConfig.getName(config.getFile()), cartMenu);
        }
        this.plugin.info("Loaded " + this.cartMenuMap.size() + " product cart UIs!");
    }

    @NotNull
    public ProductDataManager getProductDataManager() {
        return productDataManager;
    }

    @NotNull
    public Set<Shop> getShops() {
        Set<Shop> shops = new HashSet<>();

        VirtualShopModule virtualShopModule = plugin.getVirtualShop();
        if (virtualShopModule != null) {
            shops.addAll(virtualShopModule.getShops());
        }

        ChestShopModule chestShopModule = plugin.getChestShop();
        if (chestShopModule != null) {
            shops.addAll(chestShopModule.getShops());
        }

        return shops;
    }

    private void updateShops() {
        this.getShops().forEach(shop -> {
            if (!shop.isLoaded()) return;

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

            shop.getPricer().updatePrices();
        });
    }

    @Nullable
    public CartMenu getCartUI(@NotNull String id) {
        return this.cartMenuMap.getOrDefault(id.toLowerCase(), this.cartMenuMap.get(Placeholders.DEFAULT));
    }

    public void onProductClick(@NotNull Player player, @NotNull Product product, @NotNull ClickType clickType, @NotNull Menu source) {
        if (!product.isAvailable(player)) {
            source.runNextTick(() -> source.flush(player));
            return;
        }

        Shop shop = product.getShop();

        ShopClickAction action = ShopUtils.getClickAction(player, clickType, shop, product);
        if (action == ShopClickAction.UNDEFINED) return;

        this.startTrade(player, product, action, source);
    }

    public boolean startTrade(@NotNull Player player, @NotNull Product product, @NotNull ShopClickAction action, @Nullable Menu source) {
        TradeType tradeType = action.getTradeType();

        if (tradeType != null) {
            return this.startTrade(player, product, tradeType, action, source);
        }

        this.openPurchaseOption(player, product);
        return true;
    }

    public boolean startTrade(@NotNull Player player, @NotNull Product product, @NotNull TradeType tradeType, @Nullable ShopClickAction action, @Nullable Menu source) {
        Shop shop = product.getShop();

        if (!shop.isTransactionEnabled(tradeType)) {
            return false;
        }

        if (tradeType == TradeType.BUY) {
            if (!product.isBuyable()) {
                Lang.SHOP_PRODUCT_ERROR_UNBUYABLE.getMessage().send(player);
                return false;
            }
            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && !product.hasSpace(player)) {
                Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY.getMessage().send(player);
                return false;
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!product.isSellable()) {
                Lang.SHOP_PRODUCT_ERROR_UNSELLABLE.getMessage().send(player);
                return false;
            }
        }

        // For Virtual Shop will return either Stock or Player Limit amount.
        // For Chest Shop will return inventory space or item amount.
        int canPurchase = product.getAvailableAmount(player, tradeType);
        if (canPurchase == 0) {
            LangText msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK;
            }
            else {
                if (shop instanceof ChestShop) {
                    msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE;
                }
                else msgStock = Lang.SHOP_PRODUCT_ERROR_FULL_STOCK;
            }
            msgStock.getMessage().send(player);
            return false;
        }

        boolean isSellAll = (action == ShopClickAction.SELL_ALL);
        PreparedProduct prepared = product.getPrepared(player, tradeType, isSellAll);

        if (action != null) {
            if (action == ShopClickAction.BUY_SINGLE || action == ShopClickAction.SELL_SINGLE || prepared.isAll()) {
                prepared.trade();

                if (source != null) {
                    source.flush(player);
                }
                return false;
            }
        }

        return this.openProductCart(player, prepared);
    }

    public boolean openProductCart(@NotNull Player player, @NotNull PreparedProduct product) {
        CartMenu cartMenu = this.getCartUI(product.getShop().getModule().getDefaultCartUI(product.getTradeType()));
        if (cartMenu == null) {
            Lang.SHOP_PRODUCT_ERROR_INVALID_CART_UI.getMessage().send(player);
            return false;
        }

        cartMenu.open(player, product);
        return true;
    }

    public void openPurchaseOption(@NotNull Player player, @NotNull Product product) {
        this.purchaseOptionMenu.open(player, product);
    }
}
