package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.type.ChestType;

import java.util.List;
import java.util.stream.Stream;

public class CreateCmd extends ShopModuleCommand<ChestShop> {

    public CreateCmd(@NotNull ChestShop module) {
        super(module, new String[]{"create"}, Perms.CHEST_CREATE);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.Command_Create_Desc).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.Command_Create_Usage).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Stream.of(ChestType.values()).filter(type -> type.hasPermission(player)).map(Enum::name).toList();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 100);

        ChestType type = args.length >= 2 ? CollectionsUtil.getEnum(args[1], ChestType.class) : ChestType.PLAYER;
        if (type == null) {
            this.errorType(sender, ChestType.class);
            return;
        }

        this.module.createShop(player, block, type);
    }
}
