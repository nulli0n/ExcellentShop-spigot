package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.function.UnaryOperator;

public interface PreparedProduct {

    @NotNull UnaryOperator<String> replacePlaceholders();

    @NotNull Transaction trade();

    @NotNull Player getPlayer();

    @NotNull Shop getShop();

    @NotNull Product getProduct();

    @NotNull TradeType getTradeType();

    boolean isAll();

    boolean isSilent();

    void setSilent(boolean silent);

    @NotNull Inventory getInventory();

    void setInventory(@NotNull Inventory inventory);

    int getUnits();

    void setUnits(int units);

    int getAmount();

    double getPrice();


}
