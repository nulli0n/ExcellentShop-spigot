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
            else this.store(product, amount, tradeType);
        }
    }

    @Override
    @Nullable
    protected ChestProduct findProduct(@NotNull Product product) {
        return this.shop.getProductById(product.getId());
    }

    @Override
    public int countItem(@NotNull ChestProduct product, @NotNull TradeType type, @Nullable Player player) {
        if (this.shop.isInactive()) return 0;
        if (this.shop.isAdminShop()) return -1;
        if (!(product.getPacker() instanceof ItemPacker packer)) return -1;

        if (ChestUtils.isInfiniteStorage()) {
            return type == TradeType.SELL ? -1 : (int) Math.floor(product.getQuantity() / (double) product.getUnitAmount());
        }

        Inventory inventory = this.shop.getInventory();

        // Для покупки со стороны игрока, возвращаем количество реальных предметов в контейнере.
        if (type == TradeType.BUY) {
            double totalItems = Stream.of(inventory.getContents()).filter(has -> has != null && packer.isItemMatches(has))
                .mapToInt(ItemStack::getAmount).sum();
            return (int) Math.floor(totalItems / (double) product.getUnitAmount());
        }
        // Для продажи со стороны игрока, возвращаем количество в свободных и идентичных стопках для предмета.
        else {
            ItemStack item = packer.getItem();
            double totalSlots = (int) Stream.of(inventory.getContents())
                .filter(itemHas -> itemHas == null || itemHas.getType().isAir() || packer.isItemMatches(itemHas)).count();
            double totalSpace = totalSlots * (double) item.getMaxStackSize();
            int unitsSpace = (int) Math.ceil(totalSpace / (double) product.getUnitAmount());

            return unitsSpace - this.count(product, TradeType.BUY);
        }
    }

    @Override
    public boolean consumeItem(@NotNull ChestProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        if (this.shop.isInactive()) return false;
        if (!(product.getPacker() instanceof ItemPacker packer)) return false;

        amount = Math.abs(amount * product.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            product.setQuantity(product.getQuantity() - amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        ShopUtils.takeItem(inventory, packer::isItemMatches, amount);
        return true;
    }

    @Override
    public boolean storeItem(@NotNull ChestProduct product, int amount, @NotNull TradeType type, @Nullable Player player) {
        if (this.shop.isInactive()) return false;
        if (!(product.getPacker() instanceof ItemPacker packer)) return false;

        amount = Math.abs(amount * product.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            product.setQuantity(product.getQuantity() + amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        ShopUtils.addItem(inventory, packer.getItem(), amount);
        return true;
    }

    @Override
    public boolean restockItem(@NotNull ChestProduct product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        return false;
    }
}
