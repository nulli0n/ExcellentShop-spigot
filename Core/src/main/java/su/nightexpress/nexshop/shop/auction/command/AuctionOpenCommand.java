package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

public class AuctionOpenCommand extends ModuleCommand<AuctionManager> {

    public AuctionOpenCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"open"}, Perms.AUCTION_COMMAND_OPEN);
        this.setDescription(plugin.getMessage(AuctionLang.COMMAND_OPEN_DESC));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2 && !(sender instanceof Player)) {
            this.errorSender(sender);
            return;
        }
        if (result.length() >= 2 && !sender.hasPermission(Perms.AUCTION_COMMAND_OPEN_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        Player player = PlayerUtil.getPlayer(result.getArg(1, sender.getName()));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        if (sender == player && !this.module.canBeUsedHere(player)) return;

        this.module.getMainMenu().open(player, 1);
    }
}
