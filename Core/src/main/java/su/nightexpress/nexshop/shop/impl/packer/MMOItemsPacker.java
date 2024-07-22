package su.nightexpress.nexshop.shop.impl.packer;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.AdventureUtils;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;

import java.util.logging.Level;

public class MMOItemsPacker extends AbstractPluginItemPacker {

    public MMOItemsPacker() {
        super("", 0);
    }

    public MMOItemsPacker(@NotNull String itemId, int amount) {
        super(itemId, amount);
    }

    @Override
    @NotNull
    public ItemStack createItem() {
        String[] split = this.getItemId().split(":");

        Type type = Type.get(split[0]);
        if (type == null || split.length < 2) return new ItemStack(Material.AIR);

        String id = split[1];

        MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
        if (template != null) {
            ItemStack itemStack = template.newBuilder().build().newBuilder().buildSilently();
            if (itemStack != null) return itemStack;
        }

        ShopAPI.PLUGIN.warn("Could not create MMOItems stack! Type: " + type.getName() + ", Id: " + id);
        return new ItemStack(Material.AIR);
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        String[] split = this.getItemId().split(":");

        Type type = Type.get(split[0]);
        if (type == null || split.length < 2) return new ItemStack(Material.AIR);

        String id = split[1];

        MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
        if (template != null) {
            // Stupid GenerateLoreEvent
            ItemStackBuilder builder = template.newBuilder().build().newBuilder();
            return buildNBT(builder);
        }

        ShopAPI.PLUGIN.warn("Could not create MMOItems display stack! Type: " + type.getName() + ", Id: " + id);
        return new ItemStack(Material.AIR);
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        String[] split = this.getItemId().split(":");

        Type type = Type.get(split[0]);
        if (type == null || split.length < 2) return false;

        String id = split[1];

        MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
        if (template == null) return false;

        ItemStack itemStack = template.newBuilder().build().newBuilder().buildSilently();
        return itemStack != null;

//        return MMOItems.plugin.getItem(type, id) != null;
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack itemStack) {
        Type type = MMOItems.getType(itemStack);
        String id = MMOItems.getID(itemStack);
        if (type == null || id == null) return null;

        return type.getName() + ":" + id;
    }

    @SuppressWarnings("unchecked")
    public ItemStack buildNBT(@NotNull ItemStackBuilder builder) {
        MMOItem builtMMOItem = builder.getMMOItem().clone();
        if (!builtMMOItem.hasData(ItemStats.ENCHANTS)) {
            builtMMOItem.setData(ItemStats.ENCHANTS, ItemStats.ENCHANTS.getClearStatData());
        }

        if (!builtMMOItem.hasData(ItemStats.NAME)) {
            builtMMOItem.setData(ItemStats.NAME, ItemStats.NAME.getClearStatData());
        }

        if (builtMMOItem.getStatHistory(ItemStats.NAME) == null) {
            StatHistory.from(builtMMOItem, ItemStats.NAME);
        }

        builtMMOItem.getStats().forEach(stat -> {
            try {
                stat.whenApplied(builder, builtMMOItem.getData(stat));
            }
            catch (NullPointerException | IllegalArgumentException exception) {
                MMOItems.print(Level.WARNING, "An error occurred while trying to generate item '$f{0}$b' with stat '$f{1}$b': {2}", "ItemStackBuilder", builtMMOItem.getId(), stat.getId(), exception.getMessage());
            }
        });

        if (builder.getMeta().hasDisplayName()) {
            String displayName = builder.getMeta().getDisplayName();
            if (builder.getLore().hasTooltip()) {
                displayName = builder.getLore().getTooltip().getTop() + displayName;
            }

            displayName = MythicLib.plugin.getPlaceholderParser().parse(null, displayName);
            displayName = builder.getLore().applySpecialPlaceholders(displayName);
            if (builder.getLore().hasTooltip() && builder.getLore().getTooltip().getCenteringOptions() != null && builder.getLore().getTooltip().getCenteringOptions().displayName()) {
                displayName = builder.getLore().getTooltip().getCenteringOptions().centerName(displayName);
            }

            AdventureUtils.setDisplayName(builder.getMeta(), ChatColor.WHITE + displayName);
        }

        ItemStack itemStack = builder.getItemStack();
        itemStack.setItemMeta(builder.getMeta());

        return itemStack;
    }
}
