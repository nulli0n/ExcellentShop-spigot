package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.data.rotation.ShopRotationData;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ProductSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.shop.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.util.Placeholders;

import java.util.UUID;

public class RotatingProduct extends VirtualProduct<RotatingProduct, RotatingShop> {

    private double rotationChance;

    public RotatingProduct(@NotNull ProductSpecific spec, @NotNull Currency currency) {
        this(UUID.randomUUID().toString(), spec, currency);
    }

    public RotatingProduct(@NotNull String id, @NotNull ProductSpecific spec, @NotNull Currency currency) {
        super(id, spec, currency);

        this.placeholderMap
            .add(Placeholders.PRODUCT_ROTATION_CHANCE, () -> NumberUtil.format(this.getRotationChance()));
    }

    @Override
    @NotNull
    protected RotatingProduct get() {
        return this;
    }

    @Override
    protected void writeAdditionalData(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Rotation.Chance", this.getRotationChance());
    }

    public boolean isInRotation() {
        ShopRotationData data = this.getShop().getData();
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
