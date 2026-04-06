package su.nightexpress.excellentshop.api.product.price;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;

import java.util.UUID;

public class EmptyPriceData implements PriceData {

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
    public void countTransaction(@NonNull TradeType tradeType, int amount) {

    }

    @Override
    public double getOffset(@NonNull TradeType type) {
        return 0;
    }

    @Override
    public void setOffset(@NonNull TradeType type, double offset) {

    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isExpirable() {
        return false;
    }

    @Override
    public void setExpired() {

    }

    @Override
    public double getBuyOffset() {
        return 0;
    }

    @Override
    public void setBuyOffset(double buyOffset) {

    }

    @Override
    public double getSellOffset() {
        return 0;
    }

    @Override
    public void setSellOffset(double sellOffset) {

    }

    @Override
    public long getExpireDate() {
        return 0;
    }

    @Override
    public void setExpireDate(long expireDate) {

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
}
