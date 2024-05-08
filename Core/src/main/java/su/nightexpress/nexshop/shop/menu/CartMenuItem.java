package su.nightexpress.nexshop.shop.menu;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.menu.item.MenuItem;

class CartMenuItem extends MenuItem {

    private int units;

    public CartMenuItem(@NotNull MenuItem menuItem) {
        super(menuItem.getItemStack(), menuItem.getPriority(), menuItem.getSlots());
        this.setOptions(menuItem.getOptions());
        this.setHandler(menuItem.getHandler());
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = Math.abs(units);
    }
}
