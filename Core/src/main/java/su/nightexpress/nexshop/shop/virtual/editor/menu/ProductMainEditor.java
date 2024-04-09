package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;

public class ProductMainEditor extends EditorMenu<ExcellentShop, AbstractVirtualProduct<?>> {

    private static final String TEXTURE_DOLLAR  = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=";
    private static final String TEXTURE_BOX_1   = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNmNWIxY2ZlZDFjMjdkZDRjM2JlZjZiOTg0NDk5NDczOTg1MWU0NmIzZmM3ZmRhMWNiYzI1YjgwYWIzYiJ9fX0=";
    private static final String TEXTURE_BOX_2   = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4MmRlNzJiZjYxYzZkMjMzNjRlMmZlMmQ3Y2MyOGRkZjgzMTQ1ZDE4ZjE5Mzg1N2QzNjljZjlkZjY5MiJ9fX0=";

    private ProductPriceEditor editorPrice;

    public ProductMainEditor(@NotNull ExcellentShop plugin, @NotNull AbstractVirtualProduct<?> product) {
        super(plugin, product, "Product Editor (Shop: " + product.getShop().getId() + ")", 54);

        this.addReturn(49).setClick((viewer, event) -> {
            int page = product instanceof StaticProduct staticProduct ? staticProduct.getPage() : 1;
            product.getShop().getEditor().getProductsEditor().openNextTick(viewer.getPlayer(), page);
        });

        for (ProductHandler handler : ProductHandlerRegistry.getHandlerMap().values()) {
            handler.loadEditor(this, product);
        }

        if (product instanceof RotatingProduct rotatingProduct) {
            this.addItem(Material.ENDER_EYE, VirtualLocales.PRODUCT_ROTATION_CHANCE, 8).setClick((viewer, event) -> {
                this.handleInput(viewer, VirtualLang.EDITOR_ENTER_CHANCE, wrapper -> {
                    rotatingProduct.setRotationChance(wrapper.asDouble());
                    this.save(viewer);
                    return true;
                });
            });
        }

        /*this.addItem(Material.ANVIL, VirtualLocales.PRODUCT_HANDLER, 1).setClick((viewer, event) -> {
            EditorManager.suggestValues(viewer.getPlayer(), ProductHandlerRegistry.getHandlerMap().keySet(), true);
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_RANK, wrapepr -> {
                ProductHandler handler = ProductHandlerRegistry.getHandler(wrapepr.getTextRaw());
                if (handler != null) {
                    product.setHandler(handler);
                    product.getShop().saveProducts();
                }
                return true;
            });
        });*/

        ProductPacker packer = product.getPacker();

        boolean usePreview = !(packer instanceof ItemPacker) || ((ItemPacker)packer).isUsePreview();
        int slot = 4;
        if (packer instanceof ItemPacker itemPacker && itemPacker.isUsePreview()) {
            slot = 3;
        }

        // Preview button
        this.addItem(new ItemStack(Material.ITEM_FRAME), slot).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                PlayerUtil.addItem(viewer.getPlayer(), product.getPreview());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            product.getPacker().setPreview(cursor);
            event.getView().setCursor(null);
            this.save(viewer);
        }).getOptions().setVisibilityPolicy(viewer -> usePreview).setDisplayModifier((viewer, item) -> {
            ItemStack original = product.getPacker().getPreview();
            item.setType(original.getType());
            item.setAmount(original.getAmount());
            item.setItemMeta(original.getItemMeta());
            ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_PREVIEW).hideFlags().writeMeta();
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_DOLLAR), VirtualLocales.PRODUCT_PRICE_MANAGER, 21).setClick((viewer, event) -> {
            if (event.getClick() == ClickType.DROP) {
                //product.getShop().getPricer().deleteData(product);
                product.getShop().getPricer().updatePrice(product);
                this.save(viewer);
            }
            else if (event.isLeftClick()) {
                this.getEditorPrice().open(viewer.getPlayer(), 1);
            }
            else {
                EditorManager.suggestValues(viewer.getPlayer(), plugin.getCurrencyManager().getCurrencyIds(), true);
                this.handleInput(viewer, Lang.EDITOR_PRODUCT_ENTER_CURRENCY, wrapper -> {
                    Currency currency = this.plugin.getCurrencyManager().getCurrency(wrapper.getTextRaw());
                    if (currency != null) {
                        product.setCurrency(currency);
                        product.getShop().saveProducts();
                    }
                    return true;
                });
            }
        });

        this.addItem(Material.NAME_TAG, VirtualLocales.PRODUCT_DISCOUNT, 23).setClick((viewer, event) -> {
            product.setDiscountAllowed(!product.isDiscountAllowed());
            this.save(viewer);
        });

        this.addItem(Material.GOLDEN_HELMET, VirtualLocales.PRODUCT_RANKS_AND_PERMS, 25).setClick((viewer, event) -> {
            boolean isPerm = event.isShiftClick();

            if (event.isRightClick()) {
                if (isPerm) {
                    product.getRequiredPermissions().clear();
                }
                else {
                    product.getAllowedRanks().clear();
                }
                this.save(viewer);
                return;
            }

            this.handleInput(viewer, isPerm ? VirtualLang.EDITOR_ENTER_PERMISSION : VirtualLang.EDITOR_ENTER_RANK, wrapepr -> {
                if (isPerm) {
                    product.getRequiredPermissions().add(wrapepr.getTextRaw());
                }
                else {
                    product.getAllowedRanks().add(wrapepr.getTextRaw().toLowerCase());
                }
                product.getShop().saveProducts();
                return true;
            });
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX_1), VirtualLocales.PRODUCT_GLOBAL_STOCK, 38).setClick(this.getStockClick(product.getStockValues()));
        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX_2), VirtualLocales.PRODUCT_PLAYER_STOCK, 42).setClick(this.getStockClick(product.getLimitValues()));

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.create(item).readMeta().trimmed()
                .replace(product.getPlaceholders())
                .writeMeta();
        }));
    }

    @NotNull
    private ItemClick getStockClick(@NotNull StockValues values) {
        return (viewer, event) -> {
            if (event.getClick() == ClickType.DROP) {
                for (TradeType tradeType : TradeType.values()) {
                    values.setInitialAmount(tradeType, -1);
                    values.setRestockSeconds(tradeType, 0);
                }
                this.save(viewer);
                return;
            }

            boolean isSell = event.isShiftClick();
            boolean isTime = event.isRightClick();
            TradeType tradeType = isSell ? TradeType.SELL : TradeType.BUY;
            this.handleInput(viewer,  isTime ? Lang.EDITOR_GENERIC_ENTER_SECONDS : Lang.EDITOR_GENERIC_ENTER_AMOUNT, wrapper -> {
                int amount = wrapper.asAnyInt(0);
                if (isTime) {
                    values.setRestockSeconds(tradeType, amount);
                }
                else {
                    values.setInitialAmount(tradeType, amount);
                }
                this.object.getShop().saveProducts();
                return true;
            });
        };
    }

    private void save(@NotNull MenuViewer viewer) {
        this.object.getShop().saveProducts();
        this.plugin.runTask(task -> this.open(viewer.getPlayer(), viewer.getPage()));
    }

    @Override
    public void clear() {
        super.clear();
        if (this.editorPrice != null) {
            this.editorPrice.clear();
            this.editorPrice = null;
        }
    }

    @NotNull
    public ProductPriceEditor getEditorPrice() {
        if (this.editorPrice == null) {
            this.editorPrice = new ProductPriceEditor(this.object);
        }
        return editorPrice;
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        if (slotType == SlotType.PLAYER || slotType == SlotType.PLAYER_EMPTY) {
            event.setCancelled(false);
        }
    }
}
