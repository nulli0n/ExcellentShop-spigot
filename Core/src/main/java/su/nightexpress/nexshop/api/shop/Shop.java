package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.Discount;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class Shop<
    S extends Shop<S, P>,
    P extends Product<P, S, ?>> extends AbstractConfigHolder<ExcellentShop> implements IEditable, IPlaceholder {

    protected final Set<Discount>           discounts;
    protected final Map<TradeType, Boolean> transactions;
    protected final Map<String, P>          products;

    protected String      name;
    protected ShopBank<S> bank;

    public Shop(@NotNull ExcellentShop plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.discounts = new HashSet<>();
        this.transactions = new HashMap<>();
        this.products = new LinkedHashMap<>();
    }

    public Shop(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull String id) {
        super(plugin, cfg, id);
        this.discounts = new HashSet<>();
        this.transactions = new HashMap<>();
        this.products = new LinkedHashMap<>();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.SHOP_ID, this.getId())
            .replace(Placeholders.SHOP_NAME, this.getName())
            .replace(Placeholders.SHOP_BUY_ALLOWED, LangManager.getBoolean(this.isTransactionEnabled(TradeType.BUY)))
            .replace(Placeholders.SHOP_SELL_ALLOWED, LangManager.getBoolean(this.isTransactionEnabled(TradeType.SELL)))
            .replace(Placeholders.SHOP_DISCOUNT_AMOUNT, NumberUtil.format(this.getDiscountPlain()))
            ;
    }

    public abstract boolean canAccess(@NotNull Player player, boolean notify);

    public void open(@NotNull Player player, int page) {
        if (!this.canAccess(player, true)) {
            return;
        }
        this.getView().open(player, page);
    }

    @Override
    public void save() {
        super.save();
    }

    @NotNull
    protected abstract S get();

    @NotNull
    public abstract ShopView<? extends Shop<?, ?>> getView();

    public abstract void setupView();

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        this.name = StringUtil.color(name);
    }

    public boolean isTransactionEnabled(@NotNull TradeType tradeType) {
        return this.transactions.getOrDefault(tradeType, true);
    }

    public void setTransactionEnabled(@NotNull TradeType tradeType, boolean enabled) {
        this.transactions.put(tradeType, enabled);
    }

    @NotNull
    public ShopBank<S> getBank() {
        if (this.bank == null) {
            throw new IllegalStateException("Bank is undefined!");
        }
        return this.bank;
    }

    public void setBank(@NotNull ShopBank<S> bank) {
        this.bank = bank;
    }

    // TODO Add per rank (Player arg)
    @NotNull
    public Set<Discount> getDiscounts() {
        this.discounts.removeIf(Discount::isExpired);
        return this.discounts;
    }

    public boolean hasDiscount() {
        return this.getDiscountPlain() != 0D;
    }

    public boolean hasDiscount(@NotNull Product<?, ?, ?> product) {
        return this.getDiscountPlain(product) != 0D;
    }

    public double getDiscountModifier() {
        return 1D - this.getDiscountPlain() / 100D;
    }

    public double getDiscountModifier(@NotNull Product<?, ?, ?> product) {
        return 1D - this.getDiscountPlain(product) / 100D;
    }

    public double getDiscountPlain() {
        return Math.min(100D, this.getDiscounts().stream().mapToDouble(Discount::getDiscountPlain).sum());
    }

    public double getDiscountPlain(@NotNull Product<?, ?, ?> product) {
        return product.isDiscountAllowed() ? this.getDiscountPlain() : 0D;
    }

    public void addProduct(@NotNull P product) {
        this.removeProduct(product.getId());
        this.getProductMap().put(product.getId(), product);
        product.setShop(this.get());
    }

    public void removeProduct(@NotNull P product) {
        this.removeProduct(product.getId());
    }

    public void removeProduct(@NotNull String id) {
        P product = this.getProductMap().remove(id);
        if (product != null) {
            product.clear();
        }
    }

    @NotNull
    protected Map<String, P> getProductMap() {
        return this.products;
    }

    @NotNull
    public Collection<P> getProducts() {
        return this.getProductMap().values();
    }

    @Nullable
    public P getProductById(@NotNull String id) {
        return this.getProductMap().get(id.toLowerCase());
    }

    public boolean isProduct(@NotNull Product<?, ?, ?> product) {
        return this.getProductMap().containsKey(product.getId());
    }

    public boolean isProduct(@NotNull ItemStack item) {
        return this.getProducts().stream().anyMatch(product -> product.isItemMatches(item));
    }
}
