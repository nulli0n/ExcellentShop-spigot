package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nightcore.util.placeholder.Placeholder;

public interface PreparedProduct extends Placeholder {

    @NotNull Transaction trade();

    @NotNull Player getPlayer();

    @NotNull Shop getShop();

    @NotNull Product getProduct();

    @NotNull TradeType getTradeType();

    boolean isAll();

    @NotNull Inventory getInventory();

    void setInventory(@NotNull Inventory inventory);

    int getUnits();

    void setUnits(int units);

    int getAmount();

    double getPrice();


}
