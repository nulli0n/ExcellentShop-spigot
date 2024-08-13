package su.nightexpress.nexshop.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.impl.handler.VanillaCommandHandler;
import su.nightexpress.nexshop.shop.impl.handler.VanillaItemHandler;

import java.util.*;

public class ProductHandlerRegistry {

    public static final VanillaItemHandler    BUKKIT_ITEM    = new VanillaItemHandler();
    public static final VanillaCommandHandler BUKKIT_COMMAND = new VanillaCommandHandler();

    private static final Map<String, ProductHandler> HANDLER_MAP = new HashMap<>();

    @NotNull
    public static Map<String, ProductHandler> getHandlerMap() {
        return HANDLER_MAP;
    }

    public static void register(@NotNull ProductHandler handler) {
        String name = handler.getName().toLowerCase();
        if (Config.DISABLED_PRODUCT_HANDLERS.get().contains(name)) {
            return;
        }

        getHandlerMap().put(name, handler);
        ShopAPI.PLUGIN.info("Registered '" + handler.getName() + "' product handler.");
    }

    @NotNull
    public static VanillaItemHandler forBukkitItem() {
        return BUKKIT_ITEM;
    }

    @NotNull
    public static VanillaCommandHandler forBukkitCommand() {
        return BUKKIT_COMMAND;
    }

    @NotNull
    public static Collection<ProductHandler> getHandlers() {
        return getHandlerMap().values();
    }

    @NotNull
    public static Collection<PluginItemPacker> getPluginItemPackers() {
        Set<PluginItemPacker> set = new HashSet<>();
        getHandlers().forEach(handler -> {
            if (handler.createPacker() instanceof PluginItemPacker pluginItem) {
                set.add(pluginItem);
            }
        });
        return set;
    }

    @Nullable
    public static ProductHandler getHandler(@NotNull String name) {
        return getHandlerMap().get(name.toLowerCase());
    }

    @NotNull
    public static ProductHandler getHandler(@Nullable ItemStack item) {
        if (item == null) return forBukkitItem();

        return getHandlers().stream().filter(handler -> {
            if (handler != BUKKIT_ITEM && handler instanceof ItemHandler itemHandler) {
                return itemHandler.canHandle(item);
            }
            return false;
        }).findFirst().orElse(forBukkitItem());
    }
}
