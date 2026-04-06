package su.nightexpress.excellentshop.api.product.limit;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;

public class EmptyLimitData implements LimitData {

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
    public void reset() {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isRestockTime() {
        return false;
    }

    @Override
    public void addPurchases(int amount) {

    }

    @Override
    public void addSales(int amount) {

    }

    @Override
    public int getTrades(@NonNull TradeType type) {
        return 0;
    }

    @Override
    public int getPurchases() {
        return 0;
    }

    @Override
    public void setPurchases(int purchases) {

    }

    @Override
    public int getSales() {
        return 0;
    }

    @Override
    public void setSales(int sales) {

    }

    @Override
    public long getRestockDate() {
        return -1L;
    }

    @Override
    public void setRestockDate(long restockDate) {

    }
}
