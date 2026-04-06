package su.nightexpress.nexshop.util;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.util.LowerCase;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BalanceHolder {

    public static final BalanceHolder EMPTY = new BalanceHolder(Collections.emptyMap());

    private final Map<String, Double> balanceMap;

    public BalanceHolder() {
        this(new ConcurrentHashMap<>());
    }

    public BalanceHolder(@NonNull Map<String, Double> balanceMap) {
        this.balanceMap = balanceMap;
    }

    @NonNull
    public String format(@NonNull String delimiter) {
        return this.format(Currency::format, delimiter);
    }

    @NonNull
    public String format(@NonNull BiFunction<Currency, Double, String> function, @NonNull String delimiter) {
        return balanceMap.entrySet().stream().map(entry -> {
            Currency currency = EconomyBridge.api().getCurrency(entry.getKey());
            if (currency == null) return null;

            return function.apply(currency, entry.getValue());
        }).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }

    public boolean isEmpty() {
        return this.balanceMap.isEmpty();
    }

    public boolean contains(@NonNull Currency currency) {
        return this.contains(currency.getInternalId());
    }

    public boolean contains(@NonNull String currencyId) {
        return this.balanceMap.containsKey(LowerCase.INTERNAL.apply(currencyId));
    }

    public void storeAll(@NonNull BalanceHolder other) {
        other.balanceMap.forEach(this::store);
    }

    public boolean beatsAll(@NonNull BalanceHolder other) {
        return other.balanceMap.keySet().stream().allMatch(id -> this.query(id) >= other.query(id));
    }

    public boolean has(@NonNull Currency currency, double amount) {
        return this.has(currency.getInternalId(), amount);
    }

    public boolean has(@NonNull String currencyId, double amount) {
        return this.query(currencyId) >= amount;
    }

    public double query(@NonNull Currency currency) {
        return this.query(currency.getInternalId());
    }

    public double query(@NonNull String currencyId) {
        return this.balanceMap.getOrDefault(LowerCase.INTERNAL.apply(currencyId), 0D);
    }

    public void store(@NonNull Currency currency, double amount) {
        this.store(currency.getInternalId(), amount);
    }

    public void store(@NonNull String currencyId, double amount) {
        String key = LowerCase.INTERNAL.apply(currencyId);

        // Thread-safe way
        this.balanceMap.merge(key, amount, (currentBalance, addedAmount) ->
            Math.max(0, currentBalance + addedAmount)
        );
    }

    public void remove(@NonNull Currency currency, double amount) {
        this.remove(currency.getInternalId(), amount);
    }

    public void remove(@NonNull String currencyId, double amount) {
        this.store(currencyId, -amount);
    }

    public void set(@NonNull Currency currency, double amount) {
        this.set(currency.getInternalId(), amount);
    }

    public void set(@NonNull String currencyId, double amount) {
        this.balanceMap.put(LowerCase.INTERNAL.apply(currencyId), amount);
    }

    @NonNull
    public Map<String, Double> getBalanceMap() {
        return Map.copyOf(this.balanceMap);
    }
}
