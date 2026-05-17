package su.nightexpress.excellentshop.playershop.impl;

import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class Showcase implements Writeable {

    private final String    id;
    private final String    name;
    private final NightItem displayItem;

    public Showcase(@NonNull String id, @NonNull String name, @NonNull NightItem displayItem) {
        this.id = id.toLowerCase();
        this.name = name;
        this.displayItem = displayItem;
    }

    @NonNull
    public static Showcase fromMaterial(@NonNull Material material) {
        return new Showcase(BukkitThing.getValue(material), ShopPlaceholders.GENERIC_TYPE, NightItem.fromType(
            material));
    }

    @NonNull
    public static Showcase read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
        String name = ConfigValue.create(path + ".Name", ShopPlaceholders.GENERIC_TYPE).read(config);
        NightItem item = ConfigValue.create(path + ".DisplayBlock", NightItem.fromType(Material.GLASS)).read(config);

        return new Showcase(id, name, item);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Name", this.name);
        config.set(path + ".DisplayBlock", this.displayItem);
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    @NonNull
    public NightItem getDisplayItem() {
        return this.displayItem.copy();
    }
}
