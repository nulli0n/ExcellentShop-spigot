package su.nightexpress.nexshop.shop.chest.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.stream.Collectors;

public class ShopListener extends AbstractListener<ShopPlugin> {

    private final ChestShopModule module;

    public ShopListener(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var displayHandler = this.module.getDisplayHandler();
        if (displayHandler != null) {
            displayHandler.handleQuit(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ChestConfig.isAutoBankEnabled()) return;

        Player player = event.getPlayer();
        ChestBank bank = this.module.getPlayerBank(player);
        bank.getBalanceMap().forEach((currency, amount) -> {
            currency.give(player, amount);
        });

        if (bank.getBalanceMap().values().stream().anyMatch(amount -> amount > 0)) {
            String balances = bank.getBalanceMap().entrySet().stream().map(entry -> entry.getKey().format(entry.getValue())).collect(Collectors.joining(", "));
            ChestLang.NOTIFICATION_SHOP_EARNINGS.getMessage()
                .replace(Placeholders.GENERIC_AMOUNT, balances)
                .send(player);
        }

        bank.getBalanceMap().clear();
        this.module.savePlayerBank(bank);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopPlaceCreation(BlockPlaceEvent event) {
        if (!ChestConfig.SHOP_ITEM_CREATION_ENABLED.get()) return;

        ItemStack itemStack = event.getItemInHand();
        if (ChestUtils.isShopItem(itemStack)) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            EquipmentSlot slot = event.getHand();
            Block block = event.getBlockPlaced();

            this.plugin.runTask(task -> {
                if (this.module.createShopFromItem(player, block, itemStack)) {
                    player.getInventory().setItem(slot, itemStack);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShopInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        this.module.interactShop(event, event.getPlayer(), block);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        ChestShop shop = this.module.getShop(block);
        if (shop == null) return;

        if (!shop.isOwner(player)) {
            event.setCancelled(true);
            ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || !this.module.deleteShop(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopExpansion(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        this.plugin.runTask(task -> {
            if (!(block.getState() instanceof Chest chest)) return;
            if (!(chest.getInventory() instanceof DoubleChestInventory inventory)) return;

            Location left = inventory.getLeftSide().getLocation();
            Location right = inventory.getRightSide().getLocation();
            ChestShop shopLeft = left == null ? null : this.module.getShop(left);
            ChestShop shopRight = right == null ? null : this.module.getShop(right);
            if ((shopLeft == null && shopRight == null)) return;

            ChestShop shop = shopRight == null ? shopLeft : shopRight;
            shop.updatePosition();
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this.module::isShop);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExplode2(EntityExplodeEvent event) {
        event.blockList().removeIf(this.module::isShop);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopPistonRestrictPull(BlockPistonRetractEvent event) {
        event.setCancelled(event.getBlocks().stream().anyMatch(this.module::isShop));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopPistonRestrictPush(BlockPistonExtendEvent event) {
        event.setCancelled(event.getBlocks().stream().anyMatch(this.module::isShop));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onShopHopperRestrict(InventoryMoveItemEvent event) {
        Inventory target = event.getDestination();
        Inventory from = event.getSource();

        // Prevent to steal items from the chest shop.
        if (target.getType() == InventoryType.HOPPER && from.getHolder() instanceof Container) {
            ChestShop shop = this.module.getShop(from);
            if (shop != null) {
                event.setCancelled(true);
                return;
            }
        }

        // Prevent to put different from a product items to the chest shop.
        if (target.getHolder() instanceof Container && from.getType() == InventoryType.HOPPER) {
            ChestShop shop = this.module.getShop(target);
            if (shop == null) return;

            ItemStack item = event.getItem();
            if (!shop.isProduct(item) || ChestUtils.isInfiniteStorage()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBadProductClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        ChestShop shop = this.module.getShop(inventory);
        if (shop == null) return;

        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            event.setCancelled(true);
            return;
        }

        boolean isShopInv = event.getRawSlot() < inventory.getSize();

        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir() && isShopInv && !shop.isProduct(cursor)) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && !item.getType().isAir() && !isShopInv && !shop.isProduct(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopWorldLoad(WorldLoadEvent event) {
        this.module.getShops(event.getWorld()).forEach(ChestShop::updatePosition);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopWorldUnload(WorldUnloadEvent event) {
        this.module.getShops(event.getWorld()).forEach(ChestShop::deactivate);
    }
}
