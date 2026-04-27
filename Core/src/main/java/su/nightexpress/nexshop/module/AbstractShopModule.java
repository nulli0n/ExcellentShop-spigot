package su.nightexpress.nexshop.module;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.shop.formatter.ProductFormatter;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.api.ShopModule;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.core.Perms;
import su.nightexpress.excellentshop.shop.ShopManager;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.ui.inventory.Menu;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractShopModule extends AbstractModule implements ShopModule {

    protected final ShopManager shopManager;

    public AbstractShopModule(@NonNull ModuleContext context, @NonNull ShopManager shopManager) {
        super(context);
        this.shopManager = shopManager;
    }

    @NonNull
    public abstract ShopModuleSettings getSettings();

    //public abstract ProductFormatter<?> getProductFormatter();

    public abstract Set<? extends Shop> getShops(@NonNull Player player);

    public abstract void openPurchaseOptionsDialog(@NonNull ProductClickContext context);

    public boolean openBuyingMenu(@NonNull Player player, @NonNull Product product, int shopPage, int initialUnits) {
        return this.shopManager.openBuyingMenu(player, this, product, shopPage, initialUnits);
    }

    public boolean openSellingMenu(@NonNull Player player) {
        return this.shopManager.openSellingMenu(player, this, null, null, 1);
    }

    public boolean openSellingMenu(@NonNull Player player, @NonNull Shop shop, int shopPage) {
        return this.shopManager.openSellingMenu(player, this, shop, null, shopPage);
    }

    public boolean openSellingMenu(@NonNull Player player, @NonNull Product product, int shopPage) {
        return this.shopManager.openSellingMenu(player, this, null, product, shopPage);
    }

    @NonNull
    protected <P extends Product> List<String> formatProductInfo(@NonNull P product,
                                                                 @NonNull ProductFormatter<P> formatter,
                                                                 @NonNull Player player) {
        TradeStatus status = product.getTradeStatus();
        List<String> masterLore = this.getSettings().getProductDisplayMasterInfo(status);

        return this.formatProductInfo(product, formatter, masterLore, player);
    }

    @NonNull
    protected <P extends Product> List<String> formatProductInfo(@NonNull P product,
                                                                 @NonNull ProductFormatter<P> formatter,
                                                                 @NonNull List<String> masterLore,
                                                                 @NonNull Player player) {
        List<String> finalLore = new ArrayList<>();

        for (String masterLine : masterLore) {
            if (masterLine.isBlank()) {
                finalLore.add(masterLine);
                continue;
            }

            String formatted = formatter.formatLine(masterLine, product, player);

            if (!formatted.isEmpty()) {
                finalLore.add(formatted);
            }
        }

        return finalLore;
    }

    public void handleProductClick(@NonNull Player player, @NonNull Product product, int shopPage,
                                   @NonNull InventoryClickEvent event) {
        TradeStatus status = product.getTradeStatus();
        if (status == TradeStatus.UNAVAILABLE) return;

        ClickType clickType = event.getClick();
        ProductClickAction action = this.getSettings().getProductClickSettings().getClickAction(status, clickType);
        if (action == ProductClickAction.NONE) return;

        ProductClickContext context = new ProductClickContext(player, product, event, shopPage);

        if (Players.isBedrock(player) || action == ProductClickAction.PURCHASE_DIALOG) {
            this.openPurchaseOptionsDialog(context);
            return;
        }

        this.handleProductClickAction(context, action);
    }

    public void handleProductClickAction(@NonNull ProductClickContext context, @NonNull ProductClickAction action) {
        Player player = context.player();
        Product product = context.product();

        if (action == ProductClickAction.SELL_ALL && !player.hasPermission(Perms.KEY_SELL_ALL)) {
            this.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, player);
            return;
        }
        if (action == ProductClickAction.BUY_ALL && !player.hasPermission(Perms.KEY_BUY_ALL)) {
            this.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, player);
            return;
        }

        if (action == ProductClickAction.OPEN_BUY_MENU || action == ProductClickAction.OPEN_SELL_MENU) {
            TradeType type = action == ProductClickAction.OPEN_BUY_MENU ? TradeType.BUY : TradeType.SELL;
            boolean allowMenu = type == TradeType.BUY ? product.isBuyMenuAllowed() : product.isSellMenuAllowed();

            if (allowMenu) {
                EPreparedTransaction transaction = EPreparedTransaction.builder(player, type).addProduct(product, 1)
                    .setPreview(true).build();
                this.previewTransaction(transaction, result -> {
                    if (result != ETransactionResult.SUCCESS) return;

                    switch (type) {
                        case BUY -> this.openBuyingMenu(player, product, context.shopPage(), 1);
                        case SELL -> this.openSellingMenu(player, product, context.shopPage());
                    }
                });
                return;
            }

            action = type == TradeType.BUY ? ProductClickAction.BUY_ONE : ProductClickAction.SELL_ONE;
        }

        TradeType type = switch (action) {
            case BUY_ALL, BUY_ONE -> TradeType.BUY;
            case SELL_ALL, SELL_ONE -> TradeType.SELL;
            default -> null;
        };
        if (type == null) return;

        int units = 1;

        InventoryClickEvent event = context.event();
        if (event != null && event
            .getClick() == ClickType.NUMBER_KEY && (action == ProductClickAction.BUY_ONE || action == ProductClickAction.SELL_ONE)) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton >= 0) units = hotbarButton + 1;
        }
        else {
            if (action == ProductClickAction.SELL_ALL) units = product.getMaxSellableUnitAmount(player, player
                .getInventory());
            if (action == ProductClickAction.BUY_ALL) units = product.getMaxBuyableUnitAmount(player, player
                .getInventory());
        }

        int totalUnits = Math.max(1, units); // Prevent zero values.

        EPreparedTransaction transaction = EPreparedTransaction.builder(player, type).addProduct(product, totalUnits)
            .build();

        this.proceedTransaction(transaction, completed -> {
            Menu menu = this.plugin.getMenuRegistry().getActiveMenu(player);
            if (menu != null) {
                menu.refresh(player);
            }
        });
    }

    @NonNull
    public EPreparedTransaction prepareTransaction(@NonNull ERawTransaction raw) {
        Player player = raw.getPlayer();
        TradeType type = raw.getType();

        EPreparedTransaction.Builder transaction = EPreparedTransaction.builder(player, type).setOptions(raw
            .getOptions());

        Set<? extends Shop> targetShops = new HashSet<>(raw.hasTargetShops() ? raw.getTargetShops() : this.getShops(
            player));
        targetShops.removeIf(shop -> !shop.canAccess(player, false));
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
            Set<Product> candidates = new HashSet<>();

            targetShops.forEach(targetShop -> {
                Product best = targetShop.getBestProduct(itemStack, type, player);
                if (best == null) return;

                int maxUnits = switch (type) {
                    case BUY -> best.getMaxBuyableUnitAmount(player, raw.getUserInventory());
                    case SELL -> best.getMaxSellableUnitAmount(player, raw.getUserInventory());
                };
                if (maxUnits <= 0) return;

                candidates.add(best);
            });

            Product result = ShopUtils.getBestProduct(candidates, type, player);
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

    public void previewTransaction(@NonNull ERawTransaction transaction,
                                   @NonNull Consumer<ETransactionResult> callback) {
        if (!transaction.isPreview()) throw new IllegalArgumentException("Transaction is not preview!");

        this.processTransaction(this.prepareTransaction(transaction), completed -> callback.accept(completed.result()));
    }

    public void proceedTransaction(@NonNull ERawTransaction transaction,
                                   @NonNull Consumer<ECompletedTransaction> callback) {
        if (transaction.isPreview()) throw new IllegalArgumentException("Transaction is preview!");

        this.processTransaction(this.prepareTransaction(transaction), callback);
    }

    public void previewTransaction(@NonNull EPreparedTransaction transaction,
                                   @NonNull Consumer<ETransactionResult> callback) {
        if (!transaction.isPreview()) throw new IllegalArgumentException("Transaction is not preview!");

        this.processTransaction(transaction, completed -> callback.accept(completed.result()));
    }

    public void proceedTransaction(@NonNull EPreparedTransaction transaction,
                                   @NonNull Consumer<ECompletedTransaction> callback) {
        if (transaction.isPreview()) throw new IllegalArgumentException("Transaction is preview!");

        this.processTransaction(transaction, callback);
    }

    @NonNull
    protected abstract CompletableFuture<Boolean> handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction);

    protected abstract void finishSuccessfulTransaction(@NonNull ECompletedTransaction transaction);

    private void processTransaction(@NonNull EPreparedTransaction transaction,
                                    @NonNull Consumer<ECompletedTransaction> callback) {
        Player player = transaction.getPlayer();

        // TODO Custom transaction event

        if (!this.checkTransactionBasics(transaction, callback)) return;

        this.checkTransactionAffordance(transaction).whenCompleteAsync((affordable, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_UNEXPECTED_ERROR, player);
                return;
            }

            if (!affordable) {
                ECompletedTransaction completedTransaction = transaction.complete(ETransactionResult.OUT_OF_MONEY);

                if (!transaction.isSilent()) {
                    this.sendPrefixed(Lang.SHOP_TRADE_SHOP_OUT_OF_FUNDS, player, builder -> this
                        .addTransactionPlaceholderContext(builder, completedTransaction));
                }

                callback.accept(completedTransaction);
                return;
            }

            // Confirm the product is available for buying/selling.
            if (!this.checkTransactionBasics(transaction, callback)) return;

            ECompletedTransaction completedTransaction = transaction.complete(ETransactionResult.SUCCESS);

            if (transaction.isPreview()) {
                callback.accept(completedTransaction);
                return;
            }

            this.handleSuccessfulTransaction(completedTransaction).whenCompleteAsync((success, throwable2) -> {
                if (throwable2 != null) {
                    throwable2.printStackTrace();
                    this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_UNEXPECTED_ERROR, player);
                    return;
                }

                if (success) {
                    this.finishSuccessfulTransaction(completedTransaction);

                    List<ETransactionItem> looseItems = completedTransaction.looseItems();
                    if (!looseItems.isEmpty()) {
                        this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_LOOSE_ITEMS, player, builder -> this
                            .addTransactionPlaceholderContext(builder, completedTransaction));
                    }
                }
                else {
                    this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_UNEXPECTED_ERROR, player);
                }

                callback.accept(completedTransaction);

            }, this.plugin::runTask);
        }, this.plugin::runTask);
    }

    protected boolean checkTransactionBasics(@NonNull EPreparedTransaction transaction,
                                             @NonNull Consumer<ECompletedTransaction> callback) {
        Player player = transaction.getPlayer();
        TradeType tradeType = transaction.getType();
        Inventory inventory = transaction.getUserInventory();
        List<ETransactionItem> products = transaction.getItemsList();

        if (products.isEmpty()) {
            ECompletedTransaction completedTransaction = transaction.complete(ETransactionResult.FAILURE);

            if (!transaction.isSilent()) {
                this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_EMPTY, player, builder -> this
                    .addTransactionPlaceholderContext(builder, completedTransaction));
            }

            callback.accept(completedTransaction);
            return false;
        }

        if (!transaction.isStrict()) {
            products.removeIf(transactionItem -> {
                Product product = transactionItem.product();
                int units = transactionItem.units();

                int maxUnits = switch (tradeType) {
                    case BUY -> product.getMaxBuyableUnitAmount(player, inventory);
                    case SELL -> product.getMaxSellableUnitAmount(player, inventory);
                };
                if (maxUnits < 0 || maxUnits >= units) return false;

                if (maxUnits == 0) {
                    transaction.getLooseItems().add(transactionItem);
                    transaction.getItems().remove(product);
                    return true;
                }

                int loseUnits = units - maxUnits;
                Currency currency = product.getCurrency();

                BalanceHolder loseWorth = new BalanceHolder();
                BalanceHolder maxWorth = new BalanceHolder();

                loseWorth.store(currency, product.getFinalSellPrice(player, loseUnits));
                maxWorth.store(currency, product.getFinalSellPrice(player, maxUnits));

                transaction.getLooseItems().add(new ETransactionItem(product, loseUnits, loseWorth));
                transaction.getItems().put(product, new ETransactionItem(product, maxUnits, maxWorth));
                return false;
            });

            if (products.isEmpty()) {
                ECompletedTransaction completedTransaction = transaction.complete(ETransactionResult.FAILURE);

                if (!transaction.isSilent()) {
                    this.sendPrefixed(Lang.SHOP_TRADE_FEEDBACK_LOOSE_ITEMS, player, builder -> this
                        .addTransactionPlaceholderContext(builder, completedTransaction));
                }

                callback.accept(completedTransaction);
                return false;
            }

            return true;
        }

        ETransactionResult result = ETransactionResult.SUCCESS;
        MessageLocale errorLocale = null;
        ETransactionItem failed = null;

        for (ETransactionItem transactionItem : products) {
            Product product = transactionItem.product();
            int units = transactionItem.units();
            failed = transactionItem;

            if (tradeType == TradeType.BUY) {
                if (!product.canTrade(player) || !product.isBuyable()) {
                    result = ETransactionResult.NOT_AVAILABLE;
                    errorLocale = Lang.SHOP_TRADE_PRODUCT_UNBUYABLE;
                    break;
                }

                int stockUnits = product.getStock();
                if (stockUnits >= 0 && stockUnits < units) {
                    result = ETransactionResult.OUT_OF_STOCK;
                    errorLocale = Lang.SHOP_TRADE_PRODUCT_OUT_OF_STOCK;
                    break;
                }

                int inventorySpace = product.countSpace(inventory);
                if (inventorySpace >= 0 && inventorySpace < UnitUtils.unitsToAmount(product, units)) {
                    result = ETransactionResult.OUT_OF_INVENTORY_SPACE;
                    errorLocale = Lang.SHOP_TRADE_PLAYER_FULL_INVENTORY;
                    break;
                }

                int maxAffordable = product.getMaxAffordableUnitAmount(player);
                if (maxAffordable >= 0 && maxAffordable < units) {
                    result = ETransactionResult.TOO_EXPENSIVE;
                    errorLocale = Lang.SHOP_TRADE_PLAYER_OUT_OF_MONEY;
                    break;
                }
            }
            else {
                if (!product.canTrade(player) || !product.isSellable()) {
                    result = ETransactionResult.NOT_AVAILABLE;
                    errorLocale = Lang.SHOP_TRADE_PRODUCT_UNSELLABLE;
                    break;
                }

                if (product.countUnits(inventory) < units) {
                    result = ETransactionResult.NOT_ENOUGH_ITEMS;
                    errorLocale = Lang.SHOP_TRADE_PLAYER_NOT_ENOUGH_ITEMS;
                    break;
                }

                int spaceUnits = product.getSpace();
                if (spaceUnits >= 0 && spaceUnits < units) {
                    result = ETransactionResult.OUT_OF_SHOP_SPACE;
                    errorLocale = Lang.SHOP_TRADE_PRODUCT_OUT_OF_SPACE;
                    break;
                }
            }

            int tradeLimit = product.getTradeLimit(tradeType);
            if (tradeLimit >= 0) {
                int trades = product.getLimitData(player).getTrades(tradeType);
                int left = tradeLimit - trades;
                if (left < units) {
                    result = ETransactionResult.LIMIT_REACHED;
                    errorLocale = Lang.SHOP_TRADE_PLAYER_OUT_OF_LIMIT;
                    break;
                }
            }

            failed = null;
        }

        if (result != ETransactionResult.SUCCESS) {
            ECompletedTransaction completedTransaction = transaction.complete(result);
            ETransactionItem reason = failed;

            if (!transaction.isSilent()) {
                this.sendPrefixed(errorLocale, player, builder -> this.addTransactionPlaceholderContext(builder,
                    completedTransaction)
                    .with(reason.product().placeholders())
                );
            }

            callback.accept(completedTransaction);
            return false;
        }

        return true;
    }

    @NonNull
    private CompletableFuture<Boolean> checkTransactionAffordance(@NonNull EPreparedTransaction transaction) {
        if (transaction.getType() == TradeType.BUY) return CompletableFuture.completedFuture(true);

        List<CompletableFuture<Void>> shopBalanceFutures = new ArrayList<>();

        Map<Shop, Set<ETransactionItem>> shopsWithProducts = transaction.getItemsList().stream().collect(Collectors
            .groupingBy(
                qp -> qp.product().getShop(),
                Collectors.mapping(qp -> qp, Collectors.toSet())
            ));

        Map<String, BalanceHolder> shopBalanceMap = new ConcurrentHashMap<>();

        shopsWithProducts.forEach((shop, products) -> {
            if (shop.isAdminShop()) return;

            BalanceHolder balanceHolder = shopBalanceMap.computeIfAbsent(shop.getId(), k -> new BalanceHolder());

            products.stream().map(ETransactionItem::product).map(Product::getCurrency).distinct().forEach(currency -> {
                shopBalanceFutures.add(shop.queryBalance(currency)
                    .thenAccept(result -> {
                        // -1 means no shop balance access/data.
                        if (result >= 0) {
                            balanceHolder.store(currency, result);
                        }
                    })
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    })
                );
            });
        });

        return CompletableFuture.allOf(shopBalanceFutures.toArray(new CompletableFuture[0])).thenApply(v -> {
            Map<String, BalanceHolder> shopSellWorthMap = new HashMap<>();

            return shopsWithProducts.entrySet().stream().allMatch(entry -> {
                Shop shop = entry.getKey();
                if (shop.isAdminShop()) return true;

                BalanceHolder shopBalance = shopBalanceMap.get(shop.getId());
                if (shopBalance == null || shopBalance.isEmpty()) return false; // No balance data.

                BalanceHolder worthHolder = shopSellWorthMap.computeIfAbsent(shop.getId(), k -> new BalanceHolder());
                Set<ETransactionItem> products = entry.getValue();

                for (ETransactionItem quantified : products) {
                    worthHolder.storeAll(quantified.price());

                    if (!shopBalance.beatsAll(worthHolder)) {
                        if (transaction.isStrict()) {
                            return false;
                        }

                        transaction.getItems().remove(quantified.product());
                        transaction.getLooseItems().add(quantified);
                    }
                }

                return true;
            });
        });
    }

    protected PlaceholderContext.@NonNull Builder addTransactionPlaceholderContext(PlaceholderContext.@NonNull Builder builder,
                                                                                   @NonNull ECompletedTransaction transaction) {
        return builder
            .with(ShopPlaceholders.GENERIC_WORTH, () -> transaction.worth().format(Lang.OTHER_PRICE_DELIMITER.text()))
            .with(ShopPlaceholders.GENERIC_TOTAL_AMOUNT, () -> String.valueOf(transaction.countTotalAmount()))
            .with(ShopPlaceholders.GENERIC_TOTAL_UNITS, () -> String.valueOf(transaction.countTotalUnits()))
            .with(ShopPlaceholders.GENERIC_SIZE, () -> String.valueOf(transaction.items().size()))
            .with(ShopPlaceholders.GENERIC_LOOSE_SIZE, () -> String.valueOf(transaction.looseItems().size()))
            .with(ShopPlaceholders.GENERIC_PRODUCTS, () -> formatTransactionProducts(transaction.items()))
            .with(ShopPlaceholders.GENERIC_LOOSE, () -> formatTransactionProducts(transaction.looseItems()));
    }

    @NonNull
    protected static String formatTransactionProducts(@NonNull Collection<ETransactionItem> items) {
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
            .collect(Collectors.joining("\n"));
    }
}
