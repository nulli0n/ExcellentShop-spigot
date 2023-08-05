package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.Comparator;
import java.util.List;

public class ShopBrowseMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<OfflinePlayer> {

    public static final String FILE = "shop_browse.yml";

    private final ChestShopModule module;

    private final String       objectName;
    private final List<String> objectLore;
    private final int[]        objectSlots;

    public ShopBrowseMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getMenusPath(), FILE));
        this.module = module;

        this.objectName = Colorizer.apply(cfg.getString("Player.Name", Placeholders.PLAYER_NAME));
        this.objectLore = Colorizer.apply(cfg.getStringList("Player.Lore"));
        this.objectSlots = cfg.getIntArray("Player.Slots");

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.registerHandler(Type.class)
            .addClick(Type.SEARCH, (viewer, event) -> {
                Player player = viewer.getPlayer();

                this.plugin.getMessage(ChestLang.SEARCH_ENTER_ITEM).send(player);
                this.plugin.runTask(task -> player.closeInventory());
                EditorManager.startEdit(player, wrapper -> {
                    this.plugin.runTaskLater(task -> this.module.getSearchMenu().open(player, wrapper.getTextRaw()), 2L);
                    return true;
                });
            });

        this.load();
    }

    enum Type {
        SEARCH
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return this.objectSlots;
    }

    @Override
    @NotNull
    public List<OfflinePlayer> getObjects(@NotNull Player player) {
        return this.module.getShops().stream().map(ChestShop::getOwner).distinct()
            .sorted(Comparator.comparing(o -> String.valueOf(o.getName()))).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull OfflinePlayer owner) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.mapMeta(item, meta -> {
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(owner);
            }
            meta.setDisplayName(this.objectName);
            meta.setLore(this.objectLore);
            meta.addItemFlags(ItemFlag.values());
            ItemUtil.replace(meta, str -> str
                .replace(Placeholders.PLAYER_NAME, String.valueOf(owner.getName()))
                .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(this.module.getShops(owner.getUniqueId()).size()))
            );
        });
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull OfflinePlayer owner) {
        return (viewer, event) -> {
            this.module.getListMenu().open(viewer.getPlayer(), owner.getUniqueId(), 1);
        };
    }
}
