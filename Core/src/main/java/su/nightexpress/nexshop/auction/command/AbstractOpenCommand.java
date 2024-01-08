package su.nightexpress.nexshop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.menu.AbstractAuctionMenu;
import su.nightexpress.nexshop.module.ModuleCommand;

import java.util.List;
import java.util.UUID;

abstract class AbstractOpenCommand extends ModuleCommand<AuctionManager> {

    public AbstractOpenCommand(@NotNull AuctionManager module, @NotNull String[] aliases, @Nullable Permission permission) {
        super(module, aliases, permission);
        this.setPlayerOnly(true);
    }

    @NotNull
    protected abstract AbstractAuctionMenu<?> getMenu();

    @Nullable
    protected abstract Permission getPermissionsOthers();

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        if (!this.module.canBeUsedHere(player)) return;

        UUID uuid = player.getUniqueId();
        if (result.length() >= 2) {
            if (this.getPermissionsOthers() != null && !sender.hasPermission(this.getPermissionsOthers())) {
                this.errorPermission(sender);
                return;
            }

            ShopUser user = plugin.getUserManager().getUserData(result.getArg(1));
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }
            uuid = user.getId();
        }

        this.getMenu().open(player, 1, uuid);
    }
}
