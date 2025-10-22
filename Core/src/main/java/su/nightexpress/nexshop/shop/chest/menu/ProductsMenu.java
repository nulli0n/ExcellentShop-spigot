package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ProductsMenu extends LinkedMenu<ShopPlugin, ProductsMenu.Data> implements Filled<ChestProduct>, ConfigBased {

    private final ChestShopModule module;

    private int[] productSlots;
    private int[] highlightSlots;

    private NightItem freeSlotIcon;
    private NightItem lockedSlotIcon;
    private NightItem highlightIcon;

    public ProductsMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Shop Products"));
        this.module = module;
    }

    public record Data(@NotNull ChestShop shop, ChestProduct product, int index){

        public boolean test(@NotNull Predicate<ChestProduct> predicate) {
            return this.product != null && predicate.test(this.product);
        }

        public boolean hasProduct() {
            return this.product != null;
        }
    }

    public void open(@NotNull Player player, @NotNull ChestShop shop) {
        ChestProduct product = null;
        int index = -1;

        List<ChestProduct> products = new ArrayList<>(shop.getProducts());
        if (!products.isEmpty()) {
            product = products.getFirst();
            index = 0;
        }

        this.open(player, shop, product, index);
    }

    private void open(@NotNull Player player, @NotNull ChestShop shop, @Nullable ChestProduct product, int index) {
        this.open(player, new Data(shop, product, index));
    }

    @Override
    @NotNull
    public MenuFiller<ChestProduct> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player).shop;

        return MenuFiller.builder(this)
            .setSlots(this.productSlots)
            .setItems(shop.getProducts())
            .setItemCreator(product -> {
                return NightItem.fromItemStack(product.getPreviewOrPlaceholder());
            })
            .setItemClick(product -> (viewer1, event) -> {
                int index = Lists.indexOf(this.productSlots, event.getRawSlot());
                this.runNextTick(() -> this.open(player, shop, product, index));
            })
            .build();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        ChestShop shop = data.shop;
        int slotIndex = data.index;

        int maxProducts = ChestUtils.getProductLimit(player);
        if (maxProducts < 0) maxProducts = this.productSlots.length;

        int shopProducts = shop.countProducts();

        for (int index = shopProducts; index < this.productSlots.length; index++) {
            int productCount = index + 1;
            int slot = this.productSlots[index];

            NightItem icon = (productCount > maxProducts ? this.lockedSlotIcon : this.freeSlotIcon).copy();

            this.addItem(viewer, icon.hideAllComponents().toMenuItem().setSlots(slot));
        }

        if (slotIndex >= 0 && slotIndex < this.highlightSlots.length) {
            int highlightSlot = this.highlightSlots[slotIndex];
            this.addItem(viewer, this.highlightIcon.copy().toMenuItem().setSlots(highlightSlot).setPriority(10));
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);

        if (!result.isInventory()) return;

        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player).shop;

        int maxProducts = ChestUtils.getProductLimit(player);
        int hasProducts = shop.getProducts().size();
        boolean canAdd = maxProducts < 0 || hasProducts < maxProducts;
        if (!canAdd) return;

        ItemStack item = result.getItemStack();
        if (item == null || item.getType().isAir()) return;

        ChestProduct product = shop.createProduct(player, item, event.isShiftClick());
        if (product == null) return;

        shop.markDirty();
        shop.updateStockCache();
        this.module.getDisplayManager().remake(shop);

        List<ChestProduct> products = new ArrayList<>(shop.getProducts());
        int index = products.indexOf(product);
        if (index < 0) return;

        this.runNextTick(() -> this.open(player, shop, product, index));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        ChestShop shop = data.shop;
        ChestProduct product = data.product;

        item.replacement(replacer -> {
            replacer.replace(shop.replacePlaceholders());
            if (product != null) replacer.replace(product.replacePlaceholders(player));
        });
    }

    private void handleReturn(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.module.openShopSettings(viewer.getPlayer(), this.getLink(viewer).shop));
    }

    private void handlePrice(@NotNull MenuViewer viewer, @NotNull TradeType tradeType) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        ChestShop shop = data.shop;
        ChestProduct product = data.product;

        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_PRICE.text(), input -> {
            if (product.getPricing() instanceof FlatPricing pricing) {
                pricing.setPrice(tradeType, input.asDoubleAbs());
                product.updatePrice(false);
            }
            shop.markDirty();
            this.updateCartGUI(product);
            return true;
        }));
    }

    // Force update cart GUI when there is price changes. (Added by request)
    private void updateCartGUI(@NotNull ChestProduct product) {
        MenuRegistry.getViewers().forEach(viewer -> {
            if (viewer.getMenu() instanceof CartMenu cartMenu && cartMenu.getLink(viewer).source().getProduct() == product) {
                cartMenu.flush(viewer);
            }
        });
    }

    private void handlePriceOff(@NotNull MenuViewer viewer, @NotNull TradeType tradeType) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        ChestShop shop = data.shop;
        ChestProduct product = data.product;

        if (product.getPricing() instanceof FlatPricing pricing) {
            pricing.setPrice(tradeType, ProductPricing.DISABLED);
            product.updatePrice(false);
        }
        shop.markDirty();
        this.module.getDisplayManager().remake(shop); // Remake due to hologram size changes.

        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleCurrency(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        ChestShop shop = data.shop;
        ChestProduct product = data.product;

        List<Currency> currencies = new ArrayList<>(this.module.getAvailableCurrencies(player));
        if (currencies.isEmpty()) return;

        int index = currencies.indexOf(product.getCurrency()) + 1;
        if (index >= currencies.size()) index = 0;

        product.setCurrencyId(currencies.get(index).getInternalId());
        shop.markDirty();

        this.runNextTick(() -> this.flush(viewer));
    }

    private void handleRestock(@NotNull MenuViewer viewer, boolean all) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        ChestProduct product = data.product;

        if (all) {
            int units = product.countUnits(player);
            if (units == 0) return;

            this.module.depositToShop(player, product, units);
            this.runNextTick(() -> this.flush(viewer));
            return;
        }

        this.handleInput(Dialog.builder(player, input -> {
            int amount = input.asIntAbs();
            this.module.depositToShop(player, product, amount);
            return true;
        }).setPrompt(Lang.EDITOR_GENERIC_ENTER_AMOUNT.text()));
    }

    private void handleWithdraw(@NotNull MenuViewer viewer, boolean all) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        ChestProduct product = data.product;

        if (all) {
            int units = product.countUnitAmount();
            this.module.withdrawFromShop(player, product, units);
            this.runNextTick(() -> this.flush(viewer));
            return;
        }

        this.handleInput(Dialog.builder(player, input -> {
            int amount = input.asIntAbs();
            this.module.withdrawFromShop(player, product, amount);
            return true;
        }).setPrompt(Lang.EDITOR_GENERIC_ENTER_AMOUNT.text()));
    }

    private void handleRemove(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        ChestShop shop = data.shop;
        ChestProduct product = data.product;

        if (product.countStock(TradeType.BUY, null) > 0) {
            ChestLang.EDITOR_ERROR_PRODUCT_LEFT.message().send(player);
            return;
        }

        UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer1, event) -> {
                shop.removeProduct(product.getId());
                shop.markDirty();
                this.module.getDisplayManager().remake(shop);
                this.runNextTick(() -> this.module.openProductsMenu(player, shop));
            })
            .onReturn((viewer1, event) -> {
                this.runNextTick(() -> this.open(player, shop, product, data.index));
            })
            .build());
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.productSlots = ConfigValue.create("Product.ItemSlots", new int[]{9,18,27}).read(config);
        this.highlightSlots = ConfigValue.create("Product.HighlightSlots", new int[]{10,19,28}).read(config);

        this.freeSlotIcon = ConfigValue.create("Product.Icon.FreeSlot", NightItem.fromType(Material.LIME_DYE)
            .setDisplayName(GREEN.wrap(BOLD.wrap("Free Slot")))
            .setLore(Lists.newList(
                GRAY.wrap("Click an item in " + GREEN.wrap(UNDERLINED.wrap("your inventory"))),
                GRAY.wrap("to add it to the shop.")
            ))
            .hideAllComponents()
        ).read(config);

        this.lockedSlotIcon = ConfigValue.create("Product.Icon.LockedSlot", NightItem.fromType(Material.PINK_DYE)
            .setDisplayName(RED.wrap(BOLD.wrap("Locked Slot")))
            .setLore(Lists.newList(
                GRAY.wrap("Upgrade your " + RED.wrap(UNDERLINED.wrap("/rank"))),
                GRAY.wrap("to unlock more slots.")
            ))
            .hideAllComponents()
        ).read(config);

        this.highlightIcon = ConfigValue.create("Product.Icon.Highlight", NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
            .setDisplayName(GREEN.wrap("← " + BOLD.wrap("Selected Product")))
            .hideAllComponents()
        ).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> this.handleReturn(viewer)).setPriority(10).build());

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(1,10,19,28,37,45,46,47,48,49,50,51,52,53));
        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(0,36));

        // Compatibility with old data.
        loader.addHandler(new ItemHandler("price_advanced", (viewer, event) -> {
            this.runNextTick(() -> this.module.openAdvancedPriceMenu(viewer.getPlayer(), this.getLink(viewer).product));
        }, ItemOptions.builder()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct())
            .build()
        ));

        // Currency button.

        loader.addDefaultItem(NightItem.fromType(Material.EMERALD)
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("Currency")) + " " + GRAY.wrap("[" + WHITE.wrap(PRODUCT_CURRENCY) + "]"))
            .setLore(Lists.newList(
                GRAY.wrap("Currency used for sales and purchases."),
                " ",
                LIGHT_GREEN.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .toMenuItem()
            .setPriority(10)
            .setSlots(23)
            .setHandler(new ItemHandler("set_currency", (viewer, event) -> this.handleCurrency(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct() && this.module.getAvailableCurrencies(viewer.getPlayer()).size() > 1)
                    .build()
            )));

        // Buy price buttons.

        loader.addDefaultItem(NightItem.fromType(Material.FIREWORK_STAR)
            .setDisplayName(GREEN.wrap(BOLD.wrap("Buy Price")) + " " + GRAY.wrap("[" + WHITE.wrap("Unset") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap("You haven't set the buy price."),
                " ",
                GREEN.wrap("→ " + UNDERLINED.wrap("Click to set"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(12)
            .setHandler(new ItemHandler("add_buy_price", (viewer, event) -> this.handlePrice(viewer, TradeType.BUY),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).test(product -> !product.hasBuyPrice()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.FIREWORK_STAR)
            .setDisplayName(GREEN.wrap(BOLD.wrap("Buy Price")) + " " + GRAY.wrap("[" + WHITE.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)) + "]"))
            .setLore(Lists.newList(
                GRAY.wrap("Players will pay you " + GREEN.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)) + " for"),
                GRAY.wrap("each " + GREEN.wrap("x" + PRODUCT_UNIT_AMOUNT + " " + PRODUCT_PREVIEW_NAME) + " purchased."),
                " ",
                GREEN.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .setColor(Color.fromRGB(0, 255, 0))
            .toMenuItem()
            .setPriority(10)
            .setSlots(12)
            .setHandler(new ItemHandler("change_buy_price", (viewer, event) -> this.handlePrice(viewer, TradeType.BUY),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).test(ChestProduct::hasBuyPrice))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.REPEATER)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Disable Buying")))
            .setLore(Lists.newList(
                GRAY.wrap("Makes this item unbuyable for players."),
                " ",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to toggle"))
            ))
            .toMenuItem()
            .setPriority(10)
            .setSlots(13)
            .setHandler(new ItemHandler("disable_buying", (viewer, event) -> this.handlePriceOff(viewer, TradeType.BUY),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.DISABLE_BUYING) && this.getLink(viewer).test(ChestProduct::hasBuyPrice))
                    .build()
            )));

        // Sell price buttons.

        loader.addDefaultItem(NightItem.fromType(Material.FIREWORK_STAR)
            .setDisplayName(RED.wrap(BOLD.wrap("Sell Price")) + " " + GRAY.wrap("[" + WHITE.wrap("Unset") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap("You haven't set the sell price."),
                " ",
                RED.wrap("→ " + UNDERLINED.wrap("Click to set"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(15)
            .setHandler(new ItemHandler("add_sell_price", (viewer, event) -> this.handlePrice(viewer, TradeType.SELL),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.DISABLE_SELLING) && this.getLink(viewer).test(product -> !product.hasSellPrice()))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.FIREWORK_STAR)
            .setDisplayName(RED.wrap(BOLD.wrap("Sell Price")) + " " + GRAY.wrap("[" + WHITE.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)) + "]"))
            .setLore(Lists.newList(
                GRAY.wrap("You'll pay players " + RED.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)) + " for"),
                GRAY.wrap("each " + RED.wrap("x" + PRODUCT_UNIT_AMOUNT + " " + PRODUCT_PREVIEW_NAME) + " sold."),
                " ",
                RED.wrap("→ " + UNDERLINED.wrap("Click to change"))
            ))
            .hideAllComponents()
            .setColor(Color.RED)
            .toMenuItem()
            .setPriority(10)
            .setSlots(15)
            .setHandler(new ItemHandler("change_sell_price", (viewer, event) -> this.handlePrice(viewer, TradeType.SELL),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).test(AbstractProduct::hasSellPrice))
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.REPEATER)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Disable Selling")))
            .setLore(Lists.newList(
                GRAY.wrap("Makes this item unsellable for players."),
                " ",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to toggle"))
            ))
            .toMenuItem()
            .setPriority(10)
            .setSlots(16)
            .setHandler(new ItemHandler("disable_selling", (viewer, event) -> this.handlePriceOff(viewer, TradeType.SELL),
                ItemOptions.builder()
                .setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.DISABLE_SELLING) && this.getLink(viewer).test(ChestProduct::hasSellPrice))
                .build()
            )));

        // Stock buttons.

        loader.addDefaultItem(NightItem.fromType(Version.isAtLeast(Version.MC_1_21_4) ? Material.LIGHT_BLUE_BUNDLE : Material.BUNDLE)
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Restock")) + " " + GRAY.wrap("[" + WHITE.wrap("Custom Amount") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_BLUE.wrap("✔ ") + "Stock: " + LIGHT_BLUE.wrap(PRODUCT_AMOUNT) + "/" + LIGHT_BLUE.wrap(PRODUCT_CAPACITY)),
                "",
                GRAY.wrap("Restock the product with " + LIGHT_BLUE.wrap("desired")),
                GRAY.wrap("amount from your inventory."),
                "",
                LIGHT_BLUE.wrap("→ " + UNDERLINED.wrap("Click to restock"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(30)
            .setHandler(new ItemHandler("restock_custom", (viewer, event) -> this.handleRestock(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct() && !this.getLink(viewer).shop.isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.WATER_BUCKET)
            .setDisplayName(LIGHT_BLUE.wrap(BOLD.wrap("Restock")) + " " + GRAY.wrap("[" + WHITE.wrap("All") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_BLUE.wrap("✔ ") + "Stock: " + LIGHT_BLUE.wrap(PRODUCT_AMOUNT) + "/" + LIGHT_BLUE.wrap(PRODUCT_CAPACITY)),
                "",
                GRAY.wrap("Restock the product with " + LIGHT_BLUE.wrap("all")),
                GRAY.wrap("possible amount from your inventory."),
                "",
                LIGHT_BLUE.wrap("→ " + UNDERLINED.wrap("Click to restock"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(31)
            .setHandler(new ItemHandler("restock_all", (viewer, event) -> this.handleRestock(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct() && !this.getLink(viewer).shop.isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Version.isAtLeast(Version.MC_1_21_4) ? Material.ORANGE_BUNDLE : Material.BUNDLE)
            .setDisplayName(LIGHT_ORANGE.wrap(BOLD.wrap("Withdraw")) + " " + GRAY.wrap("[" + WHITE.wrap("Custom Amount") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_ORANGE.wrap("✔ ") + "Stock: " + LIGHT_ORANGE.wrap(PRODUCT_AMOUNT) + "/" + LIGHT_ORANGE.wrap(PRODUCT_SPACE)),
                "",
                GRAY.wrap("Withdraw " + LIGHT_ORANGE.wrap("desired amount") + " of the"),
                GRAY.wrap("product straight to your inventory."),
                "",
                LIGHT_ORANGE.wrap("→ " + UNDERLINED.wrap("Click to withdraw"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(33)
            .setHandler(new ItemHandler("withdraw_custom", (viewer, event) -> this.handleWithdraw(viewer, false),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct() && !this.getLink(viewer).shop.isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.LAVA_BUCKET)
            .setDisplayName(LIGHT_ORANGE.wrap(BOLD.wrap("Withdraw")) + " " + GRAY.wrap("[" + WHITE.wrap("All") + "]"))
            .setLore(Lists.newList(
                GRAY.wrap(LIGHT_ORANGE.wrap("✔ ") + "Stock: " + LIGHT_ORANGE.wrap(PRODUCT_AMOUNT) + "/" + LIGHT_ORANGE.wrap(PRODUCT_SPACE)),
                "",
                GRAY.wrap("Withdraw " + LIGHT_ORANGE.wrap("all possible") + " amount"),
                GRAY.wrap("of the product straight to your inventory."),
                "",
                LIGHT_ORANGE.wrap("→ " + UNDERLINED.wrap("Click to withdraw"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(34)
            .setHandler(new ItemHandler("withdraw_all", (viewer, event) -> this.handleWithdraw(viewer, true),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct() && !this.getLink(viewer).shop.isAdminShop())
                    .build()
            )));

        loader.addDefaultItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("Remove Product")))
            .setLore(Lists.newList(
                GRAY.wrap("Removes this product from the shop."),
                " ",
                LIGHT_RED.wrap("→ " + UNDERLINED.wrap("Click to remove"))
            ))
            .toMenuItem()
            .setPriority(10)
            .setSlots(53)
            .setHandler(new ItemHandler("remove_item", (viewer, event) -> this.handleRemove(viewer),
                ItemOptions.builder()
                    .setVisibilityPolicy(viewer -> this.getLink(viewer).hasProduct())
                    .build()
            )));
    }
}
