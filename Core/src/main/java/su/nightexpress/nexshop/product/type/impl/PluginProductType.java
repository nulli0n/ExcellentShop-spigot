package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.ItemBridge;
import su.nightexpress.economybridge.api.item.ItemHandler;
import su.nightexpress.nexshop.api.shop.Module;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PluginTyping;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.function.UnaryOperator;

public class PluginProductType extends PhysicalProductType implements PluginTyping {

    public static final String DELIMITER = ":::";

    private final String handlerName;
    private final String itemId;
    private final int    amount;

    public PluginProductType(@NotNull String handlerName, @NotNull String itemId, int amount) {
        this.handlerName = handlerName;
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
    }

    @NotNull
    public static PluginProductType read(@NotNull Module module, @NotNull FileConfig config, @NotNull String path) {
        String handlerId = config.getString(path + ".Handler", "dummy");
        ItemHandler handler = ItemBridge.getHandler(handlerId);
        if (handler == null) {
            handler = ItemBridge.getItemManager().getDummyHandler();
            module.error("Invalid item handler '" + handlerId + "'. Caused by '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
        }

        // ------- REVERT 4.13.3 CHANGES - START ------- //
        String serialized = config.getString(path + ".Data");
        if (serialized != null) {
            String delimiter = " \\| ";
            String[] split = serialized.split(delimiter);
            String itemId = split[0];
            int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

            config.set(path + ".Content.ItemId", itemId);
            config.set(path + ".Content.Amount", amount);
            config.remove(path + ".Data");
        }
        // ------- REVERT 4.13.3 CHANGES - END ------- //

        String itemId = config.getString(path + ".Content.ItemId", "null");
        int amount = config.getInt(path + ".Content.Amount", 1);

        if (!handler.isDummy() && !handler.isValidId(itemId)) {
            module.error("Invalid item ID '" + itemId + "' for '" + handlerId + "' handler. Caused by '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
        }

        return new PluginProductType(handler.getName(), itemId, amount);
    }

    @Nullable
    public static PluginProductType deserialize(@NotNull String serialized) {
        String[] split = serialized.split(DELIMITER);
        if (split.length < 2) return null;

        String handlerName = split[0];
        String itemId = split[1];
        int amount = split.length >= 3 ? NumberUtil.getIntegerAbs(split[2]) : 1;

        return new PluginProductType(handlerName, itemId, amount);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        ItemHandler handler = this.getHandler();
        if (handler.isDummy()) return; // Do not override with dummy handler.

        config.set(path + ".Handler", handler.getName());
        config.set(path + ".Content.ItemId", this.itemId);
        config.set(path + ".Content.Amount", this.amount);
        config.remove(path + ".Data"); // ------- REVERT 4.13.3 CHANGES ------- //
    }

    @NotNull
    public ItemHandler getHandler() {
        return ItemBridge.getHandlerOrDummy(this.handlerName);
    }

    @Override
    @NotNull
    public String serialize() {
        return this.handlerName + DELIMITER + this.itemId + DELIMITER + this.amount;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return s -> s; // TODO Item id
    }

    @Override
    @NotNull
    public ProductType type() {
        return ProductType.PLUGIN;
    }

    @Override
    @NotNull
    public String getName() {
        return this.handlerName;
    }

    @Override
    public boolean isValid() {
        ItemHandler handler = this.getHandler();
        if (handler.isDummy()) return false;

        return handler.isValidId(this.itemId);
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        ItemStack itemStack = this.getHandler().createItem(this.itemId);
        if (itemStack == null) {
            itemStack = getBrokenItem();
        }
        return itemStack;
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack other) {
        String itemId = ItemBridge.getItemId(this.handlerName, other);
        return itemId != null && itemId.equalsIgnoreCase(this.itemId);
    }

    @NotNull
    @Override
    public String getItemId() {
        return this.itemId;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }
}
