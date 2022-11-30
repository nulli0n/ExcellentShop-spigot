package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.ProductStock;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.stock.ProductStockData;
import su.nightexpress.nexshop.data.stock.ProductStockManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class VirtualProductStock extends ProductStock<VirtualProduct> {

    private final Map<StockType, Map<TradeType, Integer>> initialAmount;
    private final Map<StockType, Map<TradeType, Integer>> restockTime;

    public VirtualProductStock() {
        this.initialAmount = new HashMap<>();
        this.restockTime = new HashMap<>();
        this.lock();
    }

    @NotNull
    public static VirtualProductStock read(@NotNull JYML cfg, @NotNull String path) {
        VirtualProductStock stock = new VirtualProductStock();
        for (StockType stockType : StockType.values()) {
            for (TradeType tradeType : TradeType.values()) {
                String path2 = path + "." + stockType.name() + "." + tradeType.name() + ".";
                int stockInitAmount = cfg.getInt(path2 + "Initial_Amount", -1);
                int stockRestockTime = cfg.getInt(path2 + "Restock_Time", 0);
                stock.setInitialAmount(stockType, tradeType, stockInitAmount);
                stock.setRestockCooldown(stockType, tradeType, stockRestockTime);
            }
        }
        return stock;
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        for (StockType stockType : StockType.values()) {
            for (TradeType tradeType : TradeType.values()) {
                cfg.set(path + "." + stockType.name() + "." + tradeType.name() + ".Initial_Amount", this.getInitialAmount(stockType, tradeType));
                cfg.set(path + "." + stockType.name() + "." + tradeType.name() + ".Restock_Time", this.getRestockCooldown(stockType, tradeType));
            }
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        ExcellentShop plugin = ShopAPI.PLUGIN;

        int stockGLBuyAmountInit = this.getInitialAmount(StockType.GLOBAL, TradeType.BUY);
        int stockGLSellAmountInit = this.getInitialAmount(StockType.GLOBAL, TradeType.SELL);
        int stockPLBuyAmountInit = this.getInitialAmount(StockType.PLAYER, TradeType.BUY);
        int stockPLSellAmountInit = this.getInitialAmount(StockType.PLAYER, TradeType.SELL);

        int stockGLBuyAmountLeft = this.getLeftAmount(TradeType.BUY);
        int stockGLSellAmountLeft = this.getLeftAmount(TradeType.SELL);

        long stockGLBuyRestockTime = this.getRestockCooldown(StockType.GLOBAL, TradeType.BUY) * 1000L;
        long stockGLSellRestockTime = this.getRestockCooldown(StockType.GLOBAL, TradeType.SELL) * 1000L;
        long stockPLBuyRestockTime = this.getRestockCooldown(StockType.PLAYER, TradeType.BUY) * 1000L;
        long stockPLSellRestockTime = this.getRestockCooldown(StockType.PLAYER, TradeType.SELL) * 1000L;

        long stockGLBuyRestockDate = this.getRestockDate(TradeType.BUY);
        long stockGLSellRestockDate = this.getRestockDate(TradeType.SELL);

        String never = plugin.getMessage(Lang.OTHER_NEVER).getLocalized();
        String infin = plugin.getMessage(Lang.OTHER_INFINITY).getLocalized();

        return str -> str
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_INITIAL, stockGLBuyAmountInit < 0 ? infin : String.valueOf(stockGLBuyAmountInit))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_INITIAL, stockGLSellAmountInit < 0 ? infin : String.valueOf(stockGLSellAmountInit))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_INITIAL, stockPLBuyAmountInit < 0 ? infin : String.valueOf(stockPLBuyAmountInit))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_INITIAL, stockPLSellAmountInit < 0 ? infin : String.valueOf(stockPLSellAmountInit))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_LEFT, stockGLBuyAmountLeft < 0 ? infin : String.valueOf(stockGLBuyAmountLeft))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_LEFT, stockGLSellAmountLeft < 0 ? infin : String.valueOf(stockGLSellAmountLeft))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_TIME, stockGLBuyRestockTime < 0 ? never : TimeUtil.formatTime(stockGLBuyRestockTime))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_TIME, stockGLSellRestockTime < 0 ? never : TimeUtil.formatTime(stockGLSellRestockTime))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_TIME, stockPLBuyRestockTime < 0 ? never : TimeUtil.formatTime(stockPLBuyRestockTime))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_TIME, stockPLSellRestockTime < 0 ? never : TimeUtil.formatTime(stockPLSellRestockTime))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_DATE, stockGLBuyRestockDate < 0 ? never : stockGLBuyRestockDate == 0 ? "-" : TimeUtil.formatTimeLeft(stockGLBuyRestockDate))
            .replace(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_DATE, stockGLSellRestockDate < 0 ? never : stockGLSellRestockDate == 0 ? "-" : TimeUtil.formatTimeLeft(stockGLSellRestockDate))
            ;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        ExcellentShop plugin = ShopAPI.PLUGIN;

        int stockPLBuyAmountLeft = this.getLeftAmount(TradeType.BUY, player);
        int stockPLSellAmountLeft = this.getLeftAmount(TradeType.SELL, player);

        long stockPLBuyRestockDate = this.getRestockDate(TradeType.BUY, player);
        long stockPLSellRestockDate = this.getRestockDate(TradeType.SELL, player);

        String never = plugin.getMessage(Lang.OTHER_NEVER).getLocalized();
        String infin = plugin.getMessage(Lang.OTHER_INFINITY).getLocalized();

        return str -> this.replacePlaceholders().apply(str)
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_LEFT, stockPLBuyAmountLeft < 0 ? infin : String.valueOf(stockPLBuyAmountLeft))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_LEFT, stockPLSellAmountLeft < 0 ? infin : String.valueOf(stockPLSellAmountLeft))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_DATE, stockPLBuyRestockDate < 0 ? never : stockPLBuyRestockDate == 0 ? "-" : TimeUtil.formatTimeLeft(stockPLBuyRestockDate))
            .replace(Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_DATE, stockPLSellRestockDate < 0 ? never : stockPLSellRestockDate == 0 ? "-" : TimeUtil.formatTimeLeft(stockPLSellRestockDate))
            ;
    }

    @Nullable
    public ProductStockData getProductStockData(@NotNull TradeType tradeType, @Nullable Player player) {
        StockType stockType = player == null ? StockType.GLOBAL : StockType.PLAYER;
        String holder = player == null ? this.getProduct().getShop().getId() : player.getUniqueId().toString();

        return this.getProductStockData(holder, stockType, tradeType);
    }

    @Nullable
    private ProductStockData getProductStockData(@NotNull String holder,
                                                 @NotNull StockType stockType, @NotNull TradeType tradeType) {
        // Если лимит не установлен, то и записи в БД нет.
        if (this.isUnlimited(stockType, tradeType)) {
            ProductStockManager.removeProductStockData(holder, this.getProduct(), stockType, tradeType);
            return null;
        }

        // Получаем запись из БД о текущих лимитах.
        // Если такая есть, проверяем актуальность и пополняем если нужно.
        ProductStockData stockData = ProductStockManager.getData(holder, this.getProduct().getId(), stockType, tradeType);
        if (stockData != null && stockData.isRestockTime()) {
            if (stockType == StockType.GLOBAL) {
                stockData.restock(this);
                ProductStockManager.saveProductStockData(holder, stockData);
            }
            // Для Юзер стока удаляем запись вместо пополнения, чтобы не шло время обновления, пока не будет
            // совершена хотя бы одна покупка.
            else {
                ProductStockManager.removeProductStockData(holder, this.getProduct(), stockType, tradeType);
                return null;
            }
        }
        return stockData;
    }

    /**
     * Обрабатываем покупку продукта, обновляя лимиты у записи в БД.
     */
    @Override
    public void onPurchase(@NotNull ShopPurchaseEvent event) {
        TradeType tradeType = event.getTradeType();
        int amount = event.getPrepared().getAmount();
        Player player = event.getPlayer();

        if (!this.isUnlimited(StockType.GLOBAL, tradeType)) {
            int amountLeft = this.getLeftAmount(tradeType);
            int amountLeftOpp = this.getLeftAmount(tradeType.getOpposite());

            this.setLeftAmount(tradeType, amountLeft - amount);
            this.setLeftAmount(tradeType.getOpposite(), amountLeftOpp + amount);
        }
        if (!this.isUnlimited(StockType.PLAYER, tradeType)) {
            int amountLeft = this.getLeftAmount(tradeType, player);
            int amountLeftOpp = this.getLeftAmount(tradeType.getOpposite(), player);

            this.setLeftAmount(tradeType, amountLeft - amount, player);
            this.setLeftAmount(tradeType.getOpposite(), amountLeftOpp + amount, player);
        }
    }

    @Override
    public int getInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return this.initialAmount.getOrDefault(stockType, Collections.emptyMap()).getOrDefault(tradeType, -1);
    }

    @Override
    public void setInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType, int amount) {
        this.initialAmount.computeIfAbsent(stockType, k -> new HashMap<>()).put(tradeType, amount);
    }

    @Override
    public int getRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return this.restockTime.getOrDefault(stockType, Collections.emptyMap()).getOrDefault(tradeType, 0);
    }

    @Override
    public void setRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType, int cooldown) {
        this.restockTime.computeIfAbsent(stockType, k -> new HashMap<>()).put(tradeType, cooldown);
    }

    @Override
    public int getPossibleAmount(@NotNull TradeType tradeType, @NotNull Player player) {
        if (this.isLocked()) return 0;

        int itemsLeftGlobal = this.getLeftAmount(tradeType);
        int itemsLeftUser = this.getLeftAmount(tradeType, player);

        // Если есть оба лимита (Глобал и Юзер), возвращаем наименьший от их остатка.
        if (itemsLeftGlobal >= 0 && itemsLeftUser >= 0) return Math.min(itemsLeftGlobal, itemsLeftUser);

        // В противном случае возвращаем тот, где есть лимит.
        return itemsLeftGlobal < 0 ? itemsLeftUser : itemsLeftGlobal;
    }

    @Override
    public int getLeftAmount(@NotNull TradeType tradeType, @Nullable Player player) {
        StockType stockType = player == null ? StockType.GLOBAL : StockType.PLAYER;
        ProductStockData stockData = this.getProductStockData(tradeType, player);

        return stockData == null ? this.getInitialAmount(stockType, tradeType) : stockData.getItemsLeft();
    }

    @Override
    public void setLeftAmount(@NotNull TradeType tradeType, int amount, @Nullable Player player) {
        if (this.isLocked()) return;

        StockType stockType = player == null ? StockType.GLOBAL : StockType.PLAYER;
        String holder = player == null ? this.getProduct().getShop().getId() : player.getUniqueId().toString();
        ProductStockData stockData = this.getProductStockData(holder, stockType, tradeType);

        int initial = this.getInitialAmount(stockType, tradeType);
        if (stockData == null) {
            stockData = new ProductStockData(this, tradeType, stockType);
            stockData.setItemsLeft(Math.min(initial, amount));
            ProductStockManager.createProductStockData(holder, stockData);
        }
        else {
            stockData.setItemsLeft(Math.min(initial, amount));
            ProductStockManager.saveProductStockData(holder, stockData);
        }
    }

    @Override
    public long getRestockDate(@NotNull TradeType tradeType, @Nullable Player player) {
        ProductStockData stockData = this.getProductStockData(tradeType, player);
        return stockData == null ? 0L : stockData.getRestockDate();
    }
}
