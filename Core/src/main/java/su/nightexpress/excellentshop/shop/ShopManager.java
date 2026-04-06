package su.nightexpress.excellentshop.shop;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.shop.menu.BuyingMenu;
import su.nightexpress.excellentshop.shop.menu.SellingMenu;
import su.nightexpress.excellentshop.ShopFiles;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;

public class ShopManager extends AbstractManager<ShopPlugin> {

    private BuyingMenu  buyingMenu;
    private SellingMenu sellingMenu;

    public ShopManager(@NonNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.loadUI();

        this.plugin.runTaskLater(task -> this.printBadProducts(), 100L);
    }

    @Override
    protected void onShutdown() {
        if (this.sellingMenu != null) {
            this.sellingMenu.cleanUp();
            this.sellingMenu = null;
        }
    }

    private void loadUI() {
        // TODO If enabled
        this.buyingMenu = this.initMenu(new BuyingMenu(this.plugin), this.plugin.dataPath().resolve(ShopFiles.DIR_MENU).resolve(ShopFiles.FILE_BUYING_MENU));

        // TODO If enabled
        this.sellingMenu = this.initMenu(new SellingMenu(this.plugin), this.plugin.dataPath().resolve(ShopFiles.DIR_MENU).resolve(ShopFiles.FILE_SELLING_MENU));
    }

    private void printBadProducts() {
        // TODO this.getShops().forEach(Shop::printBadProducts);
    }

    public boolean openBuyingMenu(@NonNull Player player, @NonNull AbstractShopModule module, @NonNull Product product, int shopPage, int initialUnits) {
        return this.buyingMenu.show(player, module, product, shopPage, initialUnits);
    }

    public boolean openSellingMenu(@NonNull Player player, @NonNull AbstractShopModule module, @Nullable Shop targetShop, @Nullable Product targetProduct, int shopPage) {
        return this.sellingMenu.show(player, module, targetShop, targetProduct, shopPage);
    }

    @Deprecated
    public void openConfirmation(@NonNull Player player, @NonNull Confirmation confirmation) {
        UIUtils.openConfirmation(player, confirmation);
    }
}
