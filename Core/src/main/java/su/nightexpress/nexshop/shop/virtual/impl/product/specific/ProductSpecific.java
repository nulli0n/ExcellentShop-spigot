package su.nightexpress.nexshop.shop.virtual.impl.product.specific;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;

public interface ProductSpecific extends Placeholder {

    @NotNull ItemStack getPreview();

    boolean hasSpace(@NotNull Player player);

    void delivery(@NotNull Player player, int count);

    void take(@NotNull Player player, int count);

    int count(@NotNull Player player);

    int getUnitAmount();
}
