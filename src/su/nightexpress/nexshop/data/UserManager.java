package su.nightexpress.nexshop.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.data.object.ShopUser;

public class UserManager extends AbstractUserManager<ExcellentShop, ShopUser> {

    public UserManager(@NotNull ExcellentShop plugin) {
        super(plugin, plugin);
    }

    @Override
    @NotNull
    protected ShopUser createData(@NotNull Player player) {
        return new ShopUser(plugin, player);
    }
}
