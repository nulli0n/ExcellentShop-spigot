package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.ICurrency;

import java.util.Map;

public interface IBank {

    @NotNull IShop getShop();

    boolean deposit(@NotNull ICurrency currency, double amount);

    boolean withdraw(@NotNull ICurrency currency, double amount);

    @NotNull Map<String, Double> getBalance();

    double getBalance(@NotNull ICurrency currency);

    boolean hasEnough(@NotNull ICurrency currency, double amount);
}
