package su.nightexpress.excellentshop.product.content;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.nexshop.util.ErrorHandler;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedItemStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.Version;

public class ItemContent extends ProductContent {

    private final AdaptedItem adaptedItem;

    private boolean compareNbt;

    public ItemContent(@NonNull AdaptedItem adaptedItem, boolean compareNbt) {
        super(ContentType.ITEM);
        this.adaptedItem = adaptedItem;
        this.setCompareNbt(compareNbt);
    }

    @Nullable
    public static ItemContent read(@NonNull FileConfig config, @NonNull String path) throws ProductLoadException {
        updateVanillaConfig(config, path);
        updatePluginConfig(config, path);

        AdaptedItem adaptedItem = AdaptedItemStack.read(config, path + ".Item");
        if (adaptedItem == null) return null;

        boolean compareNbt = config.getBoolean(path + ".Item.CompareNBT");

        return new ItemContent(adaptedItem, compareNbt);
    }

    private static void updateVanillaConfig(@NonNull FileConfig config, @NonNull String path) {
        if (config.contains(path + ".Content.Item")) {
            String tagString = String.valueOf(config.getString(path + ".Content.Item"));

            ItemTag tag = new ItemTag(tagString, -1);
            ItemStack itemStack = tag.getItemStack();
            if (itemStack == null) {
                ErrorHandler.configError("Could not update itemstack '" + tagString + "'!", config, path);
            }

            config.remove(path + ".Content.Item");
            config.set(path + ".ItemTag", new ItemTag(tagString, Version.getCurrent().getDataVersion()));
        }

        if (config.contains(path + ".ItemTag")) {
            ItemTag tag = ItemTag.read(config, path + ".ItemTag");
            boolean respectMeta = config.getBoolean(path + ".Item_Meta_Enabled");

            config.remove(path + ".ItemTag");
            config.remove(path + ".Item_Meta_Enabled");

            AdaptedVanillaStack vanillaStack = new AdaptedVanillaStack(tag);
            config.set(path + ".Item", vanillaStack);
            config.set(path + ".Item.CompareNBT", respectMeta);
        }
    }

    private static void updatePluginConfig(@NonNull FileConfig config, @NonNull String path) {
        if (!config.contains(path + ".Handler")) return;

        String handlerId = config.getString(path + ".Handler", "dummy");
        if (handlerId.equalsIgnoreCase("bukkit_item")) {
            config.remove(path + ".Handler");
            return;
        }

        ItemAdapter<?> adapter = ItemBridge.getAdapter(handlerId);
        if (!(adapter instanceof IdentifiableItemAdapter identifiableAdapter)) {
            ErrorHandler.configError("Invalid item handler '" + handlerId + "'.", config, path);
            return;
        }

        String itemId = config.getString(path + ".Content.ItemId", "null");
        int amount = config.getInt(path + ".Content.Amount", 1);

        AdaptedCustomStack customStack = new AdaptedCustomStack(identifiableAdapter, new ItemIdData(itemId, amount));
        config.set(path + ".Item", customStack);
        config.remove(path + ".Handler");
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Item", this.adaptedItem);
        config.set(path + ".Item.CompareNBT", this.compareNbt);
    }

    @NonNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public void delivery(@NonNull Inventory inventory, int count) {
        var result = ShopUtils.addItem(inventory, this.getItem(), UnitUtils.unitsToAmount(this.getUnitAmount(), count));
        if (result.isEmpty()) return;

        Location location = inventory.getLocation();
        World world = location == null ? null : location.getWorld();
        if (location != null && world != null) {
            result.values().forEach(itemStack -> world.dropItemNaturally(location, itemStack));
        }
    }

    @Override
    public void take(@NonNull Inventory inventory, int count) {
        ShopUtils.takeItem(inventory, this::isItemMatches, UnitUtils.unitsToAmount(this.getUnitAmount(), count));
    }

    @Override
    public int count(@NonNull Inventory inventory) {
        return ShopUtils.countItem(inventory, this::isItemMatches);
    }

    @Override
    public boolean hasSpace(@NonNull Inventory inventory) {
        return this.countSpace(inventory) >= this.getUnitAmount();
    }

    @Override
    public int countSpace(@NonNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this::isItemMatches, this.getItem().getMaxStackSize());
    }

    @Override
    public int getUnitAmount() {
        return this.adaptedItem.getAmount();
    }

    @Override
    public int getMaxStackSize() {
        return this.adaptedItem.itemStack().map(ItemStack::getMaxStackSize).orElse(1);
    }

    public boolean isItemMatches(@NonNull ItemStack other) {
        if (!this.isValid()) return false;

        if (this.adaptedItem.getAdapter().isVanilla()) {
            ItemStack itemStack = this.getItem();
            return this.compareNbt ? itemStack.isSimilar(other) : itemStack.getType() == other.getType();
        }

        return this.adaptedItem.isSimilar(other);
    }

    @NonNull
    public ItemStack getItem() {
        ItemStack itemStack = this.adaptedItem.getItemStack();
        if (itemStack == null) throw new IllegalStateException("Could not produce ItemStack from the AdaptedItem. Check #isValid before calling this method.");

        return itemStack;
    }

    @Override
    @NonNull
    public String getName() {
        return this.adaptedItem.getAdapter().getName();
    }

    @Override
    public boolean isValid() {
        return this.adaptedItem.isValid();
    }

    @Override
    public boolean isPhysical() {
        return true;
    }

    @NonNull
    public AdaptedItem getAdaptedItem() {
        return this.adaptedItem;
    }

    public boolean isCompareNbt() {
        return compareNbt;
    }

    public void setCompareNbt(boolean compareNbt) {
        this.compareNbt = compareNbt;
    }
}
