package su.nightexpress.nexshop.currency.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrencyConfig;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyConfig extends AbstractLoadableItem<ExcellentShop> implements ICurrencyConfig {

    protected final boolean isEnabled;
    protected final String name;
    protected final String format;
    protected final DecimalFormat numberFormat;
    protected final ItemStack icon;

    public CurrencyConfig(@NotNull ExcellentShop plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.isEnabled = cfg.getBoolean("Enabled", true);
        this.name = Colorizer.apply(cfg.getString("Name", StringUtil.capitalizeFully(this.getId().replace("_", " "))));
        this.format = Colorizer.apply(cfg.getString("Format", Placeholders.GENERIC_PRICE + " " + Placeholders.CURRENCY_NAME));
        this.numberFormat = new DecimalFormat(cfg.getString("Number_Format", "#,###.##"), new DecimalFormatSymbols(Locale.ENGLISH));

        ItemStack icon = cfg.getItem("Icon");
        if (icon.getType().isAir()) {
            this.icon = new ItemStack(Material.SUNFLOWER);

            ItemMeta meta = this.icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + Placeholders.CURRENCY_NAME);
            }
            this.icon.setItemMeta(meta);
        }
        else this.icon = icon;
    }

    @Override
    public void onSave() {
        this.cfg.set("Enabled", this.isEnabled());
        this.cfg.set("Name", this.getName());
        this.cfg.set("Format", this.getFormat());
        this.cfg.set("Number_Format", this.getNumberFormat().toPattern());
        this.cfg.setItem("Icon", this.getIcon());
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String getFormat() {
        return format;
    }

    @NotNull
    @Override
    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    @NotNull
    @Override
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }
}
