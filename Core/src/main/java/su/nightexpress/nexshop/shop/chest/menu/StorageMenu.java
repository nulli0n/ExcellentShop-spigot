package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.impl.ChestStock;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.shop.chest.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class StorageMenu extends ShopEditorMenu implements AutoFilled<ChestProduct>, Linked<ChestShop> {

    public static final String FILE_NAME = "shop_storage.yml";

    private final ChestShopModule     module;
    private final ItemHandler         returnHandler;
    private final ViewLink<ChestShop> link;

    private int[]        objectSlots;
    private String       objectName;
    private List<String> objectLore;

    public StorageMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            ChestShop shop = this.getLink(viewer);
            if (shop.isActive()) {
                this.runNextTick(() -> this.module.openShopSettings(viewer.getPlayer(), shop));
            }
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            // Added for currency PAPI support.
            menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                ItemReplacer.replacePlaceholderAPI(itemStack, viewer.getPlayer());
            });
        });
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ChestProduct> autoFill) {
        Player player = viewer.getPlayer();
        ChestShop shop = this.getLink(player);
        ChestStock stock = shop.getStock();

        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(shop.getValidProducts());
        autoFill.setItemCreator(product -> {
            ItemStack icon = product.getPreview();
            ItemReplacer.create(icon).hideFlags().trimmed()
                .setDisplayName(this.objectName)
                .setLore(this.objectLore)
                .replace(product.getPlaceholders())
                .replace(GENERIC_AMOUNT, NumberUtil.format(product.getQuantity()))
                .replacePlaceholderAPI(player)
                .writeMeta();
            return icon;
        });
        autoFill.setClickAction(product -> (viewer1, event) -> {
            if (event.getClick() == ClickType.DROP) {
                int units = product.countUnits(player);
                this.module.depositToStorage(player, product, units);
                this.runNextTick(() -> this.flush(viewer));
                return;
            }

            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                int units = stock.countItem(product, TradeType.BUY, player);
                this.module.withdrawFromStorage(player, product, units);
                this.runNextTick(() -> this.flush(viewer));
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                String msg = input.getTextRaw();
                Type type = Type.UNSPECIFIED;
                if (!Players.isBedrock(player)) {
                    if (event.isLeftClick()) type = Type.DEPOSIT;
                    else if (event.isRightClick()) type = Type.WITHDRAW;
                }

                if (type == Type.UNSPECIFIED) {
                    if (msg.startsWith("+")) type = Type.DEPOSIT;
                    else if (msg.startsWith("-")) type = Type.WITHDRAW;
                    else return false;

                    msg = msg.substring(1);
                }

                int amount = NumberUtil.getInteger(msg, 0);
                if (amount == 0) {
                    dialog.error(Lang.EDITOR_INPUT_ERROR_GENERIC.getMessage());
                    return false;
                }

                boolean result;
                if (type == Type.DEPOSIT) {
                    this.module.depositToStorage(player, product, amount);
                }
                else {
                    this.module.withdrawFromStorage(player, product, amount);
                }
                return true;
            });
        });
    }

    private enum Type {
        UNSPECIFIED,
        DEPOSIT,
        WITHDRAW,
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Shop Storage"), MenuSize.CHEST_18);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(13).setPriority(10).setHandler(this.returnHandler));

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
        this.objectSlots = ConfigValue.create("Product.Slots", IntStream.range(0, 9).toArray()).read(cfg);

        this.objectName = ConfigValue.create("Product.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(PRODUCT_PREVIEW_NAME))
        ).read(cfg);

        this.objectLore = ConfigValue.create("Product.Lore", Lists.newList(
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Quantity: ") + GENERIC_AMOUNT),
            "",
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("deposit") + "."),
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("withdraw") + "."),
            LIGHT_GRAY.enclose(LIGHT_ORANGE.enclose("[▶]") + " [Q/Drop] Key to " + LIGHT_ORANGE.enclose("deposit all") + "."),
            LIGHT_GRAY.enclose(LIGHT_ORANGE.enclose("[▶]") + " [F/Swap] Key to " + LIGHT_ORANGE.enclose("withdraw all") + ".")
        )).read(cfg);
    }
}
