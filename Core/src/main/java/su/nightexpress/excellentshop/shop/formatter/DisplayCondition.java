package su.nightexpress.excellentshop.shop.formatter;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;

@FunctionalInterface
public interface DisplayCondition<P extends Product> {

    boolean check(@NonNull P product, @NonNull Player player);
}
