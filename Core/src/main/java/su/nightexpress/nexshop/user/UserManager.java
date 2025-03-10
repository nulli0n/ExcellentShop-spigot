package su.nightexpress.nexshop.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nightcore.db.AbstractUserManager;

import java.util.UUID;

public class UserManager extends AbstractUserManager<ShopPlugin, ShopUser> {

    public UserManager(@NotNull ShopPlugin plugin, @NotNull DataHandler handler) {
        super(plugin, handler);
    }

    @Override
    @NotNull
    public ShopUser create(@NotNull UUID uuid, @NotNull String name) {
        return ShopUser.create(uuid, name);
    }
}
