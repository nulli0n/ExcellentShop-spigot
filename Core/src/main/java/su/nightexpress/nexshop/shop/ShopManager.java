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
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.menu.Breadcumb;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nexshop.shop.menu.PurchaseOptionMenu;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.util.ItemUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopManager extends AbstractManager<ShopPlugin> {

    private final Map<String, CartMenu> cartMenuMap;

    private PurchaseOptionMenu purchaseOptionMenu;

    public ShopManager(@NotNull ShopPlugin plugin) {
        super(plugin);
        this.cartMenuMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadUI();
        this.loadCartUIs();

        this.addTask(this::updateShops, Config.SHOP_UPDATE_INTERVAL.get());

        this.plugin.runTaskLater(task -> this.printBadProducts(), 100L);
    }

    @Override
    protected void onShutdown() {
        if (this.purchaseOptionMenu != null) this.purchaseOptionMenu.clear();

        this.cartMenuMap.values().forEach(Menu::clear);
        this.cartMenuMap.clear();
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
            // ============= UPDATE TO 4.14 - START =============
            config.getSection("Content").forEach(itemId -> {
                int units = config.getInt("Content." + itemId + ".Units", 0);
                if (units == 0) return;

                String type = config.getString("Content." + itemId + ".Type");
                if (type == null || type.isBlank()) return;

                String result;
                if (units > 1000 && type.equalsIgnoreCase("set")) result = "set_max";
                else if (type.equalsIgnoreCase("set_custom")) result = type;
                else result = type + "_" + units;

                config.set("Content." + itemId + ".Type", result);
                config.remove("Content." + itemId + ".Units");
            });
            // ============= UPDATE TO 4.14 - END =============

            CartMenu cartMenu = new CartMenu(this.plugin, config);
            this.cartMenuMap.put(FileConfig.getName(config.getFile()), cartMenu);
        }
        this.plugin.info("Loaded " + this.cartMenuMap.size() + " product cart UIs!");
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
            shops.addAll(chestShopModule.lookup().getAll());
        }

        return shops;
    }

    public void updateShops() {
        this.getShops().forEach(shop -> {
            shop.update();
            shop.updatePrices(false);
        });
    }

    private void printBadProducts() {
        this.getShops().forEach(Shop::printBadProducts);
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

        if (action == ShopClickAction.SELL_ALL && !player.hasPermission(Perms.KEY_SELL_ALL)) {
            CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(player);
            return;
        }

        source.runNextTick(() -> this.startTrade(player, product, action, source));
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

        if (tradeType == TradeType.BUY) {
            if (!product.isBuyable()) {
                Lang.SHOP_PRODUCT_ERROR_UNBUYABLE.message().send(player);
                return false;
            }
            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && !product.hasSpace(player)) {
                Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY.message().send(player);
                return false;
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!product.isSellable()) {
                Lang.SHOP_PRODUCT_ERROR_UNSELLABLE.message().send(player);
                return false;
            }
            if (product.countUnits(player) < 1) {
                Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, product.getUnitAmount())
                    .replace(Placeholders.GENERIC_ITEM, ItemUtil.getNameSerialized(product.getPreviewOrPlaceholder()))
                );
                return false;
            }
        }

        // For Virtual Shop will return either Stock or Player Limit amount.
        // For Chest Shop will return inventory space or item amount.
        int canPurchase = product.getAvailableAmount(player, tradeType);
        if (canPurchase == 0) {
            MessageLocale msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK;
            }
            else {
                if (shop instanceof ChestShop) {
                    msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE;
                }
                else msgStock = Lang.SHOP_PRODUCT_ERROR_FULL_STOCK;
            }
            msgStock.message().send(player);
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
                return true;
            }
        }

        return this.openProductCart(player, prepared);
    }

    public boolean openProductCart(@NotNull Player player, @NotNull PreparedProduct product) {
        MenuViewer viewer = MenuRegistry.getViewer(player);
        int page = viewer == null ? 1 : viewer.getPage();

        CartMenu cartMenu = this.getCartUI(product.getShop().getModule().getDefaultCartUI(product.getTradeType()));
        if (cartMenu == null) {
            Lang.SHOP_PRODUCT_ERROR_INVALID_CART_UI.message().send(player);
            return false;
        }

        cartMenu.open(player, new Breadcumb<>(product, page));
        return true;
    }

    public void openPurchaseOption(@NotNull Player player, @NotNull Product product) {
        MenuViewer viewer = MenuRegistry.getViewer(player);
        int page = viewer == null ? 1 : viewer.getPage();

        this.purchaseOptionMenu.open(player, new Breadcumb<>(product, page));
    }

    @Deprecated
    public void openConfirmation(@NotNull Player player, @NotNull Confirmation confirmation) {
        UIUtils.openConfirmation(player, confirmation);
    }
}
