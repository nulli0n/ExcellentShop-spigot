package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;

import java.util.*;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nexmedia.engine.utils.Colors2.*;

public class CartMenu extends ConfigMenu<ExcellentShop> {

    private final Map<Player, PreparedProduct> products;
    private final Map<Player, Double>          balance;

    private int[]    productSlots;
    private UniSound productSound;

    private enum ButtonType {
        CONFIRM, DECLINE, ADD, SET, TAKE, SET_CUSTOM
    }

    public CartMenu(@NotNull ExcellentShop plugin) {
        super(plugin, JYML.loadOrExtract(plugin, Config.DIR_MENU + "product_cart.yml"));
        this.products = new WeakHashMap<>();
        this.balance = new WeakHashMap<>();

        this.registerHandler(ButtonType.class)
            .addClick(ButtonType.CONFIRM, (viewer, event) -> {
                this.getPrepared(viewer).ifPresent(prepared -> {
                    prepared.trade();
                    this.plugin.runTask(task -> {
                        int page = 1;
                        if (prepared.getProduct() instanceof StaticProduct virtualProduct) {
                            page = virtualProduct.getPage();
                        }

                        if (Config.GENERAL_CLOSE_GUI_AFTER_TRADE.get()) {
                            viewer.getPlayer().closeInventory();
                        }
                        else prepared.getShop().open(viewer.getPlayer(), page);
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

                this.getPrepared(viewer).ifPresent(prepared -> {
                    prepared.setUnits(prepared.getUnits() + cartItem.getUnits());
                    this.open(viewer.getPlayer(), prepared);
                });
            })
            .addClick(ButtonType.SET, (viewer, event) -> {
                MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
                if (!(menuItem instanceof ShopCartItem cartItem)) return;

                this.getPrepared(viewer).ifPresent(prepared -> {
                    prepared.setUnits(cartItem.getUnits());
                    this.open(viewer.getPlayer(), prepared);
                });
            })
            .addClick(ButtonType.TAKE, (viewer, event) -> {
                MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
                if (!(menuItem instanceof ShopCartItem cartItem)) return;

                this.getPrepared(viewer).ifPresent(prepared -> {
                    prepared.setUnits(prepared.getUnits() - cartItem.getUnits());
                    this.open(viewer.getPlayer(), prepared);
                });
            })
            .addClick(ButtonType.SET_CUSTOM, (viewer, event) -> {
                PreparedProduct prepared = this.getPrepared(viewer).orElse(null);
                if (prepared == null) return;

                Player player = viewer.getPlayer();
                this.plugin.runTask(task -> {
                    player.closeInventory();

                    plugin.getMessage(Lang.SHOP_CART_ENTER_AMOUNT).send(player);
                    EditorManager.startEdit(player, wrapper -> {
                        prepared.setUnits(wrapper.asInt());
                        this.open(player, prepared);
                        return true;
                    });
                });
            });

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            PreparedProduct prepared = this.getPrepared(viewer).orElse(null);
            if (prepared == null) return;

            Player player = viewer.getPlayer();
            Product shopProduct = prepared.getProduct();
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
        if (menuItem.getType() instanceof ButtonType) {
            ShopCartItem cartItem = new ShopCartItem(menuItem);
            cartItem.setUnits(cfg.getInt(path + ".Units"));
            return cartItem;
        }
        return menuItem;
    }

    @Override
    protected void writeItem(@NotNull MenuItem menuItem, @NotNull String path) {
        super.writeItem(menuItem, path);
        if (menuItem instanceof ShopCartItem cartItem) {
            cfg.set(path + ".Units", cartItem.getUnits());
        }
    }

    @Override
    public boolean open(@NotNull Player player, int page) {
        if (!this.products.containsKey(player)) {
            throw new IllegalStateException("Attempt to open an empty Shop Cart Menu!");
        }
        return super.open(player, page);
    }

    public void open(@NotNull Player player, @NotNull PreparedProduct prepared) {
        this.products.put(player, prepared);
        this.validateAmount(player, prepared);
        this.open(player, 1);
    }

    @NotNull
    private Optional<PreparedProduct> getPrepared(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        PreparedProduct prepared = this.products.get(player);
        return Optional.ofNullable(prepared);
    }

    private int getCartInventorySpace(@NotNull PreparedProduct prepared) {
        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int fullSize = stackSize * this.productSlots.length;

        return fullSize / prepared.getProduct().getUnitAmount();
    }

    private void validateAmount(@NotNull MenuViewer viewer) {
        PreparedProduct prepared = this.getPrepared(viewer).orElse(null);
        if (prepared == null) return;

        Player player = viewer.getPlayer();
        this.validateAmount(player, prepared);
    }

    private void validateAmount(@NotNull Player player, @NotNull PreparedProduct prepared) {
        Product product = prepared.getProduct();
        Shop shop = product.getShop();

        this.productSound.play(player);
        TradeType tradeType = prepared.getTradeType();

        int hasAmount = prepared.getUnits();
        int capacityInventory = Integer.MAX_VALUE;
        int capacityCart = this.getCartInventorySpace(prepared);
        int capacityProduct = product.getAvailableAmount(player, prepared.getTradeType());//shop.getStock().count(player, product, prepared.getTradeType());
        double shopBalance = shop instanceof ChestShop chestShop ? chestShop.getOwnerBank().getBalance(product.getCurrency()) : -1D;
        double userBalance = product.getCurrency().getHandler().getBalance(player);

        if (product.getPacker() instanceof ItemPacker itemPacker) {
            if (tradeType == TradeType.BUY) {
                // Allow to buy no more than player can carry.
                capacityInventory = itemPacker.countSpace(player.getInventory())/*PlayerUtil.countItemSpace(player, item)*/ / product.getUnitAmount();
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

        PreparedProduct prepared = this.getPrepared(viewer).orElse(null);
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

    @Override
    public boolean isCodeCreation() {
        return true;
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions("Product Cart", 54, InventoryType.CHEST);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack acceptItem = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=");
        ItemUtil.mapMeta(acceptItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN + BOLD + "Accept");
            meta.setLore(Arrays.asList(
                LIGHT_GREEN + BOLD + "Details:",
                LIGHT_GREEN + "▪ " + LIGHT_GRAY + "Quantity: " + LIGHT_GREEN + GENERIC_UNITS,
                LIGHT_GREEN + "▪ " + LIGHT_GRAY + "Total Amount: " + LIGHT_GREEN + GENERIC_AMOUNT,
                LIGHT_GREEN + "▪ " + LIGHT_GRAY + "Price: " + LIGHT_GREEN + GENERIC_PRICE,
                LIGHT_GREEN + "▪ " + LIGHT_GRAY + "Balance: " + LIGHT_GREEN + GENERIC_BALANCE
            ));
        });
        list.add(new MenuItem(acceptItem).setPriority(100).setType(ButtonType.CONFIRM).setSlots(53));

        ItemStack cancelItem = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==");
        ItemUtil.mapMeta(cancelItem, meta -> {
            meta.setDisplayName(LIGHT_RED + BOLD + "Cancel");
        });
        list.add(new MenuItem(cancelItem).setPriority(100).setType(ButtonType.DECLINE).setSlots(45));

        ItemStack paneItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemUtil.mapMeta(paneItem, meta -> {

        });
        list.add(new MenuItem(paneItem).setPriority(1).setSlots(46,47,48,49,50,51,52));

        for (ButtonType buttonType : new ButtonType[] {ButtonType.ADD, ButtonType.TAKE, ButtonType.SET}) {

            int[] units = buttonType == ButtonType.SET ? new int[] {1, 16, 10000} : new int[] {1, 8, 16};
            int[] slots = buttonType == ButtonType.ADD ? new int[] {42, 43, 44} : buttonType == ButtonType.TAKE ?  new int[] {36, 37, 38} : new int[] {39, 40, 41};
            String[] textures = buttonType == ButtonType.ADD ?
                new String[]{
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ2NWNlODNmMWFhNWI2ZTg0ZjliMjMzNTk1MTQwZDViNmJlY2ViNjJiNmQwYzY3ZDFhMWQ4MzYyNWZmZCJ9fX0=",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJlZTEzNzFkOGYwZjVhOGI3NTljMjkxODYzZDcwNGFkYzQyMWFkNTE5ZjE3NDYyYjg3NzA0ZGJmMWM3OGE0In19fQ==",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTlmNjJhNDY5YTIwNmFkZDczODg3YzczNjYzNzZhNmM0ZjMzNzdiMmY1Yjk3OTM1MWU5NmFjNjM0NTcyIn19fQ=="
                } : buttonType == ButtonType.TAKE ?
                new String[]{
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQyNDU0ZTRjNjdiMzIzZDViZTk1M2I1YjNkNTQxNzRhYTI3MTQ2MDM3NGVlMjg0MTBjNWFlYWUyYzExZjUifX19",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4MzQ0MGM2NDQ3YzE5NWFhZjc2NGUyN2ExMjU5MjE5ZTkxYzZkOGFiNmJkODlhMTFjYThkMmNjNzk5ZmE4In19fQ==",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWUzZTRiYzcxZTNhYzMzMDgzNjE4MWVkYTk2YmM2ZjEyOGU1YzUzMTNhYjk1MmM4ZmY2ZGVkNTQ5ZTEzYTUifX19"
                } :
                new String[] {
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY2MTI2OTczNWYxZTQ0NmJlY2ZmMjVmOWNiM2M4MjM2Nzk3MTlhMTVmN2YwZmJjOWEwMzkxMWE2OTJiZGQifX19",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc3YTU2Y2U0MTVkN2MzMDgwODcwNmE5NGNjMmJhZmE4OTdjYjdlNDg2Mjg3YzMzN2E0NGFmNDJiOTI4YzQzIn19fQ==",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNhNDg3YjFmODFjOWVjYzZlMTg4NTdjNjU2NjUyOWU3ZWZhMjNlZWY1OTgxNGZlNTdkNjRkZjhlMmNmMSJ9fX0="
                };
            String namePrefix = buttonType == ButtonType.ADD ? LIGHT_GREEN + BOLD + "Add +" : buttonType == ButtonType.TAKE ? LIGHT_RED + BOLD + "Remove -" : LIGHT_BLUE + BOLD + "Set ";

            for (int index = 0; index < 3; index++) {
                int unitAmount = units[index];
                String name = namePrefix + (unitAmount == 10000 ? "Max" : String.valueOf(unitAmount));

                ItemStack cartStack = ItemUtil.createCustomHead(textures[index]);
                ItemUtil.mapMeta(cartStack, meta -> meta.setDisplayName(name));

                ShopCartItem cartItem = new ShopCartItem(new MenuItem(cartStack).setPriority(100).setSlots(slots[index]));
                cartItem.setType(buttonType);
                cartItem.setUnits(units[index]);
                list.add(cartItem);
            }
        }

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.productSlots = JOption.create("Product.Slots",
            JYML::getIntArray,
            IntStream.range(0, 36).toArray()
        ).setWriter(JYML::setIntArray).read(cfg);

        this.productSound = JOption.create("Product.Sound",
            UniSound.of(Sound.ENTITY_ITEM_PICKUP),
            "Sets sound to play when using 'ADD', 'SET' or 'TAKE' buttons.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html"
        ).read(cfg);
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
