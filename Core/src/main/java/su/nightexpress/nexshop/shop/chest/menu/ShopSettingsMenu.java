package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class ShopSettingsMenu extends LinkedMenu<ShopPlugin, ChestShop> implements ConfigBased {

    public static final String FILE = "shop_settings.yml";

    private final ChestShopModule module;

    public ShopSettingsMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, BLACK.enclose(SHOP_NAME));
        this.module = module;

        this.load(FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE));
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        return this.getLink(viewer).replacePlaceholders().apply(this.title);
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        loader.addDefaultItem(NightItem.fromType(Material.RED_BANNER)
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Admin Shop")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Enabled: ") + CHEST_SHOP_IS_ADMIN),
                "",
                LIGHT_GRAY.enclose("Controls whether shop is " + LIGHT_YELLOW.enclose("admin shop") + "."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("toggle") + ".")
            ))
            .toMenuItem().setSlots(4).setPriority(10).setHandler(new ItemHandler("shop_change_type", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                shop.setAdminShop(!shop.isAdminShop());
                module.remakeDisplay(shop);
                shop.saveSettings();
                this.runNextTick(() -> this.flush(viewer));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.ADMIN_SHOP)).build())));


        loader.addDefaultItem(NightItem.asCustomHead("f5a19af0e61ca42532c0599fa0a391753df6b71f9fa4a177f1aa9b1d81fe6ee2")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Display Name")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Current: ") + SHOP_NAME),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("change") + ".")
            ))
            .toMenuItem().setSlots(20).setPriority(10).setHandler(new ItemHandler("shop_change_name", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, input -> {
                    if (this.module.renameShop(viewer.getPlayer(), shop, input.getText())) {
                        shop.saveSettings();
                    }
                    return true;
                }));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).canRename(viewer.getPlayer())).build())));


        loader.addDefaultItem(NightItem.asCustomHead("900d28ff7b543dd088d004b1b1f95b38d444ea0461ff5ae3c68d76c0c16e2527")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Products")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Manage shop products here."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ))
            .toMenuItem().setSlots(21).setPriority(10).setHandler(new ItemHandler("shop_change_products", (viewer, event) -> {
                this.runNextTick(() -> this.module.openProductsMenu(viewer.getPlayer(), this.getLink(viewer)));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).canManageProducts(viewer.getPlayer())).build())));


        loader.addDefaultItem(NightItem.asCustomHead("28a47804b73e454a5992cf6411a7872df56788237c246bb245199d30a1eed58e")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Display Settings")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Customize display of your shop!"),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ))
            .toMenuItem().setSlots(22).setPriority(10).setHandler(new ItemHandler("shop_display", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                this.runNextTick(() -> module.openDisplayMenu(viewer.getPlayer(), shop));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> viewer.getPlayer().hasPermission(ChestPerms.DISPLAY_CUSTOMIZATION)).build())));


        loader.addDefaultItem(NightItem.fromType(Material.GREEN_DYE)
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Buying")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Allowed: ") + SHOP_BUYING_ALLOWED),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("toggle") + ".")
                ))
            .toMenuItem().setSlots(23).setPriority(10).setHandler(new ItemHandler("shop_buying", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                shop.setBuyingAllowed(!shop.isBuyingAllowed());
                shop.saveSettings();
                this.runNextTick(() -> this.flush(viewer));
            })));

        loader.addDefaultItem(NightItem.fromType(Material.RED_DYE)
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Selling")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose("Allowed: ") + SHOP_SELLING_ALLOWED),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("toggle") + ".")
            ))
            .toMenuItem().setSlots(24).setPriority(10).setHandler(new ItemHandler("shop_selling", (viewer, event) -> {
                ChestShop shop = this.getLink(viewer);
                shop.setSellingAllowed(!shop.isSellingAllowed());
                shop.saveSettings();
                this.runNextTick(() -> this.flush(viewer));
            })));

        loader.addDefaultItem(NightItem.asCustomHead("8fa065f04290ecf431f9aa900ab6ea17bc354f70a596f1826bb23592f87ddba7")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Rent Settings")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Manage rent settings here."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ))
            .toMenuItem().setSlots(30).setPriority(10).setHandler(new ItemHandler("shop_rent_settings", (viewer, event) -> {
                this.runNextTick(() -> this.module.openRentSettings(viewer.getPlayer(), this.getLink(viewer)));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> ChestConfig.isRentEnabled() && this.getLink(viewer).canManageRent(viewer.getPlayer())).build())));

        loader.addDefaultItem(NightItem.asCustomHead("a77be664b48eb834c05a79cf2bcea4a0b49215211254b0b4d965ccb221dbedbb")
            .setDisplayName(GREEN.enclose(BOLD.enclose("Extend Rent")))
            .setLore(Lists.newList(
                GREEN.enclose("● " + LIGHT_GRAY.enclose("Expires in ") + CHEST_SHOP_RENT_EXPIRES_IN),
                GREEN.enclose("● " + LIGHT_GRAY.enclose("Period: ") + CHEST_SHOP_RENT_DURATION),
                GREEN.enclose("● " + LIGHT_GRAY.enclose("Price: ") + CHEST_SHOP_RENT_PRICE),
                "",
                LIGHT_GRAY.enclose(GREEN.enclose("➥") + " Click to " + GREEN.enclose("extend") + ".")
            ))
            .toMenuItem().setSlots(30).setPriority(10).setHandler(new ItemHandler("shop_rent_extend", (viewer, event) -> {
                Player player = viewer.getPlayer();
                ChestShop shop = this.getLink(player);
                this.runNextTick(() -> plugin.getShopManager().openConfirmation(player, Confirmation.create(
                    (viewer1, event1) -> {
                        this.module.extendRentShop(viewer.getPlayer(), shop);
                        this.runNextTick(() -> module.openShopSettings(player, shop));
                    },
                    (viewer1, event1) -> {
                        this.runNextTick(() -> module.openShopSettings(player, shop));
                    }
                )));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> ChestConfig.isRentEnabled() && this.getLink(viewer).isRenter(viewer.getPlayer())).build())));

        loader.addDefaultItem(NightItem.asCustomHead("edc36c9cb50a527aa55607a0df7185ad20aabaa903e8d9abfc78260705540def")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Storage")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Store items here."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ))
            .toMenuItem().setSlots(31).setPriority(10).setHandler(new ItemHandler("shop_storage", (viewer, event) -> {
                this.runNextTick(() -> module.openStorage(viewer.getPlayer(), this.getLink(viewer)));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> ChestUtils.isInfiniteStorage()).build())));

        loader.addDefaultItem(NightItem.asCustomHead("5f96717bef61c37ce4dcd0b067da4b57c8a1b0f83c2926868b083444f7eade54")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Bank")))
            .setLore(Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + Placeholders.CHEST_SHOP_BANK_BALANCE),
                "",
                LIGHT_GRAY.enclose("Bank used to store your funds."),
                "",
                LIGHT_GRAY.enclose("Bank " + LIGHT_GREEN.enclose("gains") + " funds on sales,"),
                LIGHT_GRAY.enclose("and " + LIGHT_RED.enclose("spends") + " them on purchases."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ))
            .toMenuItem().setSlots(32).setPriority(10).setHandler(new ItemHandler("shop_bank", (viewer, event) -> {
                this.runNextTick(() -> module.openBank(viewer.getPlayer(), this.getLink(viewer)));
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> !ChestConfig.isAutoBankEnabled() && this.getLink(viewer).canManageBank(viewer.getPlayer())).build())));


        loader.addDefaultItem(NightItem.asCustomHead("b465f80bf02b408885987b00957ca5e9eb874c3fa88305099597a333a336ee15")
            .setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Destroy Shop")))
            .setLore(Lists.newList(
                LIGHT_GRAY.enclose("Permanently removes the shop."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("remove") + ".")
            ))
            .toMenuItem().setSlots(8).setPriority(10).setHandler(new ItemHandler("shop_delete", (viewer, event) -> {
                Player player = viewer.getPlayer();
                if (!event.isShiftClick() && !Players.isBedrock(player)) return;

                module.deleteShop(player, this.getLink(viewer));
                this.runNextTick(player::closeInventory);
            }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).canRemove(viewer.getPlayer())).build())));
    }
}
