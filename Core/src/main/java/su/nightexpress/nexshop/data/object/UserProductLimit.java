package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtualPrepared;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class UserProductLimit {

    private final TradeType tradeType;
    private final String shopId;
    private final String productId;
    private int count;
    private long expireDate;

    public UserProductLimit(@NotNull IProductVirtualPrepared product, @NotNull TradeType tradeType) {
        this(tradeType, product.getShop().getId(), product.getProduct().getId(), 0, 0L);

        IProductVirtual shopProduct = product.getProduct();
        if (!shopProduct.isLimitExpirable(tradeType)) {
            this.expireDate = -1L;
            return;
        }
        this.expireDate = System.currentTimeMillis() + shopProduct.getLimitCooldown(tradeType) * 1000L;
    }

    public UserProductLimit(
            @NotNull TradeType tradeType,
            @NotNull String shopId,
            @NotNull String productId,
            int count,
            long expireDate) {
        this.tradeType = tradeType;
        this.shopId = shopId;
        this.productId = productId;
        this.count = count;
        this.expireDate = expireDate;
        this.update();
    }

    public void update() {
        VirtualShop virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return;

        IShopVirtual shop = virtualShop.getShopById(this.getShopId());
        if (shop == null) return;

        IProductVirtual product = shop.getProductById(this.getProductId());
        if (product == null) return;

        long expireDate = this.getExpireDate();
        if ((expireDate < 0 && product.isLimitExpirable(tradeType)) || !product.isLimited(tradeType)) {
            this.expireDate = 0L;
        }
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getProductId() {
        return this.productId;
    }

    public void addCount(int amount) {
        this.count += amount;
    }

    public int getCount() {
        return this.count;
    }

    public long getExpireDate() {
        return this.expireDate;
    }

    public boolean isExpired() {
        return this.getExpireDate() >= 0 && System.currentTimeMillis() > this.getExpireDate();
    }

    public int getItemsLeft() {
        VirtualShop virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return -1;

        IShopVirtual shop = virtualShop.getShopById(this.getShopId());
        if (shop == null) return -1;

        IProductVirtual product = shop.getProductById(this.getProductId());
        if (product == null) return -1;

        if (!product.isLimited(this.tradeType) || (this.isExpired())) {
            return -1;
        }

        return Math.max(0, product.getLimitAmount(tradeType) - Math.max(0, this.getCount()));
    }
}
