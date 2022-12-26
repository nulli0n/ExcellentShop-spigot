package su.nightexpress.nexshop.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.currency.config.CurrencyConfig;
import su.nightexpress.nexshop.currency.config.CurrencyItemConfig;
import su.nightexpress.nexshop.currency.external.GamePointsCurrency;
import su.nightexpress.nexshop.currency.external.GemsEconomyCurrency;
import su.nightexpress.nexshop.currency.external.PlayerPointsCurrency;
import su.nightexpress.nexshop.currency.external.VaultEcoCurrency;
import su.nightexpress.nexshop.currency.internal.ExpCurrency;
import su.nightexpress.nexshop.currency.internal.ItemCurrency;
import su.nightexpress.nexshop.hooks.HookId;

import java.util.*;

public class CurrencyManager extends AbstractManager<ExcellentShop> {

    public static final String DIR_DEFAULT = "/currency/default/";
    public static final String DIR_CUSTOM  = "/currency/custom_item/";

    private Map<String, ICurrency> currencyMap;

    public CurrencyManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.currencyMap = new HashMap<>();
        this.plugin.getConfigManager().extractResources(DIR_DEFAULT);
        this.plugin.getConfigManager().extractResources(DIR_CUSTOM);

        this.loadDefault();
        this.loadCustom();
    }

    private void loadDefault() {
        CurrencyId.stream().forEach(currencyId -> {
            switch (currencyId) {
                case CurrencyId.EXP -> {
                    CurrencyConfig config = this.loadConfigDefault(CurrencyId.EXP);
                    config.save();
                    this.registerCurrency(new ExpCurrency(config));
                }
                case CurrencyId.VAULT -> {
                    CurrencyConfig config = this.loadConfigDefault(CurrencyId.VAULT);
                    config.save();
                    if (Hooks.hasVault() && VaultHook.hasEconomy()) {
                        this.registerCurrency(new VaultEcoCurrency(config));
                    }
                }
                case CurrencyId.GAME_POINTS -> {
                    CurrencyConfig config = this.loadConfigDefault(CurrencyId.GAME_POINTS);
                    config.save();
                    if (Hooks.hasPlugin(HookId.GAME_POINTS)) {
                        this.registerCurrency(new GamePointsCurrency(config));
                    }
                }
                case CurrencyId.PLAYER_POINTS -> {
                    CurrencyConfig config = this.loadConfigDefault(CurrencyId.PLAYER_POINTS);
                    config.save();
                    if (Hooks.hasPlugin(HookId.PLAYER_POINTS)) {
                        this.registerCurrency(new PlayerPointsCurrency(config));
                    }
                }
                case CurrencyId.GEMS_ECONOMY -> {
                    if (Hooks.hasPlugin(HookId.GEMS_ECONOMY)) {
                        GemsEconomyCurrency.registerCurrencies();
                    }
                }
            }
        });
    }

    @NotNull
    private CurrencyConfig loadConfigDefault(@NotNull String id) {
        JYML cfg = JYML.loadOrExtract(plugin, DIR_DEFAULT + id + ".yml");
        return new CurrencyConfig(this.plugin, cfg);
    }

    private void loadCustom() {
        for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + DIR_CUSTOM, true)) {
            CurrencyItemConfig config = new CurrencyItemConfig(plugin, cfg);
            this.registerCurrency(new ItemCurrency(config));
        }
    }

    @Override
    protected void onShutdown() {
        if (this.currencyMap != null) {
            this.currencyMap.clear();
            this.currencyMap = null;
        }
    }

    public boolean registerCurrency(@NotNull ICurrency currency) {
        if (currency.getConfig().isEnabled()) {
            this.currencyMap.put(currency.getId(), currency);
            this.plugin.info("Registered currency: " + currency.getId());
            return true;
        }
        return false;
    }

    public boolean hasCurrency() {
        return !this.currencyMap.isEmpty();
    }

    @NotNull
    public Collection<ICurrency> getCurrencies() {
        return currencyMap.values();
    }

    @NotNull
    public Set<String> getCurrencyIds() {
        return this.currencyMap.keySet();
    }

    @Nullable
    public ICurrency getCurrency(@NotNull String id) {
        return this.currencyMap.get(id.toLowerCase());
    }

    @NotNull
    @Deprecated
    public ICurrency getCurrencyFirst() {
        Optional<ICurrency> opt = this.getCurrencies().stream().filter(Objects::nonNull).findFirst();
        if (opt.isEmpty()) throw new IllegalArgumentException("No currencies are installed!");
        return opt.get();
    }

}
