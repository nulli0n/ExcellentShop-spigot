package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.AbstractProduct;
import su.nightexpress.nexshop.api.shop.IProductPricer;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.data.object.UserProductLimit;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopProduct;

import java.util.*;
import java.util.function.UnaryOperator;

public class ProductVirtual extends AbstractProduct<IShopVirtual> implements IProductVirtual {

    private       int                   shopSlot;
    private       int                   shopPage;
    private final Map<TradeType, int[]> limit;
    private       List<String>          commands;

    private EditorShopProduct editor;

    public ProductVirtual(@NotNull IShopVirtual shop, @NotNull ICurrency currency,
                          @NotNull ItemStack item, int slot, int page) {
        this(
            shop, UUID.randomUUID().toString(),
            item, item,
            currency, new ProductPricer(),
            true, true, slot, page,
            new HashMap<>(),
            new ArrayList<>()
        );
    }

    public ProductVirtual(
        @NotNull IShopVirtual shop,
        @NotNull String id,

        @NotNull ItemStack preview,
        @Nullable ItemStack item,

        @NotNull ICurrency currency,
        @NotNull IProductPricer pricer,

        boolean isDiscountAllowed,
        boolean isItemMetaEnabled,

        int shopSlot,
        int shopPage,
        @NotNull Map<TradeType, int[]> limit,
        @NotNull List<String> commands) {
        super(shop, id, preview, item, currency, pricer, isDiscountAllowed, isItemMetaEnabled);

        this.setSlot(shopSlot);
        this.setPage(shopPage);

        this.limit = limit;
        this.setCommands(commands);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
            .replace(Placeholders.PRODUCT_VIRTUAL_COMMANDS, String.join(DELIMITER_DEFAULT, this.getCommands()))
        );
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholdersView() {
        return str -> super.replacePlaceholdersView().apply(str
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_BUY_AMOUNT, String.valueOf(this.getLimitAmount(TradeType.BUY)))
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_BUY_COOLDOWN, TimeUtil.formatTime(this.getLimitCooldown(TradeType.BUY) * 1000L))
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_SELL_AMOUNT, String.valueOf(this.getLimitAmount(TradeType.SELL)))
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_SELL_COOLDOWN, TimeUtil.formatTime(this.getLimitCooldown(TradeType.SELL) * 1000L))
        );
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        long resBuy = this.getStockResetTime(player, TradeType.BUY);
        long resSell = this.getStockResetTime(player, TradeType.SELL);

        if (resBuy == 0) resBuy = this.getLimitCooldown(TradeType.BUY) * 1000L;
        if (resSell == 0) resSell = this.getLimitCooldown(TradeType.SELL) * 1000L;

        String never = this.getShop().plugin().getMessage(Lang.OTHER_NEVER).getLocalized();
        String restockBuy = resBuy < 0 ? never : TimeUtil.formatTime(resBuy);
        String restocSell = resSell < 0 ? never : TimeUtil.formatTime(resSell);

        return str -> super.replacePlaceholders(player).apply(str
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_BUY_RESET, restockBuy)
            .replace(Placeholders.PRODUCT_VIRTUAL_LIMIT_SELL_RESET, restocSell)
        );
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @Override
    @NotNull
    public EditorShopProduct getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopProduct(this.getShop().plugin(), this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    public ProductVirtualPrepared getPrepared(@NotNull TradeType buyType) {
        return new ProductVirtualPrepared(this, buyType);
    }

    @Override
    public boolean isBuyable() {
        if (this.getLimitAmount(TradeType.BUY) == 0) {
            return false;
        }
        return super.isBuyable();
    }

    @Override
    public boolean isSellable() {
        if (this.getLimitAmount(TradeType.SELL) == 0) {
            return false;
        }
        return super.isSellable();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && !this.hasCommands();
    }

    @Override
    public int getSlot() {
        return this.shopSlot;
    }

    @Override
    public void setSlot(int slot) {
        this.shopSlot = slot;
    }

    @Override
    public int getPage() {
        return this.shopPage;
    }

    @Override
    public void setPage(int page) {
        this.shopPage = page;
    }

    @Override
    @NotNull
    public List<String> getCommands() {
        return this.commands;
    }

    @Override
    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
    }

    @Override
    public int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType) {
        ShopUser user = this.getShop().plugin().getUserManager().getUserData(player);
        UserProductLimit userLimit = user.getVirtualProductLimit(tradeType, this.getId());
        return userLimit != null ? userLimit.getItemsLeft() : this.getLimitAmount(tradeType);
    }

    @Override
    public long getStockResetTime(@NotNull Player player, @NotNull TradeType tradeType) {
        if (!this.isLimitExpirable(tradeType)) return -1;

        ShopUser user = this.getShop().plugin().getUserManager().getUserData(player);
        UserProductLimit userLimit = user.getVirtualProductLimit(tradeType, this.getId());

        if (userLimit != null) {
            return Math.max(0, userLimit.getExpireDate() - System.currentTimeMillis());
        }
        return 0;
    }

    @Override
    public int getLimitAmount(@NotNull TradeType tradeType) {
        return this.limit.getOrDefault(tradeType, new int[]{-1, -1})[0];
    }

    @Override
    public void setLimitAmount(@NotNull TradeType tradeType, int amount) {
        int[] has = this.limit.getOrDefault(tradeType, new int[]{-1, -1});
        has[0] = amount;
        this.limit.put(tradeType, has);
    }

    @Override
    public int getLimitCooldown(@NotNull TradeType tradeType) {
        return this.limit.getOrDefault(tradeType, new int[]{-1, -1})[1];
    }

    @Override
    public void setLimitCooldown(@NotNull TradeType tradeType, int cooldown) {
        int[] has = this.limit.getOrDefault(tradeType, new int[]{-1, -1});
        has[1] = cooldown;
        this.limit.put(tradeType, has);
    }
}
