package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.dialog.DialogManager;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.bukkit.NightSound;

import java.util.function.Function;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class CartMenu extends LinkedMenu<ShopPlugin, Breadcumb<PreparedProduct>> implements ConfigBased {

    private int[]      productSlots;
    private NightSound productSound;

    public CartMenu(@NotNull ShopPlugin plugin, @NotNull FileConfig config) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Product Cart"));

        this.load(config);
    }

    private void onAmountClick(@NotNull MenuViewer viewer, @NotNull Function<Integer, Integer> function) {
        PreparedProduct product = this.getLink(viewer).source();

        product.setUnits(function.apply(product.getUnits()));
        this.productSound.play(viewer.getPlayer());

        this.flush(viewer);
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
        double userBalance = product.getCurrency().getBalance(player);

        if (product.getType() instanceof PhysicalTyping itemPacker) {
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
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Breadcumb<PreparedProduct> bread = this.getLink(viewer);
        if (bread == null) return;

        PreparedProduct prepared = bread.source();
        Player player = viewer.getPlayer();
        this.validateAmount(player, prepared);

        ItemStack preview = prepared.getProduct().getPreview();
        int stackSize = preview.getType().getMaxStackSize();
        int preparedAmount = prepared.getAmount();// prepared.getUnits() * prepared.getProduct().getUnitAmount();
        int count = 0;

        while (preparedAmount > 0 && count < this.productSlots.length) {
            NightItem display = NightItem.fromItemStack(preview).setAmount(Math.min(preparedAmount, stackSize));

            preparedAmount -= stackSize;

            this.addItem(viewer, display.toMenuItem().setSlots(this.productSlots[count++]));
        }
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        // Do not apply on product items.
        if (viewer.hasItem(menuItem)) return;

        Player player = viewer.getPlayer();
        PreparedProduct prepared = this.getLink(player).source();
        Currency currency = prepared.getProduct().getCurrency();

        item.replacement(replacer -> replacer
            .replace(currency.replacePlaceholders())
            .replace(prepared.replacePlaceholders())
            .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(currency.getBalance(player)))
        );
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        int[] buttonAmounts = ConfigValue.create("Product.Amount_Buttons", new int[]{1,8,16,32,64},
            "Registers 'add', 'set' and 'take' button types for specific amounts.",
            "By default registers 'add_1', 'add_8', 'add_16', 'add_32', 'add_64' button types (the same for 'set' and 'take' ones)."
        ).read(config);

        this.productSlots = ConfigValue.create("Product.Slots", IntStream.range(0, 36).toArray()).read(config);

        this.productSound = ConfigValue.create("Product.Sound",
            NightSound.of(Sound.ENTITY_ITEM_PICKUP),
            "Sets sound to play when using amount buttons."
        ).read(config);

        loader.addDefaultItem(NightItem.asCustomHead(SKIN_CHECK_MARK)
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("Accept")))
            .setLore(Lists.newList(
                LIGHT_GREEN.wrap(BOLD.wrap("Details:")),
                LIGHT_GREEN.wrap("▪ " + LIGHT_GRAY.wrap("Quantity: ") + GENERIC_UNITS),
                LIGHT_GREEN.wrap("▪ " + LIGHT_GRAY.wrap("Total Amount: ") + GENERIC_AMOUNT),
                LIGHT_GREEN.wrap("▪ " + LIGHT_GRAY.wrap("Price: ") + GENERIC_PRICE),
                LIGHT_GREEN.wrap("▪ " + LIGHT_GRAY.wrap("Balance: ") + GENERIC_BALANCE)
            ))
            .toMenuItem()
            .setPriority(50)
            .setSlots(53)
            .setHandler(new ItemHandler("confirm", (viewer, event) -> {
                Player player = viewer.getPlayer();
                var breadcumb = this.getLink(player);
                int page = breadcumb.page();
                PreparedProduct preparedProduct = breadcumb.source();
                Product product = preparedProduct.getProduct();

                if (product.isAvailable(player)) {
                    preparedProduct.trade();
                }

                if (Config.GENERAL_CLOSE_GUI_AFTER_TRADE.get()) {
                    this.runNextTick(player::closeInventory);
                }
                else {
                    this.runNextTick(() -> product.getShop().open(viewer.getPlayer(), page, true));
                }
            }))
        );

        loader.addDefaultItem(NightItem.asCustomHead(SKIN_WRONG_MARK)
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("Cancel")))
            .toMenuItem()
            .setPriority(50)
            .setSlots(45)
            .setHandler(new ItemHandler("decline", (viewer, event) -> {
                Player player = viewer.getPlayer();
                var breadcumb = this.getLink(player);
                Product product = breadcumb.source().getProduct();
                int page = breadcumb.page();

                this.runNextTick(() -> product.getShop().open(player, page, true));
            }))
        );

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).toMenuItem().setSlots(46,47,48,49,50,51,52));

        // Add Buttons

        loader.addDefaultItem(NightItem.asCustomHead("6d65ce83f1aa5b6e84f9b233595140d5b6beceb62b6d0c67d1a1d83625ffd")
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("+1")))
            .toMenuItem().setSlots(42).setHandler(new ItemHandler("add_1"))
        );

        loader.addDefaultItem(NightItem.asCustomHead("f2ee1371d8f0f5a8b759c291863d704adc421ad519f17462b87704dbf1c78a4")
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("+8")))
            .toMenuItem().setSlots(43).setHandler(new ItemHandler("add_8"))
        );

        loader.addDefaultItem(NightItem.asCustomHead("19f62a469a206add73887c7366376a6c4f3377b2f5b979351e96ac634572")
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("+16")))
            .toMenuItem().setSlots(44).setHandler(new ItemHandler("add_16"))
        );

        // Take Buttons

        loader.addDefaultItem(NightItem.asCustomHead("8d2454e4c67b323d5be953b5b3d54174aa271460374ee28410c5aeae2c11f5")
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("-1")))
            .toMenuItem().setSlots(36).setHandler(new ItemHandler("take_1"))
        );

        loader.addDefaultItem(NightItem.asCustomHead("1683440c6447c195aaf764e27a1259219e91c6d8ab6bd89a11ca8d2cc799fa8")
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("-8")))
            .toMenuItem().setSlots(37).setHandler(new ItemHandler("take_8"))
        );

        loader.addDefaultItem(NightItem.asCustomHead("ae3e4bc71e3ac330836181eda96bc6f128e5c5313ab952c8ff6ded549e13a5")
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("-16")))
            .toMenuItem().setSlots(38).setHandler(new ItemHandler("take_16"))
        );

        // Set Buttons

        loader.addDefaultItem(NightItem.asCustomHead("bd21b0bafb89721cac494ff2ef52a54a18339858e4dca99a413c42d9f88e0f6")
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Set to 1")))
            .toMenuItem().setSlots(39).setHandler(new ItemHandler("set_1"))
        );

        loader.addDefaultItem(NightItem.asCustomHead("d5e1be33374fd7b2bae9c8fc9146b6ed3eedcb1476b3b7b8010f5f44bfa843e")
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Set to Max.")))
            .toMenuItem().setSlots(40).setHandler(new ItemHandler("set_max", (viewer, event) -> {
                this.onAmountClick(viewer, current -> 10000);
            }))
        );

        loader.addDefaultItem(NightItem.asCustomHead("117f3666d3cedfae57778c78230d480c719fd5f65ffa2ad3255385e433b86e")
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Custom Amount")))
            .toMenuItem().setSlots(41).setHandler(new ItemHandler("set_custom", (viewer, event) -> {
                Player player = viewer.getPlayer();
                var breadcumb = this.getLink(viewer);
                this.cache.addAnchor(player);

                DialogManager.startDialog(Dialog.builder(viewer, input -> {
                    breadcumb.source().setUnits(input.asIntAbs(1));
                    this.open(player, breadcumb);
                    return true;
                }).setPrompt(Lang.SHOP_CART_ENTER_AMOUNT).build().setLastMenu(null));

                this.runNextTick(player::closeInventory);
            }))
        );

        // This will override 'empty' handlers of 'addDefaultItem' items.
        for (int amount : buttonAmounts) {
            loader.addHandler("add_" + amount, (viewer, event) -> {
                this.onAmountClick(viewer, current -> current + amount);
            });

            loader.addHandler("take_" + amount, (viewer, event) -> {
                this.onAmountClick(viewer, current -> current - amount);
            });

            loader.addHandler("set_" + amount, (viewer, event) -> {
                this.onAmountClick(viewer, current -> amount);
            });
        }
    }
}
