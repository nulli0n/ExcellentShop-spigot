package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ItemProduct;

import java.util.UUID;

public class VirtualItemProduct extends VirtualProduct implements ItemProduct {

    private ItemStack item;
    private boolean respectItemMeta;

    public VirtualItemProduct(@NotNull ItemStack item, @NotNull Currency currency) {
        this(UUID.randomUUID().toString(), item, currency);
    }

    public VirtualItemProduct(@NotNull String id, @NotNull ItemStack item, @NotNull Currency currency) {
        super(id, currency);
        this.setItem(item);
        this.setRespectItemMeta(item.hasItemMeta());

        this.placeholderMap
            .add(Placeholders.PRODUCT_ITEM_META_ENABLED, () -> LangManager.getBoolean(this.isRespectItemMeta()))
        ;
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        int amount = this.getUnitAmount() * count;
        PlayerUtil.addItem(player, this.getItem(), amount);
    }

    @Override
    public void take(@NotNull Player player, int count) {
        int amount = this.getUnitAmount() * count;
        PlayerUtil.takeItem(player, this::isItemMatches, amount);
    }

    @Override
    public int count(@NotNull Player player) {
        return PlayerUtil.countItem(player, this::isItemMatches);
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public boolean isRespectItemMeta() {
        return this.respectItemMeta;
    }

    @Override
    public void setRespectItemMeta(boolean respectItemMeta) {
        this.respectItemMeta = respectItemMeta;
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return PlayerUtil.countItemSpace(player, this.getItem()) > 0;
    }
}
