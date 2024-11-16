package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class CentralMenu extends ConfigMenu<ShopPlugin> {

    public static final String FILE_NAME = "main.menu.yml";

    private final VirtualShopModule module;
    private final ItemHandler sellAllHandler;

    public CentralMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
        this.module = module;
        this.applyPAPI = Config.usePlaceholdersForGUI();

        this.cfg.options().setHeader(Lists.newList(
            "=".repeat(50),
            "Available Placeholders:",
            "- " + GENERIC_BALANCE + " -> Player's balance for default Virtual Shop currency.",
            "- " + GENERIC_SELL_MULTIPLIER + " -> Player's sell multiplier (set in VirtualShop settings.yml).",
            "- " + URL_WIKI_PLACEHOLDERS + " -> Placeholders of: Shop, Virtual Shop, Static/Rotating Shop.",
            "- " + Plugins.PLACEHOLDER_API + " -> Any of them. Enable PlaceholderAPI setting.",
            "=".repeat(50)
        ));

        this.addHandler(this.sellAllHandler = new ItemHandler("sell_all", (viewer, event) -> {
            Player player = viewer.getPlayer();
            this.module.sellAll(player, player.getInventory());
        }));


        this.load();

        Currency currency = module.getDefaultCurrency();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
            ItemReplacer.create(itemStack).readMeta()
                .replacement(replacer -> replacer
                    .replace(GENERIC_BALANCE, () -> currency.format(currency.getBalance(viewer.getPlayer())))
                    .replace(GENERIC_SELL_MULTIPLIER, () -> NumberUtil.format(VirtualShopModule.getSellMultiplier(viewer.getPlayer())))
                    .replacePlaceholderAPI(viewer.getPlayer())
                )
                .writeMeta();
        }));
    }

    public int getLegacySlot(@NotNull VirtualShop shop) {
        if (!this.cfg.contains("Shops." + shop.getId())) return -1;

        int slot = this.cfg.getInt("Shops." + shop.getId(), -1);

        this.cfg.remove("Shops." + shop.getId());
        this.cfg.saveChanges();

        return slot;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.module.getShops().forEach(shop -> {
            int slot = shop.getMainMenuSlot();//this.getSlot(shop);
            if (slot < 0 || slot >= options.getSize()) return;

            ItemStack icon = shop.getIcon();
            ItemReplacer.create(icon).hideFlags()
                .setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get())
                .setLore(VirtualConfig.SHOP_FORMAT_LORE.get())
                .replacement(replacer -> replacer.replace(shop.replacePlaceholders()))
                .writeMeta();

            MenuItem menuItem = new MenuItem(icon);
            menuItem.setSlots(slot);
            menuItem.setPriority(Integer.MAX_VALUE);
            menuItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            if (VirtualConfig.MAIN_MENU_HIDE_NO_PERM_SHOPS.get()) {
                menuItem.getOptions().setVisibilityPolicy(viewer1 -> shop.canAccess(viewer1.getPlayer(), false));
            }
            menuItem.setHandler((viewer1, event) -> {
                this.runNextTick(() -> shop.open(viewer1.getPlayer()));
            });
            this.addItem(menuItem);
        });
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose(BOLD.enclose("Shop")), MenuSize.CHEST_45);
    }

    @Override
    protected void loadAdditional() {

    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack balanceItem = ItemUtil.getSkinHead("5f96717bef61c37ce4dcd0b067da4b57c8a1b0f83c2926868b083444f7eade54");
        ItemUtil.editMeta(balanceItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Wallet")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + GENERIC_BALANCE)
            ));
        });
        list.add(new MenuItem(balanceItem).setSlots(37).setPriority(10));


        ItemStack sellItem = ItemUtil.getSkinHead("9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d");
        ItemUtil.editMeta(sellItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Sell All")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Sell everything from your"),
                LIGHT_GRAY.enclose("inventory to all available shops."),
                "",
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell Multiplier: ") + "x" + GENERIC_SELL_MULTIPLIER),
                "",
                LIGHT_YELLOW.enclose("[▶]") + LIGHT_GRAY.enclose(" Click to " + LIGHT_YELLOW.enclose("sell all") + ".")
            ));
        });
        list.add(new MenuItem(sellItem).setSlots(43).setPriority(10).setHandler(this.sellAllHandler));


        return list;
    }
}
