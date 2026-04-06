package su.nightexpress.excellentshop.feature.virtualshop.core;

import org.bukkit.GameMode;
import su.nightexpress.excellentshop.feature.virtualshop.command.VirtualCommands;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.RankMap;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.excellentshop.ShopPlaceholders.*;

public class VirtualConfig {

    public static final ConfigValue<Integer> SAVE_INTERVAL = ConfigValue.create("General.Save_Interval",
        300,
        "Sets how often (in seconds) shop changes will be saved (written) to their configuration files.",
        "[Asynchronous]",
        "[Default is 300]"
    );

    public static final ConfigValue<String> DEFAULT_LAYOUT = ConfigValue.create("General.Default_Layout",
        DEFAULT,
        "Sets default shop layout configuration in case if shop's one is not existing anymore."
    );

    public static final ConfigValue<Boolean> MAIN_MENU_ENABLED = ConfigValue.create("General.Main_Menu.Enabled",
        true,
        "Enables the Main Menu feature, where you can list all your Virtual Shops."
    );

    public static final ConfigValue<Boolean> MAIN_MENU_HIDE_NO_PERM_SHOPS = ConfigValue.create("General.Main_Menu.Hide_No_Permission_Shops",
        true,
        "When enabled, hides shops from the main menu a player don't have access to."
    );

    public static final ConfigValue<Boolean> SHOP_SHORTCUTS_ENABLED = ConfigValue.create("General.Shop_Shortcut.Enabled",
        true,
        "Enables the Shop Shortcut and Shop Aliases commands features. Allows to quickly open shops."
    );

    public static final ConfigValue<String[]> SHOP_SHORTCUTS_COMMANDS = ConfigValue.create("General.Shop_Shortcut.Commands",
        new String[]{VirtualCommands.DEF_SHOP},
        "Command aliases for quick main menu and shop access.", "Split with commas."
    );

    public static final ConfigValue<Boolean> SELL_MENU_ENABLED = ConfigValue.create("General.Sell_Menu.Enabled",
        true,
        "When 'true' enables the Sell Menu commands, where you can quickly sell all your items."
    );

    public static final ConfigValue<String[]> SELL_MENU_COMMANDS = ConfigValue.create("General.Sell_Menu.Commands",
        new String[]{"sellgui", "sellmenu"},
        "Custom command aliases to open the Sell Menu. Split with commas."
    );

    public static final ConfigValue<Boolean> SELL_ALL_ENABLED = ConfigValue.create("General.Sell_All.Enabeled",
        true,
        "Enables the Sell All command feature."
    );

    public static final ConfigValue<String[]> SELL_ALL_COMMANDS = ConfigValue.create("General.Sell_All.Commands",
        new String[]{"sellall"},
        "Sell All command aliases. Split with commas."
    );

    public static final ConfigValue<Boolean> SELL_HAND_ENABLED = ConfigValue.create("General.Sell_Hand.Enabled",
        true,
        "Enables the Sell Hand feature."
    );

    public static final ConfigValue<String[]> SELL_HAND_COMMANDS = ConfigValue.create("General.Sell_Hand.Commands",
        new String[]{"sellhand"},
        "Sell Hand command aliases. Split with commas."
    );

    public static final ConfigValue<Boolean> SELL_HAND_ALL_ENABLED = ConfigValue.create("General.Sell_Hand_All.Enabled",
        true,
        "Enables the Sell Hand All feature."
    );

    public static final ConfigValue<String[]> SELL_HAND_ALL_COMMANDS = ConfigValue.create("General.Sell_Hand_All.Commands",
        new String[]{"sellhandall"},
        "Sell Hand All command aliases. Split with commas."
    );

    public static final ConfigValue<RankMap<Double>> SELL_RANK_MULTIPLIERS = ConfigValue.create("General.Sell_Multipliers",
        (cfg, path, def) -> RankMap.readDouble(cfg, path, 1D),
        (cfg, path, rankMap) -> rankMap.write(cfg, path),
        () -> new RankMap<>(RankMap.Mode.RANK, VirtualPerms.PREFIX_SELL_MULTIPLIER, 1D, Map.of(
            "vip", 1.5D,
            "gold", 2D
        )),
        "Here you can define Sell Multipliers for certain ranks.",
        "If you want to use permission based system, you can use '" + VirtualPerms.PREFIX_SELL_MULTIPLIER + "[name]' permission pattern.",
        "(make sure to use names different from your permission ranks then)",
        "Formula: '<sellPrice> * <sellMultiplier>'. So, 1.0 = 100% (no changes), 1.5 = +50%, 0.75 = -25%, etc."
    );

    public static final ConfigValue<Set<GameMode>> DISABLED_GAMEMODES = ConfigValue.forSet("General.Disabled_In_Gamemodes",
        id -> StringUtil.getEnum(id, GameMode.class).orElse(null),
        (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
        () -> Lists.newSet(GameMode.CREATIVE),
        "Players can not use shops in specified gamemodes.",
        "Available values: " + StringUtil.inlineEnum(GameMode.class, ", ")
    );

    public static final ConfigValue<Set<String>> DISABLED_WORLDS = ConfigValue.create("General.Disabled_In_Worlds",
        Lists.newSet("world_name", "example_world123"),
        "Players can not use shops in specified worlds. Case sensetive."
    );

    public static final ConfigValue<String> SHOP_FORMAT_NAME = ConfigValue.create("GUI.Shop_Format.Name",
        VIRTUAL_SHOP_ICON_NAME,
        "Sets display name for the shop item in the Main Menu.",
        "You can use 'Vritual Shop' placeholders:" + URL_WIKI_PLACEHOLDERS
    );

    public static final ConfigValue<List<String>> SHOP_FORMAT_LORE = ConfigValue.create("GUI.Shop_Format.Lore",
        Lists.newList(
            VIRTUAL_SHOP_DESCRIPTION
        ),
        "Sets lore for the shop item in the Main Menu.",
        "You can use 'Virtual Shop' placeholders: " + URL_WIKI_PLACEHOLDERS
    );

    public static boolean isCentralMenuEnabled() {
        return MAIN_MENU_ENABLED.get();
    }
}
