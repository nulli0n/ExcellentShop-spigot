package su.nightexpress.nexshop.shop.virtual.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorInputHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EditorHandlerDiscount extends VirtualEditorInputHandler<IShopDiscount> {

    private final VirtualShop virtualShop;

    public EditorHandlerDiscount(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin());
        this.virtualShop = virtualShop;
    }

    @Override
    public boolean onType(
            @NotNull Player player, @Nullable IShopDiscount discount,
            @NotNull VirtualEditorType type, @NotNull String msg) {

        if (discount == null) return true;

        switch (type) {
            case DISCOUNT_CHANGE_DISCOUNT -> {
                double value = StringUT.getDouble(StringUT.colorOff(msg), 0);
                discount.setDiscount(value);
            }
            case DISCOUNT_CHANGE_DAY -> {
                DayOfWeek day = CollectionsUT.getEnum(msg, DayOfWeek.class);
                if (day == null) {
                    EditorUtils.errorEnum(player, DayOfWeek.class);
                    return false;
                }
                discount.getDays().add(day);
            }
            case DISCOUNT_CHANGE_TIME -> {
                String[] raw = msg.split(" ");
                LocalTime[] times = new LocalTime[raw.length];

                for (int count = 0; count < raw.length; count++) {
                    String[] split = raw[count].split(":");
                    int hour = StringUT.getInteger(split[0], 0);
                    int minute = StringUT.getInteger(split.length >= 2 ? split[1] : "0", 0);
                    times[count] = LocalTime.of(hour, minute);
                }
                if (times.length < 2) return false;

                discount.getTimes().add(times);
            }
            default -> {}
        }

        // Find that shop discount comparing by memory address
        IShopVirtual shop = this.virtualShop.getShops().stream().filter(shop2 -> {
            return shop2.getDiscounts().stream().anyMatch(d -> d == discount);
        }).findFirst().orElse(null);
        if (shop == null) return true;

        shop.save();
        return true;
    }
}
