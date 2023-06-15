package su.nightexpress.nexshop.data.user;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.data.stock.ProductStockStorage;

import java.util.UUID;

public class ShopUser extends AbstractUser<ExcellentShop> {

    private final UserSettings settings;

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name) {
        this(
            plugin, uuid, name,
            System.currentTimeMillis(), System.currentTimeMillis(),
            new UserSettings(true, true)
        );
    }

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name, long dateCreated, long lastOnline,
        @NotNull UserSettings settings
    ) {
        super(plugin, uuid, name, dateCreated, lastOnline);

        this.settings = settings;
        ProductStockStorage.loadData(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        ProductStockStorage.unloadData(this);
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }
}
