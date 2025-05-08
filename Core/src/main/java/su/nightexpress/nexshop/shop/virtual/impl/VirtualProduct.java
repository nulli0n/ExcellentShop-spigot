package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.product.StockData;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class VirtualProduct extends AbstractProduct<VirtualShop> implements Writeable {

    private StockValues stockValues;
    private StockValues limitValues;
    private Set<String> allowedRanks;
    private Set<String> requiredPermissions;
    private Set<String> forbiddenPermissions;

    private boolean discountAllowed;
    private boolean rotating;
    private int     shopSlot;
    private int     shopPage;

    public VirtualProduct(@NotNull ShopPlugin plugin,
                          @NotNull String id,
                          @NotNull VirtualShop shop,
                          @NotNull Currency currency,
                          @NotNull ProductTyping type) {
        super(plugin, id, shop, currency, type);
        this.allowedRanks = new HashSet<>();
        this.requiredPermissions = new HashSet<>();
        this.forbiddenPermissions = new HashSet<>();
        this.stockValues = StockValues.unlimited();
        this.limitValues = StockValues.unlimited();
    }

    public void load(@NotNull FileConfig config, @NotNull String path) {
        this.setRotating(config.getBoolean(path + ".Rotating"));
        this.setAllowedRanks(config.getStringSet(path + ".Allowed_Ranks"));
        this.setRequiredPermissions(config.getStringSet(path + ".Required_Permissions"));
        this.setForbiddenPermissions(config.getStringSet(path + ".Forbidden_Permissions"));
        this.setPricer(AbstractProductPricer.read(config, path + ".Price"));
        this.setStockValues(StockValues.read(config, path + ".Stock.GLOBAL"));
        this.setLimitValues(StockValues.read(config, path + ".Stock.PLAYER"));
        this.setSlot(config.getInt(path + ".Shop_View.Slot", -1));
        this.setPage(config.getInt(path + ".Shop_View.Page", -1));
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Type", this.type.type().name());
        config.set(path, this.type);

        config.set(path + ".Rotating", this.rotating);
        config.set(path + ".Allowed_Ranks", this.allowedRanks);
        config.set(path + ".Required_Permissions", this.requiredPermissions);
        config.set(path + ".Forbidden_Permissions", this.forbiddenPermissions);
        if (!this.currency.isDummy()) {
            config.set(path + ".Currency", this.currency.getInternalId());
        }
        this.pricer.write(config, path + ".Price");
        this.stockValues.write(config, path + ".Stock.GLOBAL");
        this.limitValues.write(config, path + ".Stock.PLAYER");
        config.set(path + ".Shop_View.Slot", this.shopSlot);
        config.set(path + ".Shop_View.Page", this.shopPage);
    }

    public void save() {
        this.shop.saveProduct(this);
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forVirtualProduct(this, player);
    }

    @Override
    @NotNull
    public VirtualPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new VirtualPreparedProduct(this.plugin, player, this, buyType, all);
    }

    @Override
    protected double applyPriceModifiers(@NotNull TradeType tradeType, double currentPrice, @Nullable Player player) {
        if (tradeType == TradeType.BUY && currentPrice > 0 && this.isDiscountAllowed()) {
            currentPrice *= this.shop.getDiscountModifier();
        }
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
            .map(rotation -> plugin.getDataManager().getRotationData(rotation)).filter(Objects::nonNull)
            .anyMatch(data -> data.containsProduct(this.getId()));
    }

    public boolean hasRequiredPermissions() {
        return !this.requiredPermissions.isEmpty();
    }

    public boolean hasForbiddenPermissions() {
        return !this.forbiddenPermissions.isEmpty();
    }

    public boolean hasRequiredPermission(@NotNull Player player) {
        return this.hasListedPermissions(player, this.requiredPermissions);
    }

    public boolean hasForbiddenPermission(@NotNull Player player) {
        return this.hasListedPermissions(player, this.forbiddenPermissions);
    }

    private boolean hasListedPermissions(@NotNull Player player, @NotNull Set<String> set) {
        return set.stream().anyMatch(player::hasPermission);
    }

    public boolean hasRequiredRanks(@NotNull Player player) {
        Set<String> groups = Players.getPermissionGroups(player);
        return this.allowedRanks.isEmpty() || this.allowedRanks.stream().anyMatch(groups::contains);
    }

    public boolean hasAccess(@NotNull Player player) {
        if (!this.hasRequiredRanks(player)) return false;
        if (this.hasRequiredPermissions() && !this.hasRequiredPermission(player)) return false;
        if (this.hasForbiddenPermissions() && this.hasForbiddenPermission(player)) return false;

        if (this.isRotating()) {
            return this.isInRotation();
        }

        return true;
    }

    @Override
    public boolean isAvailable(@NotNull Player player) {
        if (!this.hasAccess(player)) {
            Lang.ERROR_NO_PERMISSION.getMessage(this.plugin).send(player);
            return false;
        }

        return this.isBuyable() || this.isSellable();
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        int inStock = this.countStock(tradeType, null);
        int inLimit = this.countStock(tradeType, player.getUniqueId());

        // If both, stock & limit, are present, return minimal of them.
        if (inStock >= 0 && inLimit >= 0) return Math.min(inStock, inLimit);

        // In other case return the one that is not unlimited.
        return inStock < 0 ? inLimit : inStock;
    }

    @NotNull
    private StockData getStockData(@Nullable UUID playerId) {
        return plugin.getDataManager().getStockDataOrCreate(this, playerId); // Already restocked if needed.
    }

    @Override
    public int countStock(@NotNull TradeType type, @Nullable UUID playerId) {
        StockValues values = this.getStocksOrLimits(playerId);
        if (values.isUnlimited(type)) return -1;

        StockData data = this.getStockData(playerId);
        //data.restockIfReady(values);

        return data.countStock(type);
    }

    @Override
    public boolean consumeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId) {
        StockValues values = this.getStocksOrLimits(playerId);
        if (values.isUnlimited(type)) return false;

        StockData data = this.getStockData(playerId);
        //data.restockIfReady(values);
        data.consumeStock(type, amount);
        data.startRestockIfAbsent(values);
        data.setSaveRequired(true);
        return true;
    }

    @Override
    public boolean storeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId) {
        StockValues values = this.getStocksOrLimits(playerId);
        if (values.isUnlimited(type)) return false;

        StockData data = this.getStockData(playerId);
        //data.restockIfReady(values);
        data.fillStock(type, amount);
        data.startRestockIfAbsent(values);
        data.setSaveRequired(true);
        return true;
    }

    @Override
    public boolean restock(@NotNull TradeType type, boolean force, @Nullable UUID playerId) {
        StockValues values = this.getStocksOrLimits(playerId);
        if (values.isUnlimited(type)) return false;

        StockData data = this.getStockData(playerId);
        if (force || data.isRestockTime()) {
            data.restock(values);
            data.setSaveRequired(true);
            return true;
        }
        return false;
    }

    @Override
    public long getRestockDate(@Nullable UUID playerId) {
        StockValues values = this.getStocksOrLimits(playerId);
        if (values.getRestockTime() == 0L) return 0L;

        StockData data = this.getStockData(playerId);
        return data.getRestockDate();
    }

    @NotNull
    public StockValues getStocksOrLimits(@Nullable UUID playerId) {
        return playerId == null ? this.stockValues : this.limitValues;
    }

    @NotNull
    public StockValues getStockValues() {
        return this.stockValues;
    }

    public void setStockValues(@NotNull StockValues stockValues) {
        this.stockValues = stockValues;
    }

    @NotNull
    public StockValues getLimitValues() {
        return this.limitValues;
    }

    public void setLimitValues(@NotNull StockValues limitValues) {
        this.limitValues = limitValues;
    }

    @NotNull
    public Set<String> getAllowedRanks() {
        return this.allowedRanks;
    }

    public void setAllowedRanks(@NotNull Set<String> allowedRanks) {
        this.allowedRanks = Lists.modify(allowedRanks, String::toLowerCase);
    }

    @NotNull
    public Set<String> getRequiredPermissions() {
        return this.requiredPermissions;
    }

    public void setRequiredPermissions(@NotNull Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    @NotNull
    public Set<String> getForbiddenPermissions() {
        return this.forbiddenPermissions;
    }

    public void setForbiddenPermissions(@NotNull Set<String> forbiddenPermissions) {
        this.forbiddenPermissions = forbiddenPermissions;
    }

    public boolean isDiscountAllowed() {
        return this.discountAllowed;
    }

    public void setDiscountAllowed(boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
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
