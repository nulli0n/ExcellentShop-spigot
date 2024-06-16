package su.nightexpress.nexshop.data.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.database.AbstractUser;

import java.util.UUID;

public class ShopUser extends AbstractUser<ShopPlugin> {

    private final UserSettings settings;

    public static ShopUser create(@NotNull ShopPlugin plugin, @NotNull UUID uuid, @NotNull String name) {
        long date = System.currentTimeMillis();
        UserSettings settings = UserSettings.createDefault();

        return new ShopUser(plugin, uuid, name, date, date, settings);
    }

    public ShopUser(@NotNull ShopPlugin plugin,
                    @NotNull UUID uuid,
                    @NotNull String name,
                    long dateCreated,
                    long lastOnline,
                    @NotNull UserSettings settings
    ) {
        super(plugin, uuid, name, dateCreated, lastOnline);
        this.settings = settings;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        VirtualShopModule module = this.plugin.getVirtualShop();
        if (module != null) {
            this.plugin.runTaskAsync(task -> {
                module.getShops().forEach(shop -> shop.getStock().load(this.getId()));
            });
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();

        VirtualShopModule module = this.plugin.getVirtualShop();
        if (module != null) {
            module.getShops().forEach(shop -> shop.getStock().unload(this.getId()));
        }
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
