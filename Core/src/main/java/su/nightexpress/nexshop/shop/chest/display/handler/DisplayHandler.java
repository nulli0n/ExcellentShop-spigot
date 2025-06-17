package su.nightexpress.nexshop.shop.chest.display.handler;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.display.impl.FakeEntity;

import java.util.List;
import java.util.Set;

public abstract class DisplayHandler<T> {

    protected final EntityType itemType;
    protected final EntityType showcaseType;
    protected final EntityType hologramType;

    public DisplayHandler() {
        this.itemType = EntityType.ITEM;

        if (ChestUtils.canUseDisplayEntities()) {
            this.showcaseType = EntityType.ITEM_DISPLAY;
            this.hologramType = EntityType.TEXT_DISPLAY;
        }
        else {
            this.showcaseType = EntityType.ARMOR_STAND;
            this.hologramType = EntityType.ARMOR_STAND;
        }
    }

    public void broadcastDestroyPacket(@NotNull Set<Integer> idSet) {
        this.broadcastPacket(this.createDestroyPacket(idSet));
    }

    public void sendDestroyPacket(@NotNull Player player, @NotNull Set<Integer> idSet) {
        this.sendPacket(player, this.createDestroyPacket(idSet));
    }

    public void createItemPackets(@NotNull Player player, @NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        this.getItemPackets(entity, needSpawn, item).forEach(packet -> this.sendPacket(player, packet));
    }

    public void createShowcasePackets(@NotNull Player player, @NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        this.getShowcasePackets(entity, needSpawn, item).forEach(packet -> this.sendPacket(player, packet));
    }

    public void createHologramPackets(@NotNull Player player, @NotNull FakeEntity entity, boolean needSpawn, @NotNull String textLine) {
        this.getHologramPackets(entity, needSpawn, textLine).forEach(packet -> this.sendPacket(player, packet));
    }

    protected abstract void broadcastPacket(@NotNull T packet);

    protected abstract void sendPacket(@NotNull Player player, @NotNull T packet);

    @NotNull
    protected abstract T createSpawnPacket(@NotNull EntityType entityType, @NotNull FakeEntity entity);

    @NotNull
    protected abstract T createDestroyPacket(@NotNull Set<Integer> list);

    @NotNull
    protected abstract List<T> getHologramPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull String textLine);

    @NotNull
    protected abstract List<T> getShowcasePackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item);

    @NotNull
    protected abstract List<T> getItemPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item);
}
