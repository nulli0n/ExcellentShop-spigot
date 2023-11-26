package su.nightexpress.nexshop.shop.impl.packer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.nexshop.Placeholders;

public class VanillaItemPacker extends AbstractItemPacker {

    private ItemStack item;
    private boolean respectItemMeta;

    public VanillaItemPacker() {
        super(new ItemStack(Material.BARRIER));

        this.placeholderMap
            .add(Placeholders.PRODUCT_ITEM_META_ENABLED, () -> LangManager.getBoolean(this.isRespectItemMeta()));
    }

    /*@NotNull
    public static VanillaItemPacker forChestShop(@NotNull ItemStack item) {
        VanillaItemPacker packer = new VanillaItemPacker();
        packer.load(item);
        packer.setUsePreview(false);
        return packer;
    }

    @NotNull
    public static VanillaItemPacker forVirtualShop(@NotNull ItemStack item) {
        VanillaItemPacker packer = new VanillaItemPacker();
        packer.load(item);
        return packer;
    }*/

    @Override
    public boolean load(@NotNull JYML cfg, @NotNull String path) {
        ItemStack item = cfg.getItemEncoded(path + ".Content.Item");
        if (item == null) return false;

        ItemStack preview = cfg.getItemEncoded(path + ".Content.Preview");
        if (preview == null) preview = new ItemStack(item);

        boolean meta = cfg.getBoolean(path + ".Item_Meta_Enabled");

        this.setPreview(preview);
        this.setItem(item);
        this.setRespectItemMeta(meta);
        return true;
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        cfg.setItemEncoded(path + ".Content.Item", this.getItem());
        cfg.set(path + ".Item_Meta_Enabled", this.isRespectItemMeta());
    }

    @Override
    public void load(@NotNull ItemStack item) {
        this.setItem(item);
        this.setPreview(item);
        this.setRespectItemMeta(item.hasItemMeta());
        this.setUsePreview(true);
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        return this.isRespectItemMeta() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }

    public boolean isRespectItemMeta() {
        return this.respectItemMeta;
    }

    public void setRespectItemMeta(boolean respectItemMeta) {
        this.respectItemMeta = respectItemMeta;
    }
}
