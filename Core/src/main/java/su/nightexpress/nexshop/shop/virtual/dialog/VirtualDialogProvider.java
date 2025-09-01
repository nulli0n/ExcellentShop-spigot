package su.nightexpress.nexshop.shop.virtual.dialog;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.dialog.AbstractDialogProvider;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public abstract class VirtualDialogProvider<T> extends AbstractDialogProvider<T> {

    protected static final String TITLE_PREFIX = SOFT_AQUA.and(BOLD).wrap("SHOP EDITOR") + DARK_GRAY.wrap(" » ");

    protected final VirtualShopModule module;

    public VirtualDialogProvider(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin);
        this.module = module;
    }
}
