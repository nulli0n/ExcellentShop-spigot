package su.nightexpress.excellentshop.shop.transaction;

import static su.nightexpress.excellentshop.api.product.TradeType.BUY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.UnitUtils;
import su.nightexpress.excellentshop.api.event.TransactionCompletedEvent;
import su.nightexpress.excellentshop.api.event.TransactionPreValidateEvent;
import su.nightexpress.excellentshop.api.event.TransactionValidatedEvent;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.AbstractShopModule;
import su.nightexpress.excellentshop.shop.transaction.validation.BuyValidator;
import su.nightexpress.excellentshop.shop.transaction.validation.EmptinessValidator;
import su.nightexpress.excellentshop.shop.transaction.validation.SellValidator;
import su.nightexpress.excellentshop.shop.transaction.validation.ShopBalanceValidator;
import su.nightexpress.excellentshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

@NullMarked
public class TransactionEngine {

    private final ShopPlugin         plugin;
    private final AbstractShopModule module;

    public TransactionEngine(ShopPlugin plugin, AbstractShopModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    public void processTransaction(ERawTransaction rawTransaction, TransactionCallback callback) {
        EPreparedTransaction preparedTransaction = this.prepareTransaction(rawTransaction);

        this.processTransaction(preparedTransaction, callback);
    }

    public void processTransaction(EPreparedTransaction transaction, TransactionCallback callback) {
        TransactionPreValidateEvent preValidateEvent = new TransactionPreValidateEvent(transaction);
        this.plugin.getPluginManager().callEvent(preValidateEvent);
        if (preValidateEvent.isCancelled()) return;

        List<TransactionValidator> validators = this.prepareValidators(transaction);
        for (TransactionValidator validator : validators) {
            if (!this.handleValidator(transaction, validator, callback)) {
                return;
            }
        }

        TransactionValidatedEvent validatedEvent = new TransactionValidatedEvent(transaction);
        this.plugin.getPluginManager().callEvent(validatedEvent);
        if (validatedEvent.isCancelled()) return;

        ECompletedTransaction completedTransaction = this.completeTransaction(transaction, ETransactionResult.SUCCESS);

        TransactionCompletedEvent completedEvent = new TransactionCompletedEvent(completedTransaction);
        this.plugin.getPluginManager().callEvent(completedEvent);
        if (completedEvent.isCancelled()) return;

        if (transaction.isPreview()) {
            callback.accept(completedTransaction);
            return;
        }

        this.performBalanceUpdates(completedTransaction);
        this.performItemUpdates(completedTransaction);
        this.module.notifySuccessfulTransaction(completedTransaction);
        this.notifyLooseItems(completedTransaction);

        callback.accept(completedTransaction);
    }

    @NonNull
    public EPreparedTransaction prepareTransaction(@NonNull ERawTransaction raw) {
        Player player = raw.getPlayer();
        TradeType type = raw.getType();

        EPreparedTransaction.Builder transaction = EPreparedTransaction.builder(player, type)
            .setOptions(raw.getOptions());

        Set<Shop> targetShops = raw.getTargetShops();
        if (targetShops.isEmpty()) {
            return transaction.build();
        }

        // Precalc total amount for each item for best results.
        Map<ItemStack, Integer> distinctItems = new HashMap<>();
        raw.getItems().forEach(itemStack -> {
            ItemStack copy = new ItemStack(itemStack);
            copy.setAmount(1);

            int current = distinctItems.getOrDefault(copy, 0);
            distinctItems.put(copy, current + itemStack.getAmount());
        });

        distinctItems.forEach((itemStack, itemAmount) -> {
            itemStack.setAmount(itemAmount);
            Product result = ShopUtils.findBestProduct(itemStack, type, targetShops);
            if (result == null) return;

            int maxUnits = switch (type) {
                case BUY -> result.getMaxBuyableUnitAmount(player, raw.getUserInventory());
                case SELL -> result.getMaxSellableUnitAmount(player, raw.getUserInventory());
            };
            if (maxUnits <= 0) return;

            transaction.addProduct(result, maxUnits);
        });

        return transaction.build();
    }

    private ECompletedTransaction completeTransaction(EPreparedTransaction transaction, ETransactionResult result) {
        Player player = transaction.getPlayer();
        TradeType tradeType = transaction.getType();
        List<ETransactionItem> items = List.copyOf(transaction.getItemsList());
        List<ETransactionItem> looseItems = List.copyOf(transaction.getLooseItems());
        Inventory inventory = transaction.getUserInventory();
        BalanceHolder worth = transaction.calculateWorth();
        boolean silent = transaction.isSilent();

        return new ECompletedTransaction(player, tradeType, items, looseItems, inventory, worth, result, silent);
    }

    private List<TransactionValidator> prepareValidators(EPreparedTransaction transaction) {
        List<TransactionValidator> validators = new ArrayList<>();

        validators.add(new EmptinessValidator());

        if (transaction.getType() == BUY) {
            validators.add(new BuyValidator());
        }
        else {
            validators.add(new SellValidator());
        }

        validators.add(new ShopBalanceValidator());

        return validators;
    }

    private boolean handleValidator(EPreparedTransaction transaction,
                                    TransactionValidator validator,
                                    TransactionCallback callback) {
        ValidationResult result = validator.validate(transaction);
        if (result.result() == ETransactionResult.SUCCESS) return true;

        Player player = transaction.getPlayer();

        ECompletedTransaction completedTransaction = this.completeTransaction(transaction, result.result());
        ETransactionItem cause = result.cause();
        MessageLocale errorMessage = result.errorMessage();

        if (!transaction.isSilent() && errorMessage != null) {
            this.module.sendPrefixed(errorMessage, player, builder -> {
                this.addTransactionPlaceholders(builder, completedTransaction);

                if (cause != null) {
                    builder.with(cause.product().placeholders());
                }
            });
        }

        callback.accept(completedTransaction);
        return false;
    }

    private void performBalanceUpdates(ECompletedTransaction transaction) {
        Player player = transaction.player();
        TradeType type = transaction.type();

        // Group items by shop to process each shop's inventory together
        Map<Shop, List<ETransactionItem>> itemsByShop = transaction.items()
            .stream()
            .collect(Collectors.groupingBy(item -> item.product().getShop()));

        itemsByShop.forEach((shop, items) -> {
            BalanceHolder worth = new BalanceHolder();

            items.forEach(item -> worth.storeAll(item.price()));

            worth.getBalanceMap().forEach((currencyId, amount) -> {
                Currency currency = EconomyBridge.api().getCurrency(currencyId);
                if (currency == null) return;

                this.performBalanceUpdate(player, shop, currency, type, amount);
            });
        });
    }

    private void performBalanceUpdate(Player player, Shop shop, Currency currency, TradeType type, double amount) {
        if (type == TradeType.BUY) {
            if (!shop.isAdminShop()) shop.depositBalance(currency, amount);
            currency.withdraw(player, amount);
        }
        else {
            if (!shop.isAdminShop()) shop.withdrawBalance(currency, amount);
            currency.deposit(player, amount);
        }
    }

    private void performItemUpdates(ECompletedTransaction transaction) {
        transaction.items().forEach(item -> {
            item.product().onSuccessfulTransaction(transaction, item.units());
        });
    }

    private void notifyLooseItems(ECompletedTransaction transaction) {
        List<ETransactionItem> looseItems = transaction.looseItems();
        if (looseItems.isEmpty()) return;

        Player player = transaction.player();
        this.module.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_LOOSE_ITEMS, player, builder -> this
            .addTransactionPlaceholders(builder, transaction)
        );
    }

    public PlaceholderContext.Builder addTransactionPlaceholders(PlaceholderContext.Builder builder,
                                                                 ECompletedTransaction transaction) {
        return builder
            .with(ShopPlaceholders.GENERIC_WORTH, () -> transaction.worth().format(Lang.OTHER_PRICE_DELIMITER.text()))
            .with(ShopPlaceholders.GENERIC_TOTAL_AMOUNT, () -> String.valueOf(transaction.countTotalAmount()))
            .with(ShopPlaceholders.GENERIC_TOTAL_UNITS, () -> String.valueOf(transaction.countTotalUnits()))
            .with(ShopPlaceholders.GENERIC_SIZE, () -> String.valueOf(transaction.items().size()))
            .with(ShopPlaceholders.GENERIC_LOOSE_SIZE, () -> String.valueOf(transaction.looseItems().size()))
            .with(ShopPlaceholders.GENERIC_PRODUCTS, () -> this.formatProducts(transaction.items()))
            .with(ShopPlaceholders.GENERIC_LOOSE, () -> this.formatProducts(transaction.looseItems()));
    }

    private String formatProducts(Collection<ETransactionItem> items) {
        String entry = (items.size() > 1 ? Lang.SHOP_TRADE_PRODUCT_ENTRY_MANY : Lang.SHOP_TRADE_PRODUCT_ENTRY_ONE)
            .text();

        return items.stream()
            .map(item -> PlaceholderContext.builder()
                .with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(UnitUtils.unitsToAmount(item.product(), item
                    .units())))
                .with(ShopPlaceholders.GENERIC_UNITS, () -> String.valueOf(item.units()))
                .with(ShopPlaceholders.GENERIC_PRICE, () -> item.price().format(Lang.OTHER_PRICE_DELIMITER.text()))
                .with(item.product().placeholders())
                .with(item.product().getShop().placeholders())
                .build()
                .apply(entry))
            .collect(Collectors.joining(TagWrappers.BR));
    }
}
