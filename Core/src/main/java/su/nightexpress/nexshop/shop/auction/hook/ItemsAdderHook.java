package su.nightexpress.nexshop.shop.auction.hook;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderHook {

    public static boolean isCustomItem(@NotNull String namespace) {
        return CustomStack.isInRegistry(namespace) || CustomBlock.isInRegistry(namespace);
    }
}
