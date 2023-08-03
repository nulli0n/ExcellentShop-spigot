package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.shop.chest.menu.ProductPriceMenu;
import su.nightexpress.nexshop.shop.chest.util.ShopUtils;
import su.nightexpress.nexshop.shop.price.FlatProductPricer;
import su.nightexpress.nexshop.shop.price.FloatProductPricer;
import su.nightexpress.nexshop.shop.util.TimeUtils;

import java.util.UUID;

public class ChestProduct extends Product<ChestProduct, ChestShop, ChestProductStock> implements ItemProduct {

    private ItemStack item;
    private ProductPriceMenu priceEditor;

    public ChestProduct(@NotNull Currency currency, @NotNull ItemStack item) {
        this(UUID.randomUUID().toString(), currency, item);
    }

    public ChestProduct(@NotNull String id, @NotNull Currency currency, @NotNull ItemStack item) {
        super(id, currency);
        this.setItem(item);
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
                pricer.setDays(TimeUtils.parseDays(cfg.getString(path + ".Purchase.Randomizer.Times.Days", "")));
                pricer.setTimes(TimeUtils.parseTimesOld(cfg.getStringList(path + ".Purchase.Randomizer.Times.Times")));
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

        String currencyId = cfg.getString(path + ".Currency", CurrencyManager.VAULT);
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(currencyId);
        if (currency == null || !ShopUtils.isAllowedCurrency(currency)) {
            currency = ShopUtils.getDefaultCurrency();
        }

        ItemStack item = cfg.getItemEncoded(path + ".Reward.Item");
        if (item == null) {
            throw new IllegalStateException("Invalid product item!");
        }

        ChestProduct product = new ChestProduct(id, currency, item);
        //product.setItemMetaEnabled(true);
        //product.setItem(item);
        product.setPricer(ProductPricer.read(cfg, path + ".Price"));
        product.setStock(new ChestProductStock());
        return product;
    }

    public static void write(@NotNull ChestProduct product, @NotNull JYML cfg, @NotNull String path) {
        ProductPricer pricer = product.getPricer();
        cfg.set(path + ".Currency", product.getCurrency().getId());
        cfg.set(path + ".Price.Type", pricer.getType().name());
        //cfg.set(path + ".Price", pricer);
        pricer.write(cfg, path + ".Price");
        cfg.setItemEncoded(path + ".Reward.Item", product.getItem());
    }

    public void clear() {
        if (this.priceEditor != null) {
            this.priceEditor.clear();
            this.priceEditor = null;
        }
    }

    @NotNull
    public ProductPriceMenu getPriceEditor() {
        if (this.priceEditor == null) {
            this.priceEditor = new ProductPriceMenu(this.getShop().plugin(), this);
        }
        return priceEditor;
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        int amount = this.getUnitAmount() * count;
        PlayerUtil.addItem(player, this.getItem(), amount);
    }

    @Override
    public void take(@NotNull Player player, int count) {
        int amount = this.getUnitAmount() * count;
        PlayerUtil.takeItem(player, this::isItemMatches, amount);
    }

    @Override
    public int count(@NotNull Player player) {
        return PlayerUtil.countItem(player, this::isItemMatches);
    }

    @Override
    @NotNull
    protected ChestProduct get() {
        return this;
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return PlayerUtil.countItemSpace(player, this.getItem()) > 0;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return new ItemStack(item);
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
        //this.item.setAmount(1);
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public void setRespectItemMeta(boolean respectItemMeta) {

    }

    @Override
    public boolean isRespectItemMeta() {
        return true;
    }

    /*@Override
    public void setStock(@NotNull ChestProductStock stock) {
        this.stock = stock;
        this.stock.setProduct(this);
    }*/

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(player, this, buyType, all);
    }
}
