package su.nightexpress.nexshop.shop.virtual.impl.shop;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.api.shop.ShopView;
import su.nightexpress.nexshop.api.type.ShopClickAction;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.rotation.ShopRotationData;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.product.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.menu.ShopMainMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VirtualShopView<
    S extends VirtualShop<S, P>,
    P extends VirtualProduct<P, S>> extends ShopView<S, P> {

    public VirtualShopView(@NotNull S shop, @NotNull JYML cfg) {
        super(shop, cfg);

        cfg.addMissing("Title", StringUtil.capitalizeUnderscored(shop.getId()));
        cfg.addMissing("Size", 54);
        cfg.saveChanges();

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.RETURN, (viewer, event) -> this.plugin.runTask(task -> {
                ShopMainMenu mainMenu = this.getShop().getModule().getMainMenu();
                if (mainMenu != null) {
                    mainMenu.open(viewer.getPlayer(), 1);
                }
                else viewer.getPlayer().closeInventory();
            }));

        this.reload();
    }

    @NotNull
    public JYML getConfig() {
        return this.cfg;
    }

    public void reload() {
        this.getItems().clear();

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemUtil.replace(item, this.shop.replacePlaceholders());
            if (Config.GUI_PLACEHOLDER_API.get()) {
                ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
            }
        }));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        options.setTitle(this.shop.replacePlaceholders().apply(options.getTitle()));
        if (EngineUtils.hasPlaceholderAPI()) {
            options.setTitle(PlaceholderAPI.setPlaceholders(viewer.getPlayer(), options.getTitle()));
        }

        if (this.shop instanceof StaticShop staticShop) {
            this.displayStatic(staticShop, viewer, options);
        }
        else if (this.shop instanceof RotatingShop rotatingShop) {
            this.displayRotating(rotatingShop, viewer, options);
        }
    }

    private void displayStatic(@NotNull StaticShop shop, @NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        viewer.setPages(shop.getPages());
        viewer.finePage();

        int page = viewer.getPage();
        Player player = viewer.getPlayer();

        for (StaticProduct product : shop.getProducts()) {
            if (product.getPage() != page) continue;

            this.addProductItem(player, product, product.getSlot());
        }
    }

    private void displayRotating(@NotNull RotatingShop shop, @NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ShopRotationData data = shop.getData();
        Player player = viewer.getPlayer();
        Set<String> products = data.getProducts();
        int[] slots = shop.getProductSlots();

        int limit = slots.length;
        int pages = (int) Math.ceil((double) products.size() / (double) limit);

        viewer.setPages(pages);
        viewer.finePage();

        int skip = (viewer.getPage() - 1) * limit;
        List<String> list = new ArrayList<>(products.stream().skip(skip).limit(limit).toList());

        int count = 0;
        for (String prodId : list) {
            RotatingProduct product = shop.getProductById(prodId);
            if (product == null) continue;

            int slot = slots[count++];
            this.addProductItem(player, product, slot);
        }
    }

    private void addProductItem(@NotNull Player player, @NotNull VirtualProduct<?, ?> product, int slot) {
        ItemStack preview = product.getPreview();
        ItemUtil.mapMeta(preview, meta -> {
            List<String> loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL_ALL.get();
            if (!product.isBuyable() || !shop.isTransactionEnabled(TradeType.BUY)) {
                loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL_SELL_ONLY.get();
            }
            if (!product.isSellable() || !shop.isTransactionEnabled(TradeType.SELL)) {
                loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL_BUY_ONLY.get();
            }

            List<String> lore = new ArrayList<>();

            Label_Format:
            for (String lineFormat : loreFormat) {
                if (lineFormat.equalsIgnoreCase("%lore%")) {
                    List<String> list2 = meta.getLore();
                    if (list2 != null) lore.addAll(list2);
                    continue;
                }
                else if (lineFormat.equalsIgnoreCase("%discount%")) {
                    if (this.getShop().hasDiscount(product)) {
                        lore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_DISCOUNT.get());
                    }
                    continue;
                }
                else if (lineFormat.equalsIgnoreCase("%permission%")) {
                    if (!product.hasAccess(player)) {
                        lore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_NO_PERMISSION.get());
                    }
                    continue;
                }
                for (StockType stockType : StockType.values()) {
                    for (TradeType tradeType : TradeType.values()) {
                        if (lineFormat.equalsIgnoreCase("%stock_" + stockType.name() + "_" + tradeType.name() + "%")) {
                            if (!product.getStock().isUnlimited(stockType, tradeType)) {
                                lore.addAll(VirtualConfig.PRODUCT_FORMAT_LORE_STOCK.get().getOrDefault(stockType, Collections.emptyMap()).getOrDefault(tradeType, Collections.emptyList()));
                            }
                            continue Label_Format;
                        }
                    }
                }
                lore.add(lineFormat);
            }

            PlaceholderMap placeholderMap = new PlaceholderMap();
            placeholderMap.getKeys().addAll(product.getPlaceholders(player).getKeys());
            placeholderMap.getKeys().addAll(product.getCurrency().getPlaceholders().getKeys());
            placeholderMap.getKeys().addAll(shop.getPlaceholders().getKeys());
            lore.replaceAll(placeholderMap.replacer());
            if (Config.GUI_PLACEHOLDER_API.get()) {
                lore.replaceAll(str -> PlaceholderAPI.setPlaceholders(player, str));
            }
            meta.setLore(StringUtil.stripEmpty(lore));
        });

        MenuItem menuItem = new MenuItem(preview, Integer.MAX_VALUE, ItemOptions.personalWeak(player), slot);
        menuItem.setClick((viewer2, event) -> {
            ShopClickAction clickType = Config.GUI_CLICK_ACTIONS.get().get(event.getClick());
            if (clickType == null) return;

            if (!product.hasAccess(player)) {
                this.plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                return;
            }

            // In case if some "smart" guy have shop GUI opened during the rotation.
            if (product instanceof RotatingProduct rotatingProduct && !rotatingProduct.isInRotation()) {
                this.openNextTick(viewer2, viewer2.getPage());
                return;
            }

            product.prepareTrade(viewer2.getPlayer(), clickType);
        });
        this.addItem(menuItem);
    }
}
