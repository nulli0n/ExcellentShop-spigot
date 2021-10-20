package su.nightexpress.nexshop.config;

import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.core.config.CoreLang;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.auction.menu.AuctionMainMenu.AuctionSortType;


public class Lang extends CoreLang {

    public Lang(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    protected void setupEnums() {
        this.setupEnum(GameMode.class);
        this.setupEnum(AuctionSortType.class);
        this.setupEnum(TradeType.class);
    }

    public ILangMsg Module_Cmd_Reload = new ILangMsg(this, "Module &a%module% &7reloaded!");

    public ILangMsg Editor_Enter_Command  = new ILangMsg(this, "&7Enter new command...");
    public ILangMsg Editor_Enter_Price    = new ILangMsg(this, "&7Enter new price...");
    public ILangMsg Editor_Enter_Currency = new ILangMsg(this, "&7Enter currency id...");

    public ILangMsg Shop_Product_Error_Unbuyable      = new ILangMsg(this, "&cYou can not buy this item.");
    public ILangMsg Shop_Product_Error_Unsellable     = new ILangMsg(this, "&cYou can not sell this item!");
    public ILangMsg Shop_Product_Error_OutOfStock = new ILangMsg(this, "&cThis product is out of stock!");
    public ILangMsg Shop_Product_Error_OutOfSpace = new ILangMsg(this, "&cThis shop is out of space!");
    public ILangMsg Shop_Product_Error_OutOfFunds = new ILangMsg(this, "This shop is out of money!");
    public ILangMsg Shop_Product_Error_FullStock  = new ILangMsg(this, "&cThis product is full of stock!");
    public ILangMsg Shop_Product_Error_FullInventory  = new ILangMsg(this, "&cYou can't buy items while your inventory is full!");
    public ILangMsg Shop_Product_Error_TooExpensive   = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&c&lToo Expensive! \n &7You need: &c%price%&7!");
    public ILangMsg Shop_Product_Error_NotEnoughItems = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&c&lNot Enough Items! \n &7You need: &cx%amount% %item%&7!");

    public ILangMsg Auction_Command_Open_Desc     = new ILangMsg(this, "Open auction.");
    public ILangMsg Auction_Command_Sell_Desc     = new ILangMsg(this, "Add item on auction.");
    public ILangMsg Auction_Command_Sell_Usage    = new ILangMsg(this, "<price>");
    public ILangMsg Auction_Command_Expired_Desc  = new ILangMsg(this, "List of expired items.");
    public ILangMsg Auction_Command_Expired_Usage = new ILangMsg(this, "[player]");
    public ILangMsg Auction_Command_History_Desc  = new ILangMsg(this, "List of sold items.");
    public ILangMsg Auction_Command_History_Usage = new ILangMsg(this, "[player]");

    public ILangMsg Auction_Listing_Add_Success_Info             = new ILangMsg(this, "&7You added &ax%listing_item_amount% %listing_item_name%&7 on auction for &a$%listing_price%&7.");
    public ILangMsg Auction_Listing_Add_Success_Tax              = new ILangMsg(this, "&7You paid &c%tax-percent%% &7tax &4($%tax-amount%) &7to add item on auction.");
    public ILangMsg Auction_Listing_Add_Success_Announce         = new ILangMsg(this, "&a%player% &7just put &ax%listing_item_amount% %listing_item_name% &7on auction for &e$%listing_price%&7!");
    public ILangMsg Auction_Listing_Add_Error_BadItem            = new ILangMsg(this, "&e%item% &ccould not be added on auction!");
    public ILangMsg Auction_Listing_Add_Error_Limit              = new ILangMsg(this, "&cYou can not add more than &e%amount% &cactive bids on auction!");
    public ILangMsg Auction_Listing_Add_Error_Price_Tax          = new ILangMsg(this, "&cYou're unable to pay &e%tax-percent%% &cprice tax: &e$%tax-amount%&c!");
    public ILangMsg Auction_Listing_Add_Error_Price_Min          = new ILangMsg(this, "&cItem price can not be lower than &e$%min%&c!");
    public ILangMsg Auction_Listing_Add_Error_Price_Max          = new ILangMsg(this, "&cItem price can not be greater than &e$%max%&c!");
    public ILangMsg Auction_Listing_Add_Error_Price_Negative     = new ILangMsg(this, "&cItem price can not be negative!");
    public ILangMsg Auction_Listing_Add_Error_Price_Material_Min = new ILangMsg(this, "&e%material% &cprice (for 1 item) can not be lower than &e$%min%&c!");
    public ILangMsg Auction_Listing_Add_Error_Price_Material_Max = new ILangMsg(this, "&e%material% &cprice (for 1 item) can not be greater than &e$%max%&c!");
    public ILangMsg Auction_Listing_Add_Error_DisabledGamemode   = new ILangMsg(this, "&cYou can't add items in this game mode!");

    public ILangMsg Auction_Listing_Buy_Success_Info  = new ILangMsg(this, "{message: ~prefix: false;}&eYou bought &6x%listing_item_amount% %listing_item_name% &efrom &6%listing_seller% &efor &6$%listing_price%&e!");
    public ILangMsg Auction_Listing_Buy_Error_NoMoney = new ILangMsg(this, "{message: ~prefix: false;}&cYou don't have enough money! You have: &e$%balance%&c, need: &e$%listing_price%&c.");

    public ILangMsg Auction_Listing_Sell_Success_Info = new ILangMsg(this, "{message: ~prefix: false;}&eYou sold &6x%listing_item_amount% %listing_item_name% &eto &6%listing_buyer% &efor &6$%listing_price%&e!");
    public ILangMsg Auction_Listing_Expired_Notify    = new ILangMsg(this, "{message: ~prefix: false;}&eYou have &6%amount% &eexpired auction listings! Take them at &6/auc expired &ebefore they are deleted!");

    public ILangMsg Auction_Error_DisabledWorld = new ILangMsg(this, "&cAuction is disabled in this world!");

    public ILangMsg Virtual_Shop_Command_Open_Desc  = new ILangMsg(this, "Opens specified shop.");
    public ILangMsg Virtual_Shop_Command_Open_Usage = new ILangMsg(this, "[shop] [player]");

    public ILangMsg Virtual_Shop_MainMenu_Error_Disabled = new ILangMsg(this, "&cMain shop menu is disabled!");
    public ILangMsg Virtual_Shop_Open_Error_BadWorld     = new ILangMsg(this, "&cShop is disabled in this world!");
    public ILangMsg Virtual_Shop_Open_Error_BadGamemode  = new ILangMsg(this, "&cYou can't use shop while in &e%mode% &cgamemode!");
    public ILangMsg Virtual_Shop_Open_Error_InvalidShop  = new ILangMsg(this, "&cNo such shop!");

    public ILangMsg Virtual_Shop_Product_Purchase_Sell = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful! \n &7You sold &ax%amount% %item% &7for &a%price%&7!");
    public ILangMsg Virtual_Shop_Product_Purchase_Buy  = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful Purchase! \n &7You bought &ax%amount% %item% &7for &a%price%&7!");

    public ILangMsg Virtual_Shop_Editor_Enter_Id    = new ILangMsg(this, "&7Enter shop id...");
    public ILangMsg Virtual_Shop_Editor_Enter_NpcId = new ILangMsg(this, "&7Enter Citizens id...");
    public ILangMsg Virtual_Shop_Editor_Enter_Title = new ILangMsg(this, "&7Enter shop title...");

    public ILangMsg Virtual_Shop_Editor_Enter_Amount           = new ILangMsg(this, "&7Enter new amount...");
    public ILangMsg Virtual_Shop_Editor_Enter_Time_Seconds     = new ILangMsg(this, "&7Enter &cseconds&7 amount...");
    public ILangMsg Virtual_Shop_Editor_Enter_Day              = new ILangMsg(this, "&7Enter day name &cin English&7...");
    public ILangMsg Virtual_Shop_Editor_Enter_Time_Full        = new ILangMsg(this, "&7Enter 2 times like &c18:00 19:00&7...");
    public ILangMsg Virtual_Shop_Editor_Create_Error_Exist     = new ILangMsg(this, "&cShop with such ID already exist!");
    public ILangMsg Virtual_Shop_Editor_Product_Error_Currency = new ILangMsg(this, "&cInvalid currency!");

    public ILangMsg Chest_Shop_Command_List_Desc = new ILangMsg(this, "List of your shops.");

    public ILangMsg Chest_Shop_Command_Create_Desc  = new ILangMsg(this, "Creates shop of the chest that you're looking on.");
    public ILangMsg Chest_Shop_Command_Create_Usage = new ILangMsg(this, "[admin(true/false)]");

    public ILangMsg Chest_Shop_Command_Search_Desc  = new ILangMsg(this, "Search for shops with specified item.");
    public ILangMsg Chest_Shop_Command_Search_Usage = new ILangMsg(this, "<material>");

    public ILangMsg Chest_Shop_Command_Remove_Desc = new ILangMsg(this, "Removes the shop from the chest that you're looking at.");

    public ILangMsg Chest_Shop_Error_NotOwner                = new ILangMsg(this, "You're not the owner of this shop!");
    public ILangMsg Chest_Shop_Creation_Info_Done            = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lShop Created!\n&7Use &aShift-Click &7to enter in &aEdit Mode&7.");
    public ILangMsg Chest_Shop_Creation_Error_AlreadyShop    = new ILangMsg(this, "This chest is already shop!");
    public ILangMsg Chest_Shop_Creation_Error_NotEmpty       = new ILangMsg(this, "Please remove all items from the chest first.");
    public ILangMsg Chest_Shop_Creation_Error_NotAChest      = new ILangMsg(this, "This block is not a chest!");
    public ILangMsg Chest_Shop_Creation_Error_BadLocation    = new ILangMsg(this, "You can't create shop here!");
    public ILangMsg Chest_Shop_Creation_Error_LimitReached   = new ILangMsg(this, "You have reached the limit of shops! You can't create more.");
    public ILangMsg Chest_Shop_Creation_Error_BadArea        = new ILangMsg(this, "You can create shops only inside your own claim!");
    public ILangMsg Chest_Shop_Creation_Error_NotEnoughFunds = new ILangMsg(this, "You don't have enough funds!");
    public ILangMsg Chest_Shop_Removal_Info_Done             = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&c&lShop Removed.");
    public ILangMsg Chest_Shop_Removal_Error_NotAShop        = new ILangMsg(this, "This block is not a shop!");
    public ILangMsg Chest_Shop_Product_Error_BadItem         = new ILangMsg(this, "This item can not be traded!");

    public ILangMsg Chest_Shop_ShopList_Info_Switch = new ILangMsg(this, "Global shop list: %state%&7.");

    public ILangMsg Chest_Shop_Trade_Buy_Info_User   = new ILangMsg(this, "{message: ~prefix: false;}&eYou bought &6x%amount% %item% &efor &6%price% &efrom &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public ILangMsg Chest_Shop_Trade_Buy_Info_Owner  = new ILangMsg(this, "{message: ~prefix: false;}&6%player% &ejust bought &6x%amount% %item% &efor &6%price% &efrom your &6%shop_name%&e shop.");
    public ILangMsg Chest_Shop_Trade_Sell_Info_User  = new ILangMsg(this, "{message: ~prefix: false;}&eYou sold &6x%amount% %item% &efor &6%price% &eto &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public ILangMsg Chest_Shop_Trade_Sell_Info_Owner = new ILangMsg(this, "{message: ~prefix: false;}&6%player% &ejust sold &6x%amount% %item% &efor &6%price% &eto your &6%shop_name%&e shop.");

    public ILangMsg Chest_Shop_Editor_Tip_Name                  = new ILangMsg(this, "&7Enter new name...");
    public ILangMsg Chest_Shop_Editor_Error_Currency_NotAllowed = new ILangMsg(this, "&eThis currency can not be used!");
    public ILangMsg Chest_Shop_Editor_Error_Negative            = new ILangMsg(this, "&7Number must be positive!");
    public ILangMsg Chest_Shop_Editor_Error_ProductLeft         = new ILangMsg(this, "&cFirst you have to take all of this product from the chest!");

    public ILangMsg Other_Never    = new ILangMsg(this, "Never");
    public ILangMsg Other_Infinity = new ILangMsg(this, "Infinity");
}
