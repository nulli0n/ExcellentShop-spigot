package su.nightexpress.nexshop.data;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.data.user.ShopUser;

import java.util.UUID;

public class UserManager extends AbstractUserManager<ExcellentShop, ShopUser> {

    public UserManager(@NotNull ExcellentShop plugin) {
        super(plugin, plugin);
    }

    @Override
    protected @NotNull ShopUser createData(@NotNull UUID uuid, @NotNull String name) {
        return new ShopUser(plugin, uuid, name);
    }
}
