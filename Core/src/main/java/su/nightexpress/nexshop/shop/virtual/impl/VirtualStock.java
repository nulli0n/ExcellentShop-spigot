package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.data.ProductData;
import su.nightexpress.nexshop.product.stock.StockAmount;
import su.nightexpress.nexshop.product.data.impl.StockData;
import su.nightexpress.nexshop.shop.impl.AbstractStock;

public class VirtualStock extends AbstractStock<VirtualShop, VirtualProduct> {

    public VirtualStock(@NotNull ShopPlugin plugin, @NotNull VirtualShop shop) {
        super(plugin, shop);
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

        StockAmount globalAmount = this.getAmount(product, tradeType, null);
        if (globalAmount != null && globalAmount.isAwaiting()) {
            globalAmount.updateRestockDate(stockValues, tradeType);
        }

        StockValues limitValues = product.getLimitValues();
        if (!limitValues.isUnlimited(tradeType)) {
            this.consume(product, amount, tradeType, player);
        }

        StockAmount playerAmount = this.getAmount(product, tradeType, player);
        if (playerAmount != null && playerAmount.isAwaiting()) {
            playerAmount.updateRestockDate(limitValues, tradeType);
        }
    }

    @Override
    @Nullable
    protected VirtualProduct findProduct(@NotNull Product product) {
        return this.getShop().getProductById(product.getId());
    }

    @NotNull
    private ProductData getProductData(@NotNull Product product) {
        return this.getProductData(product.getId());
    }

    @NotNull
    private ProductData getProductData(@NotNull String productId) {
        return this.plugin.getShopManager().getProductDataManager().getData(this.shop.getId(), productId);
    }

    @Nullable
    private StockData getStockData(@NotNull Product product) {
        return this.getProductData(product).getStockData();
    }

    @Nullable
    public StockAmount getAmount(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        StockValues values = player == null ? product.getStockValues() : product.getLimitValues();
        if (values.isUnlimited(type)) return null;

        ProductData productData = this.getProductData(product);
        StockData data = productData.getStockData();

        if (data == null) {
            data = new StockData(product);
            productData.loadStock(data);
            this.insertData(data);
        }

        StockAmount amounts = player == null ? data.getGlobalAmount(type) : data.getPlayerAmount(type, player.getUniqueId());
        if (amounts.isRestockTime()) {
            amounts.restock(values, type);
            this.saveData(data);
        }

        return amounts;
    }

    private void insertData(@NotNull StockData data) {
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().insertStockData(data));
    }

    public void resetGlobalAmount(@NotNull Product product) {
        StockData data = this.getStockData(product);
        if (data == null) return;

        for (TradeType tradeType : TradeType.values()) {
            data.getGlobalAmounts().remove(tradeType);
        }

        this.saveData(data);
    }

    public void resetPlayerAmount(@NotNull VirtualProduct product) {
        this.resetPlayerAmount(product, null);
    }

    public void resetPlayerAmount(@NotNull VirtualProduct product, @Nullable TradeType tradeType) {
        StockData data = this.getStockData(product);
        if (data == null) return;

        for (TradeType type : TradeType.values()) {
            if (tradeType == null || tradeType == type) {
                data.getPlayerAmounts().remove(tradeType);
            }
        }

        this.saveData(data);
    }



    private void saveData(@NotNull StockData data) {
        data.cleanUp();
        this.plugin.getShopManager().getProductDataManager().scheduleSave(data);
    }

    private void saveData(@NotNull Product product) {
        StockData data = this.getStockData(product);
        if (data == null) return;

        this.saveData(data);
    }

    public void deleteData() {
        this.plugin.getShopManager().getProductDataManager().getDatas(this.shop).forEach(productData -> {
            productData.loadStock(null);
        });
        this.plugin.runTaskAsync(task -> plugin.getData().getVirtualDataHandler().deleteStockData(this.shop));
    }


    private int getItemsLeft(@Nullable StockAmount amounts) {
        return amounts == null ? UNLIMITED : amounts.getItemsLeft();
    }


    public int countItem(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        return this.getItemsLeft(this.getAmount(product, type, player));
    }

    public boolean consumeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        StockAmount amounts = this.getAmount(product, type, player);
        if (amounts == null) return false;

        amounts.setItemsLeft(amounts.getItemsLeft() - amount);
        this.saveData(product);
        return true;
    }

    public boolean storeItem(@NotNull VirtualProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        StockAmount amounts = this.getAmount(product, type, player);
        if (amounts == null) return false;

        amounts.setItemsLeft(amounts.getItemsLeft() + amount);
        this.saveData(product);
        return true;
    }

    public boolean restockItem(@NotNull VirtualProduct product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        StockAmount amounts = this.getAmount(product, type, player);
        if (amounts == null) return false;

        if (force || amounts.isRestockTime()) {
            amounts.restock(player == null ? product.getStockValues() : product.getLimitValues(), type);
            this.saveData(product);
            return true;
        }
        return false;
    }



    public long getRestockDate(@NotNull VirtualProduct product, @NotNull TradeType type) {
        return this.getRestockDate(product, type, null);
    }

    public long getRestockDate(@NotNull VirtualProduct product, @NotNull TradeType type, @Nullable Player player) {
        StockAmount amounts = this.getAmount(product, type, player);
        return amounts == null ? 0L : amounts.getRestockDate();
    }
}
