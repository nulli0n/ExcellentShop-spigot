package su.nightexpress.nexshop.api.shop.virtual;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nightexpress.nexshop.api.shop.AbstractShopView;
import su.nightexpress.nexshop.api.shop.IShop;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopMain;

import java.util.Collection;
import java.util.Map;

public interface IShopVirtual extends IShop, ICleanable {

    @Override
    @NotNull
    EditorShopMain getEditor();

    @Override
    @NotNull
    AbstractShopView<IShopVirtual> getView();

    boolean isPermissionRequired();

    void setPermissionRequired(boolean isPermission);

    boolean hasPermission(@NotNull Player player);

    @NotNull
    ItemStack getIcon();

    void setIcon(@NotNull ItemStack icon);

    int getPages();

    void setPages(int pages);

    int[] getCitizensIds();

    void setCitizensIds(int[] npcIds);

    @NotNull
    @Override
    Map<String, IProductVirtual> getProductMap();

    @Override
    @NotNull
    Collection<IProductVirtual> getProducts();

    @Nullable IProductVirtual getProductById(@NotNull String id);
}
