package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBank<S extends IShop> implements IBank {

    protected final S shop;
    protected final Map<String, Double> balance;

    public AbstractBank(@NotNull S shop) {
        this.shop = shop;
        this.balance = new HashMap<>();
    }

    @NotNull
    @Override
    public S getShop() {
        return shop;
    }

    @NotNull
    @Override
    public Map<String, Double> getBalance() {
        return balance;
    }
}
