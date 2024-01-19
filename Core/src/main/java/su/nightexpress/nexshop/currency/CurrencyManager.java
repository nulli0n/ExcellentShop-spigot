package su.nightexpress.nexshop.currency;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.xanium.gemseconomy.GemsEconomy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.currency.handler.*;
import su.nightexpress.nexshop.currency.impl.*;
import su.nightexpress.nexshop.hook.HookId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CurrencyManager extends AbstractManager<ExcellentShop> {

    public static final String DIR_DEFAULT = "/currency/default/";
    public static final String DIR_CUSTOM  = "/currency/custom_item/";

    public static final String EXP   = "exp";
    public static final String VAULT = "vault";

    public static final DummyCurrency DUMMY = new DummyCurrency();

    private final Map<String, Currency> currencyMap;

    public CurrencyManager(@NotNull ExcellentShop plugin) {
        super(plugin);
        this.currencyMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_DEFAULT);
        this.plugin.getConfigManager().extractResources(DIR_CUSTOM);

        this.registerCurrency(EXP, ExpPointsHandler::new);

        if (EngineUtils.hasVault() && VaultHook.hasEconomy()) {
            this.registerCurrency(VAULT, VaultEconomyHandler::new);
        }

        this.registerPluginCurrency(HookId.PLAYER_POINTS, PlayerPointsHandler::new);
        this.registerPluginCurrency(HookId.BEAST_TOKENS, BeastTokensHandler::new);
        this.registerPluginCurrency(HookId.VOTING_PLUGIN, VotingPluginHandler::new);
        this.registerPluginCurrency(HookId.ELITEMOBS, EliteMobsHandler::new);

        if (EngineUtils.hasPlugin(HookId.COINS_ENGINE)) {
            CoinsEngineCurrency.getCurrencies().forEach(this::registerCurrency);
        }
        if (EngineUtils.hasPlugin(HookId.GEMS_ECONOMY)) {
            for (me.xanium.gemseconomy.currency.Currency currency : GemsEconomy.getInstance().getCurrencyManager().getCurrencies()) {
                this.registerCurrency("gemseconomy_" + currency.getSingular(), () -> new GemsEconomyHandler(currency));
            }
        }

        if (EngineUtils.hasPlugin(HookId.ULTRA_ECONOMY)) {
            UltraEconomy.getAPI().getCurrencies().forEach(currency -> {
                this.registerCurrency(new UltraEconomyCurrency(currency));
            });
        }

        for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + DIR_CUSTOM, true)) {
            ItemCurrency currency = new su.nightexpress.nexshop.currency.impl.ItemCurrency(plugin, cfg);
            if (currency.load()) {
                this.registerCurrency(currency);
            }
        }
    }

    @Override
    protected void onShutdown() {
        this.currencyMap.clear();
    }

    public boolean registerPluginCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
        if (!EngineUtils.hasPlugin(id)) return false;

        return this.registerCurrency(id, supplier);
    }

    public boolean registerCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
        JYML cfg = JYML.loadOrExtract(plugin, DIR_DEFAULT, id.toLowerCase() + ".yml");
        ConfigCurrency currency = new ConfigCurrency(plugin, cfg, supplier.get());
        if (!currency.load()) return false;

        return this.registerCurrency(currency);
    }

    public boolean registerCurrency(@NotNull Currency currency) {
        this.getCurrencyMap().put(currency.getId(), currency);
        this.plugin.info("Registered currency: " + currency.getId());
        return true;
    }

    public boolean hasCurrency() {
        return !this.getCurrencyMap().isEmpty();
    }

    @NotNull
    public Map<String, Currency> getCurrencyMap() {
        return currencyMap;
    }

    @NotNull
    public Collection<Currency> getCurrencies() {
        return this.getCurrencyMap().values();
    }

    @NotNull
    public Set<String> getCurrencyIds() {
        return this.getCurrencyMap().keySet();
    }

    @Nullable
    public Currency getCurrency(@NotNull String id) {
        return this.getCurrencyMap().get(id.toLowerCase());
    }

    @NotNull
    public Currency getAny() {
        return this.getCurrencies().stream().findFirst().orElseThrow();
    }
}
