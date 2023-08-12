package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.ProductStock;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.stock.ProductStockData;
import su.nightexpress.nexshop.data.stock.ProductStockStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VirtualProductStock<P extends VirtualProduct<P, ?>> extends ProductStock<P> {

    private final Map<StockType, Map<TradeType, Integer>> initialAmount;
    private final Map<StockType, Map<TradeType, Integer>> restockTime;

    public VirtualProductStock() {
        this.initialAmount = new HashMap<>();
        this.restockTime = new HashMap<>();
        this.lock();

        String never = LangManager.getPlain(Lang.OTHER_NEVER);
        String infin = LangManager.getPlain(Lang.OTHER_INFINITY);

        this.placeholderMap
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_INITIAL, () -> {
                int initialAmount = this.getInitialAmount(StockType.GLOBAL, TradeType.BUY);
                return initialAmount < 0 ? infin : String.valueOf(initialAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_INITIAL, () -> {
                int initialAmount = this.getInitialAmount(StockType.GLOBAL, TradeType.SELL);
                return initialAmount < 0 ? infin : String.valueOf(initialAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_INITIAL, () -> {
                int initialAmount = this.getInitialAmount(StockType.PLAYER, TradeType.BUY);
                return initialAmount < 0 ? infin : String.valueOf(initialAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_INITIAL, () -> {
                int initialAmount = this.getInitialAmount(StockType.PLAYER, TradeType.SELL);
                return initialAmount < 0 ? infin : String.valueOf(initialAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.BUY);
                return leftAmount < 0 ? infin : String.valueOf(leftAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.SELL);
                return leftAmount < 0 ? infin : String.valueOf(leftAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_TIME, () -> {
                long cooldown = this.getRestockCooldown(StockType.GLOBAL, TradeType.BUY) * 1000L;
                return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_TIME, () -> {
                long cooldown = this.getRestockCooldown(StockType.GLOBAL, TradeType.SELL) * 1000L;
                return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_TIME, () -> {
                long cooldown = this.getRestockCooldown(StockType.PLAYER, TradeType.BUY) * 1000L;
                return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_TIME, () -> {
                long cooldown = this.getRestockCooldown(StockType.PLAYER, TradeType.SELL) * 1000L;
                return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_DATE, () -> {
                long restockDate = this.getRestockDate(TradeType.BUY);
                return restockDate < 0 ? never : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_DATE, () -> {
                long restockDate = this.getRestockDate(TradeType.SELL);
                return restockDate < 0 ? never : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
            })
        ;
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        String never = LangManager.getPlain(Lang.OTHER_NEVER);
        String infinite = LangManager.getPlain(Lang.OTHER_INFINITY);
        return new PlaceholderMap(this.getPlaceholders())
            .add(Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.BUY, player);
                return leftAmount < 0 ? infinite : String.valueOf(leftAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.SELL, player);
                return leftAmount < 0 ? infinite : String.valueOf(leftAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_DATE, () -> {
                long restockDate = this.getRestockDate(TradeType.BUY, player);
                return restockDate < 0 ? never : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
            })
            .add(Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_DATE, () -> {
                long restockDate = this.getRestockDate(TradeType.SELL, player);
                return restockDate < 0 ? never : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
            })
            ;
    }

    @NotNull
    public static <P extends VirtualProduct<P, ?>> VirtualProductStock<P> read(@NotNull JYML cfg, @NotNull String path, @NotNull Class<P> cls) {
        VirtualProductStock<P> stock = new VirtualProductStock<>();
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

    public static void write(@NotNull VirtualProductStock<?> stock, @NotNull JYML cfg, @NotNull String path) {
        for (StockType stockType : StockType.values()) {
            for (TradeType tradeType : TradeType.values()) {
                cfg.set(path + "." + stockType.name() + "." + tradeType.name() + ".Initial_Amount", stock.getInitialAmount(stockType, tradeType));
                cfg.set(path + "." + stockType.name() + "." + tradeType.name() + ".Restock_Time", stock.getRestockCooldown(stockType, tradeType));
            }
        }
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
            ProductStockStorage.deleteData(holder, this.getProduct(), stockType, tradeType);
            return null;
        }

        // Получаем запись из БД о текущих лимитах.
        // Если такая есть, проверяем актуальность и пополняем если нужно.
        ProductStockData stockData = ProductStockStorage.getData(holder, this.getProduct().getId(), stockType, tradeType);
        if (stockData != null && stockData.isRestockTime()) {
            if (stockType == StockType.GLOBAL) {
                stockData.restock(this);
                ProductStockStorage.saveData(holder, stockData);
            }
            // Для Юзер стока удаляем запись вместо пополнения, чтобы не шло время обновления, пока не будет
            // совершена хотя бы одна покупка.
            else {
                ProductStockStorage.deleteData(holder, this.getProduct(), stockType, tradeType);
                return null;
            }
        }
        return stockData;
    }

    /**
     * Обрабатываем покупку продукта, обновляя лимиты у записи в БД.
     */
    @Override
    public void onPurchase(@NotNull ShopTransactionEvent<?> event) {
        TradeType tradeType = event.getResult().getTradeType();
        int amount = event.getResult().getUnits();
        Player player = event.getPlayer();

        int amountLeft = this.getLeftAmount(tradeType);
        int amountLeftOpp = this.getLeftAmount(tradeType.getOpposite());

        if (!this.isUnlimited(StockType.GLOBAL, tradeType)) {
            this.setLeftAmount(tradeType, amountLeft - amount);
        }
        if (!this.isUnlimited(StockType.GLOBAL, tradeType.getOpposite())) {
            this.setLeftAmount(tradeType.getOpposite(), amountLeftOpp + amount);
        }

        if (!this.isUnlimited(StockType.PLAYER, tradeType)) {
            int userAmountLeft = this.getLeftAmount(tradeType, player);
            //int amountLeftOpp = this.getLeftAmount(tradeType.getOpposite(), player);

            this.setLeftAmount(tradeType, userAmountLeft - amount, player);
            //this.setLeftAmount(tradeType.getOpposite(), amountLeftOpp + amount, player);
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
        int itemsLeft = stockType == StockType.GLOBAL ? amount : Math.min(initial, amount);
        if (stockData == null) {
            stockData = new ProductStockData(this, tradeType, stockType);
            stockData.setItemsLeft(itemsLeft);
            ProductStockStorage.createData(holder, stockData);
        }
        else {
            stockData.setItemsLeft(itemsLeft);
            ProductStockStorage.saveData(holder, stockData);
        }
    }

    @Override
    public long getRestockDate(@NotNull TradeType tradeType, @Nullable Player player) {
        ProductStockData stockData = this.getProductStockData(tradeType, player);
        return stockData == null ? 0L : stockData.getRestockDate();
    }
}
