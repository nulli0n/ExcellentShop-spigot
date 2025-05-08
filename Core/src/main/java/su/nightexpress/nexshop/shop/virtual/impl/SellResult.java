package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SellResult {

    private final List<Transaction> transactions;

    public SellResult() {
        this.transactions = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.transactions.isEmpty();
    }

    public void addTransaction(@NotNull VirtualProduct product, int amount, @NotNull Transaction transaction) {
        if (transaction.getResult() == Transaction.Result.SUCCESS) {
            this.transactions.add(transaction);
        }
    }

    @NotNull
    public String getTotalIncome() {
        Map<Currency, Double> map = new HashMap<>();

        this.transactions.forEach(transaction -> {
            Currency currency = transaction.getProduct().getCurrency();

            double has = map.getOrDefault(currency, 0D);
            map.put(currency, has + transaction.getPrice());
        });

        return map.entrySet().stream().map(entry -> entry.getKey().format(entry.getValue())).collect(Collectors.joining(", "));
    }

    @NotNull
    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    public void inherit(@NotNull SellResult other) {
        this.transactions.addAll(other.getTransactions());
    }
}
