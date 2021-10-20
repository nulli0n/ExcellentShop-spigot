package su.nightexpress.nexshop.shop.chest;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProductPrepared;
import su.nightexpress.nexshop.api.chest.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.chest.object.ShopChest;

import java.io.File;

public class ChestShopListener extends AbstractListener<ExcellentShop> {

    private final ChestShop chestShop;

    public ChestShopListener(@NotNull ChestShop chestShop) {
        super(chestShop.plugin());
        this.chestShop = chestShop;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShopPurchaseEvent(ChestShopPurchaseEvent e) {
        Player player = e.getPlayer();

        IShopChestProductPrepared prepared = e.getPrepared();
        IShopChest shop = prepared.getShop();

        if (e.isCancelled()) {
            if (e.getResult() == ChestShopPurchaseEvent.Result.TOO_EXPENSIVE) {
                plugin.lang().Shop_Product_Error_TooExpensive
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.NOT_ENOUGH_ITEMS) {
                plugin.lang().Shop_Product_Error_NotEnoughItems
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_MONEY) {
                plugin.lang().Shop_Product_Error_OutOfFunds
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_SPACE) {
                plugin.lang().Shop_Product_Error_OutOfSpace
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_STOCK) {
                plugin.lang().Shop_Product_Error_OutOfStock
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            MsgUT.sound(player, Config.SOUND_PURCHASE_FAILURE);
            return;
        }

        this.chestShop.getLogger().logTransaction(e);

        MsgUT.sound(player, Config.SOUND_PURCHASE_SUCCESS);
        Player owner = shop.getOwner().getPlayer();

        if (e.getTradeType() == TradeType.BUY) {
            plugin.lang().Chest_Shop_Trade_Buy_Info_User
                    .replace(prepared.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(player);

            if (owner != null && !shop.isAdminShop()) plugin.lang().Chest_Shop_Trade_Buy_Info_Owner
                    .replace("%player%", player.getDisplayName())
                    .replace(prepared.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(owner);
        }
        else {
            plugin.lang().Chest_Shop_Trade_Sell_Info_User
                    .replace(prepared.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(player);

            if (owner != null && !shop.isAdminShop()) plugin.lang().Chest_Shop_Trade_Sell_Info_Owner
                    .replace("%player%", player.getDisplayName())
                    .replace(prepared.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(owner);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShopInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        Player player = e.getPlayer();
        Action action = e.getAction();
        IShopChest shop = this.chestShop.getShop(block);
        if (shop == null) return;

        boolean isDenied = e.useInteractedBlock() == Result.DENY;
        e.setUseInteractedBlock(Result.DENY);

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                if (isDenied) return;

                if (shop.isOwner(player) || player.hasPermission(Perms.ADMIN)) {
                    shop.getEditor().open(player, 1);
                }
                else {
                    plugin.lang().Chest_Shop_Error_NotOwner.send(player);
                }
                return;
            }

            if (!shop.isOwner(player)) {
                shop.open(player, 1);
            }
            else if (!isDenied) {
                e.setUseInteractedBlock(Result.ALLOW);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        IShopChest shop = this.chestShop.getShop(block);
        if (shop == null) return;

        if (!shop.isOwner(player)) {
            e.setCancelled(true);
            plugin.lang().Chest_Shop_Error_NotOwner.send(player);
        }
        else {
            if (player.getGameMode() == GameMode.CREATIVE) {
                e.setCancelled(true);
                return;
            }
            this.chestShop.deleteShop(player, block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExpansion(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();
        if (!this.chestShop.isValidChest(block)) {
            return;
        }

        Player player = e.getPlayer();
        IShopChest shop = this.chestShop.getShopNearBlock(block);
        if (shop == null || block.getType() != shop.getLocation().getBlock().getType()) {
            return;
        }
        if (!shop.isOwner(player)) {
            e.setCancelled(true);
            return;
        }

        this.chestShop.getShopsMap().put(block.getLocation(), shop);
        shop.setChest((Chest) block.getState());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(this.chestShop::isShop);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopExplode2(EntityExplodeEvent e) {
        e.blockList().removeIf(this.chestShop::isShop);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopPiston1(BlockPistonRetractEvent e) {
        e.setCancelled(e.getBlocks().stream().anyMatch(this.chestShop::isShop));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopPiston2(BlockPistonExtendEvent e) {
        e.setCancelled(e.getBlocks().stream().anyMatch(this.chestShop::isShop));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopHopperTakeAdd(InventoryMoveItemEvent e) {
        Inventory to = e.getDestination();
        Inventory from = e.getSource();

        // Prevent to steal items from the chest shop.
        if (to.getType() == InventoryType.HOPPER && from.getType() == InventoryType.CHEST) {
            IShopChest shop = this.chestShop.getShop(from);
            if (shop != null) {
                e.setCancelled(true);
                return;
            }
        }

        // Prevent to put different from a product items to the chest shop.
        if (to.getType() == InventoryType.CHEST && from.getType() == InventoryType.HOPPER) {
            IShopChest shop = this.chestShop.getShop(to);
            if (shop == null) return;

            ItemStack item = e.getItem();
            if (!shop.isProduct(item)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBadProductClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.CHEST) return;

        IShopChest shop = this.chestShop.getShop(e.getInventory());
        if (shop == null) return;

        ItemStack item = e.getCurrentItem();
        if (e.getAction() == InventoryAction.HOTBAR_SWAP || (item != null && !ItemUT.isAir(item) && !shop.isProduct(item))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDataWorldLoad(WorldLoadEvent e) {
        this.chestShop.loadFails.removeIf(id -> {
            File file = new File(this.chestShop.getFullPath() + ChestShop.DIR_SHOPS + id + ".yml");
            if (!file.exists()) return true;

            try {
                ShopChest shop = new ShopChest(this.chestShop, new JYML(file));
                this.chestShop.addShop(shop);
            }
            catch (Exception ex) {}
            return true;
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDataWorldUnLoad(WorldUnloadEvent e) {
        String worldName = e.getWorld().getName();
        this.chestShop.info("Detected world unload: '" + worldName + "'. Unloading shops...");

        this.chestShop.getShops().removeIf(shop -> {
            World shopWorld = shop.getChest().getWorld();
            if (!shopWorld.getName().equalsIgnoreCase(worldName)) return false;

            this.chestShop.cacheFailedLoad(shop.getId());
            shop.clear();

            return true;
        });
    }
}
