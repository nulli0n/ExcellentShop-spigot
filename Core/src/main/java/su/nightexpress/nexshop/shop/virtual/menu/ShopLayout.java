package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.product.price.impl.RangedPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class ShopLayout extends LinkedMenu<ShopPlugin, VirtualShop> implements ConfigBased {

    private static final String TITLE_COLOR = "#3E3E3E";

    private final VirtualShopModule module;
    private final Currency currency;

    public ShopLayout(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull FileConfig config) {
        super(plugin, MenuType.GENERIC_9X6, HEX_COLOR.wrap("Shop → " + SHOP_NAME + " (" + WHITE.wrap(GENERIC_PAGE) + "/" + WHITE.wrap(GENERIC_PAGES) + ")", TITLE_COLOR));
        this.module = module;
        this.currency = module.getDefaultCurrency();
        this.setApplyPlaceholderAPI(true);

        this.load(config);
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        // Do not apply on product items.
        if (viewer.hasItem(menuItem)) return;

        item.replacement(replacer -> {
            replacer
                .replace(this.getLink(viewer).replacePlaceholders())
                .replace(GENERIC_PAGE, () -> String.valueOf(viewer.getPage()))
                .replace(GENERIC_PAGES, () -> String.valueOf(viewer.getPages()))
                .replace(GENERIC_BALANCE, () -> currency.format(currency.getBalance(viewer.getPlayer())))
                .replace(Placeholders.GENERIC_SELL_MULTIPLIER, () -> NumberUtil.format(VirtualShopModule.getSellMultiplier(viewer.getPlayer())));
        });
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        return Replacer.create()
            .replace(this.getLink(viewer).replacePlaceholders())
            .replacePlaceholderAPI(viewer.getPlayer())
            .replace(GENERIC_PAGE, () -> String.valueOf(viewer.getPage()))
            .replace(GENERIC_PAGES, () -> String.valueOf(viewer.getPages()))
            .apply(this.title);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        VirtualShop shop = this.getLink(viewer);
        int page = viewer.getPage();

        this.displayStatic(shop, viewer, page);
        this.displayRotating(shop, viewer, page);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    private void displayStatic(@NotNull VirtualShop shop, @NotNull MenuViewer viewer, int page) {
        shop.getValidProducts().forEach(product -> {
            if (product.isRotating()) return;
            if (product.getPage() != page) return;

            this.addProductItem(shop, viewer, product, product.getSlot());
        });
    }

    private void displayRotating(@NotNull VirtualShop shop, @NotNull MenuViewer viewer, int page) {
        shop.getRotations().forEach(rotation -> {
            RotationData data = plugin.getDataManager().getRotationData(rotation);
            if (data == null) return;

            List<Integer> slots = new ArrayList<>(rotation.getSlots(page));
            int limit = slots.size();
            //int skip = (page - 1) * limit;

            //Map<Integer, List<String>> productsByPage = data.getProducts();

            List<String> productIds = data.getProducts().getOrDefault(page, Collections.emptyList());//.stream()/*.skip(skip)*/.limit(limit).toList();
            if (productIds.isEmpty()) return;

            int count = 0;
            for (String productId : productIds) {
                if (count >= limit) break;

                VirtualProduct product = shop.getProductById(productId);
                if (product == null) continue;
                if (!product.isRotating()) continue;
                if (!product.isValid()) continue;

                int slot = slots.get(count++);
                this.addProductItem(shop, viewer, product, slot);
            }
        });
    }

    private void addProductItem(@NotNull VirtualShop shop, @NotNull MenuViewer viewer, @NotNull VirtualProduct product, int slot) {
        Player player = viewer.getPlayer();
        ItemStack preview = product.getPreview();

        List<String> buyLore = product.isBuyable() ? VirtualConfig.PRODUCT_FORMAT_LORE_BUY.get() : Collections.emptyList();
        List<String> sellLore = product.isSellable() ? VirtualConfig.PRODUCT_FORMAT_LORE_SELL.get() : Collections.emptyList();
        List<String> discountLore = shop.hasDiscount(product) ? VirtualConfig.PRODUCT_FORMAT_LORE_DISCOUNT.get() : Collections.emptyList();
        List<String> noPermLore = !product.hasAccess(player) ? VirtualConfig.PRODUCT_FORMAT_LORE_NO_PERMISSION.get() : Collections.emptyList();

        List<String> loreFormat = Replacer.create()
            .replace(GENERIC_BUY, buyLore)
            .replace(GENERIC_SELL, sellLore)
            .replace(GENERIC_LORE, ItemUtil.getSerializedLore(preview))
            .replace(GENERIC_DISCOUNT, discountLore)
            .replace(GENERIC_PERMISSION, noPermLore)
            .apply(VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL.get());

        Replacer loreReplacer = Replacer.create();

        for (TradeType tradeType : TradeType.values()) {
            String stockPlaceholder = Placeholders.STOCK_TYPE.apply(tradeType);
            String limitPlaceholder = Placeholders.LIMIT_TYPE.apply(tradeType);
            String priceDynamicPlaceholder = Placeholders.PRICE_DYNAMIC.apply(tradeType);

            List<String> stockLore = new ArrayList<>();
            List<String> limitLore = new ArrayList<>();
            List<String> priceDynamicLore = new ArrayList<>();
            if (!product.getStockValues().isUnlimited(tradeType)) {
                stockLore.addAll((tradeType == TradeType.BUY ? VirtualConfig.PRODUCT_FORMAT_LORE_STOCK_BUY : VirtualConfig.PRODUCT_FORMAT_LORE_STOCK_SELL).get());
            }
            if (!product.getLimitValues().isUnlimited(tradeType)) {
                limitLore.addAll((tradeType == TradeType.BUY ? VirtualConfig.PRODUCT_FORMAT_LORE_LIMIT_BUY : VirtualConfig.PRODUCT_FORMAT_LORE_LIMIT_SELL).get());
            }
            if (product.getPricer() instanceof RangedPricer) {
                priceDynamicLore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_PRICE_DYNAMIC.get().getOrDefault(tradeType, Collections.emptyList()));
            }

            loreReplacer
                .replace(priceDynamicPlaceholder, priceDynamicLore)
                .replace(stockPlaceholder, stockLore)
                .replace(limitPlaceholder, limitLore)
                .apply(loreFormat);
        }

        loreFormat = loreReplacer.apply(loreFormat);

        this.addItem(viewer, NightItem.fromItemStack(preview)
            .setHideComponents(false)
            .setLore(loreFormat)
            .replacement(replacer -> {
                replacer
                    .replace(product.replacePlaceholders(player))
                    .replace(product.getCurrency().replacePlaceholders())
                    .replace(shop.replacePlaceholders());
            })
            .toMenuItem()
            .setSlots(slot)
            .setPriority(Integer.MAX_VALUE)
            .setHandler((viewer1, event) -> {
                plugin.getShopManager().onProductClick(player, product, event.getClick(), this);
            })
        );
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1).setSlots(IntStream.range(0, 54).toArray()));
        //loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).toMenuItem().setSlots(IntStream.range(45, 54).toArray()));

        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> this.module.openMainMenu(viewer.getPlayer()));
        }).setPriority(10));

        loader.addDefaultItem(MenuItem.buildNextPage(this, 50).setPriority(10));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 48).setPriority(10));

        loader.addDefaultItem(NightItem.asCustomHead("9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d")
            .setDisplayName(HEX_COLOR.wrap(BOLD.wrap("SELL ALL"), "#ebd12a"))
            .setLore(Lists.newList(
                LIGHT_GRAY.wrap("Sells everything from your"),
                LIGHT_GRAY.wrap("inventory to all available shops."),
                "",
                LIGHT_GRAY.wrap(HEX_COLOR.wrap("➥", "#ebd12a") + " Sell Multiplier: " + HEX_COLOR.wrap("x" + GENERIC_SELL_MULTIPLIER, "#ebd12a")),
                "",
                //LIGHT_GRAY.wrap(HEX_COLOR.wrap("[▶]", "#ebd12a") + " Click to " + HEX_COLOR.wrap("sell all", "#ebd12a") + ".")
                HEX_COLOR.wrap("→ " + BOLD.wrap(UNDERLINED.wrap("CLICK")) + " to sell", "#ebd12a")
            ))
            .toMenuItem()
            .setSlots(52)
            .setPriority(10)
            .setHandler(new ItemHandler("sell_all", (viewer, event) -> {
                Player player = viewer.getPlayer();
                this.module.sellAll(player, player.getInventory(), this.getLink(player));
            })));

        loader.addDefaultItem(NightItem.asCustomHead("3324a7d61ccd44b031744b517f911a5c461614b953b17f648282e147b29d10e")
            .setDisplayName(HEX_COLOR.wrap(BOLD.wrap("BALANCE"), "#7cf1de"))
            .setLore(Lists.newList(
                LIGHT_GRAY.wrap("Here's displayed how much"),
                LIGHT_GRAY.wrap("money you have."),
                "",
                HEX_COLOR.wrap("➥", "#7cf1de") + " " + WHITE.wrap(GENERIC_BALANCE))
            )
            .toMenuItem().setSlots(46).setPriority(10)
        );

//        loader.addDefaultItem(NightItem.asCustomHead("5f96717bef61c37ce4dcd0b067da4b57c8a1b0f83c2926868b083444f7eade54")
//            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Wallet")))
//            .setLore(Lists.newList(
//                LIGHT_YELLOW.wrap("▪ " + LIGHT_GRAY.wrap("Balance: ") + GENERIC_BALANCE)
//            ))
//            .toMenuItem().setSlots(46).setPriority(10));
    }
}
