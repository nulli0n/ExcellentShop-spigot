package su.nightexpress.nexshop.api.virtual;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.AbstractShopView;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopMain;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public interface IShopVirtual extends IShop, ICleanable {

    String PLACEHOLDER_PERMISSION_REQUIRED = "%shop_permission_required%";
    String PLACEHOLDER_PERMISSION_NODE     = "%shop_permission_node%";
    String PLACEHOLDER_ICON = "%shop_icon_name%";
    String PLACEHOLDER_PAGES = "%shop_pages%";
    String PLACEHOLDER_VIEW_SIZE = "%shop_view_size%";
    String PLACEHOLDER_VIEW_TITLE = "%shop_view_title%";
    String PLACEHOLDER_NPC_IDS = "%shop_npc_ids%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IShop.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_PERMISSION_NODE, Perms.VIRTUAL_SHOP + this.getId())
                .replace(PLACEHOLDER_PERMISSION_REQUIRED, plugin().lang().getBool(this.isPermissionRequired()))
                .replace(PLACEHOLDER_ICON, ItemUT.getItemName(this.getIcon()))
                .replace(PLACEHOLDER_PAGES, String.valueOf(this.getPages()))
                .replace(PLACEHOLDER_VIEW_SIZE, String.valueOf(this.getView().getSize()))
                .replace(PLACEHOLDER_VIEW_TITLE,this.getView().getTitle())
                .replace(PLACEHOLDER_NPC_IDS, String.join(", ", IntStream.of(this.getCitizensIds()).boxed()
                        .map(String::valueOf).toList()))
        );
    }

    @Override
    @NotNull
    EditorShopMain getEditor();

    @Override
    @NotNull
    AbstractShopView<IShopVirtual> getView();

    boolean isPermissionRequired();

    void setPermissionRequired(boolean isPermission);

    default boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;
        return player.hasPermission(Perms.VIRTUAL_SHOP + this.getId());
    }

    @Override
    default double getShopBalance(@NotNull IShopCurrency currency) {
        return -1D;
    }

    @Override
    default void takeFromShopBalance(@NotNull IShopCurrency currency, double amount) {

    }

    @Override
    default void addToShopBalance(@NotNull IShopCurrency currency, double amount) {

    }

    @NotNull
    ItemStack getIcon();

    void setIcon(@NotNull ItemStack icon);

    int getPages();

    void setPages(int pages);

    int[] getCitizensIds();

    void setCitizensIds(int[] npcIds);

    @NotNull
    @Override
    Map<String, IShopVirtualProduct> getProductMap();

    @Override
    @NotNull
    default Collection<IShopVirtualProduct> getProducts() {
        return this.getProductMap().values();
    }
}
