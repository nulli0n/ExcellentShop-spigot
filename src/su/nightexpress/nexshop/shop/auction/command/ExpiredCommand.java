package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

import java.util.List;
import java.util.UUID;

public class ExpiredCommand extends ShopModuleCommand<AuctionManager> {

    public ExpiredCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"expired"}, Perms.AUCTION_CMD_OPEN);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.lang().Auction_Command_Expired_Usage.getMsg();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Auction_Command_Expired_Desc.getMsg();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return PlayerUT.getPlayerNames();
        }
        return super.getTab(player, i, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        UUID id;
        if (args.length >= 2) {
            if (!sender.hasPermission(Perms.AUCTION_CMD_OPEN_OTHERS)) {
                this.errorPermission(sender);
                return;
            }

            ShopUser user = plugin.getUserManager().getOrLoadUser(args[1], false);
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }
            id = user.getUUID();
        }
        else id = player.getUniqueId();

        this.module.getAuctionExpiredMenu().open(player, 1, id);
    }
}
