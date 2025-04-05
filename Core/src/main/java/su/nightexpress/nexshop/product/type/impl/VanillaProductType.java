package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.ItemBridge;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Module;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.config.Keys;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.PDCUtil;

import java.util.function.UnaryOperator;

public class VanillaProductType extends PhysicalProductType implements VanillaTyping {

    private ItemStack item;
    private boolean   respectMeta;

    public VanillaProductType(@NotNull ItemStack item, boolean respectMeta) {
        this.setItem(item);
        this.setRespectMeta(respectMeta);
    }

    @NotNull
    public static VanillaProductType read(@NotNull Module module, @NotNull FileConfig config, @NotNull String path) {
        // ------- REVERT 4.13.3 CHANGES - START ------- //
        String serialized = config.getString(path + ".Data");
        if (serialized != null && !serialized.isBlank()) {
            String delimiter = " \\| ";
            String[] split = serialized.split(delimiter);
            String tagString = split[0];
            boolean respectMeta = split.length >= 2 && Boolean.parseBoolean(split[1]);

            config.set(path + ".Content.Item", tagString);
            config.set(path + ".Item_Meta_Enabled", respectMeta);
            config.remove(path + ".Data");
        }
        // ------- REVERT 4.13.3 CHANGES - END ------- //

        String tagString = config.getString(path + ".Content.Item", "null");
        boolean respectMeta = config.getBoolean(path + ".Item_Meta_Enabled");

        ItemStack itemStack = ShopUtils.readItemTag(tagString);
        if (itemStack == null || itemStack.getType().isAir()) {
            itemStack = getBrokenItem();
            module.error("Invalid item tag string '" + tagString + "'! Caused by '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
        }

        return new VanillaProductType(itemStack, respectMeta);
    }

    @Nullable
    public static VanillaProductType deserialize(@NotNull String serialized) {
        ItemStack itemStack = ShopUtils.readItemTag(serialized);
        if (itemStack == null) return null;

        return new VanillaProductType(itemStack, true);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Content.Item", ShopUtils.getItemTag(this.item));
        config.set(path + ".Item_Meta_Enabled", this.respectMeta);
        config.remove(path + ".Data"); // ------- REVERT 4.13.3 CHANGES ------- //
    }

    @Override
    @NotNull
    public String serialize() {
        return String.valueOf(ShopUtils.getItemTag(this.item));
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.VANILLA_TYPING.replacer(this);
    }

    @Override
    @NotNull
    public ProductType type() {
        return ProductType.VANILLA;
    }

    @Override
    @NotNull
    public String getName() {
        return Lang.PRODUCT_TYPES.getLocalized(this.type());
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        // We don't want to count custom plugin's items as vanilla ones.
        if (ItemBridge.isCustomItem(item)) return false;

        return this.respectMeta ? this.item.isSimilar(item) : this.item.getType() == item.getType();
    }

    @Override
    public boolean isValid() {
        return PDCUtil.getBoolean(this.item, Keys.brokenItem).isEmpty();
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    private void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public boolean isRespectMeta() {
        return this.respectMeta;
    }

    @Override
    public void setRespectMeta(boolean respectMeta) {
        this.respectMeta = respectMeta;
    }
}
