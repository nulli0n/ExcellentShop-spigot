package su.nightexpress.nexshop.auction.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;

import static su.nexmedia.engine.utils.Colors.*;

public class AuctionLang extends EngineLang {

    public static final LangKey COMMAND_OPEN_DESC       = LangKey.of("Auction.Command.Open.Desc", "Open auction.");

    public static final LangKey COMMAND_SELL_DESC       = LangKey.of("Auction.Command.Sell.Desc", "Add item on auction.");
    public static final LangKey COMMAND_SELL_USAGE      = LangKey.of("Auction.Command.Sell.Usage", "<price>");
    public static final LangKey COMMAND_SELL_ERROR_NO_ITEM = LangKey.of("Auction.Command.Sell.Error.NoItem", RED + "You must hold an item to do that!");

    public static final LangKey COMMAND_EXPIRED_DESC    = LangKey.of("Auction.Command.Expired.Desc", "List of expired listings.");
    public static final LangKey COMMAND_EXPIRED_USAGE   = LangKey.of("Auction.Command.Expired.Usage", "[player]");
    public static final LangKey COMMAND_HISTORY_DESC    = LangKey.of("Auction.Command.History.Desc", "Your sales history.");
    public static final LangKey COMMAND_HISTORY_USAGE   = LangKey.of("Auction.Command.History.Usage", "[player]");
    public static final LangKey COMMAND_SELLING_DESC    = LangKey.of("Auction.Command.Selling.Desc", "List of your current listings.");
    public static final LangKey COMMAND_SELLING_USAGE   = LangKey.of("Auction.Command.Selling.Usage", "[player]");
    public static final LangKey COMMAND_UNCLAIMED_DESC  = LangKey.of("Auction.Command.Unclaimed.Desc", "List of unclaimed rewards for your listings.");
    public static final LangKey COMMAND_UNCLAIMED_USAGE = LangKey.of("Auction.Command.Unclaimed.Usage", "[player]");

    public static final LangKey LISTING_ADD_SUCCESS_INFO             = LangKey.of("Auction.Listing.Add.Success.Info", "&7You added &ax%listing_item_amount% %listing_item_name%&7 on auction for &a%listing_price%&7. Tax amount: &c%tax%&7.");
    public static final LangKey LISTING_ADD_SUCCESS_ANNOUNCE         = LangKey.of("Auction.Listing.Add.Success.Announce", "&a%player_display_name% &7just put &ax%listing_item_amount% <? show_item:\"%listing_item_value%\" ?>&a%listing_item_name%</> &7on auction for &e%listing_price%&7!");
    public static final LangKey LISTING_ADD_ERROR_BAD_ITEM           = LangKey.of("Auction.Listing.Add.Error.BadItem", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&e%item% &ccan not be added on auction!");
    public static final LangKey LISTING_ADD_ERROR_LIMIT              = LangKey.of("Auction.Listing.Add.Error.Limit", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cYou can not add more than &e%amount% &cactive listings on auction!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_TAX          = LangKey.of("Auction.Listing.Add.Error.Price.Tax", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cYou can't afford the &e%tax%% &cprice tax: &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_CURRENCY_MIN = LangKey.of("Auction.Listing.Add.Error.Price.Currency.Min", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cListing price for &e%currency_name% currency&c can not be smaller than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_CURRENCY_MAX = LangKey.of("Auction.Listing.Add.Error.Price.Currency.Max", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cListing price for &e%currency_name% currency&c can not be greater than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_NEGATIVE     = LangKey.of("Auction.Listing.Add.Error.Price.Negative", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cListing price can not be negative!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_MATERIAL_MIN = LangKey.of("Auction.Listing.Add.Error.Price.Material.Min", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cListing price for &ex1 %item%&c can not be smaller than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_MATERIAL_MAX = LangKey.of("Auction.Listing.Add.Error.Price.Material.Max", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cListing price for &ex1 %item%&c can not be greater than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_DISABLED_GAMEMODE  = LangKey.of("Auction.Listing.Add.Error.DisabledGamemode", "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" + "&cYou can't add items in this game mode!");
    public static final LangKey LISTING_BUY_SUCCESS_INFO             = LangKey.of("Auction.Listing.Buy.Success.Info", "<! prefix:\"false\" type:\"titles:20:50:20\" sound:\"" + Sound.ENTITY_PLAYER_LEVELUP.name() + "\" !>&a&lSuccessful Purchase!\n&7You bought &ax%listing_item_amount% %listing_item_name% &7from &a%listing_seller% &7for &a%listing_price%&7!");
    public static final LangKey LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS   = LangKey.of("Auction.Listing.Buy.Error.NotEnoughFunds", "<! prefix:\"false\" type:\"titles:20:50:20\" sound:\"" + Sound.BLOCK_ANVIL_PLACE.name() + "\" !>&c&lNot Enough Funds!\n&7Balance: &c%balance% &8| &7Needed: &c%listing_price%");
    public static final LangKey NOTIFY_LISTING_UNCLAIMED             = LangKey.of("Auction.Notify.Listing.Unclaimed", """
        <! prefix:"false" !>
        &8&m-------------&8&l[ &e&lAuction Notification &8&l]&8&m-------------
        &7     You have &e%amount% unclaimed rewards&7 for your listings!
        &7                 <? show_text:"&7Click to claim rewards!" run_command:"/ah unclaimed" ?>&a&lClick to Claim Now!</>
        &8&m-----------------------------------------""");
    public static final LangKey NOTIFY_LISTING_EXPIRED               = LangKey.of("Auction.Notify.Listing.Expired", """
         <! prefix:"false" !>
        &8&m-------------&8&l[ &e&lAuction Notification &8&l]&8&m-------------
        &7     You have &e%amount% expired&7 listings!
        &7           <? show_text:"&7Click to return items!" run_command:"/ah expired" ?>&a&lClick to Take Now!</>
        &8&m-----------------------------------------""");
    public static final LangKey NOTIFY_LISTING_CLAIM                 = LangKey.of("Auction.Notify.Listing.Claim", "&7You claimed &a%listing_price%&7 for &a%listing_item_name%&7!");
    public static final LangKey ERROR_DISABLED_WORLD                 = LangKey.of("Auction.Error.DisabledWorld", "&cAuction is disabled in this world!");
}
