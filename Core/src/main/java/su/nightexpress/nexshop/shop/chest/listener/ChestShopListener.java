package su.nightexpress.nexshop.shop.chest.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.*;

public class ChestShopListener extends AbstractListener<ExcellentShop> {

    private final ChestShopModule       chestShop;
    private final Map<String, Set<JYML>> unloadedShops;

    public ChestShopListener(@NotNull ChestShopModule chestShop) {
        super(chestShop.plugin());
        this.chestShop = chestShop;
        this.unloadedShops = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopPurchaseEvent(ChestShopPurchaseEvent e) {
        Player player = e.getPlayer();

        PreparedProduct<ChestProduct> prepared = e.getPrepared();
        ChestShop shop = prepared.getProduct().getShop();

        if (e.isCancelled()) {
            if (e.getResult() == ChestShopPurchaseEvent.Result.TOO_EXPENSIVE) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.NOT_ENOUGH_ITEMS) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_MONEY) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_SPACE) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_STOCK) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            MessageUtil.sound(player, Config.SOUND_PURCHASE_FAILURE);
            return;
        }

        this.chestShop.getLogger().logTransaction(e);

        MessageUtil.sound(player, Config.SOUND_PURCHASE_SUCCESS);
        Player owner = shop.getOwner().getPlayer();

        if (e.getTradeType() == TradeType.BUY) {
            plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_USER)
                .replace(prepared.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(player);

            if (owner != null && !shop.isAdminShop()) plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_OWNER)
                .replace("%player%", player.getDisplayName())
                .replace(prepared.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(owner);
        }
        else {
            plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_USER)
                .replace(prepared.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(player);

            if (owner != null && !shop.isAdminShop()) plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_OWNER)
                .replace("%player%", player.getDisplayName())
                .replace(prepared.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(owner);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShopInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        ChestShop shop = this.chestShop.getShop(block);
        if (shop == null) return;

        Player player = e.getPlayer();
        Action action = e.getAction();
        boolean isDenied = e.useInteractedBlock() == Result.DENY;
        e.setUseInteractedBlock(Result.DENY);

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                if (isDenied) return;

                ItemStack item = e.getItem();
                if (item != null) {
                    if (Tag.SIGNS.isTagged(item.getType()) || item.getType() == Material.ITEM_FRAME || item.getType() == Material.GLOW_ITEM_FRAME) {
                        if (!shop.isOwner(player)) {
                            plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
                        }
                        else e.setUseInteractedBlock(Result.ALLOW);
                        return;
                    }
                }

                if (shop.isOwner(player) || player.hasPermission(Perms.ADMIN)) {
                    shop.getEditor().open(player, 1);
                }
                else {
                    plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
                }
                return;
            }

            if (shop.isAdminShop() || !shop.isOwner(player)) {
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

        ChestShop shop = this.chestShop.getShop(block);
        if (shop == null) return;

        if (!shop.isOwner(player)) {
            e.setCancelled(true);
            plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
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
        if (!ChestShopModule.isValidContainer(block)) {
            return;
        }

        ChestShop shop = this.chestShop.getShopSideChest(block);
        if (shop == null || block.getType() != shop.getLocation().getBlock().getType()) {
            return;
        }

        Player player = e.getPlayer();
        if (!shop.isOwner(player)) {
            e.setCancelled(true);
            return;
        }

        //shop.setChest((Chest) block.getState());
        this.chestShop.getShopsMap().put(block.getLocation(), shop);
        this.plugin.runTask(c -> shop.updateDisplay(), false);
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
            ChestShop shop = this.chestShop.getShop(from);
            if (shop != null) {
                e.setCancelled(true);
                return;
            }
        }

        // Prevent to put different from a product items to the chest shop.
        if (to.getType() == InventoryType.CHEST && from.getType() == InventoryType.HOPPER) {
            ChestShop shop = this.chestShop.getShop(to);
            if (shop == null) return;

            ItemStack item = e.getItem();
            if (!shop.isProduct(item)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBadProductClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.CHEST) return;

        ChestShop shop = this.chestShop.getShop(e.getInventory());
        if (shop == null) return;

        ItemStack item = e.getCurrentItem();
        if (e.getAction() == InventoryAction.HOTBAR_SWAP || (item != null && !item.getType().isAir() && !shop.isProduct(item))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDataWorldLoad(WorldLoadEvent e) {
        this.unloadedShops.getOrDefault(e.getWorld().getName(), Collections.emptySet()).forEach(this.chestShop::loadShop);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDataWorldUnLoad(WorldUnloadEvent e) {
        World world = e.getWorld();
        this.chestShop.getShops().stream().filter(shop -> shop.getContainer().getWorld().equals(world)).forEach(shop -> {
            this.chestShop.unloadShop(shop);
            this.unloadedShops.computeIfAbsent(world.getName(), k -> new HashSet<>()).add(shop.getConfig());
        });
    }
}
