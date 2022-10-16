package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtualPrepared;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopUser extends AbstractUser<ExcellentShop> {

    private final Map<String, Map<TradeType, UserProductLimit>> virtualLimits;
    private final UserSettings                  settings;

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name) {
        this(
            plugin, uuid, name,
            System.currentTimeMillis(), System.currentTimeMillis(),

            new UserSettings(true, true),
            new HashMap<>()
        );
    }

    public ShopUser(
            @NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name,
            long dateCreated, long lastOnline,
            @NotNull UserSettings settings,
            @NotNull Map<String, Map<TradeType, UserProductLimit>> virtualLimits
    ) {
        super(plugin, uuid, name, dateCreated, lastOnline);

        this.settings = settings;
        this.virtualLimits = virtualLimits;
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }

    @NotNull
    public Map<String, Map<TradeType, UserProductLimit>> getVirtualProductLimits() {
        this.virtualLimits.values().forEach(map -> map.values().removeIf(UserProductLimit::isExpired));
        return this.virtualLimits;
    }

    public void addVirtualProductLimit(@NotNull IProductVirtualPrepared product, @NotNull TradeType tradeType) {
        IProductVirtual shopProduct = product.getProduct();
        if (!shopProduct.isLimited(tradeType) || shopProduct.getLimitCooldown(tradeType) == 0) return;

        UserProductLimit productLimit = this.getVirtualProductLimit(tradeType, shopProduct.getId());
        if (productLimit == null) {
            productLimit = new UserProductLimit(product, tradeType);
        }
        productLimit.addCount(product.getAmount());

        String id = shopProduct.getId();
        this.getVirtualProductLimits().computeIfAbsent(id, k -> new HashMap<>()).put(tradeType, productLimit);
    }

    @Nullable
    public UserProductLimit getVirtualProductLimit(@NotNull TradeType tradeType, @NotNull String id) {
        return this.getVirtualProductLimits().getOrDefault(id, Collections.emptyMap()).get(tradeType);
    }
}
