package su.nightexpress.nexshop.shop.auction.config;

import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.lang.LangKey;

public class AuctionLang implements LangColors {

    public static final LangKey COMMAND_OPEN_DESC       = LangKey.of("Auction.Command.Open.Desc", "Open auction.");
    public static final LangKey COMMAND_SELL_DESC       = LangKey.of("Auction.Command.Sell.Desc", "Add item on auction.");
    public static final LangKey COMMAND_SELL_USAGE      = LangKey.of("Auction.Command.Sell.Usage", "<price>");
    public static final LangKey COMMAND_EXPIRED_DESC    = LangKey.of("Auction.Command.Expired.Desc", "List of expired listings.");
    public static final LangKey COMMAND_EXPIRED_USAGE   = LangKey.of("Auction.Command.Expired.Usage", "[player]");
    public static final LangKey COMMAND_HISTORY_DESC    = LangKey.of("Auction.Command.History.Desc", "Your sales history.");
    public static final LangKey COMMAND_HISTORY_USAGE   = LangKey.of("Auction.Command.History.Usage", "[player]");
    public static final LangKey COMMAND_SELLING_DESC    = LangKey.of("Auction.Command.Selling.Desc", "List of your current listings.");
    public static final LangKey COMMAND_SELLING_USAGE   = LangKey.of("Auction.Command.Selling.Usage", "[player]");
    public static final LangKey COMMAND_UNCLAIMED_DESC  = LangKey.of("Auction.Command.Unclaimed.Desc", "List of unclaimed rewards for your listings.");
    public static final LangKey COMMAND_UNCLAIMED_USAGE = LangKey.of("Auction.Command.Unclaimed.Usage", "[player]");

    public static final LangKey LISTING_ADD_SUCCESS_INFO             = LangKey.of("Auction.Listing.Add.Success.Info", "&7You added &ax%listing_item_amount% %listing_item_name%&7 on auction for &a%listing_price%&7. Tax amount: &c%tax%&7.");
    public static final LangKey LISTING_ADD_SUCCESS_ANNOUNCE         = LangKey.of("Auction.Listing.Add.Success.Announce", "&a%player_display_name% &7just put &ax%listing_item_amount% {json: ~showItem: %listing_item_value%;}&a%listing_item_name%{end-json} &7on auction for &e%listing_price%&7!");
    public static final LangKey LISTING_ADD_ERROR_BAD_ITEM           = LangKey.of("Auction.Listing.Add.Error.BadItem", "{message: ~sound: ENTITY_VILLAGER_NO;}&e%item% &ccan not be added on auction!");
    public static final LangKey LISTING_ADD_ERROR_LIMIT              = LangKey.of("Auction.Listing.Add.Error.Limit", "{message: ~sound: ENTITY_VILLAGER_NO;}&cYou can not add more than &e%amount% &cactive listings on auction!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_TAX          = LangKey.of("Auction.Listing.Add.Error.Price.Tax", "{message: ~sound: ENTITY_VILLAGER_NO;}&cYou can't afford the &e%tax%% &cprice tax: &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_CURRENCY_MIN = LangKey.of("Auction.Listing.Add.Error.Price.Currency.Min", "{message: ~sound: ENTITY_VILLAGER_NO;}&cListing price for &e%currency_name% currency&c can not be smaller than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_CURRENCY_MAX = LangKey.of("Auction.Listing.Add.Error.Price.Currency.Max", "{message: ~sound: ENTITY_VILLAGER_NO;}&cListing price for &e%currency_name% currency&c can not be greater than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_NEGATIVE     = LangKey.of("Auction.Listing.Add.Error.Price.Negative", "{message: ~sound: ENTITY_VILLAGER_NO;}&cListing price can not be negative!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_MATERIAL_MIN = LangKey.of("Auction.Listing.Add.Error.Price.Material.Min", "{message: ~sound: ENTITY_VILLAGER_NO;}&cListing price for &ex1 %item%&c can not be smaller than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_PRICE_MATERIAL_MAX = LangKey.of("Auction.Listing.Add.Error.Price.Material.Max", "{message: ~sound: ENTITY_VILLAGER_NO;}&cListing price for &ex1 %item%&c can not be greater than &e%amount%&c!");
    public static final LangKey LISTING_ADD_ERROR_DISABLED_GAMEMODE  = LangKey.of("Auction.Listing.Add.Error.DisabledGamemode", "{message: ~sound: ENTITY_VILLAGER_NO;}&cYou can't add items in this game mode!");
    public static final LangKey LISTING_BUY_SUCCESS_INFO             = LangKey.of("Auction.Listing.Buy.Success.Info", "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 20; ~stay: 50; ~fadeOut: 20; ~sound: ENTITY_PLAYER_LEVELUP;}&a&lSuccessful Purchase!\n&7You bought &ax%listing_item_amount% %listing_item_name% &7from &a%listing_seller% &7for &a%listing_price%&7!");
    public static final LangKey LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS   = LangKey.of("Auction.Listing.Buy.Error.NotEnoughFunds", "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 20; ~stay: 50; ~fadeOut: 20; ~sound: BLOCK_ANVIL_PLACE;}&c&lNot Enough Funds!\n&7Balance: &c%balance% &8| &7Needed: &c%listing_price%");
    public static final LangKey NOTIFY_LISTING_UNCLAIMED             = LangKey.of("Auction.Notify.Listing.Unclaimed", """
        {message: ~prefix: false;}
        &8&m-------------&8&l[ &e&lAuction Notification &8&l]&8&m-------------
        &7     You have &e%amount% unclaimed rewards&7 for your listings!
        &7                 {json: ~showText: &7Click to claim rewards!; ~runCommand: /ah unclaimed;}&a&lClick to Claim Now!{end-json}
        &8&m-----------------------------------------""");
    public static final LangKey NOTIFY_LISTING_EXPIRED               = LangKey.of("Auction.Notify.Listing.Expired", """
        {message: ~prefix: false;}
        &8&m-------------&8&l[ &e&lAuction Notification &8&l]&8&m-------------
        &7     You have &e%amount% expired&7 listings!
        &7           {json: ~showText: &7Click to claim rewards!; ~runCommand: /ah expired;}&a&lClick to Take Now!{end-json}
        &8&m-----------------------------------------""");
    public static final LangKey NOTIFY_LISTING_CLAIM                 = LangKey.of("Auction.Notify.Listing.Claim", "&7You claimed &a%listing_price%&7 for &a%listing_item_name%&7!");
    public static final LangKey ERROR_DISABLED_WORLD                 = LangKey.of("Auction.Error.DisabledWorld", "&cAuction is disabled in this world!");
}
