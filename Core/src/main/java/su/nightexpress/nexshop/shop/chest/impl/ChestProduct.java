package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.handler.impl.DummyHandler;
import su.nightexpress.nexshop.shop.chest.Placeholders;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nightcore.config.FileConfig;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private long quantity;

    public ChestProduct(@NotNull ShopPlugin plugin,
                        @NotNull String id,
                        @NotNull ChestShop shop,
                        @NotNull Currency currency,
                        @NotNull ProductHandler handler,
                        @NotNull ProductPacker packer) {
        super(plugin, id, shop, currency, handler, packer);

        this.placeholders.add(Placeholders.forProductStock(this));
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.writeQuantity(config, path);
        if (!(this.getHandler() instanceof DummyHandler)) {
            config.set(path + ".Handler", this.getHandler().getName());
        }
        config.set(path + ".Currency", this.getCurrency().getId());
        this.getPricer().write(config, path + ".Price");
        this.getPacker().write(config, path);
    }

    public void writeQuantity(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".InfiniteStorage.Quantity", this.getQuantity());
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        return this.getShop().getStock().count(this, tradeType, player);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(this.plugin, player, this, buyType, all);
    }

    /**
     *
     * @return Product quantity for Infinite Storage system.
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Sets product's quantity for Infinite Storage system.
     * @param quantity Product quantity.
     */
    public void setQuantity(long quantity) {
        this.quantity = Math.max(0, Math.abs(quantity));
    }
}
