package su.nightexpress.excellentshop.playershop.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.playershop.ChestUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.LangUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class ShopBlock implements Writeable {

    private final Material  material;
    private final NightItem item;
    private final Showcase  showcase;

    public ShopBlock(@NonNull Material material, @NonNull NightItem item, @NonNull Showcase showcase) {
        this.material = material;
        this.item = item.copy();
        this.showcase = showcase;
    }

    @Nullable
    public static ShopBlock read(@NonNull FileConfig config, @NonNull String path) {
        Material material = BukkitThing.getMaterial(config.getString(path + ".Type", BukkitThing.getAsString(
            Material.CHEST)));
        if (material == null || !ChestUtils.isContainer(material)) return null;

        NightItem item = ConfigValue.create(path + ".Item", ChestUtils.getDefaultShopItem(material)).read(config);
        Showcase showcase = Showcase.read(config, path + ".Showcase", BukkitThing.getValue(material));

        return new ShopBlock(material, item, showcase);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Type", BukkitThing.getAsString(this.material));
        config.set(path + ".Item", this.item);
        config.set(path + ".Showcase", this.showcase);
    }

    @NonNull
    public ItemStack getItemStack() {
        ItemStack itemStack = this.getItem().replacement(replacer -> replacer.replace(ShopPlaceholders.GENERIC_TYPE,
            LangUtil.getSerializedName(this.material))).getItemStack();
        ChestUtils.setShopItemType(itemStack, this.material);
        return itemStack;
    }

    @NonNull
    public Material getMaterial() {
        return this.material;
    }

    @NonNull
    public NightItem getItem() {
        return this.item.copy();
    }

    @NonNull
    public Showcase getShowcase() {
        return this.showcase;
    }
}
