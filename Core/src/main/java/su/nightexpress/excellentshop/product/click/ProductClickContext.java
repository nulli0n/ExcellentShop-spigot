package su.nightexpress.excellentshop.product.click;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.product.Product;

public record ProductClickContext(@NonNull Player player,
                                  @NonNull Product product,
                                  @Nullable InventoryClickEvent event,
                                  int shopPage) {

}
