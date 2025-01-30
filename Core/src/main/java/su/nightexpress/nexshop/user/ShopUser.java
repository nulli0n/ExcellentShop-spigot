package su.nightexpress.nexshop.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.db.AbstractUser;

import java.util.UUID;

public class ShopUser extends AbstractUser {

    private final UserSettings settings;

    @NotNull
    public static ShopUser create(@NotNull UUID uuid, @NotNull String name) {
        long date = System.currentTimeMillis();
        UserSettings settings = UserSettings.createDefault();

        return new ShopUser(uuid, name, date, date, settings);
    }

    public ShopUser(@NotNull UUID uuid,
                    @NotNull String name,
                    long dateCreated,
                    long lastOnline,
                    @NotNull UserSettings settings
    ) {
        super(uuid, name, dateCreated, lastOnline);
        this.settings = settings;
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
