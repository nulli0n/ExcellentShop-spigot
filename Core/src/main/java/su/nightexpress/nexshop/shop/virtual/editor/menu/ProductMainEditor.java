package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorLocale;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.CommandProduct;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

import java.util.ArrayList;
import java.util.List;

public class ProductMainEditor extends EditorMenu<ExcellentShop, VirtualProduct> {

    private static final String TEXTURE_DOLLAR = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=";
    private static final String TEXTURE_BOX_1 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNmNWIxY2ZlZDFjMjdkZDRjM2JlZjZiOTg0NDk5NDczOTg1MWU0NmIzZmM3ZmRhMWNiYzI1YjgwYWIzYiJ9fX0=";
    private static final String TEXTURE_BOX_2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4MmRlNzJiZjYxYzZkMjMzNjRlMmZlMmQ3Y2MyOGRkZjgzMTQ1ZDE4ZjE5Mzg1N2QzNjljZjlkZjY5MiJ9fX0=";
    private static final String TEXTURE_COMMAND = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQwZjQwNjFiZmI3NjdhN2Y5MjJhNmNhNzE3NmY3YTliMjA3MDliZDA1MTI2OTZiZWIxNWVhNmZhOThjYTU1YyJ9fX0=";

    private ProductPriceEditor editorPrice;

    public ProductMainEditor(@NotNull ExcellentShop plugin, @NotNull VirtualProduct product) {
        super(plugin, product, Placeholders.EDITOR_VIRTUAL_TITLE, 27);

        this.addReturn(22).setClick((viewer, event) -> {
            this.plugin.runTask(task -> product.getShop().getEditor().getProductsEditor().open(viewer.getPlayer(), product.getPage()));
        });

        this.addItem(new ItemStack(Material.ITEM_FRAME), 10).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                if (product instanceof ItemProduct itemProduct) {
                    PlayerUtil.addItem(viewer.getPlayer(), itemProduct.getItem());
                }
                else if (product instanceof CommandProduct commandProduct) {
                    PlayerUtil.addItem(viewer.getPlayer(), commandProduct.getPreview());
                }
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            if (product instanceof ItemProduct itemProduct) {
                itemProduct.setItem(cursor);
            }
            else if (product instanceof CommandProduct commandProduct) {
                commandProduct.setPreview(cursor);
            }
            event.getView().setCursor(null);
            this.save(viewer);
        }).getOptions().setDisplayModifier((viewer, item) -> {
            EditorLocale locale;
            ItemStack original;
            if (product instanceof ItemProduct itemProduct) {
                locale = VirtualLocales.PRODUCT_ITEM;
                original = itemProduct.getItem();
            }
            else if (product instanceof CommandProduct commandProduct) {
                locale = VirtualLocales.PRODUCT_PREVIEW;
                original = commandProduct.getPreview();

            }
            else return;

            item.setType(original.getType());
            item.setAmount(original.getAmount());
            item.setItemMeta(original.getItemMeta());
            ItemUtil.mapMeta(item, meta -> {
                meta.setDisplayName(locale.getLocalizedName());
                meta.setLore(locale.getLocalizedLore());
                meta.addItemFlags(ItemFlag.values());
            });
        });

        this.addItem(Material.WRITABLE_BOOK, VirtualLocales.PRODUCT_RESPECT_ITEM_META, 11).setClick((viewer, event) -> {
            if (!(product instanceof ItemProduct itemProduct)) return;

            itemProduct.setRespectItemMeta(!itemProduct.isRespectItemMeta());
            this.save(viewer);
        }).getOptions().setVisibilityPolicy(viewer -> product instanceof ItemProduct);

        this.addItem(ItemUtil.createCustomHead(TEXTURE_COMMAND), VirtualLocales.PRODUCT_COMMANDS, 11).setClick((viewer, event) -> {
            if (!(product instanceof CommandProduct commandProduct)) return;

            if (event.isRightClick()) {
                commandProduct.getCommands().clear();
                this.save(viewer);
                return;
            }
            this.startEdit(viewer.getPlayer(), plugin.getMessage(VirtualLang.EDITOR_ENTER_COMMAND), chat -> {
                commandProduct.getCommands().add(Colorizer.strip(chat.getMessage()));
                product.getShop().saveProducts();
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> product instanceof CommandProduct);

        this.addItem(ItemUtil.createCustomHead(TEXTURE_DOLLAR), VirtualLocales.PRODUCT_PRICE_MANAGER, 12).setClick((viewer, event) -> {
            if (event.getClick() == ClickType.DROP) {
                product.getPricer().update();
                this.save(viewer);
            }
            else if (event.isLeftClick()) {
                this.getEditorPrice().open(viewer.getPlayer(), 1);
            }
            else {
                List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getCurrencies());
                int index = currencies.indexOf(product.getCurrency()) + 1;
                if (index >= currencies.size()) index = 0;
                product.setCurrency(currencies.get(index));
                this.save(viewer);
            }
        });

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.PRODUCT_DISCOUNT, 13).setClick((viewer, event) -> {
            product.setDiscountAllowed(!product.isDiscountAllowed());
            this.save(viewer);
        });

        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX_1), VirtualLocales.PRODUCT_GLOBAL_STOCK, 15).setClick(this.getStockClick(StockType.GLOBAL));
        this.addItem(ItemUtil.createCustomHead(TEXTURE_BOX_2), VirtualLocales.PRODUCT_PLAYER_STOCK, 16).setClick(this.getStockClick(StockType.PLAYER));

        this.getItems().forEach(menuItem -> {
            if (menuItem.getOptions().getDisplayModifier() == null) {
                menuItem.getOptions().setDisplayModifier((viewer, item) -> ItemUtil.replace(item, product.replacePlaceholders()));
            }
        });
    }

    @NotNull
    private ItemClick getStockClick(@NotNull StockType stockType) {
        return (viewer, event) -> {
            if (event.getClick() == ClickType.DROP) {
                for (TradeType tradeType : TradeType.values()) {
                    this.object.getStock().setInitialAmount(stockType, tradeType, -1);
                    this.object.getStock().setRestockCooldown(stockType, tradeType, 0);
                }
                this.save(viewer);
                return;
            }

            boolean isSell = event.isShiftClick();
            boolean isTime = event.isRightClick();
            TradeType tradeType = isSell ? TradeType.SELL : TradeType.BUY;
            LangMessage message = plugin.getMessage(isTime ? Lang.EDITOR_GENERIC_ENTER_SECONDS : Lang.EDITOR_GENERIC_ENTER_AMOUNT);
            this.startEdit(viewer.getPlayer(),  message, chat -> {
                int amount = StringUtil.getInteger(Colorizer.strip(chat.getMessage()), 0, true);
                if (isTime) {
                    this.object.getStock().setRestockCooldown(stockType, tradeType, amount);
                }
                else this.object.getStock().setInitialAmount(stockType, tradeType, amount);
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
