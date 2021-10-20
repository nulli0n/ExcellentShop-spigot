package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Chunk;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;

import java.util.HashMap;
import java.util.Map;

public class ChestDisplayHandler extends AbstractManager<ExcellentShop> {

    public static boolean ALLOW_REMOVE = false;

    private final ChestShop chestShop;

    private Map<IShopChest, ArmorStand> hologramIds;
    private Map<IShopChest, Item>       itemIds;

    private Slider slider;

    public ChestDisplayHandler(@NotNull ChestShop chestShop) {
        super(chestShop.plugin());
        this.chestShop = chestShop;
    }

    class Slider extends ITask<ExcellentShop> {

        private       int count    = 0;
        private final int maxCount = ChestShopConfig.DISPLAY_TEXT.size();

        Slider() {
            super(chestShop.plugin(), ChestShopConfig.DISPLAY_SLIDE_TIME, false);
        }

        @Override
        public void action() {
            chestShop.getShops().forEach(shop -> {
                //shop.getLocation().getChunk().setForceLoaded(false);
                if (!shop.isDisplayHas()) {
                    create(shop);
                    return;
                }
                addHologram(shop, shop.getDisplayText().get(this.count));
                addItem(shop);
            });

            if (++this.count >= this.maxCount) this.count = 0;
        }
    }

    @Override
    public void onLoad() {
        ALLOW_REMOVE = false;
        this.hologramIds = new HashMap<>();
        this.itemIds = new HashMap<>();

        if (ChestShopConfig.DISPLAY_SLIDE_TIME > 0 && !ChestShopConfig.DISPLAY_TEXT.isEmpty()) {
            this.slider = new Slider();
            this.slider.start();
        }
        this.addListener(new DisplayListener(this.plugin));
    }

    @Override
    public void onShutdown() {
        ALLOW_REMOVE = true;

        if (this.slider != null) {
            this.slider.stop();
            this.slider = null;
        }
        //CraftEntity craftEntity = null;
        //craftEntity.remove();
        this.hologramIds.values().forEach(Entity::remove);
        this.hologramIds.clear();
        this.itemIds.values().forEach(Entity::remove);
        this.itemIds.clear();
    }

    private void addHologram(@NotNull IShopChest shop, @NotNull String name) {
        ArmorStand stand = this.hologramIds.computeIfAbsent(shop, i -> chestShop.chestNMS.createHologram(shop));
        if (stand != null) {
            stand.setCustomName(name);
        }
        else hologramIds.remove(shop);
    }

    private void deleteHologram(@NotNull IShopChest shop) {
        ArmorStand stand = this.hologramIds.remove(shop);
        if (stand == null) return;

        ALLOW_REMOVE = true;
        stand.remove();
        ALLOW_REMOVE = false;
    }

    private void addItem(@NotNull IShopChest shop) {
        Item item = this.itemIds.computeIfAbsent(shop, i -> chestShop.chestNMS.createItem(shop));
        if (item != null) {
            IShopChestProduct product = Rnd.get(shop.getProducts().stream().toList());
            if (product == null) return;
            item.setItemStack(product.getItem());
        }
        else itemIds.remove(shop);
    }

    private void deleteItem(@NotNull IShopChest shop) {
        Item item = this.itemIds.remove(shop);
        if (item == null) return;

        ALLOW_REMOVE = true;
        item.remove();
        ALLOW_REMOVE = false;
    }

    public void create(@NotNull IShopChest chest) {
        if (!chestShop.chestNMS.isSafeCreation(chest.getLocation())) return;
        if (!chest.getDisplayText().isEmpty()) {
            this.addHologram(chest, chest.getDisplayText().get(0));
        }
        this.addItem(chest);
        chest.setDisplayHas(true);
    }

    public void remove(@NotNull IShopChest chest) {
        this.deleteHologram(chest);
        this.deleteItem(chest);
        chest.setDisplayHas(false);
    }

    class DisplayListener extends AbstractListener<ExcellentShop> {

        public DisplayListener(@NotNull ExcellentShop plugin) {
            super(plugin);
        }

        private Chunk chunkLast;

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChunkUnload(ChunkUnloadEvent e) {
            Chunk chunk = e.getChunk();
            if (chunk.equals(this.chunkLast)) return;

            this.chunkLast = chunk;
            for (IShopChest shop : chestShop.getShops()) {
                if (!shop.isDisplayHas()) continue;
                if (shop.getLocation().getChunk().equals(chunk)) {
                    remove(shop);
                }
            }
            this.chunkLast = null;
        }
    }
}