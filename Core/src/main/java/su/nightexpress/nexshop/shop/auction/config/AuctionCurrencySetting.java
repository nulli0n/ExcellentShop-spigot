package su.nightexpress.nexshop.shop.auction.config;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.Currency;

public class AuctionCurrencySetting {

    private final Currency currency;
    private final boolean  isDefault;
    private final boolean isPermissionRequired;
    private final String permission;

    public AuctionCurrencySetting(@NotNull Currency currency, boolean isDefault, boolean isPermissionRequired) {
        this.currency = currency;
        this.isDefault = isDefault;
        this.isPermissionRequired = isPermissionRequired;
        this.permission = Perms.PREFIX_AUCTION_CURRENCY + this.getCurrency().getId();
    }

    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Deprecated
    public boolean isEnabled() {
        return true;
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
