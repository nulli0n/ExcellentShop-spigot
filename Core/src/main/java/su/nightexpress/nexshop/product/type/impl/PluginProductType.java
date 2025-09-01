package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.ItemBridge;
import su.nightexpress.economybridge.api.item.ItemHandler;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PluginTyping;
import su.nightexpress.nexshop.util.ErrorHandler;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class PluginProductType extends PhysicalProductType implements PluginTyping {

    private final String handlerName;
    private final String itemId;
    private final int    amount;

    public PluginProductType(@NotNull String handlerName, @NotNull String itemId, int amount) {
        this.handlerName = handlerName;
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
    }

    @NotNull
    public static PluginProductType read(@NotNull FileConfig config, @NotNull String path) {
        String handlerId = config.getString(path + ".Handler", "dummy");
        ItemHandler handler = ItemBridge.getHandler(handlerId);
        if (handler == null) {
            handler = ItemBridge.getItemManager().getDummyHandler();
            ErrorHandler.configError("Invalid item handler '" + handlerId + "'.", config, path);
        }

        String itemId = config.getString(path + ".Content.ItemId", "null");
        int amount = config.getInt(path + ".Content.Amount", 1);

        return new PluginProductType(handler.getName(), itemId, amount);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        ItemHandler handler = this.getHandler();
        if (handler.isDummy()) return; // Do not override with dummy handler.

        config.set(path + ".Handler", handler.getName());
        config.set(path + ".Content.ItemId", this.itemId);
        config.set(path + ".Content.Amount", this.amount);
    }

    @NotNull
    public ItemHandler getHandler() {
        return ItemBridge.getHandlerOrDummy(this.handlerName);
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
        if (itemStack == null) return getBrokenItem();

        itemStack.setAmount(this.amount);
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
