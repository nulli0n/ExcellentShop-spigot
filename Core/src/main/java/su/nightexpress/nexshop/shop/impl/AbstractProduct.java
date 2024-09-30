package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.price.impl.FlatPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.util.RelativePlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public abstract class AbstractProduct<S extends AbstractShop<?>> implements Product {

    protected final ShopPlugin                   plugin;
    protected final String                       id;
    protected final RelativePlaceholders<Player> placeholders;

    protected S                     shop;
    protected Currency              currency;
    protected ProductHandler        handler;
    protected ProductPacker         packer;
    protected AbstractProductPricer pricer;

    public AbstractProduct(@NotNull ShopPlugin plugin,
                           @NotNull String id,
                           @NotNull S shop,
                           @NotNull Currency currency,
                           @NotNull ProductHandler handler,
                           @NotNull ProductPacker packer) {
        this.plugin = plugin;
        this.id = id.toLowerCase();
        this.shop = shop;
        this.setCurrency(currency);
        this.setPricer(new FlatPricer());
        this.setHandler(handler, packer);

        this.placeholders = Placeholders.forProduct(this);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders(@Nullable Player player) {
        PlaceholderMap map = this.placeholders.toNormal(player);
        map.add(this.getPacker().getPlaceholders()); // Packer can be changed, we can't cache it.
        map.add(this.getPricer().getPlaceholders()); // Pricer can be changed, we can't cache it.
        return map;
    }

//    @Override
//    @Deprecated
//    public void prepareTrade(@NotNull Player player, @NotNull ShopClickAction click) {
//        Shop shop = this.getShop();
//        TradeType tradeType = click.getTradeType();
//        if (!shop.isTransactionEnabled(tradeType)) {
//            return;
//        }
//
//        if (tradeType == TradeType.BUY) {
//            if (!this.isBuyable()) {
//                Lang.SHOP_PRODUCT_ERROR_UNBUYABLE.getMessage().send(player);
//                return;
//            }
//            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && !this.hasSpace(player)) {
//                Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY.getMessage().send(player);
//                return;
//            }
//        }
//        else if (tradeType == TradeType.SELL) {
//            if (!this.isSellable()) {
//                Lang.SHOP_PRODUCT_ERROR_UNSELLABLE.getMessage().send(player);
//                return;
//            }
//        }
//
//        // For Virtual Shop will return either Stock or Player Limit amount.
//        // For Chest Shop will return inventory space or item amount.
//        int canPurchase = this.getAvailableAmount(player, tradeType);
//        if (canPurchase == 0) {
//            LangText msgStock;
//            if (tradeType == TradeType.BUY) {
//                msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK;
//            }
//            else {
//                if (shop instanceof ChestShop) {
//                    msgStock = Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE;
//                }
//                else msgStock = Lang.SHOP_PRODUCT_ERROR_FULL_STOCK;
//            }
//            msgStock.getMessage().send(player);
//            return;
//        }
//
//        boolean isSellAll = (click == ShopClickAction.SELL_ALL);
//        PreparedProduct prepared = this.getPrepared(player, tradeType, isSellAll);
//        if (click == ShopClickAction.BUY_SINGLE || click == ShopClickAction.SELL_SINGLE || prepared.isAll()) {
//            prepared.trade();
//
//            Menu menu = AbstractMenu.getMenu(player);
//            if (menu instanceof ShopLayout || menu instanceof ShopView) {
//                menu.flush(player);
//            }
//            return;
//        }
//        this.plugin.getShopManager().openProductCart(player, prepared);
//    }

    @Override
    public double getPrice(@NotNull TradeType tradeType, @Nullable Player player) {
        double price = this.getPricer().getPrice(tradeType);

        if (this instanceof VirtualProduct virtualProduct) {
            if (tradeType == TradeType.BUY && price > 0 && virtualProduct.isDiscountAllowed()) {
                price *= virtualProduct.getShop().getDiscountModifier();
            }
            if (tradeType == TradeType.SELL) {
                if (player != null) {
                    double sellModifier = VirtualShopModule.getSellMultiplier(player);
                    price *= sellModifier;
                }
            }
        }
        if (!this.getCurrency().decimalsAllowed()) {
            price = Math.floor(price);
        }

        return price;
    }

    @Override
    public void setPrice(@NotNull TradeType tradeType, double price) {
        if (!this.getCurrency().decimalsAllowed()) {
            price = Math.floor(price);
        }
        this.getPricer().setPrice(tradeType, price);
    }

    @Override
    public double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.countUnits(player);
        int amountCan = this.getAvailableAmount(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        double price = balance * this.getPriceSell(player);

        return Math.max(price, 0);
    }

    @Override
    public boolean isBuyable() {
        AbstractProductPricer pricer = this.getPricer();
        return pricer.getBuyPrice() >= 0D;
    }

    @Override
    public boolean isSellable() {
        AbstractProductPricer pricer = this.getPricer();
        double priceSell = pricer.getSellPrice();
        if (priceSell < 0D) {
            return false;
        }

        // Check if this product is buyable, so we can check if sell price is over the buy price
        // to prevent money duplication.
        // If this product can not be purchased, these checks are useless.
        if (this.getShop().isTransactionEnabled(TradeType.BUY) && this.isBuyable()) {
            return priceSell <= pricer.getBuyPrice();
        }

        return true;
    }


    @Override
    @NotNull
    public S getShop() {
        return this.shop;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public ProductHandler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(@NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this.handler = handler;
        this.packer = packer;
    }

    @NotNull
    @Override
    public ProductPacker getPacker() {
        return packer;
    }

    @Override
    @NotNull
    public AbstractProductPricer getPricer() {
        return this.pricer;
    }

    @Override
    public void setPricer(@NotNull AbstractProductPricer pricer) {
        this.pricer = pricer;
    }

    @Override
    @NotNull
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public void setCurrency(@NotNull Currency currency) {
        this.currency = currency;
    }
}
