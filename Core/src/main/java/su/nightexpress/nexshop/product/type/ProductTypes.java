package su.nightexpress.nexshop.product.type;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.ItemBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.api.item.ItemHandler;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Module;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.product.type.impl.CommandProductType;
import su.nightexpress.nexshop.product.type.impl.PluginProductType;
import su.nightexpress.nexshop.product.type.impl.VanillaProductType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.ArrayList;

public class ProductTypes {

    @NotNull
    public static ProductTyping read(@NotNull Module module, @NotNull ProductType type, @NotNull FileConfig config, @NotNull String path) {
        return switch (type) {
            case VANILLA -> VanillaProductType.read(module, config, path);
            case COMMAND -> CommandProductType.read(module, config, path);
            case PLUGIN -> PluginProductType.read(module, config, path);
        };
    }

    @Nullable
    public static ProductTyping deserialize(@NotNull ProductType type, @NotNull String serialized) {
        return switch (type) {
            case VANILLA -> VanillaProductType.deserialize(serialized);
            case COMMAND -> null;
            case PLUGIN -> PluginProductType.deserialize(serialized);
        };
    }

    @NotNull
    public static PhysicalTyping fromItem(@NotNull ItemStack itemStack, boolean bypassHandler) {
        ItemHandler handler = ItemBridge.getHandler(itemStack);
        String itemId = handler == null ? null : handler.getItemId(itemStack);

        PhysicalTyping typing;
        if (bypassHandler || handler == null || itemId == null) {
            typing = new VanillaProductType(itemStack, itemStack.hasItemMeta());
        }
        else {
            typing = new PluginProductType(handler.getName(), itemId, itemStack.getAmount());
        }

        return typing;
    }

    @Nullable
    public static VirtualProduct wizardCreation(@NotNull ShopPlugin plugin,
                                                @NotNull VirtualShop shop,
                                                @NotNull ItemStack source,
                                                @NotNull ProductType type,
                                                boolean bypassHandler) {

        ProductTyping typing;
        if (type == ProductType.COMMAND) {
            typing = new CommandProductType(source, new ArrayList<>());
        }
        else {
            typing = fromItem(source, bypassHandler);
        }
        if (!typing.isValid()) return null;

        Currency currency = shop.getModule().getDefaultCurrency();
        String id = ShopUtils.generateProductId(shop, typing);

        return new VirtualProduct(plugin, id, shop, currency, typing);
    }
}
