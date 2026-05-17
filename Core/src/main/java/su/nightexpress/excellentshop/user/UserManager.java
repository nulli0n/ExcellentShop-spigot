package su.nightexpress.excellentshop.user;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.nightcore.user.AbstractUserManager;
import su.nightexpress.nightcore.user.data.DefaultUserDataAccessor;

import java.net.InetAddress;
import java.util.UUID;

public class UserManager extends AbstractUserManager<ShopPlugin, ShopUser> {

    public UserManager(@NonNull ShopPlugin plugin, @NonNull DataHandler dataHandler) {
        super(plugin, new DefaultUserDataAccessor<>(dataHandler, dataHandler));
    }

    @Override
    @NonNull
    protected ShopUser create(@NonNull UUID uuid, @NonNull String name, @NonNull InetAddress address) {
        return ShopUser.create(uuid, name);
    }

    @Override
    protected void synchronize(@NonNull ShopUser fetched, @NonNull ShopUser cached) {
        // TODO
    }
}
