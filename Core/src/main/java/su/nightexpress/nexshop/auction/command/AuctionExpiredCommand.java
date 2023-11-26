package su.nightexpress.nexshop.auction.command;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.menu.AuctionExpiredMenu;

public class AuctionExpiredCommand extends AbstractOpenCommand {

    public AuctionExpiredCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"expired"}, Perms.AUCTION_COMMAND_EXPIRED);
        this.setDescription(plugin.getMessage(AuctionLang.COMMAND_EXPIRED_DESC));
        this.setUsage(plugin.getMessage(AuctionLang.COMMAND_EXPIRED_USAGE));
    }

    @Override
    @NotNull
    protected AuctionExpiredMenu getMenu() {
        return this.module.getExpiredMenu();
    }

    @Override
    @Nullable
    protected Permission getPermissionsOthers() {
        return Perms.AUCTION_COMMAND_EXPIRED_OTHERS;
    }
}
