package su.nightexpress.nexshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemProduct {

    boolean isRespectItemMeta();

    void setRespectItemMeta(boolean respectItemMeta);

    @NotNull ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    /*@Deprecated
    default int getUnitAmount() {
        return this.getItem().getAmount();
    }*/


    /*public boolean hasItem() {
        return !this.getItem().getType().isAir();
    }*/

    default boolean isItemMatches(@NotNull ItemStack item) {
        return this.isRespectItemMeta() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }
}
