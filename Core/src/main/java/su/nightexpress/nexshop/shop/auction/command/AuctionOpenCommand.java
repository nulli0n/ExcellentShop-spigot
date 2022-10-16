package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.auction.menu.AuctionMainMenu;

import java.util.Collections;
import java.util.List;

public class AuctionOpenCommand extends AbstractOpenCommand {

    public AuctionOpenCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"open"}, Perms.AUCTION_COMMAND_OPEN);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(AuctionLang.COMMAND_OPEN_DESC).getLocalized();
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    protected AuctionMainMenu getMenu() {
        return this.module.getMainMenu();
    }

    @Override
    @Nullable
    protected Permission getPermissionsOthers() {
        return Perms.AUCTION_COMMAND_OPEN;
    }
}
