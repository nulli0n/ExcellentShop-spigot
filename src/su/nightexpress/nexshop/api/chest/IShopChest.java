package su.nightexpress.nexshop.api.chest;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.nexshop.api.AbstractShopView;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.shop.chest.editor.object.EditorShopChest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface IShopChest extends IShop, ICleanable {

    String PLACEHOLDER_OWNER        = "%shop_owner%";
    String PLACEHOLDER_LOC_X        = "%shop_location_x%";
    String PLACEHOLDER_LOC_Y        = "%shop_location_y%";
    String PLACEHOLDER_LOC_Z        = "%shop_location_z%";
    String PLACEHOLDER_LOC_WORLD    = "%shop_location_world%";
    String PLACEHOLDER_IS_ADMIN     = "%shop_is_admin%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        Location location = this.getLocation();
        World world = this.getChest().getWorld();

        return str -> IShop.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_OWNER, this.getOwnerName())
                .replace(PLACEHOLDER_LOC_X, NumberUT.format(location.getX()))
                .replace(PLACEHOLDER_LOC_Y, NumberUT.format(location.getY()))
                .replace(PLACEHOLDER_LOC_Z, NumberUT.format(location.getZ()))
                .replace(PLACEHOLDER_LOC_WORLD, CoreConfig.getWorldName(world.getName()))
                .replace(PLACEHOLDER_IS_ADMIN, plugin().lang().getBool(this.isAdminShop()))
        );
    }

    @Override
    @NotNull
    EditorShopChest getEditor();

    @Override
    @NotNull
    AbstractShopView<IShopChest> getView();

    default void teleport(@NotNull Player player) {
        Location location = this.getLocation().clone();
        Block block = location.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Directional directional) {
            Block opposite = block.getRelative(directional.getFacing()).getLocation().clone().add(0, 0.5, 0).getBlock();
            location = LocUT.getCenter(opposite.getLocation());
            location.setDirection(directional.getFacing().getOppositeFace().getDirection());
            location.setPitch(35F);
        }
        player.teleport(location);
    }

    @NotNull
    Location getLocation();

    @NotNull
    Chest getChest();

    void setChest(@NotNull Chest chest);

    @NotNull
    UUID getOwnerId();

    @NotNull
    String getOwnerName();

    boolean isAdminShop();

    void setAdminShop(boolean isAdminShop);

    boolean isDisplayHas();

    void setDisplayHas(boolean displayHas);

    void updateDisplayText();

    @NotNull
    List<String> getDisplayText();

    @NotNull
    Location getDisplayLocation();

    @NotNull
    Location getDisplayItemLocation();

    @NotNull
    OfflinePlayer getOwner();

    @Override
    default double getShopBalance(@NotNull IShopCurrency currency) {
        if (this.isAdminShop()) return -1D;

        OfflinePlayer player = this.getOwner();
        return currency.getBalance(player);
    }

    @Override
    default void takeFromShopBalance(@NotNull IShopCurrency currency, double amount) {
        if (this.isAdminShop()) return;

        currency.take(this.getOwner(), amount);
    }

    @Override
    default void addToShopBalance(@NotNull IShopCurrency currency, double amount) {
        if (this.isAdminShop()) return;

        currency.give(this.getOwner(), amount);
    }

    default boolean isOwner(@NotNull Player player) {
        return this.getOwnerId().equals(player.getUniqueId());
    }

    default boolean isProduct(@NotNull IShopProduct product) {
        return this.isProduct(product.getItem());
    }

    default boolean isProduct(@NotNull ItemStack item) {
        return this.getProducts().stream().anyMatch(product -> product.getItem().isSimilar(item));
    }

    default int getProductAmount(@NotNull IShopProduct product) {
        if (this.isAdminShop() && this.isProduct(product)) return -1;

        int amount = 0;
        ItemStack item = product.getItem();
        Inventory inventory = this.getChestInventory();
        for (ItemStack has : inventory.getContents()) {
            if (has != null && has.isSimilar(item)) {
                amount += has.getAmount();
            }
        }
        return amount;
    }

    default int getProductSpace(@NotNull IShopProduct product) {
        if (this.isAdminShop() && this.isProduct(product)) return -1;

        int space = 0;
        ItemStack item = product.getItem();
        Inventory inventory = this.getChestInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack has = inventory.getItem(slot);
            if (has == null || ItemUT.isAir(has)) {
                space += item.getMaxStackSize();
            }
            else if (has.isSimilar(item)) {
                space += (has.getMaxStackSize() - has.getAmount());
            }
        }
        return space;
    }

    boolean createProduct(@NotNull Player player, @NotNull ItemStack item);

    default void addProduct(@NotNull IShopProduct product, int amount) {
        if (!this.isProduct(product)) return;
        if (this.isAdminShop()) return;

        Inventory inventory = this.getChestInventory();
        for (int count = 0; count < amount; count++) {
            if (!inventory.addItem(product.getItem()).isEmpty()) break;
        }
    }

    default boolean hasProduct(@NotNull IShopProduct product) {
        if (!this.isProduct(product)) return false;
        if (this.isAdminShop()) return true;

        Inventory inventory = this.getChestInventory();
        return inventory.containsAtLeast(product.getItem(), 1);
    }

    default void takeProduct(@NotNull IShopProduct product, int amount) {
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
    default Inventory getChestInventory() {
        if (this.isChestDouble()) {
            DoubleChest doubleChest = (DoubleChest) this.getChest().getInventory().getHolder();
            if (doubleChest != null) {
                return doubleChest.getInventory();
            }
        }
        return this.getChest().getInventory();
    }

    @NotNull
    @Override
    Map<String, IShopChestProduct> getProductMap();

    @Override
    @NotNull
    default Collection<IShopChestProduct> getProducts() {
        return this.getProductMap().values();
    }
}
