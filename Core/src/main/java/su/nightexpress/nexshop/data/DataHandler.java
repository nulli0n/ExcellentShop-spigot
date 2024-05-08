package su.nightexpress.nexshop.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.data.impl.ChestDataHandler;
import su.nightexpress.nexshop.data.impl.VirtualDataHandler;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.data.user.UserSettings;
import su.nightexpress.nightcore.database.AbstractUserDataHandler;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLValue;
import su.nightexpress.nightcore.database.sql.column.ColumnType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DataHandler extends AbstractUserDataHandler<ShopPlugin, ShopUser> {

    private static final SQLColumn COL_USER_SETTINGS = SQLColumn.of("settings", ColumnType.STRING);

    private final VirtualDataHandler virtualDataHandler;
    private       ChestDataHandler   chestDataHandler;

    private final Function<ResultSet, ShopUser> funcUser;

    public DataHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
        //if (Config.MODULES_VIRTUAL_SHOP_ENABLED.get()) {
            this.virtualDataHandler = new VirtualDataHandler(this.plugin, this);
        //}
        if (Config.MODULES_CHEST_SHOP_ENABLED.get()) {
            this.chestDataHandler = new ChestDataHandler(this.plugin, this);
        }

        this.funcUser = (resultSet) -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName()));
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                long dateCreated = resultSet.getLong(COLUMN_USER_DATE_CREATED.getName());
                long date = resultSet.getLong(COLUMN_USER_LAST_ONLINE.getName());

                UserSettings settings = gson.fromJson(resultSet.getString(COL_USER_SETTINGS.getName()), new TypeToken<UserSettings>() {}.getType());

                return new ShopUser(plugin, uuid, name, dateCreated, date, settings);
            }
            catch (SQLException exception) {
                return null;
            }
        };
    }

    @NotNull
    public Gson gson() {
        return this.gson;
    }

    public VirtualDataHandler getVirtualDataHandler() {
        return this.virtualDataHandler;
    }

    public ChestDataHandler getChestDataHandler() {
        return this.chestDataHandler;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (this.virtualDataHandler != null) this.virtualDataHandler.load();
        if (this.chestDataHandler != null) this.chestDataHandler.load();
    }

    @Override
    public void onPurge() {
        super.onPurge();
        if (this.virtualDataHandler != null) this.virtualDataHandler.purge();
        if (this.chestDataHandler != null) this.chestDataHandler.purge();
    }

    @Override
    public void onSynchronize() {
        if (this.virtualDataHandler != null) this.virtualDataHandler.synchronize();
        if (this.chestDataHandler != null) this.chestDataHandler.synchronize();
    }

    @Override
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Collections.singletonList(COL_USER_SETTINGS);
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull ShopUser shopUser) {
        return Collections.singletonList(
            COL_USER_SETTINGS.toValue(this.gson.toJson(shopUser.getSettings()))
        );
    }

    @Override
    @NotNull
    protected Function<ResultSet, ShopUser> getUserFunction() {
        return this.funcUser;
    }
}
