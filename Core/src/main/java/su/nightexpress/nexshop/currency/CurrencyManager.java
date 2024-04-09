package su.nightexpress.nexshop.currency;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.xanium.gemseconomy.GemsEconomy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.FileUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.currency.handler.*;
import su.nightexpress.nexshop.currency.impl.CoinsEngineCurrency;
import su.nightexpress.nexshop.currency.impl.ConfigCurrency;
import su.nightexpress.nexshop.currency.impl.DummyCurrency;
import su.nightexpress.nexshop.currency.impl.UltraEconomyCurrency;
import su.nightexpress.nexshop.hook.HookId;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CurrencyManager extends AbstractManager<ExcellentShop> {

    public static final String        FILE_NAME      = "currencies.yml";
    public static final DummyCurrency DUMMY_CURRENCY = new DummyCurrency();

    private final JYML                  config;
    private final Map<String, Currency> currencyMap;

    public CurrencyManager(@NotNull ExcellentShop plugin) {
        super(plugin);
        this.currencyMap = new HashMap<>();
        this.config = JYML.loadOrExtract(plugin, FILE_NAME);
    }

    @Override
    protected void onLoad() {
        this.migrateOldConfigs();
        this.loadCurrencies();
    }

    private void migrateOldConfigs() {
        File dir = new File(this.plugin.getDataFolder() + "/currency/");
        if (!dir.exists()) return;

        File defDir = new File(this.plugin.getDataFolder() + "/currency/default/");
        if (defDir.exists()) {
            for (JYML cfg : JYML.loadAll(defDir.getAbsolutePath(), true)) {
                String id = cfg.getFile().getName().replace(".yml", "");

                ItemStackHandler handler = new ItemStackHandler(new ItemStack(Material.AIR));
                ConfigCurrency currency = ConfigCurrency.read(cfg, "", id, handler);
                this.writeCurrency(currency);
            }
        }


        File itemDir = new File(this.plugin.getDataFolder() + "/currency/custom_item/");
        if (itemDir.exists()) {
            for (JYML cfg : JYML.loadAll(itemDir.getAbsolutePath(), true)) {
                String id = cfg.getFile().getName().replace(".yml", "");

                ItemStackHandler handler = ItemStackHandler.read(cfg, "");
                if (handler == null) continue;

                ConfigCurrency currency = ConfigCurrency.read(cfg, "", id, handler);
                this.writeCurrency(currency);
            }
        }

        FileUtil.deleteRecursive(dir);
    }

    public void loadCurrencies() {
        this.loadItemCurrencies();

        this.loadCurrency(PlayerXPHandler.ID, PlayerXPHandler::new);

        if (EngineUtils.hasVault() && VaultHook.hasEconomy()) {
            this.loadCurrency(VaultEconomyHandler.ID, VaultEconomyHandler::new);
        }

        if (EngineUtils.hasPlugin(HookId.PLAYER_POINTS)) {
            this.loadCurrency(PlayerPointsHandler.ID, PlayerPointsHandler::new);
        }
        if (EngineUtils.hasPlugin(HookId.BEAST_TOKENS)) {
            this.loadCurrency(BeastTokensHandler.ID, BeastTokensHandler::new);
        }
        if (EngineUtils.hasPlugin(HookId.VOTING_PLUGIN)) {
            this.loadCurrency(VotingPluginHandler.ID, VotingPluginHandler::new);
        }
        if (EngineUtils.hasPlugin(HookId.ELITEMOBS)) {
            this.loadCurrency(EliteMobsHandler.ID, EliteMobsHandler::new);
        }
        if (EngineUtils.hasPlugin(HookId.COINS_ENGINE)) {
            CoinsEngineCurrency.getCurrencies().forEach(this::registerCurrency);
        }

        if (EngineUtils.hasPlugin(HookId.GEMS_ECONOMY)) {
            for (me.xanium.gemseconomy.currency.Currency currency : GemsEconomy.getInstance().getCurrencyManager().getCurrencies()) {
                this.loadCurrency("gemseconomy_" + currency.getSingular(), () -> new GemsEconomyHandler(currency));
            }
        }

        if (EngineUtils.hasPlugin(HookId.ULTRA_ECONOMY)) {
            UltraEconomy.getAPI().getCurrencies().forEach(currency -> {
                this.registerCurrency(new UltraEconomyCurrency(currency));
            });
        }

        this.config.saveChanges();
    }

    public void loadItemCurrencies() {
        if (!this.config.contains("Custom_Item")) {
            this.writeCurrency(this.newItemCurrency("gold", new ItemStack(Material.GOLD_INGOT)));
            this.writeCurrency(this.newItemCurrency("diamond", new ItemStack(Material.DIAMOND)));
            this.writeCurrency(this.newItemCurrency("emerald", new ItemStack(Material.EMERALD)));
        }

        for (String id : this.config.getSection("Custom_Item")) {
            String path = "Custom_Item." + id;

            ItemStackHandler handler = ItemStackHandler.read(this.config, path);
            if (handler == null) {
                this.plugin.error("Invalid 'Item' setting for '" + id + "' custom item currency.");
                continue;
            }

            this.loadCurrency(id, path, () -> handler);
        }
    }

    public void writeCurrency(@NotNull ConfigCurrency currency) {
        String path = "External." + currency.getId();

        if (currency.getHandler() instanceof ItemStackHandler) {
            path = "Custom_Item." + currency.getId();
        }

        currency.write(this.config, path);
    }

    @NotNull
    public ConfigCurrency newItemCurrency(@NotNull String id, @NotNull ItemStack itemStack) {
        ItemStackHandler handler = new ItemStackHandler(itemStack);

        return ConfigCurrency.withDefaults(id, handler);
    }

    public void createItemCurrency(@NotNull String id, @NotNull ItemStack itemStack) {
        ConfigCurrency currency = newItemCurrency(id, itemStack);

        this.writeCurrency(currency);
        this.registerCurrency(currency);
        this.config.saveChanges();
    }

    public void loadCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
        this.loadCurrency(id, "Currencies." + id, supplier);
    }

    private void loadCurrency(@NotNull String id, @NotNull String path, @NotNull Supplier<CurrencyHandler> supplier) {
        ConfigCurrency currency = ConfigCurrency.read(this.config, path, id, supplier.get());
        this.registerCurrency(currency);
    }

    @Override
    protected void onShutdown() {
        this.currencyMap.clear();
    }

    public void registerCurrency(@NotNull Currency currency) {
        this.getCurrencyMap().put(currency.getId(), currency);
        this.plugin.info("Registered currency: " + currency.getId());
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
