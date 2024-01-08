package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.data.object.RotationData;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.Placeholders;

import java.util.UUID;

public class RotatingProduct extends AbstractVirtualProduct<RotatingShop> {

    private double rotationChance;

    public RotatingProduct(@NotNull RotatingShop shop, @NotNull Currency currency,
                           @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        this(UUID.randomUUID().toString(), shop, currency, handler, packer);
    }

    public RotatingProduct(@NotNull String id, @NotNull RotatingShop shop, @NotNull Currency currency,
                           @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(id, shop, currency, handler, packer);

        this.placeholderMap
            .add(Placeholders.PRODUCT_ROTATION_CHANCE, () -> NumberUtil.format(this.getRotationChance()));
    }

    @Override
    protected void loadAdditional(@NotNull JYML cfg, @NotNull String path) {
        this.setRotationChance(cfg.getDouble(path + ".Rotation.Chance"));
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Rotation.Chance", this.getRotationChance());
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
