package su.nightexpress.nexshop.user;

import org.jetbrains.annotations.NotNull;

public class UserSettings {

    private boolean isAuctionBroadcastEnabled;
    private boolean isChestNotificationsEnabled;

    @NotNull
    public static UserSettings createDefault() {
        return new UserSettings(true, true);
    }

    public UserSettings(
            boolean isAuctionBroadcastEnabled,
            boolean isChestNotificationsEnabled
    ) {

        this.setAuctionBroadcastEnabled(isAuctionBroadcastEnabled);
        this.setChestNotificationsEnabled(isChestNotificationsEnabled);
    }

    public boolean isAuctionBroadcastEnabled() {
        return this.isAuctionBroadcastEnabled;
    }

    public void setAuctionBroadcastEnabled(boolean isAuctionBroadcastEnabled) {
        this.isAuctionBroadcastEnabled = isAuctionBroadcastEnabled;
    }

    public boolean isChestNotificationsEnabled() {
        return this.isChestNotificationsEnabled;
    }

    public void setChestNotificationsEnabled(boolean isChestNotificationsEnabled) {
        this.isChestNotificationsEnabled = isChestNotificationsEnabled;
    }
}
