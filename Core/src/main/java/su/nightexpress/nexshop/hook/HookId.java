package su.nightexpress.nexshop.hook;

import java.util.Arrays;
import java.util.List;

public class HookId {

    public static final String PROTOCOL_LIB = "ProtocolLib";
    public static final String PACKET_EVENTS = "packetevents";

    public static final String PLAYER_POINTS = "PlayerPoints";
    public static final String COINS_ENGINE  = "CoinsEngine";
    public static final String GEMS_ECONOMY  = "GemsEconomy";
    public static final String ULTRA_ECONOMY = "UltraEconomy";
    public static final String BEAST_TOKENS  = "BeastTokens";
    public static final String VOTING_PLUGIN = "VotingPlugin";

    public static final String ELITEMOBS = "EliteMobs";
    public static final String CITIZENS  = "Citizens";

    public static final String LANDS                  = "Lands";
    public static final String WORLD_GUARD            = "WorldGuard";
    public static final String GRIEF_PREVENTION       = "GriefPrevention";
    public static final String GRIEF_DEFENDER         = "GriefDefender";
    public static final String KINGDOMS               = "Kingdoms";
    public static final String ADVANCED_REGION_MARKET = "AdvancedRegionMarket";

    public static final String ORAXEN           = "Oraxen";
    public static final String ITEMS_ADDER      = "ItemsAdder";
    public static final String MMOITEMS         = "MMOItems";
    public static final String EXCELLENT_CRATES = "ExcellentCrates";

    public static final String UPGRADEABLE_HOPPERS = "UpgradeableHoppers";

    public static List<String> getItemPluginNames() {
        return Arrays.asList(ORAXEN, ITEMS_ADDER, MMOITEMS);
    }
}
