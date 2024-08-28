package su.nightexpress.nexshop.product.data;

import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.product.data.impl.PriceData;
import su.nightexpress.nexshop.product.data.impl.StockData;

public class ProductData {

    // TODO Product UUID - Get rid of shopId Map

    private StockData stockData;
    private PriceData priceData;

    public ProductData() {

    }

    public void loadPrice(@Nullable PriceData priceData) {
        this.priceData = priceData;
    }

    public void loadStock(@Nullable StockData data) {
        this.stockData = data;
    }

    @Nullable
    public StockData getStockData() {
        return stockData;
    }

    @Nullable
    public PriceData getPriceData() {
        return priceData;
    }
}
