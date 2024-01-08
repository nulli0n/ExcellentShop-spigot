package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractShop<P extends AbstractProduct<?>> extends AbstractConfigHolder<ExcellentShop> implements Shop, Placeholder {

    protected final ShopDataPricer          pricer;
    protected final Map<TradeType, Boolean> transactions;
    protected final Map<String, P>          products;
    protected final PlaceholderMap          placeholderMap;

    protected String name;

    public AbstractShop(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull String id) {
        super(plugin, cfg, id);
        this.pricer = new ShopDataPricer(plugin, this);
        this.transactions = new HashMap<>();
        this.products = new LinkedHashMap<>();
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.SHOP_ID, this::getId)
            .add(Placeholders.SHOP_NAME, this::getName)
            .add(Placeholders.SHOP_BUY_ALLOWED, () -> LangManager.getBoolean(this.isTransactionEnabled(TradeType.BUY)))
            .add(Placeholders.SHOP_SELL_ALLOWED, () -> LangManager.getBoolean(this.isTransactionEnabled(TradeType.SELL)));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    public void open(@NotNull Player player, int page) {
        this.getView().open(player, page);
    }

    public void openNextTick(@NotNull Player player, int page) {
        this.getView().openNextTick(player, page);
    }

    @Override
    public void save() {
        super.save();
    }

    @NotNull
    @Override
    public ShopDataPricer getPricer() {
        return pricer;
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = Colorizer.apply(name);
    }

    @Override
    public boolean isTransactionEnabled(@NotNull TradeType tradeType) {
        return this.transactions.getOrDefault(tradeType, true);
    }

    @Override
    public void setTransactionEnabled(@NotNull TradeType tradeType, boolean enabled) {
        this.transactions.put(tradeType, enabled);
    }

    protected void addProduct(@NotNull P product) {
        this.removeProduct(product.getId());
        this.getProductMap().put(product.getId(), product);
    }

    @Override
    public final void removeProduct(@NotNull Product product) {
        this.removeProduct(product.getId());
    }

    @Override
    public void removeProduct(@NotNull String id) {
        P product = this.getProductMap().remove(id);
        if (product != null) {
            product.clear();
        }
    }

    @Override
    @NotNull
    public Map<String, P> getProductMap() {
        return this.products;
    }

    @Override
    @NotNull
    public Collection<P> getProducts() {
        return this.getProductMap().values();
    }

    @Override
    @Nullable
    public P getProductById(@NotNull String id) {
        return this.getProductMap().get(id.toLowerCase());
    }
}
