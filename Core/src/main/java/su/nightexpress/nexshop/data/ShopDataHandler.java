package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.DataTypes;
import su.nexmedia.engine.api.data.StorageType;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.data.object.UserProductLimit;
import su.nightexpress.nexshop.data.object.UserSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ShopDataHandler extends AbstractUserDataHandler<ExcellentShop, ShopUser> {

    private static ShopDataHandler INSTANCE;

    private final Function<ResultSet, ShopUser>           FUNC_USER;

    protected ShopDataHandler(@NotNull ExcellentShop plugin) {
        super(plugin, plugin);

        this.FUNC_USER = (resultSet) -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COL_USER_UUID));
                String name = resultSet.getString(COL_USER_NAME);
                long dateCreated = resultSet.getLong(COL_USER_DATE_CREATED);
                long date = resultSet.getLong(COL_USER_LAST_ONLINE);

                UserSettings settings = gson.fromJson(resultSet.getString("settings"), new TypeToken<UserSettings>(){}.getType());
                Map<String, Map<TradeType, UserProductLimit>> limits = gson.fromJson(resultSet.getString("virtualshop_limits"), new TypeToken<Map<String, Map<TradeType, UserProductLimit>>>(){}.getType());
                if (limits == null) limits = new HashMap<>();

                return new ShopUser(plugin, uuid, name, dateCreated, date, settings, limits);
            }
            catch (SQLException e) {
                return null;
            }
        };
    }

    @NotNull
    public static ShopDataHandler getInstance(@NotNull ExcellentShop plugin) {
        if (INSTANCE == null) {
            INSTANCE = new ShopDataHandler(plugin);
        }
        return INSTANCE;
    }
	
	/*@Override
	@NotNull
	protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
		return super.registerAdapters(builder)
				.registerTypeAdapter(UserProductLimit.class, new UserLimitSerializer())
				;
	}*/

    @Override
    protected void onShutdown() {
        super.onShutdown();
        INSTANCE = null;
    }

    @Override
    public void onSynchronize() {

    }

    @Override
    protected void onTableCreate() {
        String def = this.getDataType() == StorageType.SQLITE ? "{}" : "";
        this.addColumn(this.tableUsers, "virtualshop_limits", DataTypes.STRING.build(this.getDataType()), def);

        super.onTableCreate();
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToCreate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("settings", DataTypes.STRING.build(this.getDataType()));
        map.put("virtual_limits", DataTypes.STRING.build(this.getDataType()));
        map.put("virtualshop_limits", DataTypes.STRING.build(this.getDataType()));
        return map;
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToSave(@NotNull ShopUser user) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("settings", this.gson.toJson(user.getSettings()));
        map.put("virtual_limits", "{}");
        map.put("virtualshop_limits", this.gson.toJson(user.getVirtualProductLimits()));
        return map;
    }

    @Override
    @NotNull
    protected Function<ResultSet, ShopUser> getFunctionToUser() {
        return this.FUNC_USER;
    }
}
