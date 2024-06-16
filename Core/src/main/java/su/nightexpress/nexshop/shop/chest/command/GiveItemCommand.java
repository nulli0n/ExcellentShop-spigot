package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.Placeholders;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;

public class GiveItemCommand {

    public static void build(@NotNull ChestShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("giveitem", builder -> builder
            .description(ChestLang.COMMAND_GIVE_ITEM_DESC)
            .permission(ChestPerms.COMMAND_GIVE_ITEM)
            .withArgument(ArgumentTypes.player("player").required())
            .withArgument(ArgumentTypes.material("type").required()
                .withSamples(context -> ChestConfig.SHOP_ITEM_CREATION_ITEMS.get().keySet().stream().map(BukkitThing::toString).toList())
            )
            .withArgument(ArgumentTypes.integer("amount").withSamples(context -> Lists.newList("1", "5", "10")))
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = arguments.getPlayerArgument("player");
        Material material = arguments.getMaterialArgument("type");

        ItemStack itemStack = ChestUtils.createShopItem(material);
        if (itemStack == null) {
            context.send(ChestLang.COMMAND_GIVE_ITEM_BAD_MATERIAL.getMessage());
            return false;
        }

        int amount = arguments.getIntArgument("amount", 1);
        itemStack.setAmount(amount);

        Players.addItem(player, itemStack);

        context.send(ChestLang.COMMAND_GIVE_ITEM_DONE.getMessage()
            .replace(Placeholders.GENERIC_NAME, ItemUtil.getItemName(itemStack))
            .replace(Placeholders.forPlayer(player))
        );
        return true;
    }
}
