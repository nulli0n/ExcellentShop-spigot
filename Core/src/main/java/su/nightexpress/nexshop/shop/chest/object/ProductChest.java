package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.AbstractProduct;
import su.nightexpress.nexshop.api.shop.IProductPrepared;
import su.nightexpress.nexshop.api.shop.IProductPricer;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.shop.chest.IProductChest;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChestProduct;

import java.util.UUID;

public class ProductChest extends AbstractProduct<IShopChest> implements IProductChest {

    private EditorShopChestProduct editor;

    public ProductChest(@NotNull IShopChest shop, @NotNull ICurrency currency, @NotNull ItemStack item) {
        this(
            shop, UUID.randomUUID().toString(),
            currency, new ProductPricer(),
            item
        );
    }

    public ProductChest(
            @NotNull IShopChest shop,
            @NotNull String id,

            @NotNull ICurrency currency,
            @NotNull IProductPricer pricer,

            @NotNull ItemStack item) {
        super(shop, id, item, item, currency, pricer, false, true);
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @Override
    @NotNull
    public EditorShopChestProduct getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopChestProduct(this.shop.plugin(), this);
        }
        return this.editor;
    }

    @Override
    @Deprecated
    public int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.getShop().getProductAmount(this) : this.getShop().getProductSpace(this);//this.getShop().getProductAmount(this);
    }

    @Override
    @NotNull
    public IProductPrepared getPrepared(@NotNull TradeType buyType) {
        return new ProductChestPrepared(this, buyType);
    }
}
