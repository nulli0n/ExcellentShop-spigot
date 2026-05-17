package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.product.TradeType;

@NullMarked
public abstract class ETransaction {

    protected final Player              player;
    protected final TradeType           type;
    protected final ETransactionOptions options;

    protected ETransaction(Player player, TradeType type, ETransactionOptions options) {
        this.player = player;
        this.type = type;
        this.options = options;
    }

    public Player getPlayer() {
        return this.player;
    }

    public TradeType getType() {
        return this.type;
    }

    public ETransactionOptions getOptions() {
        return this.options;
    }

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

    @NullMarked
    public abstract static class Builder<B extends Builder<B, T>, T extends ETransaction> {

        protected final Player    player;
        protected final TradeType type;

        protected Inventory userInventory;
        protected boolean   preview;
        protected boolean   strict;
        protected boolean   silent;

        protected Builder(Player player, TradeType type) {
            this.player = player;
            this.type = type;
            this.userInventory = player.getInventory();
            this.preview = false;
            this.strict = true;
            this.silent = false;
        }

        public T build() {
            return this.build(this.buildOptions());
        }

        protected abstract T build(ETransactionOptions options);

        protected abstract B getThis();

        protected ETransactionOptions buildOptions() {
            return new ETransactionOptions(this.userInventory, this.preview, this.strict, this.silent);
        }

        public B setOptions(ETransactionOptions options) {
            this.setUserInventory(options.userInventory());
            this.setPreview(options.preview());
            this.setSilent(options.silent());
            this.setStrict(options.strict());
            return this.getThis();
        }

        public B setUserInventory(Inventory userInventory) {
            this.userInventory = userInventory;
            return this.getThis();
        }

        public B setPreview(boolean preview) {
            this.preview = preview;
            return this.getThis();
        }

        public B setStrict(boolean strict) {
            this.strict = strict;
            return this.getThis();
        }

        public B setSilent(boolean silent) {
            this.silent = silent;
            return this.getThis();
        }
    }
}
