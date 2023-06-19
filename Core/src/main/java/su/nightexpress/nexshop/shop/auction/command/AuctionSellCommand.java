package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

public class AuctionSellCommand extends ModuleCommand<AuctionManager> {

    public AuctionSellCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"sell"}, Perms.AUCTION_COMMAND_SELL);
        this.setDescription(plugin.getMessage(AuctionLang.COMMAND_SELL_DESC));
        this.setUsage(plugin.getMessage(AuctionLang.COMMAND_SELL_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.errorItem(sender);
            return;
        }

        double price = result.getDouble(1, 0);
        if (price <= 0) {
            this.errorNumber(sender, result.getArg(1));
            return;
        }

        if (this.module.canAdd(player, item, price)) {
            if (this.module.getCurrencies(player).size() <= 1) {
                Currency currency = this.module.getCurrencies(player).stream().findFirst().orElse(null);
                if (currency == null) return;

                if (!this.module.add(player, item, currency, price)) {
                    return;
                }
            }
            else {
                this.module.getCurrencySelectorMenu().open(player, item, price);
            }
            player.getInventory().setItemInMainHand(null);
        }
    }
}
