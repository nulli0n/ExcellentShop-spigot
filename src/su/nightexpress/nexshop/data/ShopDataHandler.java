package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.data.DataTypes;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.data.object.UserProductLimit;
import su.nightexpress.nexshop.data.object.UserSettings;
import su.nightexpress.nexshop.shop.auction.object.AuctionHistoryItem;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class ShopDataHandler extends AbstractUserDataHandler<ExcellentShop, ShopUser> {

    private static ShopDataHandler INSTANCE;

    private final String TABLE_AUCTION_ITEMS;
    private final String TABLE_AUCTION_HISTORY;

    private final Function<ResultSet, ShopUser>           FUNC_USER;
    private final Function<ResultSet, AuctionListing>     FUNC_AUC_LISTING;
    private final Function<ResultSet, AuctionHistoryItem> FUNC_AUC_HISTORY;

    protected ShopDataHandler(@NotNull ExcellentShop plugin) throws SQLException {
        super(plugin);
        this.TABLE_AUCTION_ITEMS = plugin.getNameRaw() + "_auction_items";
        this.TABLE_AUCTION_HISTORY = plugin.getNameRaw() + "_auction_history";

        this.FUNC_USER = (rs) -> {
            try {
                UUID uuid = UUID.fromString(rs.getString(COL_USER_UUID));
                String name = rs.getString(COL_USER_NAME);
                long date = rs.getLong(COL_USER_LAST_ONLINE);

                UserSettings settings = gson.fromJson(rs.getString("settings"), new TypeToken<UserSettings>(){}.getType());
                Map<String, Map<TradeType, UserProductLimit>> limits = gson.fromJson(rs.getString("virtualshop_limits"), new TypeToken<Map<String, Map<TradeType, UserProductLimit>>>(){}.getType());

                return new ShopUser(plugin, uuid, name, date, settings, limits);
            } catch (SQLException e) {
                return null;
            }
        };

        this.FUNC_AUC_LISTING = (rs) -> {
            try {
                UUID id = UUID.fromString(rs.getString("aucId"));
                UUID owner = UUID.fromString(rs.getString("owner"));
                String ownerName = rs.getString("ownerName");
                ItemStack itemStack = ItemUT.fromBase64(rs.getString("itemStack"));
                if (itemStack == null) {
                    plugin.error("Could not load auction item!");
                    return null;
                }

                double price = rs.getDouble("price");
                long expireDate = rs.getLong("expireDate");
                long deleteDate = rs.getLong("deleteDate");

                return new AuctionListing(id, owner, ownerName, itemStack, price, expireDate, deleteDate);
            } catch (SQLException e) {
                return null;
            }
        };

        this.FUNC_AUC_HISTORY = (rs) -> {
            try {
                UUID id = UUID.fromString(rs.getString("aucId"));
                UUID owner = UUID.fromString(rs.getString("owner"));
                String ownerName = rs.getString("ownerName");
                String buyerName = rs.getString("buyerName");

                ItemStack itemStack = ItemUT.fromBase64(rs.getString("itemStack"));
                if (itemStack == null) {
                    plugin.error("Could not load auction item!");
                    return null;
                }

                double price = rs.getDouble("price");
                boolean isPaid = rs.getBoolean("isPaid");
                long buyDate = rs.getLong("buyDate");
                long deleteDate = rs.getLong("deleteDate");

                return new AuctionHistoryItem(id, owner, ownerName, buyerName, itemStack, price, isPaid, buyDate, deleteDate);
            } catch (SQLException e) {
                return null;
            }
        };
    }

    @Override
    public void purge() {
        super.purge();

        this.getAuctionHistory().forEach(historyItem -> {
            if (!historyItem.isValid()) {
                this.deleteAuctionHistory(historyItem, false);
            }
        });
    }

    @NotNull
    public static ShopDataHandler getInstance(@NotNull ExcellentShop plugin) throws SQLException {
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
    protected void onTableCreate() {
        this.addColumn(this.tableUsers, "virtualshop_limits", DataTypes.STRING.build(this.dataType), "{}");

        // Create auction items table
        LinkedHashMap<String, String> aucLis = new LinkedHashMap<>();
        aucLis.put("aucId", DataTypes.STRING.build(this.dataType));
        aucLis.put("owner", DataTypes.STRING.build(this.dataType));
        aucLis.put("ownerName", DataTypes.STRING.build(this.dataType));
        aucLis.put("itemStack", DataTypes.STRING.build(this.dataType));
        aucLis.put("price", DataTypes.DOUBLE.build(this.dataType));
        aucLis.put("expireDate", DataTypes.LONG.build(this.dataType));
        aucLis.put("deleteDate", DataTypes.LONG.build(this.dataType));
        this.createTable(this.TABLE_AUCTION_ITEMS, aucLis);

        // Create auction history table
        LinkedHashMap<String, String> aucHis = new LinkedHashMap<>();
        aucHis.put("aucId", DataTypes.STRING.build(this.dataType));
        aucHis.put("owner", DataTypes.STRING.build(this.dataType));
        aucHis.put("ownerName", DataTypes.STRING.build(this.dataType));
        aucHis.put("buyerName", DataTypes.STRING.build(this.dataType));
        aucHis.put("itemStack", DataTypes.STRING.build(this.dataType));
        aucHis.put("price", DataTypes.DOUBLE.build(this.dataType));
        aucHis.put("isPaid", DataTypes.BOOLEAN.build(this.dataType));
        aucHis.put("buyDate", DataTypes.LONG.build(this.dataType));
        aucHis.put("deleteDate", DataTypes.LONG.build(this.dataType));
        this.createTable(this.TABLE_AUCTION_HISTORY, aucHis);

        super.onTableCreate();
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToCreate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("settings", DataTypes.STRING.build(this.dataType));
        map.put("virtual_limits", DataTypes.STRING.build(this.dataType));
        map.put("virtualshop_limits", DataTypes.STRING.build(this.dataType));
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

    @NotNull
    public List<su.nightexpress.nexshop.shop.auction.object.AuctionListing> getAuctionListing() {
        return this.getDatas(this.TABLE_AUCTION_ITEMS, Collections.emptyMap(), this.FUNC_AUC_LISTING, -1);
    }

    @NotNull
    public List<su.nightexpress.nexshop.shop.auction.object.AuctionHistoryItem> getAuctionHistory() {
        return this.getDatas(this.TABLE_AUCTION_HISTORY, Collections.emptyMap(), this.FUNC_AUC_HISTORY, -1);
    }

    public void addAuctionListing(@NotNull AuctionListing listing, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("aucId", listing.getId().toString());
        map.put("owner", listing.getOwner().toString());
        map.put("ownerName", listing.getOwnerName());
        map.put("itemStack", ItemUT.toBase64(listing.getItemStack()));
        map.put("price", String.valueOf(listing.getPrice()));
        map.put("expireDate", String.valueOf(listing.getExpireDate()));
        map.put("deleteDate", String.valueOf(listing.getDeleteDate()));

        if (!async) {
            this.addData(this.TABLE_AUCTION_ITEMS, map);
        }
        else {
            this.plugin.runTask(c -> this.addData(this.TABLE_AUCTION_ITEMS, map), async);
        }
    }

    public void deleteAuctionListing(@NotNull AuctionListing bid, boolean async) {
        String sql = "DELETE FROM " + this.TABLE_AUCTION_ITEMS + " WHERE `aucId` = '" + bid.getId().toString() + "'";
        if (!async) {
            this.executeSQL(sql);
        }
        else {
            this.plugin.runTask((c) -> this.executeSQL(sql), async);
        }
    }

    public void addAuctionHistory(@NotNull AuctionHistoryItem historyItem, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("aucId", historyItem.getId().toString());
        map.put("owner", historyItem.getOwner().toString());
        map.put("ownerName", historyItem.getOwnerName());
        map.put("buyerName", historyItem.getBuyerName());
        map.put("itemStack", ItemUT.toBase64(historyItem.getItemStack()));
        map.put("price", String.valueOf(historyItem.getPrice()));
        map.put("isPaid", String.valueOf(historyItem.isPaid() ? 1 : 0));
        map.put("buyDate", String.valueOf(historyItem.getBuyDate()));
        map.put("deleteDate", String.valueOf(historyItem.getDeleteDate()));

        if (!async) {
            this.addData(this.TABLE_AUCTION_HISTORY, map);
        }
        else {
            this.plugin.runTask((c) -> this.addData(this.TABLE_AUCTION_HISTORY, map), async);
        }
    }

    public void saveAuctionHistory(@NotNull AuctionHistoryItem historyItem, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("isPaid", String.valueOf(historyItem.isPaid() ? 1 : 0));

        LinkedHashMap<String, String> mapWhere = new LinkedHashMap<>();
        mapWhere.put("aucId", historyItem.getId().toString());

        if (!async) {
            this.saveData(TABLE_AUCTION_HISTORY, map, mapWhere);
        }
        else {
            this.plugin.runTask((c) -> this.saveData(TABLE_AUCTION_HISTORY, map, mapWhere), async);
        }
    }

    public void deleteAuctionHistory(@NotNull AuctionHistoryItem historyItem, boolean async) {
        String sql = "DELETE FROM " + this.TABLE_AUCTION_HISTORY + " WHERE `aucId` = '" + historyItem.getId().toString() + "'";

        if (!async) {
            this.executeSQL(sql);
        }
        else {
            this.plugin.runTask(c -> this.executeSQL(sql), async);
        }
    }
}
