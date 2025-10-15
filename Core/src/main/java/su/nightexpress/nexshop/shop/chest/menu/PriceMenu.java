package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.menu.ProductPriceMenu;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PriceMenu extends ProductPriceMenu<ChestProduct> {

    private final ChestShopModule module;

    public PriceMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, ChestLang.SHOP_PRICE_MENU_TITLE.text());
        this.module = module;
    }

    @Override
    protected void save(@NotNull MenuViewer viewer, @NotNull ChestProduct product) {
        product.getShop().markDirty();
    }

    @Override
    protected boolean canResetPriceData(@NotNull MenuViewer viewer) {
        return viewer.getPlayer().hasPermission(ChestPerms.ADMIN_SHOP);
    }

    @Override
    protected void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull ChestProduct product) {
        this.runNextTick(() -> this.module.openProductsMenu(viewer.getPlayer(), product.getShop()));
    }

    @Override
    protected void handleCurrency(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull ChestProduct product) {
        List<Currency> currencies = new ArrayList<>(this.module.getAvailableCurrencies(viewer.getPlayer()));
        if (currencies.isEmpty()) return;

        int index = currencies.indexOf(product.getCurrency()) + 1;
        if (index >= currencies.size()) index = 0;

        product.setCurrencyId(currencies.get(index).getInternalId());
        this.saveAndFlush(viewer, product);
    }

    @Override
    protected void handlePriceType(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull ChestProduct product) {
        //Predicate<PriceType> predicate = priceType -> viewer.getPlayer().hasPermission(ChestPerms.PREFIX_PRICE_TYPE + priceType.name().toLowerCase());

        //double sell = product.getPricer().getPrice(TradeType.SELL);
        //double buy = product.getPricer().getPrice(TradeType.BUY);

        PriceType priceType = Lists.next(product.getPricingType()/*, predicate*/);
        product.setPricing(ProductPricing.from(priceType));
        plugin.getDataManager().deletePriceData(product);

        /*if (product.getPricer() instanceof RangedPricing pricer) {
            pricer.setPriceRange(TradeType.BUY, UniDouble.of(buy, buy));
            pricer.setPriceRange(TradeType.SELL, UniDouble.of(sell, sell));
        }
        product.setPrice(TradeType.BUY, buy);
        product.setPrice(TradeType.SELL, sell);*/

        this.saveAndFlush(viewer, product);
    }
}
