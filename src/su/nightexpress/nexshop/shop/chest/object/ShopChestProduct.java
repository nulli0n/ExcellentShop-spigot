package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IProductPricer;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChestProduct;

import java.util.UUID;

public class ShopChestProduct implements IShopChestProduct {

    private final IShopChest shop;
    private final String     id;

    private       IShopCurrency  currency;
    private final IProductPricer pricer;

    private ItemStack rewardItem;

    private EditorShopChestProduct editor;

    public ShopChestProduct(@NotNull IShopChest shop, @NotNull IShopCurrency currency, @NotNull ItemStack item) {
        this(
                shop, UUID.randomUUID().toString(),
                currency, new ProductPricer(),        // Price manager
                item     // Reward item, commands
        );
    }

    public ShopChestProduct(
            @NotNull IShopChest shop,
            @NotNull String id,

            @NotNull IShopCurrency currency,
            @NotNull IProductPricer pricer,

            @NotNull ItemStack rewardItem
    ) {
        this.shop = shop;
        this.id = id.toLowerCase();

        this.setCurrency(currency);
        this.pricer = pricer;
        this.pricer.setProduct(this);

        this.setItem(rewardItem);
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
    @NotNull
    public IShopChest getShop() {
        return this.shop;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public IProductPricer getPricer() {
        return this.pricer;
    }

    @Override
    @NotNull
    public IShopCurrency getCurrency() {
        return this.currency;
    }

    @Override
    public void setCurrency(@NotNull IShopCurrency currency) {
        this.currency = currency;
    }

    @Override
    @Deprecated
    public int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.getShop().getProductAmount(this) : this.getShop().getProductSpace(this);//this.getShop().getProductAmount(this);
    }

    @Override
    @NotNull
    public IProductPrepared getPrepared(@NotNull TradeType buyType) {
        return new ChestProductPrepared(this, buyType);
    }

    @Override
    public boolean isDiscountAllowed() {
        return false;
    }

    @Override
    public void setDiscountAllowed(boolean isAllowed) {

    }

    @Override
    public boolean isItemMetaEnabled() {
        return true;
    }

    @Override
    public void setItemMetaEnabled(boolean isEnabled) {

    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.rewardItem);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) { }

    @Override
    @NotNull
    public ItemStack getItem() {
        return this.rewardItem;
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.rewardItem = new ItemStack(item);
        this.rewardItem.setAmount(1);
    }
}
