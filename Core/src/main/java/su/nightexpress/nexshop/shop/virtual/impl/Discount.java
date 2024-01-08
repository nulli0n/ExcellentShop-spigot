package su.nightexpress.nexshop.shop.virtual.impl;

public class Discount {

    private final double discount;
    private final long endTime;

    public Discount(double discount, int duration) {
        this.discount = Math.max(0, discount);
        if (duration >= 0) {
            this.endTime = System.currentTimeMillis() + duration * 1000L;
        }
        else {
            this.endTime = -1L;
        }
    }

    public double getDiscount() {
        return 1D - this.discount / 100D;
    }

    public double getDiscountPlain() {
        return this.discount;
    }

    public boolean isExpired() {
        return this.endTime >= 0L && System.currentTimeMillis() > this.endTime;
    }

    @Override
    public String toString() {
        return "Discount{" +
            "discount=" + discount +
            ", endTime=" + endTime +
            '}';
    }
}
