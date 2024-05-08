package su.nightexpress.nexshop.shop.impl.handler;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.shop.impl.packer.VanillaCommandPacker;

public class VanillaCommandHandler implements ProductHandler {

    public static final String NAME = "bukkit_command";

    @Override
    @NotNull
    public ProductPacker createPacker() {
        return new VanillaCommandPacker();
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }
}
