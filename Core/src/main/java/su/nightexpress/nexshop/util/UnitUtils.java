package su.nightexpress.nexshop.util;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;

import java.util.OptionalInt;
import java.util.stream.IntStream;

public class UnitUtils {

    public static int amountToUnits(@NonNull Product product, int amount) {
        return amountToUnits(amount, product.getUnitSize());
    }

    public static int amountToUnits(int amount, int unitSize) {
        return amount / unitSize;
    }

    public static int unitsToAmount(@NonNull Product product, int count) {
        return unitsToAmount(product.getUnitSize(), count);
    }

    public static int unitsToAmount(int unitSize, int count) {
        return unitSize * count;
    }

    @NonNull
    public static OptionalInt findSmallestPositive(int... values) {
        return IntStream.of(values).filter(i -> i >= 0).min();
    }
}
