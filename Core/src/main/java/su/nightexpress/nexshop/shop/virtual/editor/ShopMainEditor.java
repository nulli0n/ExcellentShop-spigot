package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.util.function.Predicate;

public class ShopMainEditor extends EditorMenu<ShopPlugin, VirtualShop> implements ShopEditor {

    private static final String TEXTURE_NPC   = "f76cf8b7378e889395d538e6354a17a3de6b294bb6bf8db9c701951c68d3c0e6";
    private static final String TEXTURE_BOX   = "b663a178638400f16c7073c63fef13572fab9b6f42df9ea039c3b3f6773faf94";
    private static final String TEXTURE_PAINT = "5e44280d42db07b012eb76dee73249c86fc712e8eb14fa2b44847714bbb95f83";

    private final VirtualShopModule module;

    public ShopMainEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Shop Editor [" + Placeholders.SHOP_ID + "]"), MenuSize.CHEST_54);
        this.module = module;

        this.addReturn(49, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopsEditor(viewer.getPlayer()));
        });

        // =============================================
        // Generic stuff
        // =============================================

        this.addItem(Material.NAME_TAG, VirtualLocales.SHOP_DISPLAY_NAME, 10, (viewer, event, shop) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, (dialog, input) -> {
                shop.setName(input.getText());
                this.save(viewer, shop);
                return true;
            });
        });

        this.addItem(Material.WRITABLE_BOOK, VirtualLocales.SHOP_DESCRIPTION, 11, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.getDescription().clear();
                this.saveAndFlush(viewer, shop);
                return;
            }
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_DESCRIPTION, (dialog, input) -> {
                shop.getDescription().add(input.getText());
                this.save(viewer, shop);
                return true;
            });
        });

        this.addItem(Material.ITEM_FRAME, VirtualLocales.SHOP_ICON, 12, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), shop.getIcon());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            shop.setIcon(cursor);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, shop);
        }).getOptions().setDisplayModifier((viewer, item) -> {
            VirtualShop shop = this.getLink(viewer);
            item.setType(shop.getIcon().getType());
            item.setItemMeta(shop.getIcon().getItemMeta());
            ItemReplacer.create(item).readLocale(VirtualLocales.SHOP_ICON).hideFlags().trimmed()
                .replace(shop.replacePlaceholders())
                .writeMeta();
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_PAINT), VirtualLocales.SHOP_LAYOUT, 14, (viewer, event, shop) -> {
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_TITLE, (dialog, input) -> {
                shop.setLayoutName(input.getTextRaw());
                this.save(viewer, shop);
                return true;
            }).setSuggestions(this.module.getLayoutNames(), true);
        });

        this.addItem(Material.LIME_DYE, VirtualLocales.SHOP_TRADES, 15, (viewer, event, shop) -> {
            TradeType type = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
            shop.setTransactionEnabled(type, !shop.isTransactionEnabled(type));
            this.saveAndFlush(viewer, shop);
        }).getOptions().addDisplayModifier((viewer, itemStack) -> {
            VirtualShop shop = this.getLink(viewer);
            if (shop.isTransactionEnabled(TradeType.BUY) && shop.isTransactionEnabled(TradeType.SELL)) {
                itemStack.setType(Material.LIGHT_BLUE_DYE);
            }
            else if (shop.isTransactionEnabled(TradeType.BUY)) {
                itemStack.setType(Material.LIME_DYE);
            }
            else if (shop.isTransactionEnabled(TradeType.SELL)) {
                itemStack.setType(Material.RED_DYE);
            }
            else itemStack.setType(Material.GRAY_DYE);
        });

        this.addItem(Material.REDSTONE, VirtualLocales.SHOP_PERMISSION, 16, (viewer, event, shop) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.saveAndFlush(viewer, shop);
        }).getOptions().addDisplayModifier((viewer, itemStack) -> {
            itemStack.setType(this.getLink(viewer).isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER);
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_BOX), VirtualLocales.SHOP_PRODUCTS, 31, (viewer, event, shop) -> {
            if (event.getClick() == ClickType.DROP) {
                this.plugin.runTaskAsync(task -> {
                    shop.getPricer().deleteData();
                    shop.getPricer().updatePrices();
                });
                return;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                VirtualStock stock = (VirtualStock) shop.getStock();
                stock.deleteData();
                return;
            }

            this.runNextTick(() -> this.module.openProductsEditor(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_NPC), VirtualLocales.SHOP_ATTACHED_NPCS, 4, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.getNPCIds().clear();
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_NPC_ID, (dialog, input) -> {
                int id = input.asInt(-1);
                if (id < 0) return true;

                shop.getNPCIds().add(id);
                this.save(viewer, shop);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> Plugins.isLoaded(HookId.CITIZENS));

        Predicate<MenuViewer> isStaticShop = viewer -> this.getLink(viewer) instanceof StaticShop;
        Predicate<MenuViewer> isRotatingShop = viewer -> this.getLink(viewer) instanceof RotatingShop;

        // =============================================
        // Static Shop stuff
        // =============================================

        this.addItem(Material.ENDER_PEARL, VirtualLocales.SHOP_PAGES, 28, (viewer, event, shop) -> {
            StaticShop staticShop = (StaticShop) shop;
            int add = event.isLeftClick() ? 1 : -1;
            staticShop.setPages(staticShop.getPages() + add);
            this.saveAndFlush(viewer, staticShop);
        }).getOptions().setVisibilityPolicy(isStaticShop);

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.SHOP_DISCOUNTS, 34, (viewer, event, shop) -> {
            StaticShop staticShop = (StaticShop) shop;
            this.runNextTick(() -> this.module.openDiscountsEditor(viewer.getPlayer(), staticShop));
        }).getOptions().setVisibilityPolicy(isStaticShop);

        // =============================================
        // Rotating Shop stuff
        // =============================================

        this.addItem(Material.OAK_SIGN, VirtualLocales.SHOP_ROTATION_TYPE, 13, (viewer, event, shop) -> {
            RotatingShop rotatingShop = (RotatingShop) shop;
            rotatingShop.setRotationType(Lists.next(rotatingShop.getRotationType()));
            this.saveAndFlush(viewer, rotatingShop);
        }).getOptions().setVisibilityPolicy(isRotatingShop);

        this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_INTERVAL, 28, (viewer, event, shop) -> {
            RotatingShop rotatingShop = (RotatingShop) shop;
            if (event.getClick() == ClickType.DROP) {
                rotatingShop.rotate();
                this.flush(viewer);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS, (dialog, input) -> {
                rotatingShop.setRotationInterval(input.asInt());
                this.save(viewer, rotatingShop);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> isRotatingShop.test(viewer) && ((RotatingShop) this.getLink(viewer)).getRotationType() == RotationType.INTERVAL);

        this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_TIMES, 27, (viewer, event, shop) -> {
            RotatingShop rotatingShop = (RotatingShop) shop;
            this.runNextTick(() -> this.module.openRotationTimesEditor(viewer.getPlayer(), rotatingShop));
        }).getOptions().setVisibilityPolicy(viewer -> isRotatingShop.test(viewer) && ((RotatingShop) this.getLink(viewer)).getRotationType() == RotationType.FIXED);

        this.addItem(Material.CHEST_MINECART, VirtualLocales.SHOP_ROTATION_PRODUCTS, 34, (viewer, event, shop) -> {
            RotatingShop rotatingShop = (RotatingShop) shop;
            if (event.getClick() == ClickType.DROP) {
                this.handleInput(viewer, VirtualLang.EDITOR_ENTER_SLOTS, (dialog, input) -> {
                    rotatingShop.setProductSlots(NumberUtil.getIntArray(input.getTextRaw()));
                    this.save(viewer, rotatingShop);
                    return true;
                });
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
                if (event.isLeftClick()) {
                    rotatingShop.setProductMinAmount(input.asInt());
                }
                else {
                    rotatingShop.setProductMaxAmount(input.asInt());
                }
                this.saveAndFlush(viewer, rotatingShop);
                return true;
            });
        }).getOptions().setVisibilityPolicy(isRotatingShop);

        // =============================================
        // End stuff
        // =============================================

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).getPlaceholders());
        }));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.setTitle(this.getLink(viewer).replacePlaceholders().apply(options.getTitle()));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }
}
