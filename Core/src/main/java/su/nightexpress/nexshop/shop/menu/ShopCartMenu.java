package su.nightexpress.nexshop.shop.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;

import java.util.Map;
import java.util.WeakHashMap;

public class ShopCartMenu extends AbstractMenu<ExcellentShop> {

    private final int[] productSlots;

    private final Map<Player, PreparedProduct<?>> products;
    private final Map<Player, Double>             balance;

    enum ButtonType {
        ADD, SET, TAKE
    }

    public ShopCartMenu(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull TradeType buyType) {
        super(plugin, cfg, "");
        this.products = new WeakHashMap<>();
        this.balance = new WeakHashMap<>();
        this.productSlots = cfg.getIntArray("Product.Slots");

        IMenuClick click = (player, type, e) -> {
            PreparedProduct<?> prepared = this.products.get(player);
            if (prepared == null) {
                player.closeInventory();
                return;
            }

            Product<?, ?, ?> product = prepared.getProduct();
            Shop<?, ?> shop = product.getShop();

            if (type instanceof MenuItemType type2) {
                switch (type2) {
                    case CONFIRMATION_ACCEPT, CONFIRMATION_DECLINE -> {
                        if (type2 == MenuItemType.CONFIRMATION_ACCEPT) prepared.trade(player, false);
                        int page = 1;
                        if (product instanceof VirtualProduct) {
                            page = ((VirtualProduct) product).getPage();
                        }
                        shop.open(player, page);
                    }
                    default -> {}
                }
                return;
            }

            if (type instanceof ButtonType type2) {
                switch (type2) {
                    case ADD, SET, TAKE -> {
                        IMenuItem menuItem = this.getItem(player, e.getRawSlot());
                        if (!(menuItem instanceof ShopCartMenuItem cartMenuItem)) return;

                        int btnAmount = cartMenuItem.getProductAmount();
                        int hasAmount = prepared.getAmount();

                        if (type2 == ButtonType.SET) hasAmount = btnAmount;
                        else hasAmount += btnAmount;

                        MessageUtil.sound(player, Config.SOUND_CART_ADDITEM);
                        TradeType tradeType = prepared.getTradeType();

                        int capacitySpace = Integer.MAX_VALUE;
                        int capacityCart = this.getMaxPossibleAmount(prepared);
                        int capacityProduct = product.getStock().getPossibleAmount(prepared.getTradeType(), player);//product.getStockAmountLeft(player, prepared.getTradeType());
                        double shopBalance = shop.getBank().getBalance(product.getCurrency());
                        double userBalance = product.getCurrency().getBalance(player);

                        if (product.hasItem()) {
                            ItemStack item = product.getItem();
                            if (tradeType == TradeType.BUY) {
                                // Allow to buy no more than player can carry.
                                capacitySpace = PlayerUtil.countItemSpace(player, item);
                            }
                            else {
                                // Allow to sell no more than chest shop can carry.
                                if (shop instanceof ChestShop shopChest) {
                                    int chestSpace = product.getStock().getLeftAmount(TradeType.SELL);
                                    if (chestSpace >= 0) capacitySpace = chestSpace;

                                    //shopBalance = shopChest.getBank().getBalance(product.getCurrency());
                                }
                                // Allow to sell no more than player have in inventory.
                                if (tradeType == TradeType.SELL) {
                                    int userHas = PlayerUtil.countItem(player, item);
                                    capacityCart = Math.min(capacityCart, userHas);
                                }
                            }
                        }

                        // Min. amount of product capacity and selected amount.
                        hasAmount = (capacityProduct >= 0 && capacityProduct < hasAmount) ? capacityProduct : hasAmount;
                        // Min. amount of cart size and selected amount.
                        hasAmount = Math.min(capacityCart, hasAmount);
                        // Min. amount of player/shop inventory size and selected amount.
                        hasAmount = Math.min(capacitySpace, hasAmount);

                        prepared.setAmount(hasAmount);

                        // Allow to select for sell no more than shop can afford.
                        if (tradeType == TradeType.SELL && shopBalance >= 0) {
                            while (prepared.getPrice() > shopBalance && prepared.getAmount() > 1) {
                                prepared.setAmount(prepared.getAmount() - 1);
                            }
                        }
                        // Allow to select for buy no more than buyer can afford.
                        if (tradeType == TradeType.BUY) {
                            while (prepared.getPrice() > userBalance && prepared.getAmount() > 1) {
                                prepared.setAmount(prepared.getAmount() - 1);
                            }
                        }

                        // Re-open to replace placeholders.
                        this.open(player, 1);
                    }
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String id : cfg.getSection("Amount_Buttons")) {
            ShopCartMenuItem menuItem = new ShopCartMenuItem(cfg.getMenuItem("Amount_Buttons." + id, ButtonType.class));

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
                menuItem.setProductAmount(cfg.getInt("Amount_Buttons." + id + ".Product_Amount"));
            }
            this.addItem(menuItem);
        }

        cfg.saveChanges();
    }

    @Override
    public void open(@NotNull Player player, int page) {
        if (!this.products.containsKey(player)) {
            throw new IllegalStateException("Attempt to open an empty Shop Cart Menu!");
        }
        super.open(player, page);
    }

    public void open(@NotNull Player player, @NotNull PreparedProduct<?> prepared) {
        this.products.put(player, prepared);
        this.open(player, 1);
    }

    private int getMaxPossibleAmount(@NotNull PreparedProduct<?> prepared) {
        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        return stackSize * this.productSlots.length;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        PreparedProduct<?> prepared = this.products.get(player);
        if (prepared == null) return;

        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int preparedAmount = prepared.getAmount();
        int count = 0;
        while (preparedAmount > 0 && count < this.productSlots.length) {
            ItemStack preview2 = prepared.getProduct().getPreview();
            preview2.setAmount(Math.min(preparedAmount, stackSize));

            preparedAmount -= stackSize;
            this.addItem(player, preview2, this.productSlots[count++]);
        }
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.balance.remove(player);
        this.products.remove(player);
        super.onClose(player, e);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        PreparedProduct<?> prepared = this.products.get(player);
        if (prepared == null) return;

        Product<?, ?, ?> shopProduct = prepared.getProduct();
        ICurrency currency = shopProduct.getCurrency();

        ItemStack preview = shopProduct.getPreview();
        int amount = prepared.getAmount();
        int stacks = (int) ((double) amount / (double) preview.getType().getMaxStackSize());
        double balance = this.balance.computeIfAbsent(player, k -> currency.getBalance(player));

        ItemUtil.replace(item, line -> currency.replacePlaceholders().apply(line)
                .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount))
                .replace("%amount_stacks%", String.valueOf(stacks))
                .replace(Placeholders.GENERIC_PRICE, currency.format(prepared.getPrice()))
                .replace(Placeholders.GENERIC_BALANCE, currency.format(balance))
        );
    }
}
