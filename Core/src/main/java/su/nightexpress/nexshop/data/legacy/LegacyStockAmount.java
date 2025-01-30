package su.nightexpress.nexshop.data.legacy;

public class LegacyStockAmount {

    private final int  itemsLeft;
    private final long restockDate;

    public LegacyStockAmount(int itemsLeft, long restockDate) {
        this.itemsLeft = itemsLeft;
        this.restockDate = restockDate;
    }

    public int getItemsLeft() {
        return this.itemsLeft;
    }

    public long getRestockDate() {
        return this.restockDate;
    }
}
