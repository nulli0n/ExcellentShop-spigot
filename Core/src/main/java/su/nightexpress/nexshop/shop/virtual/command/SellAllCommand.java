package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.menu.ShopSellMenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SellAllCommand extends GeneralCommand<ExcellentShop> {

    private final VirtualShopModule module;

    public SellAllCommand(@NotNull VirtualShopModule module, @NotNull String[] aliases) {
        super(module.plugin(), aliases, VirtualPerms.COMMAND_SELL_ALL);
        this.setDescription(plugin.getMessage(VirtualLang.COMMAND_SELL_ALL_DESC));
        this.setUsage(plugin.getMessage(VirtualLang.COMMAND_SELL_ALL_USAGE));
        this.setPlayerOnly(true);
        this.module = module;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Pair<List<ItemStack>, Set<StaticProduct>> userItems = Pair.of(new ArrayList<>(), new HashSet<>());

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;

            StaticProduct product = this.module.getBestProductFor(player, item, TradeType.SELL);
            if (product == null) continue;

            userItems.getFirst().add(new ItemStack(item));
            userItems.getSecond().add(product);
            item.setAmount(0);
        }

        ShopSellMenu.sellAll(player, userItems);
    }
}
