package su.nightexpress.nexshop.product.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.PluginItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractPluginItemHandler extends AbstractProductHandler implements PluginItemHandler {

    public AbstractPluginItemHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public ProductPacker createPacker(@NotNull FileConfig config, @NotNull String path) {
        String itemId = config.getString(path + ".Content.ItemId", "null");
        int amount = config.getInt(path + ".Content.Amount");

        if (!this.isValidId(itemId)) {
            this.logBadItem(itemId, config, path);
        }

        return this.createPacker(itemId, amount);
    }

    @Override
    @Nullable
    public ItemPacker createPacker(@NotNull ItemStack itemStack) {
        String itemId = this.getItemId(itemStack);
        if (itemId == null) return null;

        return this.createPacker(itemId, itemStack.getAmount());
    }
}
