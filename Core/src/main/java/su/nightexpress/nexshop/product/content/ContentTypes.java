package su.nightexpress.nexshop.product.content;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.product.content.impl.CommandContent;
import su.nightexpress.nexshop.product.content.impl.EmptyContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.ItemBridge;

import java.util.function.Function;

public class ContentTypes {

    @Nullable
    public static ProductContent read(@NotNull ContentType type, @NotNull FileConfig config, @NotNull String path) {
        return switch (type) {
            case ITEM -> ItemContent.read(config, path);
            case COMMAND -> CommandContent.read(config, path);
            case EMPTY -> EmptyContent.VALUE;
        };
    }

    @NotNull
    public static ItemContent fromItem(@NotNull ItemStack itemStack, @NotNull Function<ItemAdapter<?>, Boolean> allowed) {
        ItemAdapter<?> adapter = ItemBridge.getAdapter(itemStack);
        if (adapter == null || !allowed.apply(adapter)) adapter = ItemBridge.getVanillaAdapter();

        AdaptedItem adaptedItem = adapter.adapt(itemStack).orElseThrow(() -> new IllegalStateException("Could not adapt ItemStack: " + itemStack));

        return new ItemContent(adaptedItem, itemStack.hasItemMeta());
    }
}
