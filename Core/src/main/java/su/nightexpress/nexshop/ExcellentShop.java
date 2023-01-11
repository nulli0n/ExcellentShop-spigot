package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.command.currency.CurrencyMainCommand;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.ShopDataHandler;
import su.nightexpress.nexshop.data.ShopUserManager;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.editor.GenericEditorType;
import su.nightexpress.nexshop.hooks.HookId;
import su.nightexpress.nexshop.hooks.external.BrokerHook;
import su.nightexpress.nexshop.module.ModuleManager;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.menu.AuctionMainMenu;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

public class ExcellentShop extends NexPlugin<ExcellentShop> implements UserDataHolder<ExcellentShop, ShopUser> {

    private ShopDataHandler dataHandler;
    private ShopUserManager userManager;

    private CurrencyManager currencyManager;
    private ModuleManager   moduleManager;

    @Override
    @NotNull
    protected ExcellentShop getSelf() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.getServer().getPluginManager().getPlugin(Hooks.WORLD_GUARD) != null) {
            WorldGuardFlags.setupFlag();
        }
    }

    @Override
    public void enable() {
        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();

        this.moduleManager = new ModuleManager(this);
        this.moduleManager.setup();
        this.moduleManager.loadModules();
    }

    @Override
    public void disable() {
        for (TradeType tradeType : TradeType.values()) {
            Config.getCartMenu(tradeType).clear();
        }
        if (this.moduleManager != null) {
            this.moduleManager.shutdown();
            this.moduleManager = null;
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    @Override
    public boolean setupDataHandlers() {
        try {
            this.dataHandler = ShopDataHandler.getInstance(this);
            this.dataHandler.setup();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        this.userManager = new ShopUserManager(this);
        this.userManager.setup();

        return true;
    }

    @Override
    public void loadConfig() {
        Config.load(this);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().loadMissing(ChestLang.class);
        this.getLangManager().loadMissing(VirtualLang.class);
        this.getLangManager().setupEnum(AuctionMainMenu.AuctionSortType.class);
        this.getLangManager().setupEnum(TradeType.class);
        this.getLangManager().setupEnum(ChestShopType.class);
        this.getLangManager().setupEnum(PriceType.class);
        this.getLangManager().setupEditorEnum(VirtualEditorType.class);
        this.getLangManager().setupEditorEnum(ChestEditorType.class);
        this.getLangManager().setupEditorEnum(GenericEditorType.class);
        this.getLang().saveChanges();
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentShop> mainCommand) {
        mainCommand.addChildren(new CurrencyMainCommand(this));
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.COMMAND_RELOAD));
    }

    @Override
    public void registerPermissions() {
        this.registerPermissions(Perms.class);
    }

    @Override
    public void registerHooks() {
        if (Hooks.hasPlugin(HookId.BROKER)) {
            BrokerHook.setup();
        }
    }

    @Override
    @NotNull
    public ShopDataHandler getData() {
        return this.dataHandler;
    }

    @NotNull
    @Override
    public ShopUserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public ModuleManager getModuleCache() {
        return this.moduleManager;
    }

    @Nullable
    public VirtualShopModule getVirtualShop() {
        return this.moduleManager.getVirtualShop();
    }

    @Nullable
    public ChestShopModule getChestShop() {
        return this.moduleManager.getChestShop();
    }

    @Nullable
    public AuctionManager getAuctionManager() {
        return this.moduleManager.getAuctionManager();
    }
}
