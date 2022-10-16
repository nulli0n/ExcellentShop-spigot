package su.nightexpress.nexshop.shop.auction.config;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.ICurrency;

public class AuctionCurrencySetting {

    private final ICurrency currency;
    private final boolean isDefault;
    private final boolean isEnabled;
    private final boolean isPermissionRequired;
    private final Permission permission;

    public AuctionCurrencySetting(@NotNull ICurrency currency, boolean isDefault, boolean isEnabled, boolean isPermissionRequired) {
        this.currency = currency;
        this.isDefault = isDefault;
        this.isEnabled = isEnabled;
        this.isPermissionRequired = isPermissionRequired;

        if (this.isEnabled() && this.isPermissionRequired()) {
            this.permission = new Permission(Perms.AUCTION_CURRENCY.getName() + "." + this.getCurrency().getId(), "Allows to use " + this.getCurrency().getId() + " currency on Auction.", PermissionDefault.OP);
        }
        else this.permission = null;
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

    @Nullable
    public Permission getPermission() {
        return permission;
    }

    public boolean hasPermission(@NotNull Player player) {
        return (!this.isPermissionRequired() || this.getPermission() == null) || player.hasPermission(this.getPermission());
    }
}
