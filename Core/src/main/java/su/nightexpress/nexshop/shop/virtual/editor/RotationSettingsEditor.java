package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

public class RotationSettingsEditor extends EditorMenu<ShopPlugin, RotatingShop> implements ShopEditor {

    private final VirtualShopModule module;

    public RotationSettingsEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_SHOP_SETTINGS.getString(), MenuSize.CHEST_36);
        this.module = module;

        this.addReturn(31, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopEditor(viewer.getPlayer(), this.getLink(viewer)));
        });

        this.addItem(Material.OAK_SIGN, VirtualLocales.SHOP_ROTATION_TYPE, 10, (viewer, event, shop) -> {
            shop.setRotationType(Lists.next(shop.getRotationType()));
            this.saveAndFlush(viewer, shop);
        });

        this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_INTERVAL, 12, (viewer, event, shop) -> {
            if (event.getClick() == ClickType.DROP) {
                shop.rotate();
                this.flush(viewer);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS, (dialog, input) -> {
                shop.setRotationInterval(input.asInt());
                this.save(viewer, shop);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getRotationType() == RotationType.INTERVAL);

        this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_TIMES, 14, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openRotationTimesEditor(viewer.getPlayer(), shop));
        }).getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getRotationType() == RotationType.FIXED);

        this.addItem(Material.CHEST_MINECART, VirtualLocales.SHOP_ROTATION_PRODUCTS, 16, (viewer, event, shop) -> {
            if (event.getClick() == ClickType.DROP) {
                this.handleInput(viewer, VirtualLang.EDITOR_ENTER_SLOTS, (dialog, input) -> {
                    shop.setProductSlots(NumberUtil.getIntArray(input.getTextRaw()));
                    this.save(viewer, shop);
                    return true;
                });
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                if (event.isLeftClick()) {
                    shop.setProductMinAmount(input.asInt());
                }
                else {
                    shop.setProductMaxAmount(input.asInt());
                }
                this.saveAndFlush(viewer, shop);
                return true;
            });
        });

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, Placeholders.forVirtualShopEditor(this.getLink(viewer)));
        }));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.editTitle(this.getLink(viewer).replacePlaceholders());
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
