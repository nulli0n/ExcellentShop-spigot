package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;

import java.util.Map;

public class AuctionSellCommand extends ShopModuleCommand<AuctionManager> {

    public AuctionSellCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"sell"}, Perms.AUCTION_COMMAND_SELL);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(AuctionLang.COMMAND_SELL_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(AuctionLang.COMMAND_SELL_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length < 2) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.errorItem(sender);
            return;
        }

        double price = StringUtil.getDouble(args[1], -1, false);
        if (price <= 0) {
            this.errorNumber(sender, args[1]);
            return;
        }

        if (this.module.canAdd(player, item, price)) {
            this.module.getCurrencySelectorMenu().open(player, item, price);
            player.getInventory().setItemInMainHand(null);
        }
    }
}
