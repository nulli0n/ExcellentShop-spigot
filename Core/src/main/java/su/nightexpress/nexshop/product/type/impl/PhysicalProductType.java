package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.config.Keys;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public abstract class PhysicalProductType implements PhysicalTyping {

    @NotNull
    protected static ItemStack getBrokenItem() {
        ItemStack itemStack = NightItem.fromType(Material.BARRIER).localized(Lang.EDITOR_GENERIC_BROKEN_ITEM).getItemStack();
        PDCUtil.set(itemStack, Keys.brokenItem, true);
        return itemStack;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        //int amount = this.getUnitAmount() * count;
        ShopUtils.addItem(inventory, this.getItem(), UnitUtils.unitsToAmount(this.getUnitAmount(), count));
    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {
        //int amount = this.getUnitAmount() * count;
        ShopUtils.takeItem(inventory, this::isItemMatches, UnitUtils.unitsToAmount(this.getUnitAmount(), count));
    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return ShopUtils.countItem(inventory, this::isItemMatches);
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return this.countSpace(inventory) > 0;
        //return ShopUtils.countItemSpace(inventory, this.wrapper::isItemMatches, this.wrapper.createItem().getMaxStackSize()) > 0;
    }

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this::isItemMatches, this.getItem().getMaxStackSize());
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }
}
