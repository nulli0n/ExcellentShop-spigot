package su.nightexpress.nexshop.auction.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.product.type.impl.PluginProductType;
import su.nightexpress.nexshop.product.type.impl.VanillaProductType;
import su.nightexpress.nightcore.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

public class AuctionLegacyQueries {

    public static final Function<ResultSet, ActiveListing> ACTIVE_LISTING_LOADER = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());

            ProductTyping typing;

            String serialized = resultSet.getString(AuctionDatabase.COLUMN_ITEM.getName());
            String handlerName = resultSet.getString(AuctionDatabase.COLUMN_HANDLER.getName());

            ProductType type = Enums.get(handlerName, ProductType.class);
            if (type != null) {
                if (!serialized.contains(AuctionDatabase.PLUGIN_ITEM_DELIMITER) && !serialized.contains("dataVersion")) {
                    ItemStack itemStack = readItemTag(serialized);
                    ItemTag itemTag = itemStack == null ? null : ItemNbt.getTag(itemStack);
                    if (itemStack == null || itemTag == null) {
                        ShopAPI.getPlugin().error("Could not update ItemStack: " + serialized);
                        return null;
                    }

                    typing = new VanillaProductType(itemStack, true);
                }
                else {
                    typing = AuctionDatabase.typingFromJson(type, serialized);
                }
            }
            // ------ REVERT 4.13.3 CHANGES - START ------
            else {
                if (handlerName.equalsIgnoreCase("bukkit_item") || handlerName.isBlank()) {
                    String delimiter = " \\| ";
                    String[] split = serialized.split(delimiter);
                    String tagString = split[0];

                    typing = AuctionDatabase.typingFromJson(ProductType.VANILLA, tagString);
                }
                else {
                    String delimiter = " \\| ";
                    String[] split = serialized.split(delimiter);
                    String itemId = split[0];
                    int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

                    typing = new PluginProductType(handlerName, itemId, amount);
                }
            }
            // ------ REVERT 4.13.3 CHANGES - END ------

            if (!(typing instanceof PhysicalTyping physicalTyping)/* || !physicalTyping.isValid()*/) {
                ShopAPI.getPlugin().error("Invalid listing data: '" + serialized + "'. Handler: '" + handlerName + "'.");
                return null;
            }

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

            return new ActiveListing(id, owner, ownerName, physicalTyping, currency, price, dateCreation, expireDate, deletionDate);
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

            ProductTyping typing;

            String serialized = resultSet.getString(AuctionDatabase.COLUMN_ITEM.getName());
            String handlerName = resultSet.getString(AuctionDatabase.COLUMN_HANDLER.getName());

            ProductType type = Enums.get(handlerName, ProductType.class);
            if (type != null) {
                if (!serialized.contains(AuctionDatabase.PLUGIN_ITEM_DELIMITER) && !serialized.contains("dataVersion")) {
                    ItemStack itemStack = readItemTag(serialized);
                    ItemTag itemTag = itemStack == null ? null : ItemNbt.getTag(itemStack);
                    if (itemStack == null || itemTag == null) {
                        ShopAPI.getPlugin().error("Could not update ItemStack: " + serialized);
                        return null;
                    }

                    typing = new VanillaProductType(itemStack, true);
                }
                else {
                    typing = AuctionDatabase.typingFromJson(type, serialized);
                }
            }
            // ------ REVERT 4.13.3 CHANGES - START ------
            else {
                if (handlerName.equalsIgnoreCase("bukkit_item") || handlerName.isBlank()) {
                    String delimiter = " \\| ";
                    String[] split = serialized.split(delimiter);
                    String tagString = split[0];

                    typing = AuctionDatabase.typingFromJson(ProductType.VANILLA, tagString);
                }
                else {
                    String delimiter = " \\| ";
                    String[] split = serialized.split(delimiter);
                    String itemId = split[0];
                    int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

                    typing = new PluginProductType(handlerName, itemId, amount);
                }
            }
            // ------ REVERT 4.13.3 CHANGES - END ------

            if (!(typing instanceof PhysicalTyping physicalTyping)/* || !physicalTyping.isValid()*/) {
                ShopAPI.getPlugin().error("Invalid listing data: '" + serialized + "'. Handler: '" + handlerName + "'.");
                return null;
            }

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

            return new CompletedListing(id, owner, ownerName, buyerName, physicalTyping, currency, price, dateCreation, buyDate, deletionDate, isNotified);
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
}
