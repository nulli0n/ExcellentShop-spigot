package su.nightexpress.nexshop.shop.impl.handler;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.packer.CommandPacker;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.impl.packer.VanillaCommandPacker;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;

import java.util.function.Predicate;

public class VanillaCommandHandler implements ProductHandler {

    private static final String TEXTURE_COMMAND = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQwZjQwNjFiZmI3NjdhN2Y5MjJhNmNhNzE3NmY3YTliMjA3MDliZDA1MTI2OTZiZWIxNWVhNmZhOThjYTU1YyJ9fX0=";

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

    @Override
    public void loadEditor(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product) {
        Predicate<MenuViewer> predicate = viewer -> product.getHandler() == this;

        menu.addItem(ItemUtil.createCustomHead(TEXTURE_COMMAND), VirtualLocales.PRODUCT_COMMANDS, 19).setClick((viewer, event) -> {
            if (!(product.getPacker() instanceof CommandPacker packer)) return;

            if (event.isRightClick()) {
                packer.getCommands().clear();
                product.getShop().saveProducts();
                menu.openNextTick(viewer, viewer.getPage());
                return;
            }
            menu.handleInput(viewer, VirtualLang.EDITOR_ENTER_COMMAND, wrapper -> {
                packer.getCommands().add(wrapper.getText());
                product.getShop().saveProducts();
                return true;
            });
        }).getOptions().setVisibilityPolicy(predicate);
    }
}
