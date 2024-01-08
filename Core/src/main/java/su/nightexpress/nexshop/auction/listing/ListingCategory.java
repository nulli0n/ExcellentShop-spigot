package su.nightexpress.nexshop.auction.listing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.auction.Placeholders;

import java.util.Set;
import java.util.stream.Collectors;

public class ListingCategory implements Placeholder {

    private final String      id;
    private final String      name;
    private final boolean isDefault;
    private final Set<String> materials;

    private final PlaceholderMap placeholderMap;

    public ListingCategory(@NotNull String id,
                           @NotNull String name,
                           boolean isDefault,
                           @NotNull Set<String> materials) {
        this.id = StringUtil.lowerCaseUnderscore(id);
        this.name = Colorizer.apply(name);
        this.isDefault = isDefault;
        this.materials = materials.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CATEGORY_ID, this::getId)
            .add(Placeholders.CATEGORY_NAME, this::getName);
    }

    @NotNull
    public static ListingCategory read(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        String name = cfg.getString(path + ".Name", StringUtil.capitalizeUnderscored(id));
        boolean isDefault = cfg.getBoolean(path + ".Default");
        Set<String> materials = cfg.getStringSet(path + ".Materials");

        return new ListingCategory(id, name, isDefault, materials);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    @NotNull
    public Set<String> getMaterials() {
        return this.materials;
    }

    public boolean isItemOfThis(@NotNull ItemStack item) {
        return this.isItemOfThis(item.getType());
    }

    public boolean isItemOfThis(@NotNull Material material) {
        return this.isItemOfThis(material.name());
    }

    public boolean isItemOfThis(@NotNull String name) {
        return this.materials.contains(name.toLowerCase()) || this.materials.contains(Placeholders.WILDCARD);
    }
}
