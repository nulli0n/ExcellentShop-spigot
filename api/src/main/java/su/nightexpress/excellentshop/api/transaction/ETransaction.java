package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.product.TradeType;

/**
 * Abstract transaction class representing a base trade operation.
 */
@NullMarked
public abstract class ETransaction {

    protected final Player              player;
    protected final TradeType           type;
    protected final ETransactionOptions options;

    /**
     * Constructs a new transaction.
     *
     * @param player  The player involved in the transaction.
     * @param type    The type of trade (BUY or SELL).
     * @param options Options controlling the transaction's behavior.
     */
    protected ETransaction(Player player, TradeType type, ETransactionOptions options) {
        this.player = player;
        this.type = type;
        this.options = options;
    }

    /**
     * Gets the player involved in this transaction.
     *
     * @return The player conducting the transaction.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the transaction type, indicating whether the player is buying or selling.
     *
     * @return The trade type.
     */
    public TradeType getType() {
        return this.type;
    }

    /**
     * Gets the options controlling the behavior of this transaction.
     *
     * @return The transaction options.
     */
    public ETransactionOptions getOptions() {
        return this.options;
    }

    /**
     * Gets the inventory involved in this transaction.
     * This is where items will be checked and added to or removed from.
     *
     * @return The target's inventory.
     */
    public Inventory getUserInventory() {
        return this.options.userInventory();
    }

    /**
     * Checks whether this transaction is a preview.
     * Preview transactions will have no real effect on inventories or balances.
     *
     * @return True if this is a preview transaction, false otherwise.
     */
    public boolean isPreview() {
        return this.options.preview();
    }

    /**
     * Checks whether this transaction is strict.
     * A strict transaction will fail as soon as there is a validation error in any transaction item.
     *
     * @return True if strict, false otherwise.
     */
    public boolean isStrict() {
        return this.options.strict();
    }

    /**
     * Checks whether this transaction is silent.
     * If silent, the player will receive no notifications about its failure or completion.
     *
     * @return True if silent, false otherwise.
     */
    public boolean isSilent() {
        return this.options.silent();
    }

    /**
     * Abstract builder for constructing transactions.
     *
     * @param <B> The builder type.
     * @param <T> The transaction type being built.
     */
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
