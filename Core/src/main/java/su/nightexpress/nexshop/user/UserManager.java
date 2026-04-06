package su.nightexpress.nexshop.user;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.nightcore.user.AbstractUserManager;
import su.nightexpress.nightcore.user.data.DefaultUserDataAccessor;

import java.net.InetAddress;
import java.util.UUID;

public class UserManager extends AbstractUserManager<ShopPlugin, ShopUser> {

    public UserManager(@NotNull ShopPlugin plugin, @NotNull DataHandler dataHandler) {
        super(plugin, new DefaultUserDataAccessor<>(dataHandler, dataHandler));
    }

    @Override
    @NonNull
    protected ShopUser create(@NotNull UUID uuid, @NotNull String name, @NotNull InetAddress address) {
        return ShopUser.create(uuid, name);
    }

    @Override
    protected void synchronize(@NonNull ShopUser fetched, @NonNull ShopUser cached) {
        // TODO
    }
}
