package su.nightexpress.excellentshop.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class ModuleDefinition implements Writeable {

    public static final String URL_CURRENCIES   = "https://nightexpressdev.com/nightcore/integrations/currencies/";
    public static final String URL_CUSTOM_ITEMS = "https://nightexpressdev.com/nightcore/integrations/items/";

    public static ConfigType<ModuleDefinition> configType() {
        return ConfigType.of(ModuleDefinition::read, FileConfig::set);
    }

    private final boolean     enabled;
    private final String[]    commandAliases;
    private final String      prefix;
    private final String      defaultCurrency;
    private final Set<String> enabledCurrencies;
    private final Set<String> disabledItemProviders;

    public ModuleDefinition(boolean enabled,
                            String[] commandAliases,
                            @NonNull String prefix,
                            @NonNull String defaultCurrency,
                            @NonNull Set<String> enabledCurrencies,
                            @NonNull Set<String> disabledItemProviders) {
        this.enabled = enabled;
        this.commandAliases = commandAliases;
        this.prefix = prefix;
        this.defaultCurrency = defaultCurrency.toLowerCase();
        this.enabledCurrencies = Lists.modify(enabledCurrencies, LowerCase.INTERNAL::apply);
        this.disabledItemProviders = Lists.modify(disabledItemProviders, LowerCase.INTERNAL::apply);
    }

    @NonNull
    public static ModuleDefinition createDefault(@NonNull String prefix, @NonNull String... commandAliases) {
        return new ModuleDefinition(true, commandAliases, prefix, CurrencyId.VAULT, Lists.newSet(
            Placeholders.WILDCARD), new HashSet<>());
    }

    @NonNull
    public static ModuleDefinition createNoItemHandlers(@NonNull String prefix, @NonNull String... commandAliases) {
        return new ModuleDefinition(true, commandAliases, prefix, CurrencyId.VAULT, Lists.newSet(
            Placeholders.WILDCARD), Lists.newSet(Placeholders.WILDCARD));
    }

    @NonNull
    public static ModuleDefinition read(@NonNull FileConfig config, @NonNull String path) {
        boolean enabled = ConfigValue.create(path + ".Enabled", true,
            "Controls whether module is enabled."
        ).read(config);

        String[] commandAliases = ConfigValue.create(path + ".Command_Aliases", new String[0],
            "Creates dedicated module commands with provided aliases.",
            "[*] Server reboot is required to properly apply the changes."
        ).read(config);

        String prefix = ConfigValue.create(path + ".Prefix",
            TagWrappers.YELLOW.wrap(TagWrappers.BOLD.wrap(StringUtil.capitalizeUnderscored("Shop"))) + " " +
                TagWrappers.DARK_GRAY.wrap("»") + " ",
            "Sets module prefix for messages."
        ).read(config);

        String defCurrency = ConfigValue.create(path + ".Currency.Default", CurrencyId.VAULT,
            "Sets default currency for the module.",
            "List of available currencies: " + URL_CURRENCIES
        ).read(config);

        Set<String> enabledCurrencies = ConfigValue.create(path + ".Currency.Enabled", Lists.newSet(
            Placeholders.WILDCARD),
            "Sets currencies enabled (allowed) for the module.",
            "Use '" + Placeholders.WILDCARD + "' to enable all possible currencies.",
            "List of available currencies: " + URL_CURRENCIES,
            "[*] Default currency is always enabled (allowed)."
        ).read(config);

        //boolean needDisable = id.equalsIgnoreCase(ModuleId.AUCTION) || id.equalsIgnoreCase(ModuleId.CHEST_SHOP);

        Set<String> disabledItemHandler = ConfigValue.create(path + ".ItemProviders.Disabled",
            /*needDisable ? Lists.newSet(Placeholders.WILDCARD) : */Collections.emptySet(),
            "Disables specific custom item providers for the module.",
            "Use '" + Placeholders.WILDCARD + "' to disable all providers.",
            "List of available currencies: " + URL_CUSTOM_ITEMS,
            "[*] Custom items of disabled providers will be stored as plain NBT data."
        ).read(config);

        return new ModuleDefinition(enabled, commandAliases, prefix, defCurrency, enabledCurrencies, disabledItemHandler);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Enabled", this.enabled);
        config.setStringArray(path + ".Command_Aliases", this.commandAliases);
        config.set(path + ".Prefix", this.prefix);
        config.set(path + ".Currency.Default", this.defaultCurrency);
        config.set(path + ".Currency.Enabled", this.enabledCurrencies);
        config.set(path + ".ItemProviders.Disabled", this.disabledItemProviders);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String[] getCommandAliases() {
        return this.commandAliases;
    }

    @NonNull
    public String getPrefix() {
        return this.prefix;
    }

    @NonNull
    public String getDefaultCurrency() {
        return this.defaultCurrency;
    }

    @NonNull
    public Set<String> getEnabledCurrencies() {
        return this.enabledCurrencies;
    }

    @NonNull
    public Set<String> getDisabledItemProviders() {
        return this.disabledItemProviders;
    }
}
