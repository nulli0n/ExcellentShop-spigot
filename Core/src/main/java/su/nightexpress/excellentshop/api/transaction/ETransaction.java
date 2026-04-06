package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;

public abstract class ETransaction {

    protected final Player player;
    protected final TradeType type;
    protected final ETransactionOptions options;

    public ETransaction(@NonNull Player player, @NonNull TradeType type, @NonNull ETransactionOptions options) {
        this.player = player;
        this.type = type;
        this.options = options;
    }

    @NonNull
    public Player getPlayer() {
        return this.player;
    }

    @NonNull
    public TradeType getType() {
        return this.type;
    }

    @NonNull
    public ETransactionOptions getOptions() {
        return this.options;
    }

    @NonNull
    public Inventory getUserInventory() {
        return this.options.userInventory();
    }

    public boolean isPreview() {
        return this.options.preview();
    }

    public boolean isStrict() {
        return this.options.strict();
    }

    public boolean isSilent() {
        return this.options.silent();
    }

    public static abstract class Builder<B extends Builder<B, T>, T extends ETransaction> {

        protected final Player    player;
        protected final TradeType type;

        protected Inventory userInventory;
        protected boolean   preview;
        protected boolean   strict;
        protected boolean   silent;

        public Builder(@NonNull Player player, @NonNull TradeType type) {
            this.player = player;
            this.type = type;
            this.userInventory = player.getInventory();
            this.preview = false;
            this.strict = true;
            this.silent = false;
        }

        @NonNull
        public T build() {
            return this.build(this.buildOptions());
        }

        @NonNull
        protected abstract T build(@NonNull ETransactionOptions options);

        @NonNull
        protected abstract B getThis();

        @NonNull
        protected ETransactionOptions buildOptions() {
            return new ETransactionOptions(this.userInventory, this.preview, this.strict, this.silent);
        }

        @NonNull
        public B setOptions(@NonNull ETransactionOptions options) {
            this.setUserInventory(options.userInventory());
            this.setPreview(options.preview());
            this.setSilent(options.silent());
            this.setStrict(options.strict());
            return this.getThis();
        }

        @NonNull
        public B setUserInventory(@NonNull Inventory userInventory) {
            this.userInventory = userInventory;
            return this.getThis();
        }

        @NonNull
        public B setPreview(boolean preview) {
            this.preview = preview;
            return this.getThis();
        }

        @NonNull
        public B setStrict(boolean strict) {
            this.strict = strict;
            return this.getThis();
        }

        @NonNull
        public B setSilent(boolean silent) {
            this.silent = silent;
            return this.getThis();
        }
    }
}
