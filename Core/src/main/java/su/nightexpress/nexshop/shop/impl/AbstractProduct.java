package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.impl.ItemCurrency;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.price.FlatPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public abstract class AbstractProduct<S extends AbstractShop<?>> implements Product, Placeholder {

    protected final ExcellentShop plugin;
    protected final String id;
    protected final PlaceholderMap placeholderMap;

    protected S                     shop;
    protected Currency              currency;
    protected ProductHandler        handler;
    protected ProductPacker         packer;
    protected AbstractProductPricer pricer;

    public AbstractProduct(@NotNull ExcellentShop plugin, @NotNull String id, @NotNull S shop, @NotNull Currency currency,
                           @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this.plugin = plugin;
        this.id = id.toLowerCase();
        this.shop = shop;
        this.setCurrency(currency);
        this.setPricer(new FlatPricer());
        this.setHandler(handler);
        this.packer = packer;

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.PRODUCT_HANDLER, () -> this.getHandler().getName())
            .add(Placeholders.PRODUCT_CURRENCY, () -> this.getCurrency().getName())
            .add(Placeholders.PRODUCT_PRICE_BUY, () -> NumberUtil.format(this.getPricer().getBuyPrice()))
            .add(Placeholders.PRODUCT_PRICE_BUY_FORMATTED, () -> this.getPricer().getBuyPrice() >= 0 ? getCurrency().format(this.getPricer().getBuyPrice()) : "-")
            .add(Placeholders.PRODUCT_PRICE_SELL, () -> NumberUtil.format(this.getPricer().getSellPrice()))
            .add(Placeholders.PRODUCT_PRICE_SELL_FORMATTED, () -> this.getPricer().getSellPrice() >= 0 ? getCurrency().format(this.getPricer().getSellPrice()) : "-")
            .add(Placeholders.PRODUCT_PRICE_TYPE, () -> getShop().plugin().getLangManager().getEnum(this.getPricer().getType()))
            .add(Placeholders.PRODUCT_PREVIEW_NAME, () -> ItemUtil.getItemName(this.getPreview()))
            .add(Placeholders.PRODUCT_PREVIEW_LORE, () -> String.join("\n", ItemUtil.getLore(this.getPreview())));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        PlaceholderMap map = new PlaceholderMap(this.placeholderMap);
        map.add(this.getPacker().getPlaceholders());
        map.add(this.getPricer().getPlaceholders());
        return map;
    }

    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        PlaceholderMap placeholderMap = new PlaceholderMap();
        placeholderMap
            .add(Placeholders.PRODUCT_PRICE_TYPE, () -> getShop().plugin().getLangManager().getEnum(this.getPricer().getType()))
            .add(Placeholders.PRODUCT_PREVIEW_NAME, () -> ItemUtil.getItemName(this.getPreview()))
            .add(Placeholders.PRODUCT_PREVIEW_LORE, () -> String.join("\n", ItemUtil.getLore(this.getPreview())))
            .add(Placeholders.PRODUCT_CURRENCY, () -> this.getCurrency().getName())
            .add(Placeholders.PRODUCT_PRICE_BUY, () -> NumberUtil.format(this.getPriceBuy(player)))
            .add(Placeholders.PRODUCT_PRICE_BUY_FORMATTED, () -> this.getPriceBuy(player) >= 0 ? getCurrency().format(this.getPriceBuy(player)) : "-")
            .add(Placeholders.PRODUCT_PRICE_SELL, () -> NumberUtil.format(this.getPriceSell(player)))
            .add(Placeholders.PRODUCT_PRICE_SELL_FORMATTED, () -> this.getPriceSell(player) >= 0 ? getCurrency().format(this.getPriceSell(player)) : "-")
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL, () -> NumberUtil.format(this.getPriceSellAll(player)))
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED, () -> {
                return this.getPriceSellAll(player) >= 0 ? this.getCurrency().format(this.getPriceSellAll(player)) : "-";
            })
        ;
        return placeholderMap;
    }

    //@NotNull
    //public abstract PreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);

    @Override
    public void prepareTrade(@NotNull Player player, @NotNull ShopClickAction click) {
        Shop shop = this.getShop();
        TradeType tradeType = click.getTradeType();
        if (!shop.isTransactionEnabled(tradeType)) {
            return;
        }

        if (tradeType == TradeType.BUY) {
            if (!this.isBuyable()) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_UNBUYABLE).send(player);
                return;
            }
            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && !this.hasSpace(player)) {
                this.getShop().plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player);
                return;
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!this.isSellable()) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_UNSELLABLE).send(player);
                return;
            }
        }

        // For Virtual Shop will return either Stock or Player Limit amount.
        // For Chest Shop will return inventory space or item amount.
        int canPurchase = this.getAvailableAmount(player, tradeType);
        if (canPurchase == 0) {
            LangMessage msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK);
            }
            else {
                if (shop instanceof ChestShop) {
                    msgStock = plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE);
                }
                else msgStock = plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_STOCK);
            }
            msgStock.send(player);
            return;
        }

        boolean isSellAll = (click == ShopClickAction.SELL_ALL);
        PreparedProduct prepared = this.getPrepared(player, tradeType, isSellAll);
        if (click == ShopClickAction.BUY_SINGLE || click == ShopClickAction.SELL_SINGLE || prepared.isAll()) {
            prepared.trade();

            MenuViewer viewer = shop.getView().getViewer(player);
            if (viewer != null) {
                shop.open(player, viewer.getPage()); // Update current shop page
            }
            return;
        }
        this.openTrade(player, prepared);
    }

    public void openTrade(@NotNull Player player, @NotNull PreparedProduct prepared) {
        this.getShop().plugin().getCartMenu().open(player, prepared);
    }

    @Override
    public double getPrice(@NotNull Player player, @NotNull TradeType tradeType) {
        double price = this.getPricer().getPrice(tradeType);

        if (this instanceof VirtualProduct virtualProduct) {
            if (tradeType == TradeType.BUY && price > 0 && virtualProduct.isDiscountAllowed()) {
                price *= virtualProduct.getShop().getDiscountModifier();
            }
            if (tradeType == TradeType.SELL) {
                double sellModifier = VirtualShopModule.getSellMultiplier(player);
                price *= sellModifier;
            }
        }
        if (this.getCurrency() instanceof ItemCurrency) {
            price = Math.floor(price);
        }

        return price;
    }

    @Override
    public void setPrice(@NotNull TradeType tradeType, double price) {
        if (this.getCurrency() instanceof ItemCurrency) {
            price = (int) Math.floor(price);
        }
        this.getPricer().setPrice(tradeType, price);
    }

    @Override
    public double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.countUnits(player);
        int amountCan = this.getAvailableAmount(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        return balance * this.getPriceSell(player);
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
    public void setHandler(@NotNull ProductHandler handler) {
        this.handler = handler;
        this.packer = handler.createPacker();
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
