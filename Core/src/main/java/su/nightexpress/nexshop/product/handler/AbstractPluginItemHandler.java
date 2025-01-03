package su.nightexpress.nexshop.product.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.PluginItemHandler;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

public abstract class AbstractPluginItemHandler extends AbstractProductHandler implements PluginItemHandler {

    public AbstractPluginItemHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public PluginItemPacker readPacker(@NotNull FileConfig config, @NotNull String path) {
        String serialized;

        if (config.contains(path + ".Content")) {
            String itemId = config.getString(path + ".Content.ItemId", "null");
            int amount = config.getInt(path + ".Content.Amount");

            serialized = itemId + DELIMITER + amount;
        }
        else {
            serialized = config.getString(path + ".Data", "null");
        }

        PluginItemPacker packer = this.deserialize(serialized);
        if (!this.isValidId(packer.getItemId())) {
            this.plugin.error("[" + this.getName() + "] Invalid item ID/Data '" + serialized + "'. Caused by '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
        }

        return packer;
    }

    @Override
    @NotNull
    public PluginItemPacker deserialize(@NotNull String str) {
        String[] split = str.split(DELIMITER);
        String itemId = split[0];
        int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

        return this.createPacker(itemId, amount);
    }

    @Override
    @NotNull
    public PluginItemPacker createPacker(@NotNull ItemStack itemStack) {
        String itemId = String.valueOf(this.getItemId(itemStack));

        return this.createPacker(itemId, itemStack.getAmount());
    }
}
