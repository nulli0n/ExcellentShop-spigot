package su.nightexpress.nexshop.product.packer;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.text.tag.Tags;

public abstract class AbstractItemPacker<T extends ItemHandler> extends AbstractProductPacker<T> implements ItemPacker {

    public static final ItemStack INVALID_ITEM;

    static {
        INVALID_ITEM = new ItemStack(Material.BARRIER);
        ItemUtil.editMeta(INVALID_ITEM, meta -> {
            meta.setDisplayName(NightMessage.asLegacy(Tags.LIGHT_RED.enclose("<Invalid Item>")));
        });
    }

    public AbstractItemPacker(@NotNull T handler) {
        super(handler);
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        int amount = this.getUnitAmount() * count;
        ShopUtils.addItem(inventory, this.getItem(), amount);
    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {
        int amount = this.getUnitAmount() * count;
        ShopUtils.takeItem(inventory, this::isItemMatches, amount);
    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return ShopUtils.countItem(inventory, this::isItemMatches);
    }

    /*@Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this::isItemMatches, this.getItem().getMaxStackSize()) > 0;
    }*/

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this::isItemMatches, this.getItem().getMaxStackSize());
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }
}
