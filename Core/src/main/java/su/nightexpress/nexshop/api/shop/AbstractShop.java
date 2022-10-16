package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AbstractShop<S extends IProduct> extends AbstractLoadableItem<ExcellentShop> implements IShop {

    protected final Collection<IShopDiscount> discounts;
    protected final Map<TradeType, Boolean>   purchaseAllowed;

    protected String         name;
    protected Map<String, S> products;

    public AbstractShop(@NotNull ExcellentShop plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.discounts = new HashSet<>();
        this.purchaseAllowed = new HashMap<>();

        this.products = new LinkedHashMap<>();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        IShopDiscount discount = this.getDiscount();
        String hhTimeLeft; // TODO in discount class placeholder
        if (discount != null) {
            LocalTime[] times = discount.getCurrentTimes();
            if (times != null) {
                Duration dur = Duration.between(LocalTime.now(), times[1]);
                hhTimeLeft = TimeUtil.formatTime(dur.toMillis());
            }
            else hhTimeLeft = "-";
        }
        else hhTimeLeft = "-";

        List<String> bankBalance = new ArrayList<>();
        for (ICurrency currency : this instanceof IShopChest ? ChestConfig.ALLOWED_CURRENCIES : plugin.getCurrencyManager().getCurrencies()) {
            bankBalance.add(currency.format(this.getBank().getBalance(currency)));
        }

        return str -> str
            .replace(Placeholders.SHOP_ID, this.getId())
            .replace(Placeholders.SHOP_NAME, this.getName())
            .replace(Placeholders.SHOP_BUY_ALLOWED, LangManager.getBoolean(this.isPurchaseAllowed(TradeType.BUY)))
            .replace(Placeholders.SHOP_SELL_ALLOWED, LangManager.getBoolean(this.isPurchaseAllowed(TradeType.SELL)))
            .replace(Placeholders.SHOP_DISCOUNT_AVAILABLE, LangManager.getBoolean(discount != null))
            .replace(Placeholders.SHOP_DISCOUNT_AMOUNT, discount != null ? NumberUtil.format(discount.getDiscountRaw()) : "-")
            .replace(Placeholders.SHOP_DISCOUNT_TIMELEFT, hhTimeLeft)
            .replace(Placeholders.SHOP_BANK_BALANCE, String.join(DELIMITER_DEFAULT, bankBalance))
            ;
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean isPurchaseAllowed(@NotNull TradeType tradeType) {
        return this.purchaseAllowed.getOrDefault(tradeType, true);
    }

    @Override
    public void setPurchaseAllowed(@NotNull TradeType tradeType, boolean isAllowed) {
        this.purchaseAllowed.put(tradeType, isAllowed);
    }

    @Override
    @NotNull
    public Collection<IShopDiscount> getDiscounts() {
        return this.discounts;
    }

    @Override
    @NotNull
    public Map<String, S> getProductMap() {
        return this.products;
    }

    @NotNull
    @Override
    public Collection<S> getProducts() {
        return this.getProductMap().values();
    }

    @Override
    @Nullable
    public S getProductById(@NotNull String id) {
        return this.getProductMap().get(id.toLowerCase());
    }
}
