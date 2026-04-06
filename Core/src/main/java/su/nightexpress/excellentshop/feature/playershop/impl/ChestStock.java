package su.nightexpress.excellentshop.feature.playershop.impl;

import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.feature.playershop.ChestUtils;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;

public class ChestStock implements StockData {

    private final ChestShop shop;
    private final ChestProduct product;

    public ChestStock(@NonNull ChestShop shop, @NonNull ChestProduct product) {
        this.shop = shop;
        this.product = product;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isClean() {
        return false;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void markClean() {

    }

    @Override
    public void markRemoved() {

    }

    @Override
    public boolean isRestockTime() {
        return false;
    }

    @Override
    public void setExpired() {

    }

    @Override
    public void consume(int units) {
        if (!this.shop.isAccessible()) return;
        if (!this.product.getContent().isPhysical()) return;

        int totalAmount = UnitUtils.unitsToAmount(this.product, units);

        if (ChestUtils.isInfiniteStorage()) {
            this.product.setQuantity(this.product.getQuantity() - totalAmount);
            this.product.updateStockCache();
            return;
        }

        Inventory inventory = this.shop.getInventory().orElse(null);
        if (inventory == null) return; // Shop container is not valid anymore.

        ShopUtils.takeItem(inventory, this.product.getContent()::isItemMatches, totalAmount);
        this.product.updateStockCache();
    }

    @Override
    public void store(int units) {
        if (units <= 0) return;
        if (!this.shop.isAccessible()) return;
        if (!this.product.isValid()) return;
        if (!(this.product.getContent() instanceof ItemContent content)) return;

        int totalAmount = UnitUtils.unitsToAmount(this.product, units);

        if (ChestUtils.isInfiniteStorage()) {
            this.product.setQuantity(this.product.getQuantity() + totalAmount);
            this.product.updateStockCache();
            return;
        }

        Inventory inventory = this.shop.getInventory().orElse(null);
        if (inventory == null) return; // Shop container is not valid anymore.

        ShopUtils.addItem(inventory, content.getItem(), totalAmount);
        this.product.updateStockCache();
    }

    @Override
    public int getStock() {
        if (this.shop.isAdminShop()) return -1;
        if (ChestUtils.isInfiniteStorage()) return this.product.countUnits((int) this.product.getQuantity());
        if (!this.shop.isAccessible()) return this.product.getCachedAmount();

        Inventory inventory = this.shop.getInventory().orElse(null);
        if (inventory == null) return 0; // Shop container is not valid anymore.

        return this.product.countUnits(inventory);
    }

    @Override
    public void setStock(int units) {
        this.consume(this.getStock());
        this.store(units);
    }

    @Override
    public long getRestockDate() {
        return -1L;
    }

    @Override
    public void setRestockDate(long restockDate) {

    }
}
