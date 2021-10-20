package su.nightexpress.nexshop.shop.virtual.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorInputHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class EditorHandlerShop extends VirtualEditorInputHandler<IShopVirtual> {

    public EditorHandlerShop(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IShopVirtual shop,
            @NotNull VirtualEditorType type, @NotNull String msg) {

        switch (type) {
            case SHOP_CHANGE_TITLE -> shop.getView().setTitle(msg);
            case SHOP_CHANGE_CITIZENS_ID -> {
                msg = StringUT.colorOff(msg);
                int input = StringUT.getInteger(msg, -1);
                if (input < 0) {
                    EditorUtils.errorNumber(player, false);
                    return false;
                }

                List<Integer> current = new ArrayList<>(IntStream.of(shop.getCitizensIds()).boxed().toList());
                if (current.contains(input)) break;
                current.add(input);

                shop.setCitizensIds(current.stream().mapToInt(i -> i).toArray());
            }
            default -> {}
        }

        shop.save();
        return true;
    }
}
