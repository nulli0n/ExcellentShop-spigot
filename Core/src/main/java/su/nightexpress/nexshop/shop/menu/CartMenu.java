package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
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
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.dialog.Dialog;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.tag.impl.ColorTag;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class CartMenu extends ConfigMenu<ShopPlugin> implements Linked<PreparedProduct> {

    //public static final String FILE_NAME = "product_cart.yml";

    private final ViewLink<PreparedProduct> link;

    private final ItemHandler confirmHandler;
    private final ItemHandler declineHandler;
    private final ItemHandler addHandler;
    private final ItemHandler setHandler;
    private final ItemHandler takeHandler;
    private final ItemHandler customHandler;

    private int[]    productSlots;
    private UniSound productSound;

    public CartMenu(@NotNull ShopPlugin plugin, @NotNull FileConfig config) {
        super(plugin, config);
        this.link = new ViewLink<>();

        this.addHandler(this.confirmHandler = new ItemHandler("confirm", (viewer, event) -> {
            Player player = viewer.getPlayer();
            PreparedProduct preparedProduct = this.getLink(viewer);
            Product product = preparedProduct.getProduct();

            preparedProduct.trade();

            if (Config.GENERAL_CLOSE_GUI_AFTER_TRADE.get()) {
                this.runNextTick(player::closeInventory);
            }
            else {
                int page = product instanceof StaticProduct staticProduct ? staticProduct.getPage() : 1;
                this.runNextTick(() -> product.getShop().open(viewer.getPlayer(), page));
            }
        }));

        this.addHandler(this.declineHandler = new ItemHandler("decline", (viewer, event) -> {
            Player player = viewer.getPlayer();
            Product product = this.getLink(player).getProduct();
            int page = product instanceof StaticProduct staticProduct ? staticProduct.getPage() : 1;

            this.runNextTick(() -> product.getShop().open(viewer.getPlayer(), page));
        }));

        this.addHandler(this.addHandler = new ItemHandler("add", (viewer, event) -> {
            this.onAmountClick(viewer, event, Integer::sum);
        }));

        this.addHandler(this.setHandler = new ItemHandler("set", (viewer, event) -> {
            this.onAmountClick(viewer, event, (has, units) -> units);
        }));

        this.addHandler(this.takeHandler = new ItemHandler("take", (viewer, event) -> {
            this.onAmountClick(viewer, event, (has, units) -> has - units);
        }));

        this.addHandler(this.customHandler = new ItemHandler("set_custom", (viewer, event) -> {
            Player player = viewer.getPlayer();
            PreparedProduct prepared = this.getLink(player);

            Dialog.create(player, (dialog, input) -> {
                prepared.setUnits(input.asInt());
                this.open(player, prepared);
                return true;
            }).setLastMenu(null);

            Lang.SHOP_CART_ENTER_AMOUNT.getMessage().send(player);

            this.runNextTick(player::closeInventory);
        }));

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            if (!item.hasItemMeta()) return;

            PreparedProduct prepared = this.getLink(viewer);
            Player player = viewer.getPlayer();
            Currency currency = prepared.getProduct().getCurrency();

            ItemReplacer replacer = ItemReplacer.create(item).readMeta()
                .replace(currency.getPlaceholders())
                .replace(prepared.getPlaceholders())
                .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(currency.getHandler().getBalance(player)));

            if (Config.GUI_PLACEHOLDER_API.get()) {
                replacer.replacePlaceholderAPI(player);
            }

            replacer.writeMeta();
        }));
    }

    @NotNull
    @Override
    public ViewLink<PreparedProduct> getLink() {
        return link;
    }

    private void onAmountClick(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull BiFunction<Integer, Integer, Integer> function) {
        MenuItem menuItem = this.getItem(viewer, event.getRawSlot());
        if (!(menuItem instanceof CartMenuItem cartItem)) return;

        PreparedProduct product = this.getLink(viewer);
        int units = cartItem.getUnits();

        product.setUnits(function.apply(product.getUnits(), units));
        this.productSound.play(viewer.getPlayer());

        this.flush(viewer);
    }

    @Override
    @NotNull
    protected MenuItem readItem(@NotNull String path) {
        MenuItem menuItem = super.readItem(path);

        if (this.cfg.contains(path + ".Units")) {
            CartMenuItem cartItem = new CartMenuItem(menuItem);
            cartItem.setUnits(cfg.getInt(path + ".Units"));
            return cartItem;
        }

        return menuItem;
    }

    @Override
    protected void writeItem(@NotNull MenuItem menuItem, @NotNull String path) {
        super.writeItem(menuItem, path);
        if (menuItem instanceof CartMenuItem cartItem) {
            cfg.set(path + ".Units", cartItem.getUnits());
        }
    }

    private int getCartInventorySpace(@NotNull PreparedProduct prepared) {
        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int fullSize = stackSize * this.productSlots.length;

        return fullSize / prepared.getProduct().getUnitAmount();
    }

    private void validateAmount(@NotNull Player player, @NotNull PreparedProduct prepared) {
        Product product = prepared.getProduct();
        Shop shop = product.getShop();

        TradeType tradeType = prepared.getTradeType();
        int hasAmount = prepared.getUnits();
        int capacityInventory = Integer.MAX_VALUE;
        int capacityCart = this.getCartInventorySpace(prepared);
        int capacityProduct = product.getAvailableAmount(player, prepared.getTradeType());
        double shopBalance = shop instanceof ChestShop chestShop ? chestShop.getOwnerBank().getBalance(product.getCurrency()) : -1D;
        double userBalance = product.getCurrency().getHandler().getBalance(player);

        if (product.getPacker() instanceof ItemPacker itemPacker) {
            if (tradeType == TradeType.BUY) {
                // Allow to buy no more than player can carry.
                capacityInventory = itemPacker.countSpace(player.getInventory()) / product.getUnitAmount();
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
        PreparedProduct prepared = this.getLink(viewer);
        Player player = viewer.getPlayer();
        this.validateAmount(player, prepared);

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
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Product Cart"), MenuSize.CHEST_54);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack acceptItem = ItemUtil.getSkinHead(SKIN_CHECK_MARK);
        ItemUtil.editMeta(acceptItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("Accept")));
            meta.setLore(Lists.newList(
                LIGHT_GREEN.enclose(BOLD.enclose("Details:")),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Quantity: ") + GENERIC_UNITS),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Total Amount: ") + GENERIC_AMOUNT),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + GENERIC_PRICE),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + GENERIC_BALANCE)
            ));
        });
        list.add(new MenuItem(acceptItem).setPriority(100).setHandler(this.confirmHandler).setSlots(53));


        ItemStack cancelItem = ItemUtil.getSkinHead(SKIN_WRONG_MARK);
        ItemUtil.editMeta(cancelItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("Cancel")));
        });
        list.add(new MenuItem(cancelItem).setPriority(100).setHandler(this.declineHandler).setSlots(45));


        ItemStack paneItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemUtil.editMeta(paneItem, meta -> {

        });
        list.add(new MenuItem(paneItem).setPriority(1).setSlots(46,47,48,49,50,51,52));


        for (ItemHandler handler : new ItemHandler[] {this.addHandler, this.takeHandler, this.setHandler}) {

            int[] units = handler == this.setHandler ? new int[] {1, 10000, -1} : new int[] {1, 8, 16};
            int[] slots = handler == this.addHandler ? new int[] {42, 43, 44} : handler == this.takeHandler ?  new int[] {36, 37, 38} : new int[] {39, 40, 41};
            String[] textures = handler == this.addHandler ?
                new String[]{
                    "6d65ce83f1aa5b6e84f9b233595140d5b6beceb62b6d0c67d1a1d83625ffd",
                    "f2ee1371d8f0f5a8b759c291863d704adc421ad519f17462b87704dbf1c78a4",
                    "19f62a469a206add73887c7366376a6c4f3377b2f5b979351e96ac634572"
                } : handler == this.takeHandler ?
                new String[]{
                    "8d2454e4c67b323d5be953b5b3d54174aa271460374ee28410c5aeae2c11f5",
                    "1683440c6447c195aaf764e27a1259219e91c6d8ab6bd89a11ca8d2cc799fa8",
                    "ae3e4bc71e3ac330836181eda96bc6f128e5c5313ab952c8ff6ded549e13a5"
                } :
                new String[] {
                    "bd21b0bafb89721cac494ff2ef52a54a18339858e4dca99a413c42d9f88e0f6",
                    "d5e1be33374fd7b2bae9c8fc9146b6ed3eedcb1476b3b7b8010f5f44bfa843e",
                    "117f3666d3cedfae57778c78230d480c719fd5f65ffa2ad3255385e433b86e"
                };

            String namePrefix = handler == this.addHandler ? "+" : handler == this.takeHandler ? "-" : "Set ";
            ColorTag tag = handler == this.addHandler ? LIGHT_GREEN : handler == this.takeHandler ? LIGHT_RED : LIGHT_BLUE;

            for (int index = 0; index < 3; index++) {
                int unitAmount = units[index];
                String name;
                if (unitAmount == 10000) {
                    name = namePrefix + "Max";
                }
                else if (unitAmount == -1) {
                    name = namePrefix + "Custom";
                    handler = this.customHandler;
                }
                else name = namePrefix + unitAmount;

                ItemStack cartStack = ItemUtil.getSkinHead(textures[index]);
                ItemUtil.editMeta(cartStack, meta -> meta.setDisplayName(tag.enclose(BOLD.enclose(name))));

                CartMenuItem cartItem = new CartMenuItem(new MenuItem(cartStack).setPriority(100).setSlots(slots[index]));
                cartItem.setHandler(handler);
                cartItem.setUnits(unitAmount);
                list.add(cartItem);
            }
        }

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.productSlots = ConfigValue.create("Product.Slots", IntStream.range(0, 36).toArray()).read(cfg);

        this.productSound = ConfigValue.create("Product.Sound",
            UniSound.of(Sound.ENTITY_ITEM_PICKUP),
            "Sets sound to play when using 'ADD', 'SET' or 'TAKE' buttons.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html"
        ).read(cfg);
    }
}
