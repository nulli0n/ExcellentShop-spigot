package su.nightexpress.nexshop.shop.chest.menu;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

import java.util.*;

import static su.nexmedia.engine.utils.Colors2.*;

public class ShopSearchMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestProduct> {

    public static final String FILE = "shops_search.yml";

    private static final String PLACEHOLDER_ACTION_TELEPORT = "%action_teleport%";

    private final ChestShopModule                module;
    private final Map<Player, List<ChestProduct>> searchCache;

    private final int[]        productSlots;
    private final String       productName;
    private final List<String> productLore;
    private final List<String> actionTeleportLore;

    public ShopSearchMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getMenusPath(), FILE));
        this.module = module;
        this.searchCache = new WeakHashMap<>();

        this.productSlots = cfg.getIntArray("Product.Slots");
        this.productName = cfg.getString("Product.Name", Placeholders.PRODUCT_PREVIEW_NAME);
        this.productLore = cfg.getStringList("Product.Lore");
        this.actionTeleportLore = JOption.create("Product.Action_Teleport", Lists.newArrayList(
            LIGHT_YELLOW + "[▶] " + LIGHT_GRAY + "Left-Click to " + LIGHT_YELLOW + "teleport" + LIGHT_GRAY + "."
        )).read(cfg);

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    public void open(@NotNull Player player, @NotNull String input) {
        String searchFor = input.toLowerCase();

        List<ChestProduct> products = new ArrayList<>();
        this.module.getShops().forEach(shop -> {
            shop.getProducts().forEach(product -> {
                if (!(product.getPacker() instanceof ItemPacker packer)) return;

                ItemStack item = packer.getItem();
                String material = item.getType().getKey().getKey();
                String localized = LangManager.getMaterial(item.getType()).toLowerCase();
                String displayName = ItemUtil.getItemName(item);
                if (material.contains(searchFor) || localized.contains(searchFor) || displayName.contains(searchFor)) {
                    products.add(product);
                    return;
                }

                if (packer instanceof PluginItemPacker pluginPacker) {
                    String itemId = pluginPacker.getItemId();
                    if (itemId.contains(searchFor)) {
                        products.add(product);
                    }
                }
            });
        });

        this.searchCache.put(player, products);
        this.open(player, 1);
    }

    @NotNull
    private Collection<ChestProduct> getSearchResult(@NotNull Player player) {
        return this.searchCache.getOrDefault(player, Collections.emptyList());
    }

    @Override
    public int[] getObjectSlots() {
        return this.productSlots;
    }

    @Override
    @NotNull
    public List<ChestProduct> getObjects(@NotNull Player player) {
        return this.getSearchResult(player).stream().
            sorted(Comparator.comparing(product -> product.getPricer().getBuyPrice())).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ChestProduct product) {
        boolean isOwn = product.getShop().isOwner(player);
        boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));


        ItemStack item = new ItemStack(product.getPreview());
        ItemReplacer.create(item).hideFlags().trimmed()
            .setDisplayName(this.productName)
            .setLore(this.productLore)
            .replaceLoreExact(PLACEHOLDER_ACTION_TELEPORT, canTeleport ? this.actionTeleportLore : Collections.emptyList())
            .replace(product.getPlaceholders())
            .replace(product.getShop().getPlaceholders())
            .replace(Colorizer::apply)
            .writeMeta();
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestProduct product) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            boolean isOwn = product.getShop().isOwner(player);
            boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));

            if (canTeleport) {
                product.getShop().teleport(viewer.getPlayer());
            }
        };
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.searchCache.remove(viewer.getPlayer());
    }
}
