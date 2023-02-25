package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SearchCommand extends ShopModuleCommand<ChestShopModule> {

    private static final List<String> MATERIALS = Stream.of(Material.values())
        .filter(Material::isItem).map(Enum::name).map(String::toLowerCase).toList();

    public SearchCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"search"}, ChestPerms.COMMAND_SEARCH);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(ChestLang.COMMAND_SEARCH_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(ChestLang.COMMAND_SEARCH_USAGE).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
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
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length != 2) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Material material = Material.getMaterial(args[1].toUpperCase());
        if (material == null) {
            return;
        }

        this.module.getSearchMenu().open(player, material);
    }
}
