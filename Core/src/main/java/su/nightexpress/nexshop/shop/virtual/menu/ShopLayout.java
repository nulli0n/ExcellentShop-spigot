package su.nightexpress.nexshop.shop.virtual.menu;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.data.RotationData;
import su.nightexpress.nexshop.product.price.impl.RangedPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopLayout extends ConfigMenu<ShopPlugin> implements Linked<VirtualShop> {

    private final VirtualShopModule module;
    private final ViewLink<VirtualShop> link;

    private final ItemHandler returnHandler;
    private final ItemHandler sellAllHandler;

    public ShopLayout(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull FileConfig config) {
        super(plugin, config);
        this.module = module;
        this.applyPAPI = Config.usePlaceholdersForGUI();
        this.link = new ViewLink<>();

        config.options().setHeader(Lists.newList(
            "=".repeat(50),
            "Available Placeholders:",
            "- " + GENERIC_BALANCE + " -> Player's balance for default Virtual Shop currency.",
            "- " + Placeholders.GENERIC_SELL_MULTIPLIER + " -> Player's sell multiplier (set in VirtualShop settings.yml).",
            "- " + URL_WIKI_PLACEHOLDERS + " -> Placeholders of: Shop, Virtual Shop, Static/Rotating Shop.",
            "- " + Plugins.PLACEHOLDER_API + " -> Any of them. Enable PlaceholderAPI for GUIs in the plugin config.",
            "=".repeat(50)
        ));

//        this.addHandler(ItemHandler.forNextPage(this));
//        this.addHandler(ItemHandler.forPreviousPage(this));

        this.addHandler(new ItemHandler(ItemHandler.NEXT_PAGE, (viewer, event) -> {
            viewer.setPage(viewer.getPage() + 1);
            this.runNextTick(() -> module.openShop(viewer.getPlayer(), this.getLink(viewer), viewer.getPage()));
        }, viewer -> viewer.getPage() < viewer.getPages()));

        this.addHandler(new ItemHandler(ItemHandler.PREVIOUS_PAGE, (viewer, event) -> {
            viewer.setPage(viewer.getPage() - 1);
            this.runNextTick(() -> module.openShop(viewer.getPlayer(), this.getLink(viewer), viewer.getPage()));
        }, viewer -> viewer.getPage() > 1));

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> this.module.openMainMenu(viewer.getPlayer()));
        }));

        this.addHandler(this.sellAllHandler = new ItemHandler("sell_all", (viewer, event) -> {
            Player player = viewer.getPlayer();
            this.module.sellAll(player, player.getInventory(), this.getLink(player));
        }));

        this.load();

        Currency currency = module.getDefaultCurrency();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
            ItemReplacer.create(itemStack).readMeta().trimmed()
                .replace(this.getLink(viewer).replacePlaceholders())
                .replace(GENERIC_BALANCE, () -> currency.format(currency.getBalance(viewer.getPlayer())))
                .replace(Placeholders.GENERIC_SELL_MULTIPLIER, () -> NumberUtil.format(VirtualShopModule.getSellMultiplier(viewer.getPlayer())))
                .replacePlaceholderAPI(viewer.getPlayer())
                .writeMeta();
        }));
    }

    @NotNull
    @Override
    public ViewLink<VirtualShop> getLink() {
        return link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        Player player = viewer.getPlayer();
        VirtualShop shop = this.getLink(player);
        String title = options.getTitle();

        title = shop.replacePlaceholders().apply(title);

        if (Plugins.hasPlaceholderAPI()) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        }

        if (shop instanceof StaticShop staticShop) {
            this.displayStatic(staticShop, viewer);
        }
        else if (shop instanceof RotatingShop rotatingShop) {
            this.displayRotating(rotatingShop, viewer);
        }

        title = title
            .replace(GENERIC_PAGE, String.valueOf(viewer.getPage()))
            .replace(GENERIC_PAGES, String.valueOf(viewer.getPages()));

        options.setTitle(title);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    private void displayStatic(@NotNull StaticShop shop, @NotNull MenuViewer viewer) {
        viewer.setPages(shop.getPages());

        int page = Math.min(viewer.getPage(), viewer.getPages());
        Player player = viewer.getPlayer();

        for (StaticProduct product : shop.getValidProducts()) {
            if (product.getPage() != page) continue;

            this.addProductItem(shop, player, product, product.getSlot());
        }
    }

    private void displayRotating(@NotNull RotatingShop shop, @NotNull MenuViewer viewer) {
        RotationData data = shop.getData();
        Player player = viewer.getPlayer();
        Set<String> products = data.getProducts();
        int[] slots = shop.getProductSlots();

        int limit = slots.length;
        int pages = (int) Math.ceil((double) products.size() / (double) limit);

        viewer.setPages(pages);
        int page = Math.min(viewer.getPage(), viewer.getPages());

        int skip = (page - 1) * limit;
        List<String> list = products.stream().skip(skip).limit(limit).toList();

        int count = 0;
        for (String prodId : list) {
            RotatingProduct product = shop.getProductById(prodId);
            if (product == null) continue;
            if (!product.isValid()) continue;

            int slot = slots[count++];
            this.addProductItem(shop, player, product, slot);
        }
    }

    private <T extends VirtualShop> void addProductItem(@NotNull T shop, @NotNull Player player, @NotNull VirtualProduct product, int slot) {
        ItemStack preview = product.getPreview();

        List<String> loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL.get();
        List<String> buyLore = shop.isTransactionEnabled(TradeType.BUY) && product.isBuyable() ? VirtualConfig.PRODUCT_FORMAT_LORE_BUY.get() : Collections.emptyList();
        List<String> sellLore = shop.isTransactionEnabled(TradeType.SELL) && product.isSellable() ? VirtualConfig.PRODUCT_FORMAT_LORE_SELL.get() : Collections.emptyList();
        List<String> discountLore = shop.hasDiscount(product) ? VirtualConfig.PRODUCT_FORMAT_LORE_DISCOUNT.get() : Collections.emptyList();
        List<String> noPermLore = !product.hasAccess(player) ? VirtualConfig.PRODUCT_FORMAT_LORE_NO_PERMISSION.get() : Collections.emptyList();

        ItemReplacer replacer = ItemReplacer.create(preview).trimmed().readMeta()
            .setLore(loreFormat)
            .replace(GENERIC_BUY, buyLore)
            .replace(GENERIC_SELL, sellLore)
            .replace(GENERIC_LORE, ItemUtil.getLore(preview))
            .replace(GENERIC_DISCOUNT, discountLore)
            .replace(GENERIC_PERMISSION, noPermLore)
            ;

        for (TradeType tradeType : TradeType.values()) {
            String stockPlaceholder = Placeholders.STOCK_TYPE.apply(tradeType);
            String limitPlaceholder = Placeholders.LIMIT_TYPE.apply(tradeType);
            String priceDynamicPlaceholder = Placeholders.PRICE_DYNAMIC.apply(tradeType);

            List<String> stockLore = new ArrayList<>();
            List<String> limitLore = new ArrayList<>();
            List<String> priceDynamicLore = new ArrayList<>();
            if (!product.getStockValues().isUnlimited(tradeType)) {
                stockLore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_STOCK.get().getOrDefault(tradeType, Collections.emptyList()));
            }
            if (!product.getLimitValues().isUnlimited(tradeType)) {
                limitLore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_LIMIT.get().getOrDefault(tradeType, Collections.emptyList()));
            }
            if (product.getPricer() instanceof RangedPricer) {
                priceDynamicLore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_PRICE_DYNAMIC.get().getOrDefault(tradeType, Collections.emptyList()));
            }

            replacer
                .replace(priceDynamicPlaceholder, priceDynamicLore)
                .replace(stockPlaceholder, stockLore)
                .replace(limitPlaceholder, limitLore);
        }

        if (Config.GUI_PLACEHOLDER_API.get()) {
            replacer.replacePlaceholderAPI(player);
        }

        replacer
            .replace(product.replacePlaceholders(player))
            .replace(product.getCurrency().replacePlaceholders())
            .replace(shop.replacePlaceholders());

        replacer.writeMeta();

        MenuItem menuItem = new MenuItem(preview)
            .setSlots(slot)
            .setPriority(Integer.MAX_VALUE)
            .setOptions(ItemOptions.personalWeak(player))
            .setHandler((viewer, event) -> {
                plugin.getShopManager().onProductClick(player, product, event.getClick(), this);
            });

        this.addItem(menuItem);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose(SHOP_NAME), MenuSize.CHEST_54);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        list.add(new MenuItem(border).setSlots(IntStream.range(45, 54).toArray()).setPriority(0));

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(49).setPriority(10).setHandler(this.returnHandler));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(48).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(50).setPriority(10).setHandler(ItemHandler.forNextPage(this)));


        ItemStack balanceItem = ItemUtil.getSkinHead("5f96717bef61c37ce4dcd0b067da4b57c8a1b0f83c2926868b083444f7eade54");
        ItemUtil.editMeta(balanceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Wallet")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + GENERIC_BALANCE)
            ));
        });
        list.add(new MenuItem(balanceItem).setSlots(46).setPriority(10));


        ItemStack sellItem = ItemUtil.getSkinHead("9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d");
        ItemUtil.editMeta(sellItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Sell All")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Sell everything from your"),
                LIGHT_GRAY.enclose("inventory to this shop."),
                "",
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell Multiplier: ") + "x" + Placeholders.GENERIC_SELL_MULTIPLIER),
                "",
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Click to " + LIGHT_YELLOW.enclose("sell all") + ".")
            ));
        });
        list.add(new MenuItem(sellItem).setSlots(52).setPriority(10).setHandler(this.sellAllHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {

    }
}
