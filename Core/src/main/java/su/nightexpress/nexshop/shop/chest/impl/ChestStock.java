package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.impl.AbstractStock;
import su.nightexpress.nexshop.shop.util.ShopUtils;

import java.util.stream.Stream;

public class ChestStock extends AbstractStock<ChestShop, ChestProduct> {

    public ChestStock(@NotNull ExcellentShop plugin, @NotNull ChestShop shop) {
        super(plugin, shop);
    }

    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull ChestProduct product) {
        PlaceholderMap placeholderMap = new PlaceholderMap();
        for (TradeType tradeType : TradeType.values()) {
            placeholderMap
                .add(Placeholders.PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), () -> {
                    int leftAmount = this.getShop().getStock().countItem(product, tradeType);
                    return leftAmount < 0 ? LangManager.getPlain(Lang.OTHER_INFINITY) : String.valueOf(leftAmount);
                });
        }
        return placeholderMap;
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        if (!(result.getProduct() instanceof ChestProduct product)) return;

        TradeType tradeType = event.getTransaction().getTradeType();
        int amount = event.getTransaction().getUnits();
        //Player player = event.getPlayer();

        if (!this.getShop().isAdminShop()) {
            if (tradeType == TradeType.BUY) {
                this.consume(product, amount, tradeType);
            }
            else this.store(product, amount, tradeType);
        }
    }

    @Override
    @Nullable
    protected ChestProduct findProduct(@NotNull Product product) {
        return this.getShop().getProductById(product.getId());
    }

    @Override
    public void load() {

    }

    /*@Override
    public int countItem(@NotNull Player player, @NotNull ChestProduct product, @NotNull TradeType type) {
        return this.count(product, type);
    }*/

    @Override
    public int countItem(@NotNull ChestProduct product, @NotNull TradeType type) {
        if (this.getShop().isAdminShop()) return -1;
        if (!(product.getPacker() instanceof ItemPacker packer)) return -1;

        Inventory inventory = this.getShop().getInventory();

        // Для покупки со стороны игрока, возвращаем количество реальных предметов в контейнере.
        if (type == TradeType.BUY) {
            double totalItems = Stream.of(inventory.getContents()).filter(has -> has != null && packer.isItemMatches(has))
                .mapToInt(ItemStack::getAmount).sum();
            return (int) Math.floor(totalItems / (double) product.getUnitAmount());
        }
        // Для продажи со стороны игрока, возвращаем количество в свободных и идентичных стопках для предмета.
        else {
            ItemStack item = packer.getItem();
            double totalSlots = (int) Stream.of(inventory.getContents())
                .filter(itemHas -> itemHas == null || itemHas.getType().isAir() || packer.isItemMatches(itemHas)).count();
            double totalSpace = totalSlots * (double) item.getMaxStackSize();
            int unitsSpace = (int) Math.ceil(totalSpace / (double) product.getUnitAmount());

            return unitsSpace - this.count(product, TradeType.BUY);
        }
    }

    @Override
    public boolean consumeItem(@NotNull ChestProduct product, int amount, @NotNull TradeType type) {
        if (!(product.getPacker() instanceof ItemPacker packer)) return false;

        //ItemStack item = packer.getItem();
        //item.setAmount(Math.abs(amount * product.getUnitAmount()));
        Inventory inventory = this.getShop().getInventory();
        amount = Math.abs(amount * product.getUnitAmount());

        //inventory.removeItem(item);

        ShopUtils.takeItem(inventory, packer::isItemMatches, amount);
        return true;
    }

    @Override
    public boolean storeItem(@NotNull ChestProduct product, int amount, @NotNull TradeType type) {
        if (!(product.getPacker() instanceof ItemPacker packer)) return false;

        //ItemStack item = handler.getItem();
        //item.setAmount(Math.abs(amount * product.getUnitAmount()));

        Inventory inventory = this.getShop().getInventory();
        amount = Math.abs(amount * product.getUnitAmount());

        //inventory.addItem(item);

        ShopUtils.addItem(inventory, packer.getItem(), amount);
        return true;
    }

    @Override
    public boolean restockItem(@NotNull ChestProduct product, @NotNull TradeType type, boolean force) {
        return false;
    }
}
