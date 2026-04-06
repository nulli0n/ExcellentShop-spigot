package su.nightexpress.nexshop.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.db.AbstractUser;
import su.nightexpress.nightcore.user.UserTemplate;

import java.util.UUID;

public class ShopUser extends UserTemplate {

    private final UserSettings settings;

    @NotNull
    public static ShopUser create(@NotNull UUID uuid, @NotNull String name) {
        UserSettings settings = UserSettings.createDefault();

        return new ShopUser(uuid, name, settings);
    }

    public ShopUser(@NotNull UUID uuid, @NotNull String name, @NotNull UserSettings settings) {
        super(uuid, name);
        this.settings = settings;
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
