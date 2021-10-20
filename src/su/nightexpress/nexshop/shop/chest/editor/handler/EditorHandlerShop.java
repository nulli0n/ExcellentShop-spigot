package su.nightexpress.nexshop.shop.chest.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorInputHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

public class EditorHandlerShop extends ChestEditorInputHandler<IShopChest> {

    public EditorHandlerShop(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IShopChest shop,
            @NotNull ChestEditorType type, @NotNull String msg) {

        switch (type) {
            case SHOP_CHANGE_NAME -> {
                shop.setName(msg);
                shop.updateDisplayText();
            }
            default -> { }
        }

        shop.save();
        return true;
    }

}
