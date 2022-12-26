package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.HashMap;
import java.util.Map;

public class ShopMainMenu extends AbstractMenu<ExcellentShop> {

    private final VirtualShopModule module;
    private final Map<String, Integer> shopSlots;

    public ShopMainMenu(@NotNull VirtualShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getPath() + "main.menu.yml"), "");
        this.module = module;
        this.shopSlots = new HashMap<>();
        this.cfg.getSection("Shops").forEach(shopId -> {
            this.shopSlots.put(shopId.toLowerCase(), cfg.getInt("Shops." + shopId));
        });

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        this.shopSlots.forEach((shopId, slot) -> {
            VirtualShop shop = module.getShopById(shopId);
            if (shop == null) {
                this.module.error("Invalid shop in the main menu: '" + shopId + "' !");
                return;
            }

            ItemStack icon = shop.getIcon();
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) return;

            meta.setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get());
            meta.setLore(VirtualConfig.SHOP_FORMAT_LORE.get());
            meta.addItemFlags(ItemFlag.values());
            icon.setItemMeta(meta);
            ItemUtil.replace(icon, shop.replacePlaceholders());

            MenuItem menuItem = new MenuItem(shop.getId(), icon, slot);

            menuItem.setClickHandler((player2, type, e) -> {
                shop.open(player2, 1);
            });

            this.addItem(player, menuItem);
        });
        return true;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
