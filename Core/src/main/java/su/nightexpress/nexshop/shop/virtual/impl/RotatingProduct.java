package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.data.RotationData;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class RotatingProduct extends AbstractVirtualProduct<RotatingShop> {

    private double rotationChance;

    public RotatingProduct(@NotNull ShopPlugin plugin,
                           @NotNull String id,
                           @NotNull RotatingShop shop,
                           @NotNull Currency currency,
                           @NotNull ProductHandler handler,
                           @NotNull ProductPacker packer) {
        super(plugin, id, shop, currency, handler, packer);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.setRotationChance(config.getDouble(path + ".Rotation.Chance"));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Rotation.Chance", this.rotationChance);
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forRotatingProduct(this, player);
    }

    @Override
    public boolean isAvailable(@NotNull Player player) {
        if (!super.isAvailable(player)) return false;

        return this.isInRotation();
    }

    public boolean isInRotation() {
        RotationData data = this.shop.getData();
        return data.getProducts().contains(this.getId());
    }

    public boolean canRotate() {
        return this.rotationChance > 0D;
    }

    public double getRotationChance() {
        return rotationChance;
    }

    public void setRotationChance(double rotationChance) {
        this.rotationChance = rotationChance;
    }
}
