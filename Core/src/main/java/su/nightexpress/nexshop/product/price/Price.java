package su.nightexpress.nexshop.product.price;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Price {

    private final Map<String, Double> currencyMap;

    public Price(@NotNull Map<String, Double> currencyMap) {
        this.currencyMap = currencyMap;
    }

    @NotNull
    public static Price create() {
        return new Price(new HashMap<>());
    }

    @NotNull
    public Price add(@NotNull Price other) {
        return this.add(other.getCurrencyMap());
    }

    @NotNull
    public Price add(@NotNull Map<String, Double> otherMap) {
        otherMap.forEach(this::add);
        return this;
    }

    @NotNull
    public Price add(@NotNull Currency currency, double amount) {
        return this.add(currency.getInternalId(), amount);
    }

    @NotNull
    public Price add(@NotNull String currencyId, double amount) {
        this.setAmount(currencyId, this.getAmount(currencyId) + amount);
        return this;
    }

    @NotNull
    public Map<Currency, Double> parsed() {
        Map<Currency, Double> map = new HashMap<>();

        this.currencyMap.forEach((id, amount) -> {
            Currency currency = EconomyBridge.getCurrency(id);
            if (currency == null) return;

            map.put(currency, amount);
        });

        return map;
    }

    @NotNull
    public String formatValues() {
        return this.parsed().entrySet().stream().map(entry -> entry.getKey().format(entry.getValue())).collect(Collectors.joining(", "));
    }

    @NotNull
    public List<String> formatted(@NotNull String format) {
        return this.parsed().entrySet().stream().map(entry -> entry.getKey().applyFormat(format, entry.getValue())).toList();
    }

    public double getAmount(@NotNull Currency currency) {
        return this.getAmount(currency.getInternalId());
    }

    public double getAmount(@NotNull String currencyId) {
        return this.currencyMap.getOrDefault(currencyId, 0D);
    }

    public void setAmount(@NotNull Currency currency, double amount) {
        this.setAmount(currency.getInternalId(), amount);
    }

    public void setAmount(@NotNull String currencyId, double amount) {
        this.currencyMap.put(currencyId, amount);
    }

    @NotNull
    public Map<String, Double> getCurrencyMap() {
        return this.currencyMap;
    }
}
