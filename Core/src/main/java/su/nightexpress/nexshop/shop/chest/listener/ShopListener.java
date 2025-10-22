package su.nightexpress.nexshop.shop.chest.listener;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.Objects;
import java.util.stream.Collectors;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ChestConfig.isAutoBankEnabled()) return;

        Player player = event.getPlayer();
        ChestBank bank = this.module.getPlayerBank(player);
        bank.getBalanceMap().forEach((currencyId, amount) -> {
            EconomyBridge.deposit(player, currencyId, amount);
        });

        if (bank.getBalanceMap().values().stream().anyMatch(amount -> amount > 0)) {
            String balances = bank.getBalanceMap().entrySet().stream().map(entry -> {
                    Currency currency = EconomyBridge.getCurrency(entry.getKey());
                    if (currency == null) return null;

                    return currency.format(entry.getValue());
                })
                .filter(Objects::nonNull).collect(Collectors.joining(", "));

            this.module.getPrefixed(ChestLang.NOTIFICATION_SHOP_EARNINGS).send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, balances));
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

    // TODO Update stock cache on container close

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
        if (target.getType() == InventoryType.HOPPER && from.getHolder() instanceof Container container) {
            ChestShop shop = this.module.getShop(container.getBlock());
            if (shop != null) {
                event.setCancelled(true);
                return;
            }
        }

        // Prevent to put different from a product items to the chest shop.
        if (target.getHolder() instanceof Container container && from.getType() == InventoryType.HOPPER) {
            ChestShop shop = this.module.getShop(container.getBlock());
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

                this.plugin.runTask(task -> {
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
                        originStack.setAmount(originStack.getAmount() - product.getUnitAmount());
                        product.storeStock(TradeType.BUY, 1, null);
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
        if (!(inventory.getHolder() instanceof Container container)) return;

        ChestShop shop = this.module.getShop(container.getBlock());
        if (shop == null) return;

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
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
    public void onShopInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Container container)) return;

        ChestShop shop = this.module.getShop(container.getBlock());
        if (shop == null) return;

        shop.updateStockCache();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        this.module.lookup().getAll(world).forEach(shop -> this.module.activateShop(shop, world));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        this.module.lookup().getAll(world).forEach(this.module::deactivateShop);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) return;

        Chunk chunk = event.getChunk();
        this.module.lookup().getAll(chunk).forEach(this.module::onChunkLoad);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        this.module.lookup().getAll(chunk).forEach(this.module::onChunkUnload);
    }
}
