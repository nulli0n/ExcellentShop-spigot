package su.nightexpress.excellentshop.feature.playershop.impl;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class Showcase implements Writeable {

    private final String id;
    private final String name;
    private final NightItem displayItem;

    public Showcase(@NotNull String id, @NotNull String name, @NotNull NightItem displayItem) {
        this.id = id.toLowerCase();
        this.name = name;
        this.displayItem = displayItem;
    }

    @NotNull
    public static Showcase fromMaterial(@NotNull Material material) {
        return new Showcase(BukkitThing.getValue(material), ShopPlaceholders.GENERIC_TYPE, NightItem.fromType(material));
    }

    @NotNull
    public static Showcase read(@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        String name = ConfigValue.create(path + ".Name", ShopPlaceholders.GENERIC_TYPE).read(config);
        NightItem item = ConfigValue.create(path + ".DisplayBlock", NightItem.fromType(Material.GLASS)).read(config);

        return new Showcase(id, name, item);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Name", this.name);
        config.set(path + ".DisplayBlock", this.displayItem);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public NightItem getDisplayItem() {
        return this.displayItem.copy();
    }
}
