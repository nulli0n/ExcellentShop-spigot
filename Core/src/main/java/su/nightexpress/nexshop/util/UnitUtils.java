package su.nightexpress.nexshop.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;

public class UnitUtils {

    public static int amountToUnits(@NotNull Product product, int amount) {
        return amountToUnits(amount, product.getUnitAmount());
    }

    public static int amountToUnits(int amount, int unitSize) {
        return amount / unitSize;
    }

    public static int unitsToAmount(@NotNull Product product, int count) {
        return unitsToAmount(product.getUnitAmount(), count);
    }

    public static int unitsToAmount(int unitSize, int count) {
        return unitSize * count;
    }
}
