package su.nightexpress.nexshop.currency.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

@Deprecated
public class OraxenCurrency extends ConfigCurrency {

    public OraxenCurrency(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull CurrencyHandler handler) {
        super(plugin, cfg, handler);
    }
}
