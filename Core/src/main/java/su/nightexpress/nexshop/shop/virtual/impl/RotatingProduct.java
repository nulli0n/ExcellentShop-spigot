package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.data.object.RotationData;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.UUID;

public class RotatingProduct extends AbstractVirtualProduct<RotatingShop> {

    private double rotationChance;

    public RotatingProduct(@NotNull ShopPlugin plugin,
                           @NotNull RotatingShop shop, @NotNull Currency currency,
                           @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this(plugin, UUID.randomUUID().toString(), shop, currency, handler, packer);
    }

    public RotatingProduct(@NotNull ShopPlugin plugin,
                           @NotNull String id, @NotNull RotatingShop shop, @NotNull Currency currency,
                           @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(plugin, id, shop, currency, handler, packer);

        this.placeholderRelMap
            .add(Placeholders.PRODUCT_ROTATION_CHANCE, player -> NumberUtil.format(this.getRotationChance()));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.setRotationChance(config.getDouble(path + ".Rotation.Chance"));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Rotation.Chance", this.getRotationChance());
    }

    public boolean isInRotation() {
        RotationData data = this.getShop().getData();
        return data.getProducts().contains(this.getId());
    }

    public boolean canRotate() {
        return this.getRotationChance() > 0D;
    }

    public double getRotationChance() {
        return rotationChance;
    }

    public void setRotationChance(double rotationChance) {
        this.rotationChance = rotationChance;
    }
}
