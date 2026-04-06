package su.nightexpress.excellentshop.api.product.stock;

public class UnlimitedStockData implements StockData {

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isClean() {
        return false;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void markClean() {

    }

    @Override
    public void markRemoved() {

    }

    @Override
    public boolean isRestockTime() {
        return false;
    }

    @Override
    public void setExpired() {

    }

    @Override
    public void consume(int units) {

    }

    @Override
    public void store(int units) {

    }

    @Override
    public int getStock() {
        return -1;
    }

    @Override
    public void setStock(int units) {

    }

    @Override
    public long getRestockDate() {
        return -1L;
    }

    @Override
    public void setRestockDate(long restockDate) {

    }
}
