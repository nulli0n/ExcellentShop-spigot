package su.nightexpress.nexshop.shop.virtual.dialog;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.impl.*;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.manager.SimpleManager;

import java.util.function.BiFunction;

public class VirtualDialogs extends SimpleManager<ShopPlugin> {

    private final VirtualShopModule module;

    private ShopCreationDialog    creationDialog;
    private ShopNameDialog        nameDialog;
    private ShopDescriptionDialog descriptionDialog;
    private ShopAliasesDialog     aliasesDialog;
    private ShopMenuSlotsDialog   menuSlotsDialog;
    private ShopPagesDialog       pagesDialog;
    private ShopLayoutsDialog     layoutsDialog;

    private ProductPriceDialog productPriceDialog;
    private ProductFloatPriceTimesDialog floatPriceTimesDialog;
    private ProductCurrencyDialog productCurrencyDialog;
    private ProductCommandsDialog productCommandsDialog;

    public VirtualDialogs(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void onLoad() {
        this.creationDialog = this.registerProvider(ShopCreationDialog::new);
        this.nameDialog = this.registerProvider(ShopNameDialog::new);
        this.descriptionDialog = this.registerProvider(ShopDescriptionDialog::new);
        this.aliasesDialog = this.registerProvider(ShopAliasesDialog::new);
        this.menuSlotsDialog = this.registerProvider(ShopMenuSlotsDialog::new);
        this.pagesDialog = this.registerProvider(ShopPagesDialog::new);
        this.layoutsDialog = this.registerProvider(ShopLayoutsDialog::new);

        this.productPriceDialog = this.registerProvider(ProductPriceDialog::new);
        this.floatPriceTimesDialog = this.registerProvider(ProductFloatPriceTimesDialog::new);
        this.productCurrencyDialog = this.registerProvider(ProductCurrencyDialog::new);
        this.productCommandsDialog = this.registerProvider(ProductCommandsDialog::new);
    }

    @Override
    protected void onShutdown() {

    }

    @NotNull
    private <T, P extends VirtualDialogProvider<T>> P registerProvider(@NotNull BiFunction<ShopPlugin, VirtualShopModule, P> supplier) {
        P provider = supplier.apply(this.plugin, this.module);
        this.plugin.injectLang(provider);
        return provider;
    }

    public void openShopCreationDialog(@NotNull Player player) {
        this.creationDialog.show(player, null);
    }

    public void openShopNameDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.nameDialog.show(player, shop);
    }

    public void openShopDescriptionDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.descriptionDialog.show(player, shop);
    }

    public void openShopAliasesDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.aliasesDialog.show(player, shop);
    }

    public void openShopMenuSlotsDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.menuSlotsDialog.show(player, shop);
    }

    public void openShopPagesDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.pagesDialog.show(player, shop);
    }

    public void openShopLayoutsDialog(@NotNull Player player, @NotNull VirtualShop shop) {
        this.layoutsDialog.show(player, shop);
    }

    public void openProductPrice(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productPriceDialog.show(player, product);
    }

    public void openFloatPricingTimes(@NotNull Player player, @NotNull VirtualProduct product) {
        this.floatPriceTimesDialog.show(player, product);
    }

    public void openProductCurrency(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productCurrencyDialog.show(player, product);
    }

    public void openProductCommandsDialog(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productCommandsDialog.show(player, product);
    }
}
