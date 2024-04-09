package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.object.OwnedStockData;
import su.nightexpress.nexshop.data.object.StockData;
import su.nightexpress.nexshop.shop.impl.AbstractStock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VirtualStock extends AbstractStock<VirtualShop, VirtualProduct> {

    private final Map<TradeType, Map<String, StockData>>                 globalDataMap;
    private final Map<UUID, Map<TradeType, Map<String, OwnedStockData>>> playerDataMap;

    private boolean locked;

    public VirtualStock(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(plugin, shop);
        this.globalDataMap = new ConcurrentHashMap<>();
        this.playerDataMap = new ConcurrentHashMap<>();
        this.lock();
    }

    @Override
    public void load() {
        List<StockData> dataList = this.plugin.getData().getVirtualDataHandler().getStockDatas(this.getShop().getId());
        dataList.forEach(data -> {
            this.getGlobalDataMap(data.getTradeType()).put(data.getProductId(), data);
        });

        this.unlock();
        //this.plugin.info("Loaded " + dataList.size() + " product stock datas for '" + shop.getId() + " shop.");
    }

    @Override
    public void load(@NotNull UUID playerId) {
        this.playerDataMap.remove(playerId);

        List<OwnedStockData> dataList = this.plugin.getData().getVirtualDataHandler().getPlayerLimits(playerId);
        dataList.forEach(data -> {
            this.getPlayerDataMap(playerId, data.getTradeType()).put(data.getProductId(), data);
        });
    }

    @Override
    public void unload(@NotNull UUID playerId) {
        this.playerDataMap.remove(playerId);
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

        Player player = event.getPlayer();
        TradeType tradeType = event.getTransaction().getTradeType();
        int amount = event.getTransaction().getUnits();

        StockValues stockValues = product.getStockValues();
        if (!stockValues.isUnlimited(tradeType)) {
            this.consume(product, amount, tradeType);
        }
        if (!stockValues.isUnlimited(tradeType.getOpposite())) {
            this.store(product, amount, tradeType.getOpposite());
        }

        StockData globalData = this.getGlobalData(product, tradeType);
        if (globalData != null && globalData.isAwaiting()) {
            globalData.updateRestockDate(stockValues);
        }

        StockValues limitValues = product.getLimitValues();
        if (!limitValues.isUnlimited(tradeType)) {
            this.consume(product, amount, tradeType, player);
        }

        StockData playerData = this.getPlayerData(player.getUniqueId(), product, tradeType);
        if (playerData != null && playerData.isAwaiting()) {
            playerData.updateRestockDate(limitValues);
        }
    }

    @Override
    @Nullable
    protected VirtualProduct findProduct(@NotNull Product product) {
        return this.getShop().getProductById(product.getId());
    }

    @NotNull
    private Map<String, StockData> getGlobalDataMap(@NotNull TradeType type) {
        return this.globalDataMap.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
    }

    @NotNull
    private Map<String, OwnedStockData> getPlayerDataMap(@NotNull UUID playerId, @NotNull TradeType type) {
        return this.playerDataMap.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).computeIfAbsent(type, k -> new ConcurrentHashMap<>());
    }

    @Nullable
    public StockData getGlobalData(@NotNull VirtualProduct product, @NotNull TradeType type) {
        StockValues values = product.getStockValues();
        StockData data = this.getGlobalDataMap(type).get(product.getId());
        Supplier<StockData> supplier = () -> new StockData(product, values, type);

        return this.getData(type, values, data, supplier, this::createGlobalData);
    }

    @Nullable
    public OwnedStockData getPlayerData(@NotNull UUID playerId, @NotNull VirtualProduct product, @NotNull TradeType type) {
        StockValues values = product.getLimitValues();
        OwnedStockData data = this.getPlayerDataMap(playerId, type).get(product.getId());
        Supplier<OwnedStockData> supplier = () -> new OwnedStockData(playerId, product, values, type);

        return this.getData(type, values, data, supplier, this::createPlayerData);
    }

    @Nullable
    private <T extends StockData> T getData(@NotNull TradeType type,
                                            @NotNull StockValues values,
                                            @Nullable T data,
                                            @NotNull Supplier<T> supplier,
                                            @NotNull Consumer<T> creator) {
        if (values.isUnlimited(type)) return null;

        if (data == null) {
            data = supplier.get();
            data.setItemsLeft(values.getInitialAmount(type));
            creator.accept(data);
        }
        else if (data.isRestockTime()) {
            data.restock(values);
            this.saveData(data);
        }
        return data;
    }

    @Nullable
    public StockData getRelativeData(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        return player == null ? this.getGlobalData(product, type) : this.getPlayerData(player.getUniqueId(), product, type);
    }

    private void createGlobalData(@NotNull StockData data) {
        this.getGlobalDataMap(data.getTradeType()).put(data.getProductId(), data);
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().insertStockData(data));
    }

    private void createPlayerData(@NotNull OwnedStockData data) {
        this.getPlayerDataMap(data.getOwnerId(), data.getTradeType()).put(data.getProductId(), data);
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().insertPlayerLimit(data));
    }

    private void saveGlobalData(@NotNull StockData data) {
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().saveStockData(data));
    }

    private void savePlayerData(@NotNull OwnedStockData data) {
        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().savePlayerLimit(data));
    }

    public void deleteGlobalData(@NotNull Product product) {
        for (TradeType tradeType : TradeType.values()) {
            this.deleteGlobalData(product, tradeType);
        }
    }

    public void deleteGlobalData(@NotNull Product product, @NotNull TradeType tradeType) {
        this.getGlobalDataMap(tradeType).remove(product.getId());

        this.plugin.runTaskAsync(task -> this.plugin.getData().getVirtualDataHandler().deleteStockData(product, tradeType));
    }



    private void deletePlayerLimit(@NotNull VirtualProduct product) {
        for (TradeType tradeType : TradeType.values()) {
            this.deletePlayerLimit(product, tradeType);
        }
    }

    private void deletePlayerLimit(@NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        this.playerDataMap.values().forEach(map -> {
            map.getOrDefault(tradeType, Collections.emptyMap()).remove(product.getId());
        });

        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deletePlayerLimit(product, tradeType));
    }

    private void deletePlayerLimit(@NotNull UUID playerId, @NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        this.getPlayerDataMap(playerId, tradeType).remove(product.getId());

        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deletePlayerLimit(playerId, product, tradeType));
    }

    private void deletePlayerLimit(@NotNull UUID playerId) {
        this.playerDataMap.remove(playerId);
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deletePlayerLimit(playerId));
    }



    private void saveData(@NotNull StockData data) {
        if (data instanceof OwnedStockData ownedStockData) {
            this.savePlayerData(ownedStockData);
        }
        else this.saveGlobalData(data);
    }

    public void deleteData() {
        this.globalDataMap.clear();
        this.playerDataMap.clear();
        this.plugin.runTaskAsync(task -> {
            this.plugin.getData().getVirtualDataHandler().deleteStockData(shop);
            this.plugin.getData().getVirtualDataHandler().deletePlayerLimits(shop);
        });
    }


    private int getItemsLeft(@Nullable StockData data) {
        return data == null ? UNLIMITED : data.getItemsLeft();
    }


    public int countItem(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        if (this.isLocked()) return 0;

        return this.getItemsLeft(this.getRelativeData(product, type, player));
    }

    public boolean consumeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        if (this.isLocked()) return false;

        StockData data = this.getRelativeData(product, type, player);
        if (data == null) return false;

        data.setItemsLeft(data.getItemsLeft() - amount);
        this.saveData(data);
        return true;
    }

    public boolean storeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        if (this.isLocked()) return false;

        StockData data = this.getRelativeData(product, type, player);
        if (data == null) return false;

        data.setItemsLeft(data.getItemsLeft() + amount);
        this.saveData(data);
        return true;
    }

    public boolean restockItem(@NotNull VirtualProduct product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        if (this.isLocked()) return false;

        StockData data = this.getRelativeData(product, type, player);//this.getGlobalDataMap(type).get(product.getId());
        if (data == null) return false;

        if (force || data.isRestockTime()) {
            data.restock(player == null ? product.getStockValues() : product.getLimitValues());
            this.saveData(data);
            return true;
        }
        return false;
    }



    public long getRestockDate(@NotNull VirtualProduct product, @NotNull TradeType type) {
        return this.getRestockDate(product, type, null);
    }

    public long getRestockDate(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        StockData data = this.getRelativeData(product, type, player);
        return data == null ? 0L : data.getRestockDate();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
