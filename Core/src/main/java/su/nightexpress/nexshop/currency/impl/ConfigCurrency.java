package su.nightexpress.nexshop.currency.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.currency.handler.ItemStackHandler;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

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
        this.name = name;
        this.format = format;
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
    public static ConfigCurrency read(@NotNull FileConfig config, @NotNull String path, @NotNull String id, @NotNull CurrencyHandler handler) {
        if (!path.endsWith(".") && !path.isEmpty()) path += ".";

        boolean enabled = ConfigValue.create(path + "Enabled", true).read(config);
        String name = ConfigValue.create(path + "Name", handler.getDefaultName()).read(config);
        String format = ConfigValue.create(path + "Format", handler.getDefaultFormat()).read(config);
        ItemStack icon = ConfigValue.create(path + "Icon", handler.getDefaultIcon()).read(config);

        return new ConfigCurrency(id, handler, enabled, name, format, icon);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Name", this.getName());
        config.set(path + ".Format", this.getFormat());
        config.setItem(path + ".Icon", this.getIcon());

        if (this.handler instanceof ItemStackHandler itemHandler) {
            itemHandler.write(config, path);
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
