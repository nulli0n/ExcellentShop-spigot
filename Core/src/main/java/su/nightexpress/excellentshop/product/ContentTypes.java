package su.nightexpress.excellentshop.product;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.product.content.CommandContent;
import su.nightexpress.excellentshop.product.content.EmptyContent;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.ItemBridge;

import java.util.ArrayList;
import java.util.function.Function;

public class ContentTypes {

    @NonNull
    public static ProductContent create(@NonNull ContentType type, @NonNull ItemStack preview, @NonNull Function<ItemAdapter<?>, Boolean> allowed) {
        return switch (type) {
            case ITEM -> fromItem(preview, allowed);
            case COMMAND -> new CommandContent(preview, new ArrayList<>());
            case EMPTY -> EmptyContent.VALUE;
        };
    }
    
    @Nullable
    public static ProductContent read(@NonNull ContentType type, @NonNull FileConfig config, @NonNull String path) {
        return switch (type) {
            case ITEM -> ItemContent.read(config, path);
            case COMMAND -> CommandContent.read(config, path);
            case EMPTY -> EmptyContent.VALUE;
        };
    }

    @NonNull
    public static ItemContent fromItem(@NonNull ItemStack itemStack, @NonNull Function<ItemAdapter<?>, Boolean> allowed) {
        ItemAdapter<?> adapter = ItemBridge.getAdapter(itemStack);
        if (adapter == null || !allowed.apply(adapter)) adapter = ItemBridge.getVanillaAdapter();

        AdaptedItem adaptedItem = adapter.adapt(itemStack).orElseThrow(() -> new IllegalStateException("Could not adapt ItemStack: " + itemStack));

        return new ItemContent(adaptedItem, itemStack.hasItemMeta());
    }
}
