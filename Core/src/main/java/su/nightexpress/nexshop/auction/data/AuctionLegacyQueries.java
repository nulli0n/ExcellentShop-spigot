package su.nightexpress.nexshop.auction.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.nightcore.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

public class AuctionLegacyQueries {

    static final Column COLUMN_ITEM      = Column.of("itemStack", ColumnType.STRING);
    static final Column COLUMN_ITEM_DATA = Column.of("itemData", ColumnType.STRING);
    static final Column COLUMN_HANDLER   = Column.of("itemHandler", ColumnType.STRING);

    private static final String PLUGIN_ITEM_DELIMITER = ":::";

    public static final Function<ResultSet, ActiveListing> ACTIVE_LISTING_LOADER = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());

            ItemContent typing = null;

            String serialized = resultSet.getString(COLUMN_ITEM.getName());
            String handlerName = resultSet.getString(COLUMN_HANDLER.getName());

            boolean isVanilla = handlerName.equalsIgnoreCase("VANILLA");
            boolean isCustom = handlerName.equalsIgnoreCase("PLUGIN");

            ContentType type = Enums.get(handlerName, ContentType.class);
            if (type != null || isVanilla || isCustom) {
                if (!serialized.contains(PLUGIN_ITEM_DELIMITER) && !serialized.contains("dataVersion")) {
                    ItemStack itemStack = readItemTag(serialized);
                    ItemTag itemTag = itemStack == null ? null : ItemNbt.getTag(itemStack);
                    if (itemStack == null || itemTag == null) {
                        ShopAPI.getPlugin().error("Could not update ItemStack: " + serialized);
                        return null;
                    }

                    typing = new ItemContent(new AdaptedVanillaStack(itemTag), true);
                }
                else {
                    typing = isVanilla ? vanillaTypeFromJson(serialized) : customTypeFromJson(serialized);
                }
            }
            if (typing == null) return null;

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            long expireDate = resultSet.getLong(AuctionDatabase.COLUMN_EXPIRE_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new ActiveListing(id, owner, ownerName, typing, currency, price, dateCreation, expireDate, deletionDate);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, CompletedListing> COMPLETED_LISTING_LOADER = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());
            String buyerName = resultSet.getString(AuctionDatabase.COLUMN_BUYER_NAME.getName());

            ItemContent typing = null;

            String serialized = resultSet.getString(COLUMN_ITEM.getName());
            String handlerName = resultSet.getString(COLUMN_HANDLER.getName());
            boolean isVanilla = handlerName.equalsIgnoreCase("VANILLA");
            boolean isCustom = handlerName.equalsIgnoreCase("PLUGIN");

            ContentType type = Enums.get(handlerName, ContentType.class);
            if (type != null || isVanilla || isCustom) {
                if (!serialized.contains(PLUGIN_ITEM_DELIMITER) && !serialized.contains("dataVersion")) {
                    ItemStack itemStack = readItemTag(serialized);
                    ItemTag itemTag = itemStack == null ? null : ItemNbt.getTag(itemStack);
                    if (itemStack == null || itemTag == null) {
                        ShopAPI.getPlugin().error("Could not update ItemStack: " + serialized);
                        return null;
                    }

                    typing = new ItemContent(new AdaptedVanillaStack(itemTag), true);
                }
                else {
                    typing = isVanilla ? vanillaTypeFromJson(serialized) : customTypeFromJson(serialized);
                }
            }
            if (typing == null) return null;

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            boolean isNotified = resultSet.getBoolean(AuctionDatabase.COLUMN_IS_PAID.getName());
            long buyDate = resultSet.getLong(AuctionDatabase.COLUMN_BUY_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new CompletedListing(id, owner, ownerName, buyerName, typing, currency, price, dateCreation, buyDate, deletionDate, isNotified);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };



    public static final Function<ResultSet, ActiveListing> ACTIVE_LISTING_LOADER_2 = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());

            String handlerName = resultSet.getString(COLUMN_HANDLER.getName());
            boolean isVanilla = handlerName.equalsIgnoreCase("VANILLA");
            String serialized = resultSet.getString(COLUMN_ITEM_DATA.getName());

            ItemContent typing = isVanilla ? vanillaTypeFromJson(serialized) : customTypeFromJson(serialized);
            if (typing == null) return null;

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            long expireDate = resultSet.getLong(AuctionDatabase.COLUMN_EXPIRE_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new ActiveListing(id, owner, ownerName, typing, currency, price, dateCreation, expireDate, deletionDate);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, CompletedListing> COMPLETED_LISTING_LOADER_2 = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());
            String buyerName = resultSet.getString(AuctionDatabase.COLUMN_BUYER_NAME.getName());

            String handlerName = resultSet.getString(COLUMN_HANDLER.getName());
            boolean isVanilla = handlerName.equalsIgnoreCase("VANILLA");
            String serialized = resultSet.getString(COLUMN_ITEM_DATA.getName());

            ItemContent typing = isVanilla ? vanillaTypeFromJson(serialized) : customTypeFromJson(serialized);
            if (typing == null) return null;

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            boolean isNotified = resultSet.getBoolean(AuctionDatabase.COLUMN_IS_PAID.getName());
            long buyDate = resultSet.getLong(AuctionDatabase.COLUMN_BUY_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new CompletedListing(id, owner, ownerName, buyerName, typing, currency, price, dateCreation, buyDate, deletionDate, isNotified);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };



    @Nullable
    private static ItemStack readItemTag(@NotNull String serialized) {
        return Version.isAtLeast(Version.MC_1_21) && serialized.contains("{") ? ItemNbt.fromTagString(serialized) : ItemNbt.decompress(serialized);
    }

    @Nullable
    public static ItemContent vanillaTypeFromJson(@NotNull String serialized) {
        ItemTag tag = DataHandler.GSON.fromJson(serialized, ItemTag.class);
        if (tag == null) return null;

        ItemStack itemStack = ItemNbt.fromTag(tag);
        if (itemStack == null) return null;

        return new ItemContent(new AdaptedVanillaStack(tag), true);
    }

    @Nullable
    public static ItemContent customTypeFromJson(@NotNull String serialized) {
        String[] split = serialized.split(PLUGIN_ITEM_DELIMITER);
        if (split.length < 2) return null;

        String handlerName = split[0];
        ItemAdapter<?> adapter = ItemBridge.getAdapter(handlerName);
        if (!(adapter instanceof IdentifiableItemAdapter itemAdapter)) return null;

        String itemId = split[1];
        int amount = split.length >= 3 ? NumberUtil.getIntegerAbs(split[2]) : 1;

        return new ItemContent(new AdaptedCustomStack(itemAdapter, new ItemIdData(itemId, amount)), true);
    }
}
