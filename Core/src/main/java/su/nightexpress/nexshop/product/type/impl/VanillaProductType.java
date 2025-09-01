package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.ItemBridge;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.config.Keys;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.ErrorHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.Version;

import java.util.function.UnaryOperator;

public class VanillaProductType extends PhysicalProductType implements VanillaTyping {

    private final ItemStack item;

    private boolean respectMeta;

    public VanillaProductType(@NotNull ItemStack item, boolean respectMeta) {
        this.item = new ItemStack(item);
        this.setRespectMeta(respectMeta);
    }

    @NotNull
    public static VanillaProductType read(@NotNull FileConfig config, @NotNull String path) {
        if (config.contains(path + ".Content.Item")) {
            String tagString = String.valueOf(config.getString(path + ".Content.Item"));

            ItemTag tag = new ItemTag(tagString, -1);
            ItemStack itemStack = ItemNbt.fromTag(tag);
            if (itemStack == null) {
                ErrorHandler.configError("Could not update itemstack '" + tagString + "'!", config, path);
            }

            config.remove(path + ".Content.Item");
            config.set(path + ".ItemTag", new ItemTag(tagString, Version.getCurrent().getDataVersion()));
        }

        ItemTag tag = ItemTag.read(config, path + ".ItemTag");
        boolean respectMeta = config.getBoolean(path + ".Item_Meta_Enabled");

        ItemStack itemStack = ItemNbt.fromTag(tag);
        if (itemStack == null || itemStack.getType().isAir()) {
            itemStack = getBrokenItem();
            ErrorHandler.configError("Invalid item tag string '" + tag.getTag() + "'!", config, path);
        }

        return new VanillaProductType(itemStack, respectMeta);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".ItemTag", ItemNbt.getTag(this.item));
        config.set(path + ".Item_Meta_Enabled", this.respectMeta);
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

    @Override
    public boolean isRespectMeta() {
        return this.respectMeta;
    }

    @Override
    public void setRespectMeta(boolean respectMeta) {
        this.respectMeta = respectMeta;
    }
}
