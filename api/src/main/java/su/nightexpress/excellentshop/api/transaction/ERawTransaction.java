package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.shop.Shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ERawTransaction extends ETransaction {

    private final List<ItemStack> items;
    private final List<Shop>      targetShops;

    public ERawTransaction(@NonNull Player player,
                           @NonNull TradeType type,
                           @NonNull ETransactionOptions options,
                           @NonNull List<ItemStack> items,
                           @NonNull List<Shop> targetShops) {
        super(player, type, options);
        this.items = items;
        this.targetShops = targetShops;
    }

    @NonNull
    public static Builder builder(@NonNull Player player, @NonNull TradeType type) {
        return new Builder(player, type);
    }

    public boolean hasTargetShops() {
        return !this.targetShops.isEmpty();
    }

    @NonNull
    public List<ItemStack> getItems() {
        return this.items;
    }

    @NonNull
    public List<Shop> getTargetShops() {
        return this.targetShops;
    }

    public static class Builder extends ETransaction.Builder<Builder, ERawTransaction> {

        private final List<ItemStack> items;
        private final List<Shop> targetShops;

        public Builder(@NonNull Player player, @NonNull TradeType type) {
            super(player, type);
            this.items = new ArrayList<>();
            this.targetShops = new ArrayList<>();
        }

        @Override
        @NonNull
        protected ERawTransaction build(@NonNull ETransactionOptions options) {
            return new ERawTransaction(this.player, this.type, options, this.items, this.targetShops);
        }

        @Override
        @NonNull
        protected Builder getThis() {
            return this;
        }

        @NonNull
        public Builder addItem(@NonNull ItemStack itemStack) {
            this.items.add(new ItemStack(itemStack));
            return this;
        }

        @NonNull
        public Builder addItems(@NonNull Collection<ItemStack> items) {
            items.forEach(this::addItem);
            return this;
        }

        @NonNull
        public Builder addItems(@Nullable ItemStack @NonNull [] items) {
            for (ItemStack itemStack : items) {
                if (itemStack != null) {
                    this.addItem(itemStack);
                }
            }
            return this;
        }

        @NonNull
        public Builder addItems(@NonNull Inventory inventory) {
            return this.addItems(inventory.getContents());
        }

        @NonNull
        public Builder targetShop(@NonNull Shop shop) {
            this.targetShops.add(shop);
            return this;
        }

        @NonNull
        public Builder targetShops(@NonNull Collection<Shop> shops) {
            this.targetShops.addAll(shops);
            return this;
        }
    }
}
