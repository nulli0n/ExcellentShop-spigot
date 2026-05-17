package su.nightexpress.excellentshop.user;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.AbstractUser;
import su.nightexpress.nightcore.user.UserTemplate;

import java.util.UUID;

public class ShopUser extends UserTemplate {

    private final UserSettings settings;

    @NonNull
    public static ShopUser create(@NonNull UUID uuid, @NonNull String name) {
        UserSettings settings = UserSettings.createDefault();

        return new ShopUser(uuid, name, settings);
    }

    public ShopUser(@NonNull UUID uuid, @NonNull String name, @NonNull UserSettings settings) {
        super(uuid, name);
        this.settings = settings;
    }

    @NonNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
