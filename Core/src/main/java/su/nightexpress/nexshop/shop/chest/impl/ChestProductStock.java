package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.ProductStock;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;

import java.util.stream.Stream;

public class ChestProductStock extends ProductStock<ChestProduct> {

    public ChestProductStock() {
        this.placeholderMap
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.BUY);
                return leftAmount < 0 ? LangManager.getPlain(Lang.OTHER_INFINITY) : String.valueOf(leftAmount);
            })
            .add(Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_LEFT, () -> {
                int leftAmount = this.getLeftAmount(TradeType.SELL);
                return leftAmount < 0 ? LangManager.getPlain(Lang.OTHER_INFINITY) : String.valueOf(leftAmount);
            })
        ;
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        return this.getPlaceholders();
    }

    @NotNull
    private ChestShop getShop() {
        return this.getProduct().getShop();
    }

    @Override
    public void onPurchase(@NotNull ShopTransactionEvent<?> event) {
        TradeType tradeType = event.getResult().getTradeType();
        int amount = event.getResult().getUnits();
        Player player = event.getPlayer();

        if (!this.isUnlimited(StockType.GLOBAL, tradeType)) {
            int amountLeft = this.getLeftAmount(tradeType);
            this.setLeftAmount(tradeType, amountLeft - amount);
        }
    }

    @Override
    public int getInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return -1;
    }

    @Override
    public void setInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType, int amount) {

    }

    @Override
    public boolean isUnlimited(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return this.getShop().isAdminShop();
    }

    @Override
    public int getRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return 0;
    }

    @Override
    public void setRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType, int cooldown) {

    }

    @Override
    public int getPossibleAmount(@NotNull TradeType tradeType, @Nullable Player player) {
        return this.getLeftAmount(tradeType);
    }

    @Override
    public int getLeftAmount(@NotNull TradeType tradeType, @Nullable Player player) {
        if (this.getShop().isAdminShop()) return -1;

        Inventory inventory = this.getShop().getInventory();
        ChestProduct product = this.getProduct();

        // Для покупки со стороны игрока, возвращаем количество реальных предметов в контейнере.
        if (tradeType == TradeType.BUY) {
            double totalItems = Stream.of(inventory.getContents()).filter(has -> has != null && product.isItemMatches(has))
                .mapToInt(ItemStack::getAmount).sum();
            return (int) Math.floor(totalItems / (double) product.getUnitAmount());
        }
        // Для продажи со стороны игрока, возвращаем количество в свободных и идентичных стопках для предмета.
        else {
            ItemStack item = this.getProduct().getItem();
            double totalSlots = (int) Stream.of(inventory.getContents())
                .filter(itemHas -> itemHas == null || itemHas.getType().isAir() || product.isItemMatches(itemHas)).count();
            double totalSpace = totalSlots * (double) item.getMaxStackSize();
            int unitsSpace = (int) Math.ceil(totalSpace / (double) product.getUnitAmount());

            return unitsSpace - this.getLeftAmount(TradeType.BUY);
        }
    }

    @Override
    public void setLeftAmount(@NotNull TradeType tradeType, int amount, @Nullable Player player) {
        int amountHas = this.getLeftAmount(tradeType);
        if (tradeType == TradeType.BUY) {
            // Has: 10, Set: 20, Need to add 10 items
            // Has: 10, Set: 5, Need to remove 5 items
            amount = amount - amountHas;
        }
        else {
            // Has: 10 space, Set: 20, = -10 = Need to remove 10 items
            // Has: 10 space, Set: 5, = 5 = Need to add 5 items
            amount = amountHas - amount;
        }
        boolean isRemoval = amount < 0;

        ItemStack item = this.getProduct().getItem();
        item.setAmount(Math.abs(amount * this.getProduct().getUnitAmount()));
        Inventory inventory = this.getShop().getInventory();

        if (isRemoval) {
            inventory.removeItem(item);
        }
        else {
            inventory.addItem(item);
        }
    }

    @Override
    public long getRestockDate(@NotNull TradeType tradeType, @Nullable Player player) {
        return 0;
    }
}
