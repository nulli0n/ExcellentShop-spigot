package su.nightexpress.nexshop.auction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.Set;
import java.util.stream.Collectors;

public class ListingCategory implements Placeholder {

    private final String      id;
    private final String      name;
    private final boolean     isDefault;
    private final Set<String> materials;

    private final PlaceholderMap placeholderMap;

    public ListingCategory(@NonNull String id,
                           @NonNull String name,
                           boolean isDefault,
                           @NonNull Set<String> materials) {
        this.id = StringUtil.lowerCaseUnderscore(id);
        this.name = name;
        this.isDefault = isDefault;
        this.materials = materials.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.placeholderMap = new PlaceholderMap()
            .add(ShopPlaceholders.CATEGORY_ID, this::getId)
            .add(ShopPlaceholders.CATEGORY_NAME, this::getName);
    }

    @NonNull
    public static ListingCategory read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
        String name = config.getString(path + ".Name", StringUtil.capitalizeUnderscored(id));
        boolean isDefault = config.getBoolean(path + ".Default");
        Set<String> materials = config.getStringSet(path + ".Materials");

        return new ListingCategory(id, name, isDefault, materials);
    }

    @Override
    @NonNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    @NonNull
    public Set<String> getMaterials() {

        return this.materials;
    }

    public boolean isItemOfThis(@NonNull ItemStack item) {
        return this.isItemOfThis(item.getType());
    }

    public boolean isItemOfThis(@NonNull Material material) {
        return this.isItemOfThis(material.name());
    }

    public boolean isItemOfThis(@NonNull String name) {
        return this.materials.contains(name.toLowerCase()) || this.materials.contains(ShopPlaceholders.WILDCARD);
    }
}
