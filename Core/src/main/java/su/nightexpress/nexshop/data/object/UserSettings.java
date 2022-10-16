package su.nightexpress.nexshop.data.object;

public class UserSettings {

    private boolean isAuctionBroadcastEnabled;
    private boolean isChestNotificationsEnabled;

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
