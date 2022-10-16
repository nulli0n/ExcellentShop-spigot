package su.nightexpress.nexshop.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.AbstractTimed;
import su.nightexpress.nexshop.api.shop.IShopDiscount;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class ShopDiscount extends AbstractTimed implements IShopDiscount {

    private double discount;

    public ShopDiscount(@NotNull Set<DayOfWeek> days, @NotNull Set<LocalTime[]> times, double discount) {
        super(days, times);
        this.setDiscount(discount);
    }

    @Override
    public double getDiscount() {
        return 1D - this.discount / 100D;
    }

    @Override
    public double getDiscountRaw() {
        return this.discount;
    }

    @Override
    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
