package su.nightexpress.nexshop.hooks.external;

import com.gmail.justisroot.broker.Broker;
import com.gmail.justisroot.broker.BrokerAPI;
import com.gmail.justisroot.broker.record.PurchaseRecord;
import com.gmail.justisroot.broker.record.SaleRecord;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import java.math.BigDecimal;
import java.util.*;

public class BrokerHook extends AbstractHook<ExcellentShop> {

    private VirtualShopBroker virtualShopBroker;

    public BrokerHook(@NotNull ExcellentShop plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        VirtualShopModule virtualShop = plugin.getVirtualShop();
        if (virtualShop != null) {
            this.virtualShopBroker = new VirtualShopBroker(virtualShop);
            plugin.getServer().getScheduler().runTaskLater(plugin, c -> {
                BrokerAPI.current().register(this.virtualShopBroker);
            }, 2L);
        }

        return true;
    }

    @Override
    public void shutdown() {
        if (this.virtualShopBroker != null) {
            BrokerAPI.current().unregister(this.virtualShopBroker);
            this.virtualShopBroker = null;
        }
    }

    class VirtualShopBroker implements Broker<ItemStack> {

        private final VirtualShopModule virtualShop;

        private static final String NO_PRODUCT = "No product available";

        public VirtualShopBroker(@NotNull VirtualShopModule virtualShop) {
            this.virtualShop = virtualShop;
        }

        @Override
        public String getId() {
            return plugin.getNameRaw() + "_broker_virtual";
        }

        @Override
        public String getProvider() {
            return plugin.getName();
        }

        @Override
        public Class<ItemStack> getType() {
            return ItemStack.class;
        }

        @Nullable
        private Product<?, ?, ?> getBestProduct(@NotNull Player player, @NotNull TradeType tradeType,
                                                @NotNull ItemStack item, int amount) {
            Set<Product<?, ?, ?>> products = new HashSet<>();
            this.virtualShop.getShops().stream()
                    .filter(shop -> shop.hasPermission(player) && shop.isTransactionEnabled(tradeType)).forEach(shop -> {
                products.addAll(shop.getProducts().stream().filter(product -> {
                    if (!product.isItemMatches(item)) return false;
                    if (tradeType == TradeType.BUY && !product.isBuyable()) return false;
                    if (tradeType == TradeType.SELL && !product.isSellable()) return false;
                    if (tradeType == TradeType.SELL && product.countItem(player) < amount) return false;
                    if (product.getStock().getPossibleAmount(tradeType, player) < amount) return false;
                    return true;
                }).toList());
            });

            Comparator<Product<?, ?, ?>> comp = (p1, p2) -> {
                return (int) (p1.getPricer().getPrice(tradeType) - p2.getPricer().getPrice(tradeType));
            };

            return (tradeType == TradeType.BUY ? products.stream().min(comp) : products.stream().max(comp)).orElse(null);
        }

        @Override
        public boolean canBeBought(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack) {
            return this.getBuyPrice(playerId, worldId, itemStack, 1).isPresent();
        }

        @Override
        public boolean canBeSold(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack) {
            return this.getSellPrice(playerId, worldId, itemStack, 1).isPresent();
        }

        @Override
        public Optional<BigDecimal> getBuyPrice(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack, int amount) {
            Player player = playerId.isEmpty() ? null : plugin.getServer().getPlayer(playerId.get());
            if (player == null) return Optional.empty();

            Product<?, ?, ?> product = this.getBestProduct(player, TradeType.BUY, itemStack, amount);
            return Optional.ofNullable(product == null ? null : BigDecimal.valueOf(product.getPricer().getPrice(TradeType.BUY)));
        }

        @Override
        public Optional<BigDecimal> getSellPrice(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack, int amount) {
            Player player = playerId.isEmpty() ? null : plugin.getServer().getPlayer(playerId.get());
            if (player == null) return Optional.empty();

            Product<?, ?, ?> product = this.getBestProduct(player, TradeType.SELL, itemStack, amount);
            return Optional.ofNullable(product == null ? null : BigDecimal.valueOf(product.getPricer().getPrice(TradeType.SELL)));
        }

        @Override
        public PurchaseRecord<ItemStack> buy(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack, int amount) {
            PurchaseRecord.PurchaseRecordBuilder<ItemStack> builder = PurchaseRecord.start(this, itemStack, playerId, worldId).setVolume(amount);
            if (!canBeBought(playerId, worldId, itemStack)) return builder.buildFailure(NO_PRODUCT);

            Player player = playerId.isEmpty() ? null : plugin.getServer().getPlayer(playerId.get());
            Optional<BigDecimal> value = getBuyPrice(playerId, worldId, itemStack, amount);
            if (player == null || value.isEmpty()) return builder.buildFailure(NO_PRODUCT);

            Product<?, ?, ?> product = this.getBestProduct(player, TradeType.BUY, itemStack, amount);
            if (product == null) return builder.buildFailure(NO_PRODUCT);

            PreparedProduct<?> prepared = product.getPrepared(TradeType.BUY);
            prepared.setAmount(amount);

            return builder.setValue(value.get()).buildSuccess(() -> prepared.trade(player, false));
        }

        @Override
        public SaleRecord<ItemStack> sell(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack, int amount) {
            SaleRecord.SaleRecordBuilder<ItemStack> builder = SaleRecord.start(this, itemStack, playerId, worldId).setVolume(amount);
            if (!canBeSold(playerId, worldId, itemStack)) return builder.buildFailure(NO_PRODUCT);

            Player player = playerId.isEmpty() ? null : plugin.getServer().getPlayer(playerId.get());
            Optional<BigDecimal> value = getSellPrice(playerId, worldId, itemStack, amount);
            if (player == null || value.isEmpty()) return builder.buildFailure(NO_PRODUCT);

            Product<?, ?, ?> product = this.getBestProduct(player, TradeType.SELL, itemStack, amount);
            if (product == null) return builder.buildFailure(NO_PRODUCT);

            PreparedProduct<?> prepared = product.getPrepared(TradeType.SELL);
            prepared.setAmount(amount);

            return builder.setValue(value.get()).buildSuccess(() -> prepared.trade(player, false));
        }

        @Override
        public String getDisplayName(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack) {
            return ItemUtil.getItemName(itemStack);
        }

        @Override
        public boolean handlesPurchases(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack) {
            return true;
        }

        @Override
        public boolean handlesSales(Optional<UUID> playerId, Optional<UUID> worldId, ItemStack itemStack) {
            return true;
        }
    }
}
