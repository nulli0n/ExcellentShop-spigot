package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.NumberUT;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

public interface IShopDiscount extends ITimed, IPlaceholder {

    String PLACEHOLDER_AMOUNT     = "%discount_amount%";
    String PLACEHOLDER_AMOUNT_RAW = "%discount_amount_raw%";
    String PLACEHOLDER_DAYS = "%discount_days%";
    String PLACEHOLDER_TIMES = "%discount_times%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;

        return str -> str
                .replace(PLACEHOLDER_DAYS, String.join(DELIMITER_DEFAULT, this.getDays()
                        .stream().map(DayOfWeek::name).toList()))
                .replace(PLACEHOLDER_TIMES, String.join(DELIMITER_DEFAULT, this.getTimes()
                        .stream().map(arr -> timeFormat.format(arr[0]) + "-" + timeFormat.format(arr[1])).toList()))
                .replace(PLACEHOLDER_AMOUNT, NumberUT.format(this.getDiscount()))
                .replace(PLACEHOLDER_AMOUNT_RAW, NumberUT.format(this.getDiscountRaw()))
                ;
    }

    double getDiscount();

    double getDiscountRaw();

    void setDiscount(double discount);
}
