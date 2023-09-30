package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class ShopCartMenu extends ConfigMenu<ExcellentShop> {

    private final int[]    productSlots;
    private final UniSound productSound;

    private final Map<Player, PreparedProduct<?>> products;
    private final Map<Player, Double>             balance;

    private enum ButtonType {
        CONFIRM, DECLINE, ADD, SET, TAKE
    }

    public ShopCartMenu(@NotNull ExcellentShop plugin) {
        super(plugin, JYML.loadOrExtract(plugin, Config.DIR_MENU + "product_cart.yml"));
        this.products = new WeakHashMap<>();
        this.balance = new WeakHashMap<>();
        this.productSlots = cfg.getIntArray("Product.Slots");
        this.productSound = JOption.create("Product.Sound", UniSound.of(Sound.ENTITY_ITEM_PICKUP),
            "Sets sound to play when using 'ADD', 'SET' or 'TAKE' buttons.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html").read(cfg);

        this.registerHandler(ButtonType.class)
            .addClick(ButtonType.CONFIRM, (viewer, event) -> {
                this.getPrepared(viewer).ifPresent(prepared -> {
                    prepared.trade();
                    this.plugin.runTask(task -> {
                        int page = 1;
                        if (prepared.getProduct() instanceof StaticProduct virtualProduct) {
                            page = virtualProduct.getPage();
                        }
                        prepared.getShop().open(viewer.getPlayer(), page);
                    });
                });
            })
            .addClick(ButtonType.DECLINE, (viewer, event) -> {
                this.getPrepared(viewer).ifPresent(prepared -> {
                    this.plugin.runTask(task -> {
                        int page = 1;
                        if (prepared.getProduct() instanceof StaticProduct virtualProduct) {
                            page = virtualProduct.getPage();
                        }
                        prepared.getShop().open(viewer.getPlayer(), page);
                    });
                });
            })
            .addClick(ButtonType.ADD, (viewer, event) -> {
                MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
                if (!(menuItem instanceof ShopCartItem cartItem)) return;

                this.getPrepared(viewer).ifPresent(prepared -> prepared.setUnits(prepared.getUnits() + cartItem.getUnits()));
                this.validateAmount(viewer);
                this.plugin.runTask(task -> this.open(viewer.getPlayer(), 1));
            })
            .addClick(ButtonType.SET, (viewer, event) -> {
                MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
                if (!(menuItem instanceof ShopCartItem cartItem)) return;

                this.getPrepared(viewer).ifPresent(prepared -> prepared.setUnits(cartItem.getUnits()));
                this.validateAmount(viewer);
                this.plugin.runTask(task -> this.open(viewer.getPlayer(), 1));
            })
            .addClick(ButtonType.TAKE, (viewer, event) -> {
                MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
                if (!(menuItem instanceof ShopCartItem cartItem)) return;

                this.getPrepared(viewer).ifPresent(prepared -> prepared.setUnits(prepared.getUnits() - cartItem.getUnits()));
                this.validateAmount(viewer);
                this.plugin.runTask(task -> this.open(viewer.getPlayer(), 1));
            });

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            PreparedProduct<?> prepared = this.getPrepared(viewer).orElse(null);
            if (prepared == null) return;

            Player player = viewer.getPlayer();
            Product<?, ?, ?> shopProduct = prepared.getProduct();
            Currency currency = shopProduct.getCurrency();

            PlaceholderMap placeholderMap = new PlaceholderMap();
            placeholderMap.getKeys().addAll(currency.getPlaceholders().getKeys());
            placeholderMap.getKeys().addAll(prepared.getPlaceholders().getKeys());
            placeholderMap.add(Placeholders.GENERIC_BALANCE, () -> currency.format(this.balance.computeIfAbsent(player, k -> currency.getHandler().getBalance(player))));
            ItemUtil.replace(item, placeholderMap.replacer());

            if (Config.GUI_PLACEHOLDER_API.get() && EngineUtils.hasPlaceholderAPI()) {
                ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
            }
        }));
    }

    @Override
    @NotNull
    protected MenuItem readItem(@NotNull String path) {
        MenuItem menuItem = super.readItem(path);
        if (menuItem.getType() instanceof ButtonType buttonType) {
            ShopCartItem cartItem = new ShopCartItem(menuItem);
            cartItem.setUnits(cfg.getInt(path + ".Units"));
            return cartItem;
        }
        return menuItem;
    }

    @Override
    public boolean open(@NotNull Player player, int page) {
        if (!this.products.containsKey(player)) {
            throw new IllegalStateException("Attempt to open an empty Shop Cart Menu!");
        }
        return super.open(player, page);
    }

    public void open(@NotNull Player player, @NotNull PreparedProduct<?> prepared) {
        this.products.put(player, prepared);
        this.open(player, 1);
    }

    @NotNull
    private Optional<PreparedProduct<?>> getPrepared(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        PreparedProduct<?> prepared = this.products.get(player);
        return Optional.ofNullable(prepared);
    }

    private int getCartInventorySpace(@NotNull PreparedProduct<?> prepared) {
        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int fullSize = stackSize * this.productSlots.length;

        return fullSize / prepared.getProduct().getUnitAmount();
    }

    private void validateAmount(@NotNull MenuViewer viewer) {
        PreparedProduct<?> prepared = this.getPrepared(viewer).orElse(null);
        if (prepared == null) return;

        Player player = viewer.getPlayer();
        Product<?, ?, ?> product = prepared.getProduct();
        Shop<?, ?> shop = product.getShop();

        this.productSound.play(player);
        TradeType tradeType = prepared.getTradeType();

        int hasAmount = prepared.getUnits();
        int capacityInventory = Integer.MAX_VALUE;
        int capacityCart = this.getCartInventorySpace(prepared);
        int capacityProduct = product.getStock().getPossibleAmount(prepared.getTradeType(), player);
        double shopBalance = shop instanceof ChestShop chestShop ? chestShop.getOwnerBank().getBalance(product.getCurrency()) : -1D;
        double userBalance = product.getCurrency().getHandler().getBalance(player);

        ItemProduct itemProduct = null;
        if (product instanceof ItemProduct ip) itemProduct = ip;
        else if (product instanceof VirtualProduct<?, ?> vp && vp.getSpecific() instanceof ItemProduct ip) itemProduct = ip;

        if (itemProduct != null) {
            ItemStack item = itemProduct.getItem();
            if (tradeType == TradeType.BUY) {
                // Allow to buy no more than player can carry.
                capacityInventory = PlayerUtil.countItemSpace(player, item) / product.getUnitAmount();
            }
            else if (tradeType == TradeType.SELL) {
                // Allow to sell no more than player have in inventory.
                capacityInventory = product.countUnits(player);
            }
        }

        // Min. amount of product capacity and selected amount.
        hasAmount = (capacityProduct >= 0 && capacityProduct < hasAmount) ? capacityProduct : hasAmount;
        // Min. amount of cart size and selected amount.
        hasAmount = Math.min(capacityCart, hasAmount);
        // Min. amount of player/shop inventory size and selected amount.
        hasAmount = Math.min(capacityInventory, hasAmount);

        prepared.setUnits(hasAmount);

        // Allow to select for sell no more than shop can afford.
        if (tradeType == TradeType.SELL && shopBalance >= 0) {
            while (prepared.getPrice() > shopBalance && prepared.getUnits() > 1) {
                prepared.setUnits(prepared.getUnits() - 1);
            }
        }
        // Allow to select for buy no more than buyer can afford.
        if (tradeType == TradeType.BUY) {
            while (prepared.getPrice() > userBalance && prepared.getUnits() > 1) {
                prepared.setUnits(prepared.getUnits() - 1);
            }
        }
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        PreparedProduct<?> prepared = this.getPrepared(viewer).orElse(null);
        if (prepared == null) return;

        Player player = viewer.getPlayer();
        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int preparedAmount = prepared.getUnits() * prepared.getProduct().getUnitAmount();
        int count = 0;
        while (preparedAmount > 0 && count < this.productSlots.length) {
            ItemStack preview2 = prepared.getProduct().getPreview();
            preview2.setAmount(Math.min(preparedAmount, stackSize));

            preparedAmount -= stackSize;
            this.addWeakItem(player, preview2, this.productSlots[count++]);
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Player player = viewer.getPlayer();
        this.balance.remove(player);
        this.products.remove(player);
        super.onClose(viewer, event);
    }

    private static class ShopCartItem extends MenuItem {

        private int units;

        public ShopCartItem(@NotNull MenuItem menuItem) {
            super(menuItem.getItem(), menuItem.getPriority(), menuItem.getOptions(), menuItem.getSlots());
            this.setClick(menuItem.getClick());
        }

        public int getUnits() {
            return units;
        }

        public void setUnits(int units) {
            this.units = Math.abs(units);
        }
    }
}
