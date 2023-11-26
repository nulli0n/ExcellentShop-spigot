package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.Placeholder;

public interface ProductPacker extends Placeholder {

    boolean load(@NotNull JYML cfg, @NotNull String path);

    void write(@NotNull JYML cfg, @NotNull String path);

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    boolean hasSpace(@NotNull Inventory inventory);

    void delivery(@NotNull Inventory inventory, int count);

    void take(@NotNull Inventory inventory, int count);

    int count(@NotNull Inventory inventory);

    int getUnitAmount();
}
