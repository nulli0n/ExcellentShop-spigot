package su.nightexpress.nexshop.shop.menu;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.MenuItem;

public class ShopCartMenuItem extends MenuItem {

    private int productAmount;

    public ShopCartMenuItem(@NotNull MenuItem menuItem) {
        super(menuItem);
    }

    public int getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(int productAmount) {
        this.productAmount = productAmount;
    }
}
