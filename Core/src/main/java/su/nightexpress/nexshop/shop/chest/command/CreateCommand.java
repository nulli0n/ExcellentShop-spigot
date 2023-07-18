package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

import java.util.List;
import java.util.stream.Stream;

public class CreateCommand extends ModuleCommand<ChestShopModule> {

    public CreateCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"create"}, ChestPerms.CREATE);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_CREATE_DESC));
        this.setUsage(plugin.getMessage(ChestLang.COMMAND_CREATE_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Stream.of(ShopType.values()).filter(type -> type.hasPermission(player)).map(Enum::name).toList();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 100);
        ShopType type = result.length() >= 2 ? StringUtil.getEnum(result.getArg(1), ShopType.class).orElse(ShopType.PLAYER) : ShopType.PLAYER;
        this.module.createShop(player, block, type);
    }
}
