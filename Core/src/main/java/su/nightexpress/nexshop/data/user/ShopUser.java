package su.nightexpress.nexshop.data.user;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;

import java.util.UUID;

public class ShopUser extends AbstractUser<ExcellentShop> {

    private final UserSettings settings;

    public static ShopUser create(@NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name) {
        long date = System.currentTimeMillis();
        UserSettings settings = UserSettings.createDefault();

        return new ShopUser(plugin, uuid, name, date, date, settings);
    }

    public ShopUser(@NotNull ExcellentShop plugin,
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
                module.getShops().forEach(shop -> {
                    if (shop.getStock() instanceof VirtualStock virtualStock) {
                        virtualStock.load(this.getId());
                    }
                });
            });
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();

        VirtualShopModule module = this.plugin.getVirtualShop();
        if (module != null) {
            module.getShops().forEach(shop -> {
                if (shop.getStock() instanceof VirtualStock virtualStock) {
                    virtualStock.unload(this.getId());
                }
            });
        }
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
