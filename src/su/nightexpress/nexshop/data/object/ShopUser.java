package su.nightexpress.nexshop.data.object;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProductPrepared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopUser extends AbstractUser<ExcellentShop> {

    private final Map<String, Map<TradeType, UserProductLimit>> virtualLimits;
    private final UserSettings                  settings;

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull Player player) {
        this(
                plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis(),

                new UserSettings(true, true),
                new HashMap<>()
        );
    }

    public ShopUser(
            @NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name, long lastOnline,
            @NotNull UserSettings settings,
            @NotNull Map<String, Map<TradeType, UserProductLimit>> virtualLimits
    ) {
        super(plugin, uuid, name, lastOnline);

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

    public void addVirtualProductLimit(@NotNull IShopVirtualProductPrepared product, @NotNull TradeType tradeType) {
        IShopVirtualProduct shopProduct = product.getShopProduct();
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
