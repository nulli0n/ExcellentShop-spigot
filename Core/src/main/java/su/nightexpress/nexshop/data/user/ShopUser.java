package su.nightexpress.nexshop.data.user;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.TransactionListener;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.object.StockData;
import su.nightexpress.nexshop.api.shop.Transaction;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopUser extends AbstractUser<ExcellentShop> implements TransactionListener {

    private final Map<TradeType, Map<String, StockData>> productLimitMap;
    private final UserSettings                           settings;

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name) {
        this(
            plugin, uuid, name,
            System.currentTimeMillis(), System.currentTimeMillis(),
            new UserSettings(true, true)
        );
    }

    public ShopUser(@NotNull ExcellentShop plugin,
                    @NotNull UUID uuid,
                    @NotNull String name,
                    long dateCreated, long lastOnline,
                    @NotNull UserSettings settings
    ) {
        super(plugin, uuid, name, dateCreated, lastOnline);
        this.productLimitMap = new ConcurrentHashMap<>();
        this.settings = settings;
        this.loadProductLimits();
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        if (!(result.getProduct() instanceof VirtualProduct product)) return;

        TradeType tradeType = event.getTransaction().getTradeType();
        int amount = event.getTransaction().getUnits();
        Player player = event.getPlayer();

        StockValues values = product.getLimitValues();
        if (!values.isUnlimited(tradeType)) {
            StockData data = this.getProductLimit(product, tradeType);
            if (data != null) {
                data.setItemsLeft(data.getItemsLeft() - amount);
                this.saveProductLimit(data);
            }
        }
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }

    @NotNull
    public Map<TradeType, Map<String, StockData>> getProductLimitMap() {
        return productLimitMap;
    }

    public void loadProductLimits() {
        this.plugin.runTaskAsync(task -> {
            this.plugin.getData().getVirtualDataHandler().getPlayerLimits(this.getId()).forEach(this::addProductLimit);
        });
    }

    private void addProductLimit(@NotNull StockData data) {
        this.getProductLimitMap().computeIfAbsent(data.getTradeType(), k -> new ConcurrentHashMap<>()).put(data.getProductId(), data);
    }

    @Nullable
    public StockData getProductLimit(@NotNull String productId, @NotNull TradeType tradeType) {
        return this.getProductLimitMap().getOrDefault(tradeType, Collections.emptyMap()).get(productId);
    }

    @Nullable
    public StockData getProductLimit(@NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        StockValues values = product.getLimitValues();
        if (values.isUnlimited(tradeType)) return null;

        StockData data = this.getProductLimit(product.getId(), tradeType);
        if (data == null) {
            data = new StockData(product, product.getLimitValues(), tradeType);
            data.setItemsLeft(values.getInitialAmount(tradeType));
            this.createProductLimit(data);
        }
        else if (data.isRestockTime()) {
            data.restock(product.getLimitValues());
            this.saveProductLimit(data);
        }
        return data;
    }

    public void createProductLimit(@NotNull StockData data) {
        if (this.getProductLimit(data.getProductId(), data.getTradeType()) != null) return;

        this.addProductLimit(data);
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().insertPlayerLimit(this.getId(), data));
    }

    public void saveProductLimit(@NotNull StockData stockData) {
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().savePlayerLimit(this.getId(), stockData));
    }

    public void deleteProductLimit(@NotNull VirtualProduct product) {
        for (TradeType tradeType : TradeType.values()) {
            this.deleteProductLimit(product, tradeType);
        }
    }

    public void deleteProductLimit(@NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        this.getProductLimitMap().getOrDefault(tradeType, Collections.emptyMap()).remove(product.getId());
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deletePlayerLimit(this.getId(), product, tradeType));
    }

    public void deleteProductLimits() {
        this.getProductLimitMap().clear();
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deletePlayerLimit(this.getId()));
    }
}
