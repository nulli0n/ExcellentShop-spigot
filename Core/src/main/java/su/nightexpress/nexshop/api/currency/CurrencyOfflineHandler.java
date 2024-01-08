package su.nightexpress.nexshop.api.currency;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface CurrencyOfflineHandler {

    double getBalance(@NotNull UUID playerId);

    void give(@NotNull UUID playerId, double amount);

    void take(@NotNull UUID playerId, double amount);
}
