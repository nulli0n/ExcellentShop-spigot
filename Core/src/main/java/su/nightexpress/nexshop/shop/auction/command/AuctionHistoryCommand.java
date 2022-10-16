package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.auction.menu.AuctionHistoryMenu;

public class AuctionHistoryCommand extends AbstractOpenCommand {

    public AuctionHistoryCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"history"}, Perms.AUCTION_COMMAND_HISTORY);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(AuctionLang.COMMAND_HISTORY_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(AuctionLang.COMMAND_HISTORY_DESC).getLocalized();
    }

    @Override
    @NotNull
    protected AuctionHistoryMenu getMenu() {
        return this.module.getHistoryMenu();
    }

    @Override
    @Nullable
    protected Permission getPermissionsOthers() {
        return Perms.AUCTION_COMMAND_HISTORY_OTHERS;
    }
}
