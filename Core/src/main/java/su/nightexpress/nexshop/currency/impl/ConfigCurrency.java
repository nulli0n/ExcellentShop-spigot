package su.nightexpress.nexshop.currency.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class ConfigCurrency extends AbstractConfigHolder<ExcellentShop> implements Currency {

    private String name;
    private String format;
    private ItemStack icon;

    private final CurrencyHandler handler;
    private final PlaceholderMap placeholderMap;

    public ConfigCurrency(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull CurrencyHandler handler) {
        super(plugin, cfg);
        this.handler = handler;

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName)
        ;
    }

    @Override
    public boolean load() {
        if (!this.cfg.getBoolean("Enabled")) return false;

        this.name = Colorizer.apply(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.format = Colorizer.apply(cfg.getString("Format", Placeholders.GENERIC_PRICE + " " + Placeholders.CURRENCY_NAME));
        this.icon = cfg.getItem("Icon");
        if (this.icon.getType().isAir()) {
            this.icon = new ItemStack(Material.GOLD_INGOT);
        }
        return true;
    }

    @Override
    public void onSave() {
        this.cfg.set("Name", this.getName());
        this.cfg.set("Format", this.getFormat());
        this.cfg.setItem("Icon", this.getIcon());
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    @Override
    public CurrencyHandler getHandler() {
        return handler;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getFormat() {
        return format;
    }

    @NotNull
    @Override
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }
}
