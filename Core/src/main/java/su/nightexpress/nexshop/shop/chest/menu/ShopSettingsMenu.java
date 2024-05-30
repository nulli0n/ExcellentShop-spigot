package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.shop.chest.Placeholders.*;

public class ShopSettingsMenu extends ShopEditorMenu implements Linked<ChestShop>, ShopEditor {

    public static final String FILE = "shop_settings.yml";

    private final ViewLink<ChestShop> link;

    private final ItemHandler nameHandler;
    private final ItemHandler typeHandler;
    private final ItemHandler bankHandler;
    private final ItemHandler displayHandler;
    private final ItemHandler transactHandler;
    private final ItemHandler productsHandler;
    private final ItemHandler deleteHandler;

    public ShopSettingsMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE));
        this.link = new ViewLink<>();

        this.addHandler(this.nameHandler = new ItemHandler("shop_change_name", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, (dialog, input) -> {
                module.renameShop(viewer.getPlayer(), shop, input.getText());
                this.save(viewer, shop);
                return true;
            });
        }));

        this.addHandler(this.typeHandler = new ItemHandler("shop_change_type", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            shop.setType(Lists.next(shop.getType(), shopType -> shopType.hasPermission(viewer.getPlayer())));
            this.saveAndFlush(viewer, shop);
        }));

        this.addHandler(this.transactHandler = new ItemHandler("shop_change_transactions", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            if (Players.isBedrock(viewer.getPlayer())) {
                boolean isBuy = shop.isTransactionEnabled(TradeType.BUY);
                boolean isSell = shop.isTransactionEnabled(TradeType.SELL);
                if (isBuy && isSell) {
                    shop.setTransactionEnabled(TradeType.BUY, false);
                }
                else if (!isBuy && isSell) {
                    shop.setTransactionEnabled(TradeType.SELL, false);
                }
                else if (!isBuy) {
                    shop.setTransactionEnabled(TradeType.BUY, true);
                }
                else {
                    shop.setTransactionEnabled(TradeType.SELL, true);
                }
                return;
            }

            if (event.isLeftClick()) {
                shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
            }
            else if (event.isRightClick()) {
                shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
            }
            this.saveAndFlush(viewer, shop);
        }));

        this.addHandler(this.productsHandler = new ItemHandler("shop_change_products", (viewer, event) -> {
            this.runNextTick(() -> module.openProductsMenu(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addHandler(this.bankHandler = new ItemHandler("shop_bank", (viewer, event) -> {
            this.runNextTick(() -> module.openBank(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addHandler(this.displayHandler = new ItemHandler("shop_display", (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            this.runNextTick(() -> module.openDisplayMenu(viewer.getPlayer(), shop));
        }));

        this.addHandler(this.deleteHandler = new ItemHandler("shop_delete", (viewer, event) -> {
            Player player = viewer.getPlayer();
            if (!event.isShiftClick() && !Players.isBedrock(player)) return;

            module.deleteShop(player, this.getLink(viewer));
            this.runNextTick(player::closeInventory);
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ItemReplacer.create(item).readMeta().trimmed()
                    .replace(this.getLink(viewer).getPlaceholders())
                    .replacePlaceholderAPI(viewer.getPlayer())
                    .writeMeta();
            });

            if (menuItem.getHandler() == this.bankHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> {
                    if (ChestConfig.SHOP_AUTO_BANK.get()) return false;
                    if (viewer.getPlayer().hasPermission(ChestPerms.COMMAND_BANK_OTHERS)) return true;

                    return this.getLink(viewer).isOwner(viewer.getPlayer());
                });
            }
            else if (menuItem.getHandler() == this.typeHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> {
                    ChestShop shop = this.getLink(viewer);
                    return Lists.next(shop.getType(), shopType -> shopType.hasPermission(viewer.getPlayer())) != shop.getType();
                });
            }
            else if (menuItem.getHandler() == this.displayHandler) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.DISPLAY_CUSTOMIZATION));
            }
        });
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ChestShop shop = this.getLink(viewer);
        options.setTitle(shop.replacePlaceholders().apply(options.getTitle()));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose(SHOP_NAME), MenuSize.CHEST_9);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack nameItem = ItemUtil.getSkinHead("f5a19af0e61ca42532c0599fa0a391753df6b71f9fa4a177f1aa9b1d81fe6ee2");
        ItemUtil.editMeta(nameItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Display Name")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Current: ") + SHOP_NAME),
                "",
                LIGHT_GRAY.enclose("Name your shop!"),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ));
        });
        list.add(new MenuItem(nameItem).setSlots(0).setPriority(10).setHandler(this.nameHandler));

        ItemStack productsItem = ItemUtil.getSkinHead("900d28ff7b543dd088d004b1b1f95b38d444ea0461ff5ae3c68d76c0c16e2527");
        ItemUtil.editMeta(productsItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Products")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Manage your products here!"),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(productsItem).setSlots(1).setPriority(10).setHandler(this.productsHandler));

        ItemStack bankItem = ItemUtil.getSkinHead("5f96717bef61c37ce4dcd0b067da4b57c8a1b0f83c2926868b083444f7eade54");
        ItemUtil.editMeta(bankItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Bank")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + SHOP_BANK_BALANCE),
                "",
                LIGHT_GRAY.enclose("Bank used to store your funds."),
                "",
                LIGHT_GRAY.enclose("Bank " + LIGHT_GREEN.enclose("gains") + " funds on sales,"),
                LIGHT_GRAY.enclose("and " + LIGHT_RED.enclose("spends") + " them on purchases."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(bankItem).setSlots(2).setPriority(10).setHandler(this.bankHandler));



        ItemStack displayItem = ItemUtil.getSkinHead("28a47804b73e454a5992cf6411a7872df56788237c246bb245199d30a1eed58e");
        ItemUtil.editMeta(displayItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Display Settings")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Customize display of your shop!"),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(displayItem).setSlots(4).setPriority(10).setHandler(this.displayHandler));

        ItemStack transactItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemUtil.editMeta(transactItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Trade Modes")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Buying: ") + SHOP_BUY_ALLOWED),
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Selling: ") + SHOP_SELL_ALLOWED),
                "",
                LIGHT_GRAY.enclose("Enable/disable certain operations in the shop."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-click to toggle " + LIGHT_YELLOW.enclose("buying") + "."),
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-click to toggle " + LIGHT_YELLOW.enclose("selling") + ".")
            ));
        });
        list.add(new MenuItem(transactItem).setSlots(5).setPriority(10).setHandler(this.transactHandler));

        ItemStack typeItem = new ItemStack(Material.RED_BANNER);
        ItemUtil.editMeta(typeItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Shop Type")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Current: ") + SHOP_TYPE),
                "",
                LIGHT_GRAY.enclose("Setting shop as " + LIGHT_YELLOW.enclose("admin shop") + " will"),
                LIGHT_GRAY.enclose("make it with unlimited funds and items."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ));
        });
        list.add(new MenuItem(typeItem).setSlots(6).setPriority(10).setHandler(this.typeHandler));



        ItemStack deleteItem = ItemUtil.getSkinHead("b465f80bf02b408885987b00957ca5e9eb874c3fa88305099597a333a336ee15");
        ItemUtil.editMeta(deleteItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Destroy Shop")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Permanently removes the shop."),
                "",
                LIGHT_GRAY.enclose(LIGHT_RED.enclose("[❗]") + " You have to remove all items first!"),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Sneak + Click to " + LIGHT_YELLOW.enclose("confirm") + ".")
            ));
        });
        list.add(new MenuItem(deleteItem).setSlots(8).setPriority(10).setHandler(this.deleteHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {

    }
}
