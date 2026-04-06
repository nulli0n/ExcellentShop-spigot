package su.nightexpress.excellentshop.feature.virtualshop.product;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.limit.LimitData;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.shop.data.ProductLimitData;
import su.nightexpress.excellentshop.shop.data.ProductPriceData;
import su.nightexpress.excellentshop.shop.data.ProductStockData;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.excellentshop.product.AbstractProduct;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VirtualProduct extends AbstractProduct<VirtualShop> implements Writeable {

    private final DataManager dataManager;

    private StockOptions stockOptions;
    private LimitOptions limitOptions;

    private Set<String>  allowedRanks;
    private Set<String> forbiddenRanks;

    private Set<String> requiredPermissions;
    private Set<String> forbiddenPermissions;

    private boolean rotating;
    private int     shopSlot;
    private int     shopPage;

    public VirtualProduct(@NonNull DataManager dataManager, @NonNull UUID globalId, @NonNull String id, @NonNull VirtualShop shop) {
        super(globalId, id, shop);
        this.dataManager = dataManager;
        this.allowedRanks = new HashSet<>();
        this.forbiddenRanks = new HashSet<>();
        this.requiredPermissions = new HashSet<>();
        this.forbiddenPermissions = new HashSet<>();
        this.stockOptions = new StockOptions(false, 0, 0, 0, 0L);
        this.limitOptions = new LimitOptions(false, -1, -1, 0L);
    }

    public void load(@NonNull FileConfig config, @NonNull String path) throws ProductLoadException {
        // Legacy stuff
        if (!config.contains(path + ".Handler")) {
            config.set(path + ".Handler", "bukkit_item");
        }

        if (!config.contains(path + ".Type")) {
            String handlerId = config.getString(path + ".Handler", "bukkit_item");
            if (handlerId.equalsIgnoreCase("bukkit_command")) {
                config.set(path + ".Type", ContentType.COMMAND.name());
            }
            else if (handlerId.equalsIgnoreCase("bukkit_item")) {
                config.set(path + ".Type", ContentType.ITEM.name());
            }
        }
        // Legacy end

        ContentType contentType = config.getEnum(path + ".Type", ContentType.class, ContentType.ITEM);
        ProductContent content = ContentTypes.read(contentType, config, path);
        if (content == null) throw new ProductLoadException("Invalid item data");

        this.setCurrencyId(CurrencyId.reroute(config.getString(path + ".Currency", CurrencyId.VAULT)));
        this.setContent(content);
        this.setPricing(ProductPricing.read(config, path + ".Price"));

        this.setRotating(config.getBoolean(path + ".Rotating"));
        this.setAllowedRanks(config.getStringSet(path + ".Allowed_Ranks"));
        this.setForbiddenRanks(config.getStringSet(path + ".Forbidden_Ranks"));
        this.setRequiredPermissions(config.getStringSet(path + ".Required_Permissions"));
        this.setForbiddenPermissions(config.getStringSet(path + ".Forbidden_Permissions"));

        // LEGACY - START
        if (config.contains(path + ".Stock.GLOBAL")) {
            StockOptions options = StockOptions.read(config, path + ".Stock.GLOBAL");
            config.set(path + ".GlobalStock", options);
            config.remove(path + ".Stock.GLOBAL");
        }

        if (config.contains(path + ".Stock.PLAYER")) {
            LimitOptions options = LimitOptions.read(config, path + ".Stock.PLAYER");
            config.set(path + ".PlayerLimits", options);
            config.remove(path + ".Stock.PLAYER");
        }
        // LEGACY - END

        this.setStockOptions(StockOptions.read(config, path + ".GlobalStock"));
        this.setLimitOptions(LimitOptions.read(config, path + ".PlayerLimits"));
        this.setSlot(config.getInt(path + ".Shop_View.Slot", -1));
        this.setPage(config.getInt(path + ".Shop_View.Page", -1));
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".GlobalId", this.globalId);
        config.set(path + ".Type", this.content.type().name());
        config.set(path, this.content);

        config.set(path + ".Rotating", this.rotating);
        config.set(path + ".Allowed_Ranks", this.allowedRanks);
        config.set(path + ".Forbidden_Ranks", this.forbiddenRanks);
        config.set(path + ".Required_Permissions", this.requiredPermissions);
        config.set(path + ".Forbidden_Permissions", this.forbiddenPermissions);
        config.set(path + ".Currency", this.currencyId);
        this.pricing.write(config, path + ".Price");
        config.set(path + ".GlobalStock", this.stockOptions);
        config.set(path + ".PlayerLimits", this.limitOptions);
        config.set(path + ".Shop_View.Slot", this.shopSlot);
        config.set(path + ".Shop_View.Page", this.shopPage);
    }

    @Override
    public void invalidateData() {
        StockData stockData = this.dataManager.getStockData(this.globalId);
        PriceData priceData = this.dataManager.getPriceData(this.globalId);

        if (stockData != null) stockData.markRemoved();
        if (priceData != null) priceData.markRemoved();

        this.invalidatePlayerLimits();
    }

    public void invalidatePlayerLimits() {
        this.dataManager.getLimitDatas(this.globalId).forEach(StatefulData::markRemoved);
    }

    @Override
    @NonNull
    public PlaceholderResolver placeholders() {
        return ShopPlaceholders.VIRTUAL_PRODUCT_TYPED.resolver(this);
    }

    @Override
    public void handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction, int units) {
        Player player = transaction.player();
        TradeType tradeType = transaction.type();

        StockData stockData = this.getStockData();
        LimitData limitData = this.getLimitData(player);

        if (tradeType == TradeType.BUY) {
            stockData.consume(units);
            limitData.addPurchases(units);
        }
        else {
            stockData.store(units);
            limitData.addSales(units);
        }

        if (limitData.getRestockDate() <= 0L) {
            limitData.setRestockDate(this.limitOptions.generateRestockTimestamp());
        }

        stockData.markDirty();
        limitData.markDirty();
    }

    @Override
    protected double applyPriceModifiers(@NonNull TradeType tradeType, double currentPrice, @Nullable Player player) {
        if (tradeType == TradeType.SELL) {
            if (player != null) {
                double sellModifier = VirtualShopModule.getSellMultiplier(player);
                currentPrice *= sellModifier;
            }
        }

        return currentPrice;
    }

    public boolean isInRotation() {
        return this.shop.getRotations().stream().filter(rotation -> rotation.hasProduct(this))
            .map(rotation -> this.shop.getRotationData(rotation))
            .anyMatch(data -> data.containsProduct(this.getId()));
    }

    public boolean matchPermissionConditions(@NonNull Player player) {
        if (!this.forbiddenPermissions.isEmpty()) {
            if (this.forbiddenPermissions.stream().anyMatch(player::hasPermission)) return false;
        }

        if (!this.requiredPermissions.isEmpty()) {
            return this.requiredPermissions.stream().anyMatch(player::hasPermission);
        }

        return true;
    }

    public boolean matchRankConditions(@NonNull Player player) {
        Set<String> groups = Players.getInheritanceGroups(player);
        if (!this.forbiddenRanks.isEmpty()) {
            if (groups.stream().anyMatch(this.forbiddenRanks::contains)) return false;
        }

        if (!this.allowedRanks.isEmpty()) {
            return groups.stream().anyMatch(this.allowedRanks::contains);
        }

        return true;
    }

    @Override
    public boolean canTrade(@NonNull Player player) {
        if (!this.matchRankConditions(player)) return false;
        if (!this.matchPermissionConditions(player)) return false;

        if (this.isRotating()) {
            return this.isInRotation();
        }

        return true;
    }

    @Override
    @NonNull
    public StockData getStockData() {
        if (!this.stockOptions.isEnabled()) return StockData.UNLIMITED;

        ProductStockData data = this.dataManager.getStockData(this);
        if (data != null && !data.isRemoved()) {
            if (data.isRestockTime()) {
                data.setStock(this.stockOptions.generateInitialAmount());
                data.setRestockDate(this.stockOptions.generateRestockTimestamp());
                data.markDirty();
            }
            return data;
        }

        int initial = this.stockOptions.generateInitialAmount();
        long restockDate = this.stockOptions.generateRestockTimestamp();

        ProductStockData stockData = new ProductStockData(this.globalId, initial, restockDate);
        stockData.markDirty(); // Signal to save (insert) to the database
        this.dataManager.loadStockData(stockData);
        return stockData;
    }

    @Override
    @NonNull
    public LimitData getLimitData(@NonNull Player player) {
        if (!this.limitOptions.isEnabled()) return LimitData.EMPTY;

        LimitData data = this.dataManager.getLimitData(player, this);
        if (data != null && !data.isRemoved()) {
            if (data.isRestockTime()) {
                data.reset(); // Do not set restock date until purchase.
                data.markDirty();
            }
            return data;
        }

        ProductLimitData limitData = new ProductLimitData(player.getUniqueId(), this.globalId, 0, 0, -1L);
        limitData.markDirty(); // Signal to save (insert) to the database
        this.dataManager.loadLimitData(limitData);
        return limitData;
    }

    @NonNull
    public PriceData getPriceData() {
        if (this.getPricingType() == PriceType.FLAT || this.getPricingType() == PriceType.PLAYER_AMOUNT) return PriceData.EMPTY;

        PriceData data = this.dataManager.getPriceData(this);
        if (data != null && !data.isRemoved()) {
            if (data.isExpired() && this.pricing.shouldResetOnExpire()) {
                data.reset();
                data.markDirty();
            }
            return data;
        }

        ProductPriceData priceData = new ProductPriceData(this.globalId, 0, 0, 0, 0, 0);
        priceData.markDirty(); // Signal to save (insert) to the database
        this.dataManager.loadPriceData(priceData);
        return priceData;
    }

    @Override
    public int getStock() {
        return this.getStockData().getStock();
    }

    @Override
    public int getCapacity() {
        return this.stockOptions.isEnabled() ? this.stockOptions.getCapacity() : -1;
    }

    @Override
    public int getSpace() {
        int capacity = this.getCapacity();
        if (capacity < 0) return -1;

        return capacity - this.getStock();
    }

    @Override
    public int getTradeLimit(@NonNull TradeType type) {
        return switch (type) {
            case BUY -> this.getBuyLimit();
            case SELL -> this.getSellLimit();
        };
    }

    @Override
    public int getBuyLimit() {
        return this.limitOptions.isEnabled() ? this.limitOptions.getBuyLimit() : -1;
    }

    @Override
    public int getSellLimit() {
        return this.limitOptions.isEnabled() ? this.limitOptions.getSellLimit() : -1;
    }

    @NonNull
    public StockOptions getStockOptions() {
        return this.stockOptions;
    }

    public void setStockOptions(@NonNull StockOptions stockOptions) {
        this.stockOptions = stockOptions;
    }

    @NonNull
    public LimitOptions getLimitOptions() {
        return this.limitOptions;
    }

    public void setLimitOptions(@NonNull LimitOptions limitOptions) {
        this.limitOptions = limitOptions;
    }

    @NonNull
    public Set<String> getAllowedRanks() {
        return this.allowedRanks;
    }

    public void setAllowedRanks(@NonNull Collection<String> allowedRanks) {
        this.allowedRanks = Lists.modify(new HashSet<>(allowedRanks), String::toLowerCase);
    }

    @NonNull
    public Set<String> getForbiddenRanks() {
        return this.forbiddenRanks;
    }

    public void setForbiddenRanks(@NonNull Collection<String> forbiddenRanks) {
        this.forbiddenRanks = Lists.modify(new HashSet<>(forbiddenRanks), String::toLowerCase);
    }

    @NonNull
    public Set<String> getRequiredPermissions() {
        return this.requiredPermissions;
    }

    public void setRequiredPermissions(@NonNull Collection<String> requiredPermissions) {
        this.requiredPermissions = new HashSet<>(requiredPermissions);
    }

    @NonNull
    public Set<String> getForbiddenPermissions() {
        return this.forbiddenPermissions;
    }

    public void setForbiddenPermissions(@NonNull Collection<String> forbiddenPermissions) {
        this.forbiddenPermissions = new HashSet<>(forbiddenPermissions);
    }

    public boolean isRotating() {
        return this.rotating;
    }

    public void setRotating(boolean rotating) {
        this.rotating = rotating;
    }

    public int getSlot() {
        return this.shopSlot;
    }

    public void setSlot(int slot) {
        this.shopSlot = slot;
    }

    public int getPage() {
        return this.shopPage;
    }

    public void setPage(int page) {
        this.shopPage = page;
    }
}
