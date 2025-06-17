package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ModuleConfig implements Writeable {

    private final boolean     enabled;
    private final String[]    commandAliases;
    private final String      prefix;
    private final String      defaultCurrency;
    private final Set<String> enabledCurrencies;

    public ModuleConfig(boolean enabled, String[] commandAliases, @NotNull String prefix, @NotNull String defaultCurrency, @NotNull Set<String> enabledCurrencies) {
        this.enabled = enabled;
        this.commandAliases = commandAliases;
        this.prefix = prefix;
        this.defaultCurrency = defaultCurrency.toLowerCase();
        this.enabledCurrencies = Lists.modify(enabledCurrencies, String::toLowerCase);
    }

    @NotNull
    public static Map<String, ModuleConfig> getDefaultConfigs() {
        Map<String, ModuleConfig> map = new LinkedHashMap<>();

        map.put(ModuleId.AUCTION, createDefault("Auction", "auction", "auc", "ah"));
        map.put(ModuleId.CHEST_SHOP, createDefault("ChestShop", "chestshop", "cshop", "playershop", "pshop"));
        map.put(ModuleId.VIRTUAL_SHOP, createDefault("Shop", "virtualshop", "vshop"));

        return map;
    }

    @NotNull
    public static ModuleConfig createDefault(@NotNull String prefix, @NotNull String... commandAliases) {
        return new ModuleConfig(true, commandAliases, prefix, CurrencyId.VAULT, Lists.newSet(Placeholders.WILDCARD));
    }

    @NotNull
    public static ModuleConfig read(@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        boolean enabled = ConfigValue.create(path + ".Enabled", true,
            "Controls whether module is enabled."
        ).read(config);

        String[] commandAliases = ConfigValue.create(path + ".Command_Aliases", new String[]{id},
            "Creates dedicated module commands with provided aliases.",
            "[*] Server reboot is required to properly apply the changes."
        ).read(config);

        String prefix = ConfigValue.create(path + ".Prefix",
            Tags.LIGHT_YELLOW.wrap(Tags.BOLD.wrap(StringUtil.capitalizeUnderscored(id))) + " " + Tags.DARK_GRAY.wrap("»") + " ",
            "Sets module prefix for messages."
        ).read(config);

        String defCurrency = ConfigValue.create(path + ".Currency.Default", CurrencyId.VAULT,
            "Sets default currency for the module.",
            "List of available currencies: " + Placeholders.URL_WIKI_CURRENCY
        ).read(config);

        Set<String> enabledCurrencies = ConfigValue.create(path + ".Currency.Enabled", Lists.newSet(Placeholders.WILDCARD),
            "Sets currencies enabled (allowed) for the module.",
            "Use '" + Placeholders.WILDCARD + "' to enable all possible currencies.",
            "List of available currencies: " + Placeholders.URL_WIKI_CURRENCY,
            "[*] Default currency is always enabled (allowed)."
        ).read(config);

        return new ModuleConfig(enabled, commandAliases, prefix, defCurrency, enabledCurrencies);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Enabled", this.enabled);
        config.setStringArray(path + ".Command_Aliases", this.commandAliases);
        config.set(path + ".Prefix", this.prefix);
        config.set(path + ".Currency.Default", this.defaultCurrency);
        config.set(path + ".Currency.Enabled", this.enabledCurrencies);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String[] getCommandAliases() {
        return this.commandAliases;
    }

    @NotNull
    public String getPrefix() {
        return this.prefix;
    }

    @NotNull
    public String getDefaultCurrency() {
        return this.defaultCurrency;
    }

    @NotNull
    public Set<String> getEnabledCurrencies() {
        return this.enabledCurrencies;
    }
}
