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
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.menu.ShopMainMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VirtualShopView extends ShopView<VirtualShop, VirtualProduct> {

    public VirtualShopView(@NotNull VirtualShop shop, @NotNull JYML cfg) {
        super(shop, cfg);

        cfg.addMissing("Title", StringUtil.capitalizeUnderscored(shop.getId()));
        cfg.addMissing("Size", 54);
        cfg.saveChanges();

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.CLOSE, (viewer, event) -> this.plugin.runTask(task -> viewer.getPlayer().closeInventory()))
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

        this.getItems().forEach(menuItem -> {
            if (menuItem.getOptions().getDisplayModifier() == null) {
                menuItem.getOptions().setDisplayModifier((viewer, item) -> {
                    ItemUtil.replace(item, this.shop.replacePlaceholders());
                    if (Config.GUI_PLACEHOLDER_API.get()) {
                        ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
                    }
                });
            }
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        if (EngineUtils.hasPlaceholderAPI()) {
            options.setTitle(PlaceholderAPI.setPlaceholders(viewer.getPlayer(), options.getTitle()));
        }

        viewer.setPages(this.getShop().getPages());
        viewer.finePage();

        int page = viewer.getPage();
        Player player = viewer.getPlayer();

        for (VirtualProduct product : this.shop.getProducts()) {
            if (product.getPage() != page) continue;

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

            MenuItem menuItem = new MenuItem(preview, 100, ItemOptions.personalWeak(player), product.getSlot());
            menuItem.setClick((viewer2, e) -> {
                ShopClickAction clickType = Config.GUI_CLICK_ACTIONS.get().get(e.getClick());
                if (clickType == null) return;

                if (!product.hasAccess(player)) {
                    this.plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                    return;
                }

                product.prepareTrade(viewer2.getPlayer(), clickType);
            });
            this.addItem(menuItem);
        }
    }
}
