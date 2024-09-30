package su.nightexpress.nexshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
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

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.PRODUCT_PRICE_FORMATTED;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class PurchaseOptionMenu extends ConfigMenu<ShopPlugin> implements Linked<Product> {

    public static final String FILE_NAME = "purchase_option.yml";

    private final ViewLink<Product> link;
    private final ItemHandler returnHandler;
    private final ItemHandler buyHandler;
    private final ItemHandler sellHandler;

    private int productSlot;

    public PurchaseOptionMenu(@NotNull ShopPlugin plugin) {
        super(plugin, FileConfig.loadOrExtract(plugin, Config.DIR_MENU, FILE_NAME));
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            Player player = viewer.getPlayer();
            Product product = this.getLink(player);
            int page = product instanceof StaticProduct staticProduct ? staticProduct.getPage() : 1;

            this.runNextTick(() -> product.getShop().open(player, page, true));
        }));

        this.addHandler(this.buyHandler = new ItemHandler(TradeType.BUY.getLowerCase(), (viewer, event) -> {
            this.onOptionClick(viewer, TradeType.BUY);
        }));

        this.addHandler(this.sellHandler = new ItemHandler(TradeType.SELL.getLowerCase(), (viewer, event) -> {
            this.onOptionClick(viewer, TradeType.SELL);
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getHandler() == this.buyHandler || menuItem.getHandler() == this.sellHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> ItemReplacer.replace(itemStack, this.getLink(viewer).getPlaceholders(viewer.getPlayer())));
            }
        });
    }

    private void onOptionClick(@NotNull MenuViewer viewer, @NotNull TradeType type) {
        Player player = viewer.getPlayer();
        Product product = this.getLink(player);
        if (!product.getShop().isTransactionEnabled(type) || !product.isTradeable(type)) return;

        this.runNextTick(() -> plugin.getShopManager().startTrade(player, product, type, null));
    }

    @Override
    @NotNull
    public ViewLink<Product> getLink() {
        return this.link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        if (this.productSlot >= 0 && this.productSlot <= options.getSize()) {
            this.addWeakItem(viewer.getPlayer(), this.getLink(viewer).getPreview(), this.productSlot);
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("What you would like to do?"), MenuSize.CHEST_9);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack buyItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemUtil.editMeta(buyItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("BUY")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Unit Price: " + LIGHT_GREEN.enclose(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)))
            ));
        });
        list.add(new MenuItem(buyItem).setPriority(10).setSlots(0,1,2,3).setHandler(this.buyHandler));

        ItemStack sellItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemUtil.editMeta(sellItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("SELL")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Unit Price: " + LIGHT_RED.enclose(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)))
            ));
        });
        list.add(new MenuItem(sellItem).setPriority(10).setSlots(5,6,7,8).setHandler(this.sellHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.productSlot = ConfigValue.create("Product.Slot", 4).read(cfg);
    }
}
