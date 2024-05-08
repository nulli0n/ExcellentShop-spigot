package su.nightexpress.nexshop.shop.impl.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.shop.impl.packer.VanillaItemPacker;

public class VanillaItemHandler implements ItemHandler {

    public static final String NAME = "bukkit_item";


    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public VanillaItemPacker createPacker() {
        return new VanillaItemPacker();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return !item.getType().isAir();
    }
}
