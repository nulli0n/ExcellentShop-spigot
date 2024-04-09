package su.nightexpress.nexshop.currency.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.currency.handler.ItemStackHandler;

public class ConfigCurrency implements Currency {

    private final String    id;
    private final boolean   enabled;
    private final String    name;
    private final String    format;
    private final ItemStack icon;

    private final CurrencyHandler handler;
    private final PlaceholderMap  placeholderMap;

    public ConfigCurrency(@NotNull String id, @NotNull CurrencyHandler handler,
                          boolean enabled,
                          @NotNull String name, @NotNull String format, @NotNull ItemStack icon) {
        this.id = id.toLowerCase();
        this.handler = handler;

        this.enabled = enabled;
        this.name = Colorizer.apply(name);
        this.format = Colorizer.apply(format);
        this.icon = icon.getType().isAir() ? new ItemStack(Material.GOLD_NUGGET) : new ItemStack(icon);

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName);
    }

    @NotNull
    public static ConfigCurrency withDefaults(@NotNull String id, @NotNull CurrencyHandler handler) {
        return new ConfigCurrency(id, handler, true, handler.getDefaultName(), handler.getDefaultFormat(), handler.getDefaultIcon());
    }

    @NotNull
    public static ConfigCurrency read(@NotNull JYML cfg, @NotNull String path, @NotNull String id, @NotNull CurrencyHandler handler) {
        if (!path.endsWith(".") && !path.isEmpty()) path += ".";

        boolean enabled = JOption.create(path + "Enabled", true).read(cfg);
        String name = JOption.create(path + "Name", handler.getDefaultName()).read(cfg);
        String format = JOption.create(path + "Format", handler.getDefaultFormat()).read(cfg);
        ItemStack icon = JOption.create(path + "Icon", handler.getDefaultIcon()).read(cfg);

        return new ConfigCurrency(id, handler, enabled, name, format, icon);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Name", this.getName());
        cfg.set(path + ".Format", this.getFormat());
        cfg.setItem(path + ".Icon", this.getIcon());

        if (this.handler instanceof ItemStackHandler itemHandler) {
            itemHandler.write(cfg, path);
        }
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
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
