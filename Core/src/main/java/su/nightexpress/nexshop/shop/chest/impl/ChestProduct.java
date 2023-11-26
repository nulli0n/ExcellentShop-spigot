package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.menu.ProductPriceMenu;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private ProductPriceMenu priceEditor;

    /*@Deprecated
    public ChestProduct(@NotNull ChestShop shop, @NotNull Currency currency, @NotNull ItemStack item) {
        this(UUID.randomUUID().toString(), shop, currency, ProductHandlerRegistry.forBukkitItem(), VanillaItemPacker.forChestShop(item));
    }*/

    public ChestProduct(@NotNull String id, @NotNull ChestShop shop, @NotNull Currency currency,
                        @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(shop.plugin(), id, shop, currency, handler, packer);

        this.placeholderMap.add(this.getShop().getStock().getPlaceholders(this));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        return super.getPlaceholders(player)
            .add(this.getShop().getStock().getPlaceholders(this));
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Handler", this.getHandler().getName());
        cfg.set(path + ".Currency", this.getCurrency().getId());
        this.getPricer().write(cfg, path + ".Price");
        this.getPacker().write(cfg, path);
    }

    @Override
    public void clear() {
        if (this.priceEditor != null) {
            this.priceEditor.clear();
            this.priceEditor = null;
        }
    }

    @NotNull
    public ProductPriceMenu getPriceEditor() {
        if (this.priceEditor == null) {
            this.priceEditor = new ProductPriceMenu(this.plugin, this);
        }
        return priceEditor;
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        return this.getShop().getStock().count(this, tradeType);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(this.plugin, player, this, buyType, all);
    }
}
