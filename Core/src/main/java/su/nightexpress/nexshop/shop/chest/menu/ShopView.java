package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nexshop.api.shop.type.TradeType.BUY;
import static su.nightexpress.nexshop.api.shop.type.TradeType.SELL;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.shop.chest.Placeholders.*;

public class ShopView extends ConfigMenu<ShopPlugin> implements AutoFilled<ChestProduct>, Linked<ChestShop> {

    public static final String FILE_NAME = "view.yml";

    private final ViewLink<ChestShop> link;

    private int[]        productSlots;
    private List<String> productLore;

    public ShopView(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
        this.link = new ViewLink<>();

        this.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).replacePlaceholders());
        }));
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ChestShop shop = this.getLink(viewer);
        options.setTitle(shop.replacePlaceholders().apply(options.getTitle()));
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ChestProduct> autoFill) {
        ChestShop shop = this.getLink(viewer);

        autoFill.setSlots(this.productSlots);
        autoFill.setItems(shop.getProducts());
        autoFill.setItemCreator(product -> {
            ItemStack preview = product.getPreview();
            ItemReplacer.create(preview).readMeta().trimmed()
                .setLore(this.productLore)
                .replaceLoreExact(GENERIC_LORE, ItemUtil.getLore(preview))
                .replace(product.getPlaceholders())
                .replace(product.getCurrency().getPlaceholders())
                .replace(shop.getPlaceholders())
                .writeMeta();
            return preview;
        });
        autoFill.setClickAction(product -> (viewer1, event) -> {
            Player player = viewer1.getPlayer();

            if (product.getShop().isInactive()) {
                this.runNextTick(player::closeInventory);
                return;
            }

            ShopClickAction clickType = Config.GUI_CLICK_ACTIONS.get().get(event.getClick());
            if (clickType == null) return;

            this.runNextTick(() -> {
                product.prepareTrade(viewer1.getPlayer(), clickType);
                if (clickType != ShopClickAction.BUY_SELECTION && clickType != ShopClickAction.SELL_SELECTION) {
                    this.flush(viewer);
                }
            });
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose(SHOP_NAME), MenuSize.CHEST_27);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack emptySlot = ItemUtil.getSkinHead("2a52d579afe2fdf7b8ecfa746cd016150d96beb75009bb2733ade15d487c42a1");
        ItemUtil.editMeta(emptySlot, meta -> {
            meta.setDisplayName(LIGHT_GRAY.enclose("<Empty Slot>"));
        });
        list.add(new MenuItem(emptySlot).setSlots(11,12,13,14,15).setPriority(-1));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(9).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(17).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.productSlots = ConfigValue.create("Item.Product_Slots", new int[]{11,12,13,14,15}).read(cfg);

        this.productLore = ConfigValue.create("Product_Format.Lore.Text", Lists.newList(
            GENERIC_LORE,
            "",
            GREEN.enclose(BOLD.enclose("BUY:")),
            GREEN.enclose("←" + WHITE.enclose(" Left Click to buy for ") + PRODUCT_PRICE_FORMATTED.apply(BUY)),
            GREEN.enclose("✔" + WHITE.enclose(" Items Left: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(BUY)),
            "",
            RED.enclose(BOLD.enclose("SELL:")),
            RED.enclose("→" + WHITE.enclose(" Right Click to sell for ") + PRODUCT_PRICE_FORMATTED.apply(SELL)),
            RED.enclose("→" + WHITE.enclose(" Press [F] to sell all for ") + PRODUCT_PRICE_SELL_ALL_FORMATTED),
            RED.enclose("✔" + WHITE.enclose(" Shop Space: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(SELL)),
            "",
            DARK_GRAY.enclose("Hold " + LIGHT_GRAY.enclose("Shift") + " to buy & sell quickly.")
        )).read(cfg);
    }
}
