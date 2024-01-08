package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;

public class ShopMainEditor extends EditorMenu<ExcellentShop, AbstractVirtualShop<?>> {

    private static final String TEXTURE_BOOK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGUxNTU5NDhjYTg1YjA1MTM3ZDJkM2E1YjA4MmY1N2U3NmM2ODFiZmNkZjRmMGRjZjg2ZWFmZjY4MWI5MzY3OCJ9fX0=";
    private static final String TEXTURE_NPC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlMGRjOTJkNzg2MmYwNDQzY2M3NzU3Mzc3NzRmNDA3YWFlZmJlMDVlOWM0MzIzMmJiNjkzZDM5YzE4ZmI4OSJ9fX0=";
    private static final String TEXTURE_BOX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTAwZDI4ZmY3YjU0M2RkMDg4ZDAwNGIxYjFmOTViMzhkNDQ0ZWEwNDYxZmY1YWUzYzY4ZDc2YzBjMTZlMjUyNyJ9fX0=";
    private static final String TEXTURE_PAINT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU0NDI4MGQ0MmRiMDdiMDEyZWI3NmRlZTczMjQ5Yzg2ZmM3MTJlOGViMTRmYTJiNDQ4NDc3MTRiYmI5NWY4MyJ9fX0=";
    private static final String TEXTURE_DOLLAR = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=";

    private ShopViewEditor     viewEditor;
    private ProductListEditor  productEditor;
    private DiscountListEditor discountEditor;

    public ShopMainEditor(@NotNull ExcellentShop plugin, @NotNull AbstractVirtualShop<?> shop) {
        super(plugin, shop, "Shop Editor: " + shop.getId(), 54);

        this.addReturn(49).setClick((viewer, event) -> {
            shop.getModule().getEditor().openNextTick(viewer.getPlayer(), 1);
        });

        this.addItem(Material.NAME_TAG, VirtualLocales.SHOP_NAME, 12).setClick((viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, wrapper -> {
                shop.setName(wrapper.getTextColored());
                this.save(viewer);
                return true;
            });
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOOK), VirtualLocales.SHOP_DESCRIPTION, 13).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                shop.getDescription().clear();
                this.save(viewer);
                return;
            }
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_DESCRIPTION, wrapper -> {
                shop.getDescription().add(wrapper.getTextColored());
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
            ItemReplacer.create(item).readLocale(VirtualLocales.SHOP_ICON).hideFlags().trimmed()
                .replace(shop.replacePlaceholders())
                .writeMeta();
        });

        this.addItem(Material.REDSTONE, VirtualLocales.SHOP_PERMISSION, 24).setClick((viewer, event) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.save(viewer);
        });

        this.addItem(Material.WRITABLE_BOOK, VirtualLocales.SHOP_TRADES, 22).setClick((viewer, event) -> {
            TradeType type = event.isLeftClick() ? TradeType.BUY : TradeType.SELL;
            shop.setTransactionEnabled(type, !shop.isTransactionEnabled(type));
            this.save(viewer);
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_NPC), VirtualLocales.SHOP_ATTACHED_NPCS, 8).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                shop.getNPCIds().clear();
                this.save(viewer);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_NPC_ID, wrapper -> {
                int id = wrapper.asInt(-1);
                if (id < 0) return true;

                shop.getNPCIds().add(id);
                this.save(viewer);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> EngineUtils.hasPlugin(HookId.CITIZENS));

        if (shop instanceof StaticShop staticShop) {
            this.addItem(Material.ENDER_PEARL, VirtualLocales.SHOP_PAGES, 20).setClick((viewer, event) -> {
                int add = event.isLeftClick() ? 1 : -1;
                staticShop.setPages(staticShop.getPages() + add);
                this.save(viewer);
            });

            this.addItem(ItemUtil.createCustomHead(TEXTURE_DOLLAR), VirtualLocales.SHOP_DISCOUNTS, 30).setClick((viewer, event) -> {
                this.getDiscountEditor(staticShop).openNextTick(viewer.getPlayer(), 1);
            });
        }

        if (shop instanceof RotatingShop rotatingShop) {
            this.addItem(Material.OAK_SIGN, VirtualLocales.SHOP_ROTATION_TYPE, 4).setClick((viewer, event) -> {
                rotatingShop.setRotationType(CollectionsUtil.next(rotatingShop.getRotationType()));
                this.save(viewer);
            });

            this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_INTERVAL, 20).setClick((viewer, event) -> {
                if (event.getClick() == ClickType.DROP) {
                    rotatingShop.rotate();
                    this.openNextTick(viewer, viewer.getPage());
                    return;
                }

                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, wrapper -> {
                    rotatingShop.setRotationInterval(wrapper.asInt());
                    this.save(viewer);
                    return true;
                });
            }).getOptions().setVisibilityPolicy(viewer -> rotatingShop.getRotationType() == RotationType.INTERVAL);

            this.addItem(Material.CLOCK, VirtualLocales.SHOP_ROTATION_TIMES, 20).setClick((viewer, event) -> {
                new RotationTimesEditor(plugin, rotatingShop).openNextTick(viewer, 1);
            }).getOptions().setVisibilityPolicy(viewer -> rotatingShop.getRotationType() == RotationType.FIXED);

            this.addItem(Material.CHEST_MINECART, VirtualLocales.SHOP_ROTATION_PRODUCTS, 30).setClick((viewer, event) -> {
                if (event.getClick() == ClickType.DROP) {
                    this.handleInput(viewer, VirtualLang.EDITOR_ENTER_SLOTS, wrapper -> {
                        rotatingShop.setProductSlots(StringUtil.getIntArray(wrapper.getTextRaw()));
                        this.save(viewer);
                        return true;
                    });
                    return;
                }

                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, wrapper -> {
                    if (event.isLeftClick()) {
                        rotatingShop.setProductMinAmount(wrapper.asInt());
                    }
                    else {
                        rotatingShop.setProductMaxAmount(wrapper.asInt());
                    }
                    this.save(viewer);
                    return true;
                });
            });
        }

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX), VirtualLocales.SHOP_PRODUCTS, 31).setClick((viewer, event) -> {
            if (event.getClick() == ClickType.DROP) {
                this.plugin.runTaskAsync(task -> {
                    shop.getPricer().deleteData();
                    shop.getPricer().updatePrices();
                });
                return;
            }
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                shop.getStock().deleteData();
                return;
            }

            this.getProductsEditor().openNextTick(viewer, 1);
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_PAINT), VirtualLocales.SHOP_VIEW_EDITOR, 32).setClick((viewer, event) -> {
            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    this.handleInput(viewer, VirtualLang.EDITOR_ENTER_TITLE, wrapper -> {
                        shop.getView().getOptions().setTitle(wrapper.getTextColored());
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

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, shop.replacePlaceholders());
        }));
    }

    private void save(@NotNull MenuViewer viewer) {
        this.object.saveSettings();
        this.openNextTick(viewer.getPlayer(), viewer.getPage());
    }

    @Override
    public void clear() {
        super.clear();
        if (this.discountEditor != null) {
            this.discountEditor.clear();
            this.discountEditor = null;
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
    public DiscountListEditor getDiscountEditor(@NotNull StaticShop staticShop) {
        if (this.discountEditor == null) {
            this.discountEditor = new DiscountListEditor(staticShop);
        }
        return this.discountEditor;
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
