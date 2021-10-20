package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;

import java.util.List;

public class EditorShopChestProducts extends AbstractMenu<ExcellentShop> {

    private static ItemStack    FREE_SLOT;
    private static String       PRODUCT_NAME;
    private static List<String> PRODUCT_LORE;

    private final IShopChest shop;

    public EditorShopChestProducts(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP_PRODUCTS, "");
        this.shop = shop;

        FREE_SLOT = cfg.getItem("Free_Slot");
        PRODUCT_NAME = StringUT.color(cfg.getString("Product.Name", IShopChestProduct.PLACEHOLDER_ITEM_NAME));
        PRODUCT_LORE = StringUT.color(cfg.getStringList("Product.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().open(player, 1);
                }
            }
        };

        for (String sId : cfg.getSection("content")) {
            IMenuItem menuItem = cfg.getMenuItem("content." + sId, MenuItemType.class);

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
        int maxProducts = ChestShopConfig.getMaxShopProducts(player);
        if (maxProducts < 0) maxProducts = inventory.getSize();

        for (IShopProduct shopProduct : this.shop.getProducts()) {
            if (productCount >= maxProducts || productCount >= inventory.getSize()) break;

            ItemStack preview = new ItemStack(shopProduct.getPreview());
            ItemMeta meta = preview.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(PRODUCT_NAME);
            meta.setLore(PRODUCT_LORE);
            preview.setItemMeta(meta);
            ItemUT.replace(preview, shopProduct.replacePlaceholders());

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
                            this.plugin.lang().Chest_Shop_Editor_Error_ProductLeft.send(p);
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
            ItemUT.addItem(p, cursor);
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
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
