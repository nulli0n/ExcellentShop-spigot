package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

import java.util.List;
import java.util.stream.Stream;

public class SearchCommand extends ModuleCommand<ChestShopModule> {

    private static final List<String> MATERIALS = Stream.of(Material.values())
        .filter(Material::isItem).map(Enum::name).map(String::toLowerCase).toList();

    public SearchCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"search"}, ChestPerms.COMMAND_SEARCH);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_SEARCH_DESC));
        this.setUsage(plugin.getMessage(ChestLang.COMMAND_SEARCH_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return MATERIALS;
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Material material = Material.getMaterial(result.getArg(1).toUpperCase());
        if (material == null) {
            return;
        }

        this.module.getSearchMenu().open(player, material);
    }
}
