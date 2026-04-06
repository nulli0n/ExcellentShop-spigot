package su.nightexpress.excellentshop.feature.playershop.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.ChestUtils;
import su.nightexpress.excellentshop.feature.playershop.core.ChestConfig;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestProduct;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class ShopListener extends AbstractListener<ShopPlugin> {

    private final ChestShopModule module;

    public ShopListener(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.module.getDisplayManager().removeForViewer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        this.module.getDisplayManager().removeForViewer(event.getPlayer());
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

            this.plugin.runTask(() -> {
                if (this.module.createShopFromItem(player, block, itemStack)) {
                    player.getInventory().setItem(slot, itemStack);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopInteract(PlayerInteractEvent event) {
        this.module.handleInteractEvent(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        ChestShop shop = this.module.getShop(block);
        if (shop == null) return;

        if (player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }

        if (!this.module.deleteShop(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExpansion(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        Block block = event.getBlockPlaced();
        if (!(block.getBlockData() instanceof Chest chest)) return;

        BlockFace face = chest.getFacing();
        BlockFace[] lookup = switch (face) {
            case SOUTH, NORTH -> new BlockFace[]{BlockFace.WEST, BlockFace.EAST};
            case WEST, EAST -> new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
            default -> null;
        };
        if (lookup == null) return;

        for (BlockFace blockFace : lookup) {
            Block neighibor = block.getRelative(blockFace);
            if (!this.module.isShop(neighibor)) continue;
            if (!(neighibor.getBlockData() instanceof Chest nearChest)) continue;
            if (nearChest.getFacing() != face) continue;

            event.setCancelled(true);
            break;
        }
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
        Location fromLocation = from.getLocation();
        if (target.getType() == InventoryType.HOPPER && fromLocation != null) {
            ChestShop shop = this.module.getShop(fromLocation);
            if (shop != null) {
                event.setCancelled(true);
                return;
            }
        }

        // Prevent to put different from a product items to the chest shop.
        Location targetLocation = target.getLocation();
        if (targetLocation != null && from.getType() == InventoryType.HOPPER) {
            ChestShop shop = this.module.getShop(targetLocation);
            if (shop == null) return;

            ItemStack itemStack = new ItemStack(event.getItem());
            ChestProduct product = shop.getProduct(itemStack);
            if (product == null) {
                event.setCancelled(true);
                return;
            }

            if (ChestUtils.isInfiniteStorage()) {
                event.setCancelled(true);

                Location location = from.getLocation();
                if (location == null) return;

                Block block = location.getBlock();

                // What the hell is happenning in this event? The inventory is fucking broken with stack amounts of 1 every time.
                // https://www.spigotmc.org/threads/581448/
                // https://www.spigotmc.org/threads/534714/

                this.plugin.runTask(() -> {
                    if (!(block.getState() instanceof Hopper hopper)) return; // Obtain fresh Hopper instance, do not trust this damn event anymore.

                    Inventory hopperInv = hopper.getInventory();

                    for (int index = 0; index < hopperInv.getSize(); index++) {
                        ItemStack originStack = hopperInv.getItem(index);
                        if (originStack == null || !originStack.isSimilar(itemStack)) continue;

                        int units = product.countUnits(originStack.getAmount());
                        if (units <= 0) {
                            event.setCancelled(true);
                            return;
                        }

                        // Do not cancel, instead reduce item quantity.
                        originStack.setAmount(originStack.getAmount() - product.getUnitSize());
                        //product.storeStock(TradeType.BUY, 1, null);
                        product.getStockData().store(1);
                        product.getShop().markDirty();
                        hopperInv.setItem(index, originStack);
                        break;
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBadProductClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof BlockInventoryHolder container)) return;

        ChestShop shop = this.module.getShop(container.getBlock());
        if (shop == null) return;

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            event.setCancelled(true);
            return;
        }

        boolean isShopInv = event.getRawSlot() < inventory.getSize();

        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir() && isShopInv && !shop.isProduct(cursor)) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && !item.getType().isAir() && !isShopInv && !shop.isProduct(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BlockInventoryHolder container)) return;

        ChestShop shop = this.module.getShop(container.getBlock());
        if (shop == null) return;

        shop.updateStockCache();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        this.module.handleWorldLoad(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        this.module.handleWorldUnload(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) return;

        this.module.handleChunkLoad(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.module.handleChunkUnload(event.getChunk());
    }
}
