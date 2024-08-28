package su.nightexpress.nexshop.product;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.handler.PluginItemHandler;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.product.handler.impl.*;
import su.nightexpress.nightcore.util.Plugins;

import java.util.*;
import java.util.function.Supplier;

public class ProductHandlerRegistry {

    private static DummyHandler         dummyHandler;
    private static BukkitItemHandler    bukkitItemHandler;
    private static BukkitCommandHandler bukkitCommandHandler;

    private static final Map<String, ProductHandler> HANDLER_MAP = new HashMap<>();

    @NotNull
    public static Map<String, ProductHandler> getHandlerMap() {
        return HANDLER_MAP;
    }

    public static void load(@NotNull ShopPlugin plugin) {
        register(plugin, dummyHandler = new DummyHandler(plugin));
        register(plugin, bukkitItemHandler = new BukkitItemHandler(plugin));
        register(plugin, bukkitCommandHandler = new BukkitCommandHandler(plugin));

        register(plugin, HookId.ORAXEN, () -> new OraxenItemHandler(plugin));
        register(plugin, HookId.ITEMS_ADDER, () -> new ItemsAdderHandler(plugin));
        register(plugin, HookId.MMOITEMS, () -> new MMOItemsHandler(plugin));
        register(plugin, HookId.EXCELLENT_CRATES, () -> new ExcellentCratesHandler(plugin));
    }

    public static boolean register(@NotNull ShopPlugin plugin, @NotNull String pluginName, @NotNull Supplier<ProductHandler> supplier) {
        if (!Plugins.isInstalled(pluginName)) return false;

        return register(plugin, supplier.get());
    }

    public static boolean register(@NotNull ShopPlugin plugin, @NotNull ProductHandler handler) {
        String name = handler.getName().toLowerCase();
        if (Config.DISABLED_PRODUCT_HANDLERS.get().contains(name)) {
            return false;
        }

        HANDLER_MAP.put(name, handler);
        plugin.info("Registered '" + name + "' product handler.");
        return true;
    }

    @NotNull
    public static DummyHandler getDummyHandler() {
        return dummyHandler;
    }

    @NotNull
    public static BukkitItemHandler forBukkitItem() {
        return bukkitItemHandler;
    }

    @NotNull
    public static BukkitCommandHandler forBukkitCommand() {
        return bukkitCommandHandler;
    }

    @NotNull
    public static Collection<ProductHandler> getHandlers() {
        return HANDLER_MAP.values();
    }

    @NotNull
    public static Collection<PluginItemHandler> getPluginItemHandlers() {
        Set<PluginItemHandler> set = new HashSet<>();
        getHandlers().forEach(handler -> {
            if (handler instanceof PluginItemHandler pluginItem) {
                set.add(pluginItem);
            }
        });
        return set;
    }

    @Nullable
    public static ProductHandler getHandler(@NotNull String name) {
        return HANDLER_MAP.get(name.toLowerCase());
    }

    @NotNull
    public static ItemHandler getHandler(@Nullable ItemStack item) {
        if (item == null) return bukkitItemHandler;

        for (ProductHandler handler : getHandlers()) {
            if (handler != bukkitItemHandler && handler instanceof ItemHandler itemHandler && itemHandler.canHandle(item)) {
                return itemHandler;
            }
        }

        return bukkitItemHandler;

//        return (ItemHandler) getHandlers().stream().filter(handler -> {
//            if (handler != BUKKIT_ITEM && handler instanceof ItemHandler itemHandler) {
//                return itemHandler.canHandle(item);
//            }
//            return false;
//        }).findFirst().orElse(forBukkitItem());
    }
}
