package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.IProduct;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;

import java.util.List;

public class EditorShopChestProducts extends AbstractMenu<ExcellentShop> {

    private static ItemStack    FREE_SLOT;
    private static String       PRODUCT_NAME;
    private static List<String> PRODUCT_LORE;

    private final IShopChest shop;

    public EditorShopChestProducts(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestConfig.CONFIG_SHOP_PRODUCTS, "");
        this.shop = shop;

        FREE_SLOT = cfg.getItem("Free_Slot");
        PRODUCT_NAME = StringUtil.color(cfg.getString("Product.Name", Placeholders.PRODUCT_ITEM_NAME));
        PRODUCT_LORE = StringUtil.color(cfg.getStringList("Product.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().open(player, 1);
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);
        int productCount = 0;
        int maxProducts = ChestConfig.getMaxShopProducts(player);
        if (maxProducts < 0) maxProducts = inventory.getSize();

        for (IProduct shopProduct : this.shop.getProducts()) {
            if (productCount >= maxProducts || productCount >= inventory.getSize()) break;

            ItemStack preview = new ItemStack(shopProduct.getPreview());
            ItemMeta meta = preview.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(PRODUCT_NAME);
            meta.setLore(PRODUCT_LORE);
            preview.setItemMeta(meta);
            ItemUtil.replace(preview, shopProduct.replacePlaceholders());

            IMenuItem item = new MenuItem(preview);
            item.setSlots(productCount++);
            item.setClick((p, type, e) -> {
                if (e.isLeftClick()) {
                    shopProduct.getEditor().open(p, 1);
                    return;
                }
                if (e.isShiftClick()) {
                    if (e.isRightClick()) {
                        if (this.shop.getProductAmount(shopProduct) > 0) {
                            plugin.getMessage(Lang.Editor_Error_ProductLeft).send(p);
                            return;
                        }
                        this.shop.deleteProduct(shopProduct);
                        this.shop.save();
                        this.open(p, page);
                    }
                }
            });
            this.addItem(player, item);
        }

        IMenuItem free = new MenuItem(FREE_SLOT);
        int[] freeSlots = new int[maxProducts - productCount];
        int count2 = 0;
        for (int slot = productCount; slot < maxProducts; slot++) {
            freeSlots[count2++] = slot;
        }

        free.setSlots(freeSlots);
        free.setClick((p, type, e) -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || !this.shop.createProduct(p, cursor)) return;

            e.getView().setCursor(null);
            PlayerUtil.addItem(p, cursor);
            this.shop.save();
            this.open(p, page);
        });
        this.addItem(player, free);
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            if (IMenu.getMenu(player) != null) return;

            this.shop.getEditor().open(player, 1);
        }, false);

        super.onClose(player, e);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        if (slotType == SlotType.PLAYER || slotType == SlotType.EMPTY_PLAYER) {
            return e.isShiftClick();
        }
        return true;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryDragEvent e) {
        return e.getRawSlots().stream().anyMatch(slot -> slot < e.getInventory().getSize());
    }
}
