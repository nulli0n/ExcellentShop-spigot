package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.shop.menu.ProductPriceMenu;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public class PriceMenu extends ProductPriceMenu<VirtualProduct> {

    private final VirtualShopModule module;

    public PriceMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_PRODUCT_PRICE.text());
        this.module = module;
    }

    @Override
    protected void save(@NotNull MenuViewer viewer, @NotNull VirtualProduct product) {
        product.setSaveRequired(true);
    }

    @Override
    protected boolean canResetPriceData(@NotNull MenuViewer viewer) {
        return true;
    }

    @Override
    protected void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product) {
        this.runNextTick(() -> module.openProductOptions(viewer.getPlayer(), this.getLink(viewer)));
    }

    @Override
    protected void handleCurrency(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product) {
        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_CURRENCY.text(), input -> {
            Currency currency = EconomyBridge.getCurrency(input.getTextRaw());
            if (currency != null) {
                product.setCurrencyId(currency.getInternalId());
                product.setSaveRequired(true);
            }
            return true;
        }).setSuggestions(EconomyBridge.getCurrencyIds(), true));
    }

    @Override
    protected void handlePriceType(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product) {
        PriceType priceType = Lists.next(product.getPricingType());

        /*double sell = product.getPricer().getPrice(TradeType.SELL);
        double buy = product.getPricer().getPrice(TradeType.BUY);*/

        product.setPricing(ProductPricing.from(priceType));
        plugin.getDataManager().resetPriceData(product);

        /*if (product.getPricer() instanceof RangedPricing pricer) {
            pricer.setPriceRange(TradeType.BUY, UniDouble.of(buy, buy));
            pricer.setPriceRange(TradeType.SELL, UniDouble.of(sell, sell));
        }
        product.setPrice(TradeType.BUY, buy);
        product.setPrice(TradeType.SELL, sell);*/

        this.saveAndFlush(viewer, product);
    }
}
