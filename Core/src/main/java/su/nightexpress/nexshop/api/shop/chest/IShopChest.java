package su.nightexpress.nexshop.api.shop.chest;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nightexpress.nexshop.api.shop.AbstractShopView;
import su.nightexpress.nexshop.api.shop.IProduct;
import su.nightexpress.nexshop.api.shop.IShop;
import su.nightexpress.nexshop.shop.chest.type.ChestType;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChest;

import java.util.*;
import java.util.stream.Stream;

public interface IShopChest extends IShop, ICleanable {

    @Override
    @NotNull
    EditorShopChest getEditor();

    @Override
    @NotNull
    AbstractShopView<IShopChest> getView();

    void teleport(@NotNull Player player);

    @NotNull Location getLocation();

    @NotNull Chest getChest();

    void setChest(@NotNull Chest chest);

    @NotNull UUID getOwnerId();

    @NotNull String getOwnerName();

    @NotNull ChestType getType();

    void setType(@NotNull ChestType type);

    boolean isAdminShop();

    //boolean isDisplayCreated();

    //void setDisplayCreated(boolean displayHas);

    void updateDisplayText();

    void updateDisplay();

    @NotNull List<String> getDisplayText();

    @NotNull Location getDisplayLocation();

    @NotNull Location getDisplayItemLocation();

    @NotNull OfflinePlayer getOwner();

    default boolean isOwner(@NotNull Player player) {
        return this.getOwnerId().equals(player.getUniqueId());
    }

    @NotNull
    @Override
    Map<String, IProductChest> getProductMap();

    @Override
    @NotNull
    Collection<IProductChest> getProducts();

    @Nullable IProductChest getProductById(@NotNull String id);

    default int getProductAmount(@NotNull IProduct product) {
        if (this.isAdminShop() && this.isProduct(product)) return -1;

        Inventory inventory = this.getChestInventory();

        return Stream.of(inventory.getContents()).filter(has -> has != null && product.isItemMatches(has))
            .mapToInt(ItemStack::getAmount).sum();
    }

    default int getProductSpace(@NotNull IProduct product) {
        if (this.isAdminShop() && this.isProduct(product)) return -1;

        ItemStack item = product.getItem();
        Inventory inventory = this.getChestInventory();
        int maxSpace = inventory.getSize() * item.getMaxStackSize();

        return maxSpace - this.getProductAmount(product);
    }

    boolean createProduct(@NotNull Player player, @NotNull ItemStack item);

    default void addProduct(@NotNull IProduct product, int amount) {
        if (!this.isProduct(product)) return;
        if (this.isAdminShop()) return;

        Inventory inventory = this.getChestInventory();
        for (int count = 0; count < amount; count++) {
            if (!inventory.addItem(product.getItem()).isEmpty()) break;
        }
    }

    default boolean hasProduct(@NotNull IProduct product) {
        if (!this.isProduct(product)) return false;
        if (this.isAdminShop()) return true;

        Inventory inventory = this.getChestInventory();
        return inventory.containsAtLeast(product.getItem(), 1);
    }

    default void takeProduct(@NotNull IProduct product, int amount) {
        if (!this.isProduct(product)) return;
        if (this.isAdminShop()) return;

        Inventory inventory = this.getChestInventory();
        for (int count = 0; count < amount; count++) {
            if (!inventory.removeItem(product.getItem()).isEmpty()) break;
        }
    }

    default boolean isChestDouble() {
        return this.getChest().getInventory() instanceof DoubleChestInventory;
    }

    @NotNull
    default Set<Chest> getSides() {
        Set<Chest> chests = new HashSet<>();
        chests.add(this.getChest());

        if (!this.isChestDouble()) return chests;

        DoubleChest doubleChest = (DoubleChest) this.getChestInventory().getHolder();
        if (doubleChest == null) return chests;

        Chest left = (Chest) doubleChest.getLeftSide();
        Chest right = (Chest) doubleChest.getRightSide();
        if (left != null) chests.add(left);
        if (right != null) chests.add(right);

        return chests;
    }

    @NotNull
    default Inventory getChestInventory() {
        if (this.isChestDouble()) {
            DoubleChest doubleChest = (DoubleChest) this.getChest().getInventory().getHolder();
            if (doubleChest != null) {
                return doubleChest.getInventory();
            }
        }
        return this.getChest().getInventory();
    }
}
