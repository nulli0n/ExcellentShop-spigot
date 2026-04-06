package su.nightexpress.excellentshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrapper;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class BuyingMenu extends AbstractObjectMenu<BuyingMenu.Data> {

    private static final String DEF_TITLE = (TagWrappers.BLACK.wrap("[%s]") + " Buying %s")
        .formatted(ShopPlaceholders.SHOP_NAME, ShopPlaceholders.PRODUCT_PREVIEW_NAME);

    private int[] productSlots;
    private NightItem productLockedSlotIcon;
    private boolean amountButtonsUseStackSize;

    public static class Data {

        final AbstractShopModule module;
        final Product product;
        final int shopPage;

        int selectedUnits;

        public Data(@NonNull AbstractShopModule module, @NonNull Product product, int shopPage, int selectedUnits) {
            this.module = module;
            this.product = product;
            this.shopPage = shopPage;
            this.selectedUnits = Math.max(0, selectedUnits);
        }

        @NonNull
        public BalanceHolder worth(@NonNull Player player) {
            BalanceHolder holder = new BalanceHolder();
            holder.store(this.product.getCurrency(), this.product.getFinalBuyPrice(player, this.selectedUnits));
            return holder;
        }
    }

    public enum AmountPercent {

        FULL(maxUnits -> maxUnits),
        HALF(maxUnits -> maxUnits / 2),
        QUARTER(maxUnits -> maxUnits / 4),
        //EIGHT(maxUnits -> maxUnits / 8),
        SINGLE(maxUnits -> 1);

        private final Function<Integer, Integer> exactFunc;

        AmountPercent(@NonNull Function<Integer, Integer> exactFunc) {
            this.exactFunc = exactFunc;
        }

        public int exactAmount(@NonNull Product product) {
            return this.exactFunc.apply(getMaxUnits(product));
        }
    }

    public BuyingMenu(@NonNull ShopPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X6, DEF_TITLE, Data.class);
    }

    @Override
    @NonNull
    protected String getRawTitle(@NonNull ViewerContext context) {
        Data data = this.getObject(context);

        return PlaceholderContext.builder()
            .with(data.product.placeholders())
            .with(data.product.getShop().placeholders())
            .build()
            .apply(super.getRawTitle(context));
    }

    public boolean show(@NonNull Player player, @NonNull AbstractShopModule module, @NonNull Product product, int shopPage, int units) {
        return this.show(player, new Data(module, product, shopPage, units));
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(36, 45).toArray());
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, 1,10,19,28,7,16,25,34);

        this.addDefaultButton("checkout", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.LIME_DYE)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Checkout"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Your check: " + TagWrappers.WHITE.wrap(ShopPlaceholders.GENERIC_PRICE)),
                        "",
                        TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to purchase"))
                    ))
                    .hideAllComponents()
                )
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_PRICE, () -> this.getObject(context).worth(context.getPlayer()).format(Lang.OTHER_PRICE_DELIMITER.text()))
                ))
                .action(this::handleCheckout)
                .build()
            )
            .slots(53)
            .build()
        );

        this.addDefaultButton("cancel", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.RED_DYE)
                    .setDisplayName(TagWrappers.RED.and(TagWrappers.BOLD).wrap("Cancel"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Click to go back shopping.")
                    ))
                    .hideAllComponents()
                )
                .action(this::handleCancel)
                .build()
            )
            .slots(45)
            .build()
        );

        this.addPlusButton(8, AmountPercent.SINGLE);
        this.addPlusButton(17, AmountPercent.QUARTER);
        this.addPlusButton(26, AmountPercent.HALF);
        this.addPlusButton(35, AmountPercent.FULL);

        this.addMinusButton(0, AmountPercent.SINGLE);
        this.addMinusButton(9, AmountPercent.QUARTER);
        this.addMinusButton(18, AmountPercent.HALF);
        this.addMinusButton(27, AmountPercent.FULL);
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {
        this.productSlots = config.get(ConfigTypes.INT_ARRAY, "Product.Slots", new int[]{2,3,4,5,6, 11,12,13,14,15, 20,21,22,23,24, 29,30,31,32,33});

        this.productLockedSlotIcon = config.get(ConfigTypes.NIGHT_ITEM, "Product.Locked-Slot.Icon", NightItem.fromType(Material.BARRIER)
            .setDisplayName(TagWrappers.RED.and(TagWrappers.BOLD).wrap("Locked"))
            .setLore(Lists.newList(
                TagWrappers.GRAY.wrap("We can't offer more of this"),
                TagWrappers.GRAY.wrap("item for you, or you can't"),
                TagWrappers.GRAY.wrap("afford it.")
            ))
            .hideAllComponents()
        );

        this.amountButtonsUseStackSize = config.get(ConfigTypes.BOOLEAN, "Settings.Set-Stack-Size-For-Amount-Buttons", true);
    }

    @Override
    protected void onClick(@NonNull ViewerContext context, @NonNull InventoryClickEvent event) {

    }

    @Override
    protected void onDrag(@NonNull ViewerContext context, @NonNull InventoryDragEvent event) {

    }

    @Override
    protected void onClose(@NonNull ViewerContext context, @NonNull InventoryCloseEvent event) {

    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory, @NonNull List<MenuItem> items) {
        this.validateQuantity(context, items);
    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    private void validateQuantity(@NonNull ViewerContext context, @NonNull List<MenuItem> items) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);
        Product product = data.product;

        int maxStackSize = product.getMaxStackSize();
        int cartUnitSize = this.productSlots.length;
        int cartItemsCapacity = maxStackSize * cartUnitSize;
        int cartUnitCapacity = UnitUtils.amountToUnits(product, cartItemsCapacity);
        int maxBuyableUnits = product.getMaxBuyableUnitAmount(player, player.getInventory());
        int maxAllowedUnits = maxBuyableUnits >= 0 ? Math.min(cartUnitCapacity, maxBuyableUnits) : cartUnitCapacity;

        if (data.selectedUnits <= 0) {
            data.selectedUnits = 1;
        }
        if (data.selectedUnits > maxAllowedUnits) {
            data.selectedUnits = maxAllowedUnits;
        }

        int totalAmount = UnitUtils.unitsToAmount(product, data.selectedUnits);
        int productIndex = 0;
        while (totalAmount > 0 && productIndex < cartUnitSize) {
            ItemStack itemStack = product.getEffectivePreview();
            itemStack.setAmount(Math.min(maxStackSize, totalAmount));
            items.add(MenuItem.custom()
                .defaultState(NightItem.fromItemStack(itemStack))
                .slots(this.productSlots[productIndex++])
                .build()
            );
            totalAmount -= maxStackSize;
        }

        if (cartUnitCapacity > maxAllowedUnits) {
            int maxAllowedItems = UnitUtils.unitsToAmount(product, maxAllowedUnits);
            int allowedSlots = (int) Math.ceil((double) maxAllowedItems / (double) maxStackSize);

            for (int index = allowedSlots; index < cartUnitSize; index++) {
                items.add(MenuItem.custom()
                    .defaultState(this.productLockedSlotIcon.copy())
                    .slots(this.productSlots[index])
                    .build());
            }
        }
    }

    private int getMaxUnits(@NonNull ViewerContext context) {
        return getMaxUnits(this.getObject(context).product);
    }

    private static int getMaxUnits(@NonNull Product product) {
        int unitSize = product.getUnitSize();
        int maxSize = product.getMaxStackSize();

        return maxSize / unitSize;
    }

    private void addPlusButton(int slot, @NonNull AmountPercent percent) {
        this.addPlusMinusButton(slot, percent, true);
    }

    private void addMinusButton(int slot, @NonNull AmountPercent percent) {
        this.addPlusMinusButton(slot, percent, false);
    }

    private void addPlusMinusButton(int slot, @NonNull AmountPercent percent, boolean plus) {
        String prefix = plus ? "add" : "remove";
        TagWrapper nameColor = plus ? TagWrappers.GREEN : TagWrappers.RED;
        String namePrefix = plus ? "+" : "-";
        Material material = plus ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

        this.addDefaultButton(prefix + "_" + LowerCase.INTERNAL.apply(percent.name()), MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(material)
                    .setDisplayName(nameColor.and(TagWrappers.BOLD).wrap(namePrefix + ShopPlaceholders.GENERIC_AMOUNT))
                    .hideAllComponents()
                )
                .condition(context -> {
                    int maxUnits = this.getMaxUnits(context);
                    if (maxUnits < 1) return false;
                    if (maxUnits == 1) return percent == AmountPercent.SINGLE;
                    if (maxUnits < 8) return percent == AmountPercent.SINGLE || percent == AmountPercent.FULL;
                    //if (maxUnits < 16) return percent == AmountPercent.SINGLE || percent == AmountPercent.FULL || percent == AmountPercent.QUARTER;

                    return true;
                })
                .displayModifier((context, item) -> {
                    Product product = this.getObject(context).product;

                    item
                        .setAmount(this.amountButtonsUseStackSize ? percent.exactAmount(product) : 1)
                        .replace(builder -> builder
                            .with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(percent.exactAmount(product)))
                        );
                })
                .action(context -> this.handlePlusMinus(context, percent, plus))
                .build()
            )
            .slots(slot)
            .build()
        );
    }

    private void handleCheckout(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);
        Product product = data.product;
        Shop shop = product.getShop();
        int units = data.selectedUnits;
        int shopPage = data.shopPage;

        EPreparedTransaction transaction = EPreparedTransaction.builder(player, TradeType.BUY).addProduct(product, units).build();

        data.module.proceedTransaction(transaction, completed -> {
            if (data.module.getSettings().isBuyingMenuCloseAfterPurchase()) {
                context.getViewer().closeMenu();
            }
            else {
                shop.open(player, shopPage);
            }
        });
    }

    private void handleCancel(@NonNull ActionContext context) {
        Data data = this.getObject(context);
        Shop shop = data.product.getShop();

        shop.open(context.getPlayer(), data.shopPage);
    }

    private void handlePlusMinus(@NonNull ActionContext context, @NonNull AmountPercent percent, boolean plus) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);
        Product product = data.product;

        if (!plus) {
            data.selectedUnits -= percent.exactAmount(product);
            context.getViewer().refresh();
            return;
        }

        EPreparedTransaction transaction = EPreparedTransaction.builder(player, TradeType.BUY)
            .setPreview(true)
            .addProduct(product, data.selectedUnits)
            .build();

        data.module.previewTransaction(transaction, result -> {
            if (result != ETransactionResult.SUCCESS) return;

            data.selectedUnits += percent.exactAmount(product);
            context.getViewer().refresh();
        });
    }
}
