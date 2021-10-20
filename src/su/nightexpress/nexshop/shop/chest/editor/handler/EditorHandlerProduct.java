package su.nightexpress.nexshop.shop.chest.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorInputHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EditorHandlerProduct extends ChestEditorInputHandler<IShopChestProduct> {

    public EditorHandlerProduct(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public boolean onType(
            @NotNull Player player, @NotNull IShopChestProduct product,
            @NotNull ChestEditorType type, @NotNull String msg) {

        switch (type) {
            //case PRODUCT_CHANGE_COMMANDS -> product.getCommands().add(StringUT.colorRaw(msg));
            case PRODUCT_CHANGE_CURRENCY -> {
                String id = StringUT.colorOff(msg);
                IShopCurrency currency = plugin.getCurrencyManager().getCurrency(id);
                if (currency == null) {
                    EditorUtils.errorCustom(player, plugin.lang().Virtual_Shop_Editor_Product_Error_Currency.getMsg());
                    return false;
                }
                if (!currency.hasOfflineSupport() || !ChestShopConfig.ALLOWED_CURRENCIES.contains(currency.getId())) {
                    EditorUtils.errorCustom(player, plugin.lang().Chest_Shop_Editor_Error_Currency_NotAllowed.getMsg());
                    return false;
                }

                product.setCurrency(currency);
            }
            case PRODUCT_CHANGE_PRICE_SELL_MIN, PRODUCT_CHANGE_PRICE_SELL_MAX, PRODUCT_CHANGE_PRICE_BUY_MAX, PRODUCT_CHANGE_PRICE_BUY_MIN -> {
                double price = StringUT.getDouble(StringUT.colorOff(msg), -99, true);
                if (price == -99) {
                    EditorUtils.errorNumber(player, false);
                    return false;
                }

                if (price < 0 && !player.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                    EditorUtils.errorCustom(player, plugin.lang().Chest_Shop_Editor_Error_Negative.getMsg());
                    return false;
                }

                if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                    product.getPricer().setPriceMin(TradeType.BUY, price);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                    product.getPricer().setPriceMax(TradeType.BUY, price);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX) {
                    product.getPricer().setPriceMax(TradeType.SELL, price);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN) {
                    product.getPricer().setPriceMin(TradeType.SELL, price);
                }
            }
            case PRODUCT_CHANGE_PRICE_RND_TIME_DAY -> {
                DayOfWeek day = CollectionsUT.getEnum(msg, DayOfWeek.class);
                if (day == null) {
                    EditorUtils.errorEnum(player, DayOfWeek.class);
                    return false;
                }
                product.getPricer().getDays().add(day);
            }
            case PRODUCT_CHANGE_PRICE_RND_TIME_TIME -> {
                String[] raw = msg.split(" ");
                LocalTime[] times = new LocalTime[raw.length];

                for (int count = 0; count < raw.length; count++) {
                    String[] split = raw[count].split(":");
                    int hour = StringUT.getInteger(split[0], 0);
                    int minute = StringUT.getInteger(split.length >= 2 ? split[1] : "0", 0);
                    times[count] = LocalTime.of(hour, minute);
                }
                if (times.length < 2) return false;

                product.getPricer().getTimes().add(times);
            }
            default -> {}
        }

        product.getShop().save();
        return true;
    }
}
