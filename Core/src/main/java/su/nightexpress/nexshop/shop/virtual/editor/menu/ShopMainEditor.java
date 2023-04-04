package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.EditorLocales;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

public class ShopMainEditor extends EditorMenu<ExcellentShop, VirtualShop> {

    private static final String TEXTURE_BOOK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGUxNTU5NDhjYTg1YjA1MTM3ZDJkM2E1YjA4MmY1N2U3NmM2ODFiZmNkZjRmMGRjZjg2ZWFmZjY4MWI5MzY3OCJ9fX0=";
    private static final String TEXTURE_NPC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlMGRjOTJkNzg2MmYwNDQzY2M3NzU3Mzc3NzRmNDA3YWFlZmJlMDVlOWM0MzIzMmJiNjkzZDM5YzE4ZmI4OSJ9fX0=";
    private static final String TEXTURE_BOX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTAwZDI4ZmY3YjU0M2RkMDg4ZDAwNGIxYjFmOTViMzhkNDQ0ZWEwNDYxZmY1YWUzYzY4ZDc2YzBjMTZlMjUyNyJ9fX0=";
    private static final String TEXTURE_PAINT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU0NDI4MGQ0MmRiMDdiMDEyZWI3NmRlZTczMjQ5Yzg2ZmM3MTJlOGViMTRmYTJiNDQ4NDc3MTRiYmI5NWY4MyJ9fX0=";
    private static final String TEXTURE_DOLLAR = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=";

    private EditorShopDiscounts editorDiscounts;
    private ShopViewEditor      viewEditor;
    private ProductListEditor   productEditor;

    public ShopMainEditor(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(plugin, shop, Placeholders.EDITOR_VIRTUAL_TITLE, 54);

        this.addReturn(49).setClick((viewer, event) -> {
            this.plugin.runTask(task -> shop.getModule().getEditor().open(viewer.getPlayer(), 1));
        });

        this.addItem(Material.NAME_TAG, EditorLocales.SHOP_NAME, 12).setClick((viewer, event) -> {
            this.startEdit(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_NAME), chat -> {
                shop.setName(chat.getMessage());
                this.save(viewer);
                return true;
            });
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOOK), EditorLocales.SHOP_DESCRIPTION, 13).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                shop.getDescription().clear();
                this.save(viewer);
                return;
            }
            this.startEdit(viewer.getPlayer(), plugin.getMessage(VirtualLang.EDITOR_ENTER_DESCRIPTION), chat -> {
                shop.getDescription().add(Colorizer.apply(chat.getMessage()));
                this.save(viewer);
                return true;
            });
        });

        this.addItem(shop.getIcon(), 14).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                PlayerUtil.addItem(viewer.getPlayer(), shop.getIcon());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            shop.setIcon(cursor);
            event.getView().setCursor(null);
            this.save(viewer);
        }).getOptions().setDisplayModifier((viewer, item) -> {
            item.setType(shop.getIcon().getType());
            item.setItemMeta(shop.getIcon().getItemMeta());
            ItemUtil.mapMeta(item, meta -> {
                meta.setDisplayName(EditorLocales.SHOP_ICON.getLocalizedName());
                meta.setLore(EditorLocales.SHOP_ICON.getLocalizedLore());
                meta.addItemFlags(ItemFlag.values());
                ItemUtil.replace(meta, shop.replacePlaceholders());
            });
        });

        this.addItem(Material.ENDER_PEARL, EditorLocales.SHOP_PAGES, 20).setClick((viewer, event) -> {
            if (event.isLeftClick()) {
                shop.setPages(shop.getPages() + 1);
            }
            else if (event.isRightClick()) {
                shop.setPages(Math.max(1, shop.getPages() - 1));
            }
            this.save(viewer);
        });

        this.addItem(Material.REDSTONE, EditorLocales.SHOP_PERMISSION, 24).setClick((viewer, event) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.save(viewer);
        });

        this.addItem(Material.WRITABLE_BOOK, EditorLocales.SHOP_TRADES, 22).setClick((viewer, event) -> {
            if (event.isLeftClick()) {
                shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
            }
            else if (event.isRightClick()) {
                shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
            }
            this.save(viewer);
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_NPC), EditorLocales.SHOP_ATTACHED_NPCS, 8).setClick((viewer, event) -> {
            if (!Hooks.hasCitizens()) return;

            if (event.isRightClick()) {
                shop.getNPCIds().clear();
                this.save(viewer);
                return;
            }

            this.startEdit(viewer.getPlayer(), plugin.getMessage(VirtualLang.EDITOR_ENTER_NPC_ID), chat -> {
                int id = StringUtil.getInteger(Colorizer.strip(chat.getMessage()), -1);
                if (id < 0) return true;

                shop.getNPCIds().add(id);
                this.save(viewer);
                return true;
            });
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_DOLLAR), EditorLocales.SHOP_DISCOUNTS, 30).setClick((viewer, event) -> {
            this.plugin.runTask(task -> this.getEditorDiscounts().open(viewer.getPlayer(), 1));
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX), EditorLocales.SHOP_PRODUCTS, 31).setClick((viewer, event) -> {
            this.plugin.runTask(task -> this.getProductsEditor().open(viewer.getPlayer(), 1));
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_PAINT), EditorLocales.SHOP_VIEW_EDITOR, 32).setClick((viewer, event) -> {
            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    this.startEdit(viewer.getPlayer(), plugin.getMessage(VirtualLang.EDITOR_ENTER_TITLE), chat -> {
                        shop.getView().getOptions().setTitle(Colorizer.apply(chat.getMessage()));
                        this.save(viewer);
                        return true;
                    });
                }
                else {
                    int size = shop.getView().getOptions().getSize();
                    if (size == 54) size = 0;

                    shop.getView().getOptions().setSize(size + 9);
                    this.save(viewer);
                }
                return;
            }
            this.getViewEditor().open(viewer.getPlayer(), 1);
        });

        this.getItems().forEach(menuItem -> {
            if (menuItem.getOptions().getDisplayModifier() == null) {
                menuItem.getOptions().setDisplayModifier((viewer, item) -> ItemUtil.replace(item, shop.replacePlaceholders()));
            }
        });
    }

    private void save(@NotNull MenuViewer viewer) {
        this.object.saveSettings();
        this.plugin.runTask(task -> this.open(viewer.getPlayer(), viewer.getPage()));
    }

    @Override
    public void clear() {
        super.clear();
        if (this.editorDiscounts != null) {
            this.editorDiscounts.clear();
            this.editorDiscounts = null;
        }
        if (this.viewEditor != null) {
            this.viewEditor.clear();
            this.viewEditor = null;
        }
        if (this.productEditor != null) {
            this.productEditor.clear();
            this.productEditor = null;
        }
    }

    @NotNull
    public EditorShopDiscounts getEditorDiscounts() {
        if (this.editorDiscounts == null) {
            this.editorDiscounts = new EditorShopDiscounts(this.object);
        }
        return this.editorDiscounts;
    }

    @NotNull
    public ShopViewEditor getViewEditor() {
        if (this.viewEditor == null) {
            this.viewEditor = new ShopViewEditor(this.plugin, this.object);
        }
        return this.viewEditor;
    }

    @NotNull
    public ProductListEditor getProductsEditor() {
        if (this.productEditor == null) {
            this.productEditor = new ProductListEditor(this.plugin, this.object);
        }
        return this.productEditor;
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        if (slotType == SlotType.PLAYER || slotType == SlotType.PLAYER_EMPTY) {
            event.setCancelled(false);
        }
    }
}