package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.shop.impl.AbstractStock;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.object.StockData;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.api.shop.Transaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualStock extends AbstractStock<VirtualShop, VirtualProduct> {

    private final Map<TradeType, Map<String, StockData>> dataMap;

    private boolean locked;

    public VirtualStock(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(plugin, shop);
        this.dataMap = new ConcurrentHashMap<>();
        this.lock();
    }

    @Override
    public void load() {
        List<StockData> dataList = this.plugin.getData().getVirtualDataHandler().getStockDatas(this.getShop().getId());
        dataList.forEach(data -> {
            this.getDataMap(data.getTradeType()).put(data.getProductId(), data);
        });

        this.unlock();
        //this.plugin.info("Loaded " + dataList.size() + " product stock datas for '" + shop.getId() + " shop.");
    }

    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull VirtualProduct product) {
        String never = LangManager.getPlain(Lang.OTHER_NEVER);
        String infin = LangManager.getPlain(Lang.OTHER_INFINITY);

        PlaceholderMap placeholderMap = new PlaceholderMap();

        for (TradeType tradeType : TradeType.values()) {
            placeholderMap
                .add(Placeholders.PRODUCT_STOCK_AMOUNT_INITIAL.apply(tradeType), () -> {
                    int initialAmount = product.getStockValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infin : String.valueOf(initialAmount);
                })
                .add(Placeholders.PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), () -> {
                    int leftAmount = this.countItem(product, tradeType);
                    return leftAmount < 0 ? infin : String.valueOf(leftAmount);
                })
                .add(Placeholders.PRODUCT_STOCK_RESTOCK_TIME.apply(tradeType), () -> {
                    long cooldown = product.getStockValues().getRestockTime(tradeType) * 1000L;
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                })
                .add(Placeholders.PRODUCT_STOCK_RESTOCK_DATE.apply(tradeType), () -> {
                    long restockDate = this.getRestockDate(product, tradeType);
                    return restockDate < 0 ? never : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
                })
                .add(Placeholders.PRODUCT_LIMIT_AMOUNT_INITIAL.apply(tradeType), () -> {
                    int initialAmount = product.getLimitValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infin : String.valueOf(initialAmount);
                })
                .add(Placeholders.PRODUCT_LIMIT_RESTOCK_TIME.apply(tradeType), () -> {
                    long cooldown = product.getLimitValues().getRestockTime(tradeType) * 1000L;
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                });
        }

        return placeholderMap;
    }

    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player, @NotNull VirtualProduct product) {
        PlaceholderMap placeholderMap = new PlaceholderMap(this.getPlaceholders(product));
        ShopUser user = plugin.getUserManager().getUserData(player);

        for (TradeType tradeType : TradeType.values()) {
            placeholderMap
                .add(Placeholders.PRODUCT_LIMIT_AMOUNT_LEFT.apply(tradeType), () -> {
                    StockData data = user.getProductLimit(product, tradeType);
                    int leftAmount = data == null ? 0 : data.getItemsLeft();
                    return leftAmount < 0 ? LangManager.getPlain(Lang.OTHER_INFINITY) : String.valueOf(leftAmount);
                })
                .add(Placeholders.PRODUCT_LIMIT_RESTOCK_DATE.apply(tradeType), () -> {
                    StockData data = user.getProductLimit(product, tradeType);
                    long restockDate = data == null ? 0 : data.getRestockDate();
                    return restockDate < 0 ? LangManager.getPlain(Lang.OTHER_NEVER) : restockDate == 0 ? "-" : TimeUtil.formatTimeLeft(restockDate);
                });
        }

        return placeholderMap;
    }

    public void unlock() {
        this.setLocked(false);
    }

    public void lock() {
        this.setLocked(true);
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        if (!(result.getProduct() instanceof VirtualProduct product)) return;

        TradeType tradeType = event.getTransaction().getTradeType();
        int amount = event.getTransaction().getUnits();
        Player player = event.getPlayer();

        StockValues values = product.getStockValues();
        if (!values.isUnlimited(tradeType)) {
            this.consume(product, amount, tradeType);
        }
        if (!values.isUnlimited(tradeType.getOpposite())) {
            this.store(product, amount, tradeType.getOpposite());
        }
    }

    @Override
    @Nullable
    protected VirtualProduct findProduct(@NotNull Product product) {
        return this.getShop().getProductById(product.getId());
    }

    @NotNull
    private Map<String, StockData> getDataMap(@NotNull TradeType type) {
        return this.dataMap.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
    }

    @Nullable
    public StockData getData(@NotNull VirtualProduct product, @NotNull TradeType type) {
        StockValues values = product.getStockValues();
        if (values.isUnlimited(type)) return null;

        StockData data = this.getDataMap(type).get(product.getId());
        if (data == null) {
            data = new StockData(product, product.getStockValues(), type);
            data.setItemsLeft(values.getInitialAmount(type));
            this.createData(data);
        }
        else if (data.isRestockTime()) {
            data.restock(product.getStockValues());
            this.saveData(data);
        }
        return data;
    }

    private void createData(@NotNull StockData data) {
        this.getDataMap(data.getTradeType()).put(data.getProductId(), data);
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().insertStockData(data));
    }

    private void saveData(@NotNull StockData data) {
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().saveStockData(data));
    }

    public void deleteData(@NotNull Product product) {
        for (TradeType tradeType : TradeType.values()) {
            this.deleteData(product, tradeType);
        }
    }

    public void deleteData(@NotNull Product product, @NotNull TradeType tradeType) {
        this.getDataMap(tradeType).remove(product.getId());
        //StockData data = this.getDataMap(tradeType).remove(product.getId());
        //if (data == null) return;

        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().deleteStockData(product, tradeType));
    }

    public void deleteData() {
        this.dataMap.clear();
        this.plugin.runTaskAsync(task -> {
            this.plugin.getData().getVirtualDataHandler().deleteStockData(shop);
            this.plugin.getUserManager().getUsersLoaded().forEach(user -> {
                this.shop.getProducts().forEach(user::deleteProductLimit);
            });
        });
    }

    @Override
    public int countItem(@NotNull VirtualProduct product, @NotNull TradeType type) {
        if (this.isLocked()) return 0;

        StockData data = this.getData(product, type);
        return data == null ? -1 : data.getItemsLeft();
    }

    @Override
    public boolean consumeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type) {
        if (this.isLocked()) return false;

        StockData data = this.getData(product, type);
        if (data == null) return false;

        data.setItemsLeft(data.getItemsLeft() - amount);
        this.saveData(data);
        return true;
    }

    @Override
    public boolean storeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type) {
        if (this.isLocked()) return false;

        StockData data = this.getData(product, type);
        if (data == null) return false;

        data.setItemsLeft(data.getItemsLeft() + amount);
        this.saveData(data);
        return true;
    }

    @Override
    public boolean restockItem(@NotNull VirtualProduct product, @NotNull TradeType type, boolean force) {
        if (this.isLocked()) return false;

        StockData data = this.getDataMap(type).get(product.getId());
        if (data == null) return false;

        if (force || data.isRestockTime()) {
            data.restock(product.getStockValues());
            this.saveData(data);
            return true;
        }
        return false;
    }

    public long getRestockDate(@NotNull VirtualProduct product, @NotNull TradeType type) {
        StockData data = this.getData(product, type);
        return data == null ? 0L : data.getRestockDate();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
