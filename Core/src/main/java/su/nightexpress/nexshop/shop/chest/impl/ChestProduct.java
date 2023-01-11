package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyId;
import su.nightexpress.nexshop.shop.FlatProductPricer;
import su.nightexpress.nexshop.shop.FloatProductPricer;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.editor.menu.ShopProductEditor;

import java.util.UUID;

public class ChestProduct extends Product<ChestProduct, ChestShop, ChestProductStock> {

    private ShopProductEditor editor;

    public ChestProduct(@NotNull ICurrency currency, @NotNull ItemStack item) {
        this(UUID.randomUUID().toString(), currency, item);
    }

    public ChestProduct(@NotNull String id, @NotNull ICurrency currency, @NotNull ItemStack item) {
        super(id, item, currency);
        this.setItem(item);
        this.setItemMetaEnabled(true);
    }

    @NotNull
    public static ChestProduct read(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        if (cfg.contains(path + ".Purchase")) {
            cfg.addMissing(path + ".Currency", cfg.getString(path + ".Purchase.Currency"));
            double buyMin = cfg.getDouble(path + ".Purchase.BUY.Price_Min");
            double buyMax = cfg.getDouble(path + ".Purchase.BUY.Price_Max");
            double sellMin = cfg.getDouble(path + ".Purchase.SELL.Price_Min");
            double sellMax = cfg.getDouble(path + ".Purchase.SELL.Price_Max");

            if (cfg.getBoolean(path + ".Purchase.Randomizer.Enabled")) {
                FloatProductPricer pricer = new FloatProductPricer();
                pricer.setPriceMin(TradeType.BUY, buyMin);
                pricer.setPriceMax(TradeType.BUY, buyMax);
                pricer.setPriceMin(TradeType.SELL, sellMin);
                pricer.setPriceMin(TradeType.SELL, sellMax);
                pricer.setDays(IScheduled.parseDays(cfg.getString(path + ".Purchase.Randomizer.Times.Days", "")));
                pricer.setTimes(IScheduled.parseTimesOld(cfg.getStringList(path + ".Purchase.Randomizer.Times.Times")));
                cfg.addMissing(path + ".Price.Type", pricer.getType().name());
                pricer.write(cfg, path + ".Price");
            }
            else {
                FlatProductPricer pricer = new FlatProductPricer();
                pricer.setPrice(TradeType.BUY, buyMin);
                pricer.setPrice(TradeType.SELL, sellMin);
                cfg.addMissing(path + ".Price.Type", pricer.getType().name());
                pricer.write(cfg, path + ".Price");
            }

            cfg.remove(path + ".Purchase");
            cfg.saveChanges();
        }

        String currencyId = cfg.getString(path + ".Currency", CurrencyId.VAULT);
        ICurrency currency = ShopAPI.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            currency = ChestConfig.DEFAULT_CURRENCY;
        }

        ItemStack item = cfg.getItemEncoded(path + ".Reward.Item");
        if (item == null) {
            throw new IllegalStateException("Invalid product item!");
        }

        ChestProduct product = new ChestProduct(id, currency, item);
        //product.setItemMetaEnabled(true);
        //product.setItem(item);

        PriceType priceType = cfg.getEnum(path + ".Price.Type", PriceType.class, PriceType.FLAT);
        product.setPricer(ProductPricer.read(priceType, cfg, path + ".Price"));
        product.setStock(new ChestProductStock());
        return product;
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        ProductPricer pricer = this.getPricer();
        cfg.set(path + ".Currency", this.getCurrency().getId());
        cfg.set(path + ".Price.Type", pricer.getType().name());
        cfg.set(path + ".Price", pricer);
        cfg.setItemEncoded(path + ".Reward.Item", this.getItem());
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @Override
    @NotNull
    public ShopProductEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ShopProductEditor(this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    protected ChestProduct get() {
        return this;
    }

    @Override
    public void setStock(@NotNull ChestProductStock stock) {
        this.stock = stock;
        this.stock.setProduct(this);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull TradeType buyType) {
        return new ChestPreparedProduct(this, buyType);
    }
}
