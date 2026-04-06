package su.nightexpress.excellentshop.feature.playershop.bank;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.nexshop.util.BalanceHolder;

import java.util.UUID;

public class Bank extends StatefulData {

    private final UUID          holder;
    private final BalanceHolder balanceHolder;

    public Bank(@NonNull UUID holder, @NonNull BalanceHolder balanceHolder) {
        this.holder = holder;
        this.balanceHolder = balanceHolder;
    }

    @NonNull
    public static Bank create(@NonNull UUID ownerId) {
        return new Bank(ownerId, new BalanceHolder());
    }

    @NonNull
    public UUID getHolder() {
        return this.holder;
    }

    @NonNull
    public BalanceHolder getAccount() {
        return this.balanceHolder;
    }
}
