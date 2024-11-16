package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.impl.AbstractStock;
import su.nightexpress.nexshop.util.ShopUtils;

import java.util.stream.Stream;

public class ChestStock extends AbstractStock<ChestShop, ChestProduct> {

    public ChestStock(@NotNull ShopPlugin plugin, @NotNull ChestShop shop) {
        super(plugin, shop);
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        if (!(result.getProduct() instanceof ChestProduct product)) return;

        TradeType tradeType = event.getTransaction().getTradeType();
        int amount = event.getTransaction().getUnits();
        //Player player = event.getPlayer();

        if (!this.shop.isAdminShop()) {
            if (tradeType == TradeType.BUY) {
                this.consume(product, amount, tradeType);
            }
            else {
                if (!this.store(product, amount, tradeType)) {
                    result.setResult(Transaction.Result.OUT_OF_SPACE);
                }
            }
        }
    }

    @Nullable
    private ChestProduct findProduct(@NotNull Product product) {
        return this.shop.getProductById(product.getId());
    }

    @Override
    public void resetGlobalValues(@NotNull Product product) {

    }

    @Override
    public void resetPlayerLimits(@NotNull Product product) {

    }

    @Override
    public int count(@NotNull Product raw, @NotNull TradeType type, @Nullable Player player) {
        ChestProduct product = this.findProduct(raw);
        if (product == null) return 0;
        if (this.shop.isInactive()) return 0;
        if (this.shop.isAdminShop()) return UNLIMITED;
        if (!(product.getPacker() instanceof ItemPacker packer)) return UNLIMITED;

        double unitAmount = product.getUnitAmount();

        if (ChestUtils.isInfiniteStorage()) {
            return type == TradeType.SELL ? UNLIMITED : (int) Math.floor(product.getQuantity() / unitAmount);
        }

        Inventory inventory = this.shop.getInventory();
        ItemStack[] contents = inventory.getContents();
        double totalAmount;

        // For buying (from player's perspective) return product unit amount based on similar inventory slots only.
        if (type == TradeType.BUY) {
            totalAmount = Stream.of(contents).mapToInt(content -> content != null && packer.isItemMatches(content) ? content.getAmount() : 0).sum();
        }
        // For selling (from player's perspective) return product unit amount based on free or similar inventory slots.
        else {
            ItemStack item = packer.getItem();
            totalAmount = Stream.of(contents).mapToInt(content -> {
                if (content == null || content.getType().isAir()) return item.getMaxStackSize();
                if (packer.isItemMatches(content)) return Math.max(0, content.getMaxStackSize() - content.getAmount());

                return 0;
            }).sum();
        }

        return (int) Math.floor(totalAmount / unitAmount);
    }

    @Override
    public boolean consume(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player) {
        ChestProduct origin = this.findProduct(product);
        if (origin == null) return false;
        if (this.shop.isInactive()) return false;
        if (!(origin.getPacker() instanceof ItemPacker packer)) return false;

        amount = Math.abs(amount * origin.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            origin.setQuantity(origin.getQuantity() - amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        ShopUtils.takeItem(inventory, packer::isItemMatches, amount);
        return true;
    }

    @Override
    public boolean store(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player) {
        ChestProduct origin = this.findProduct(product);
        if (origin == null) return false;
        if (this.shop.isInactive()) return false;
        if (!(origin.getPacker() instanceof ItemPacker packer)) return false;

        amount = Math.abs(amount * origin.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            origin.setQuantity(origin.getQuantity() + amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        return ShopUtils.addItem(inventory, packer.getItem(), amount);
        //return true;
    }

    @Override
    public boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        return false;
    }

    @Override
    public long getRestockTime(@NotNull Product product, @NotNull TradeType type, @Nullable Player player) {
        return 0L;
    }
}
