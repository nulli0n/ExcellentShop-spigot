package su.nightexpress.nexshop.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nightcore.database.AbstractUserManager;

import java.util.UUID;

public class UserManager extends AbstractUserManager<ShopPlugin, ShopUser> {

    public UserManager(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
    }

    @Override
    @NotNull
    public ShopUser createUserData(@NotNull UUID uuid, @NotNull String name) {
        return ShopUser.create(plugin, uuid, name);
    }
}
