package su.nightexpress.nexshop.shop.auction.config;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.ICurrency;

public class AuctionCurrencySetting {

    private final ICurrency currency;
    private final boolean isDefault;
    private final boolean isEnabled;
    private final boolean isPermissionRequired;
    private final String permission;

    public AuctionCurrencySetting(@NotNull ICurrency currency, boolean isDefault, boolean isEnabled, boolean isPermissionRequired) {
        this.currency = currency;
        this.isDefault = isDefault;
        this.isEnabled = isEnabled;
        this.isPermissionRequired = isPermissionRequired;
        this.permission = Perms.PREFIX_AUCTION_CURRENCY + this.getCurrency().getId();
    }

    @NotNull
    public ICurrency getCurrency() {
        return currency;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isPermissionRequired() {
        return isPermissionRequired;
    }

    @NotNull
    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(@NotNull Player player) {
        return !this.isPermissionRequired() ||
            (player.hasPermission(this.getPermission()) || player.hasPermission(Perms.AUCTION_CURRENCY));
    }
}
