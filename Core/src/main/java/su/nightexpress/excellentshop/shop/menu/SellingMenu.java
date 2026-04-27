package su.nightexpress.excellentshop.shop.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.menu.SellingMenuAdapter;
import su.nightexpress.excellentshop.api.menu.SellingMenuProvider;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nexshop.util.PacketUtils;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.ui.inventory.MenuRegistry;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.MenuViewer;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SellingMenu extends AbstractObjectMenu<SellingMenu.Data> implements SellingMenuProvider {

    public record Data(@NonNull AbstractShopModule module,
                       @NonNull Map<ItemStack, ProductData> products,
                       @Nullable Shop targetShop,
                       @Nullable Product targetProduct,
                       int shopPage) {

        @NonNull
        public BalanceHolder worth(@NonNull Player player) {
            BalanceHolder holder = new BalanceHolder();

            this.products.forEach((itemStack, quantified) -> {
                Product product = quantified.product();

                holder.store(product.getCurrency(), product.getFinalSellPrice(player, quantified.units()));
            });

            return holder;
        }
    }

    private record ProductData(@NonNull Product product, int units) {
    }

    private SellingMenuAdapter adapter;
    private int[]              productSlots;
    private NightItem          lockedIcon;
    private String             worthText;
    private String             amountText;

    public SellingMenu(@NonNull ShopPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X6, "Selling", Data.class);
    }

    @Override
    public void load(@NonNull FileConfig config) {
        super.load(config);

        PacketUtils.library().ifPresent(lib -> {
            this.adapter = lib.createSellingMenuAdapter(this.plugin, this);
            this.adapter.register();
        });
    }

    public void cleanUp() {
        this.close(); // Force close to return items.

        if (this.adapter != null) {
            this.adapter.unregister();
            this.adapter = null;
        }
    }

    public boolean show(@NonNull Player player, @NonNull AbstractShopModule module, @Nullable Shop targetShop,
                        @Nullable Product targetProduct, int shopPage) {
        return this.show(player, new Data(module, new LinkedHashMap<>(), targetShop, targetProduct, shopPage));
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(36, 45).toArray());
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());

        this.addDefaultButton("sellout", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.LIME_DYE)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Sell Out"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Total worth: " + TagWrappers.WHITE.wrap(ShopPlaceholders.GENERIC_PRICE)),
                        "",
                        TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to sell"))
                    ))
                    .hideAllComponents()
                )
                .condition(context -> !this.getObject(context).products.isEmpty())
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_PRICE, () -> this.getObject(context).worth(context.getPlayer())
                        .format(Lang.OTHER_PRICE_DELIMITER.text()))
                ))
                .action(this::handleSellOut)
                .build()
            )
            .state("empty", ItemState.builder()
                .icon(NightItem.fromType(Material.GRAY_DYE)
                    .setDisplayName(TagWrappers.WHITE.and(TagWrappers.BOLD).wrap("Sell Out"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Nothing to sell.")
                    ))
                    .hideAllComponents()
                )
                .condition(context -> this.getObject(context).products.isEmpty())
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
                        TagWrappers.GRAY.wrap("Click to go back.")
                    ))
                    .hideAllComponents()
                )
                .action(this::handleCancel)
                .build()
            )
            .slots(45)
            .build()
        );
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {
        this.productSlots = config.get(ConfigTypes.INT_ARRAY, "Item.Sell-Slots", IntStream.range(0, 36).toArray());

        this.lockedIcon = config.get(ConfigTypes.NIGHT_ITEM, "Item.Locked-Icon", NightItem.fromType(Material.BARRIER)
            .setDisplayName(TagWrappers.RED.and(TagWrappers.BOLD).wrap("Unsellable"))
            .setLore(Lists.newList(
                TagWrappers.GRAY.wrap("This item can not be sold here.")
            ))
        );

        this.worthText = config.get(ConfigTypes.STRING, "Item.Info.Worth", TagWrappers.GREEN.wrap("Worth: " +
            ShopPlaceholders.GENERIC_WORTH));
        this.amountText = config.get(ConfigTypes.STRING, "Item.Info.Amount", TagWrappers.RED.wrap(
            "Min. amount to sell: " + ShopPlaceholders.GENERIC_AMOUNT));
    }

    @Override
    protected void onClick(@NonNull ViewerContext context, @NonNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        int rawSlot = event.getRawSlot();
        int realSlot = event.getSlot();

        Player player = context.getPlayer();
        ClickType clickType = event.getClick();
        ItemStack clickedItem = event.getCurrentItem();

        boolean isPlayerSlot = rawSlot >= inventory.getSize();
        boolean hasClickedItem = clickedItem != null && !clickedItem.getType().isAir();

        context.getViewer().setNextClickIn(0L); // Allow fast clicks to make sure visuals update always triggers.

        if (clickType == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        if (isPlayerSlot) {
            if (hasClickedItem) {
                if (!this.isSellable(context, clickedItem)) {
                    // This prevents item visual glitch when using shift+double click.
                    this.plugin.runTask(() -> this.triggerSlotUpdate(player, realSlot));
                    return;
                }

                if (event.isShiftClick()) {
                    this.plugin.runTask(() -> {
                        this.addItem(context, clickedItem, false, leftover -> player.getInventory().setItem(realSlot,
                            leftover));
                    });
                    return;
                }
            }

            // Force trigger packet to add item worth lore.
            this.plugin.runTask(() -> this.triggerSlotUpdate(player, realSlot));
            event.setCancelled(false);
            return;
        }

        if (this.isProductSlot(rawSlot)) {
            ItemStack cursor = event.getCursor();
            this.plugin.runTask(() -> {
                if (!cursor.getType().isAir()) {
                    this.addItem(context, cursor, true, leftover -> event.getView().setCursor(leftover));
                    return;
                }

                if (hasClickedItem) {
                    this.removeItem(context, clickedItem, removed -> {
                        if (event.isShiftClick()) {
                            Players.addItem(player, removed);
                        }
                        else {
                            event.getView().setCursor(new ItemStack(removed));
                        }
                    });
                }
            });
            return;
        }

        // This prevents item visual glitch when using shift+double click.
        this.plugin.runTask(() -> this.triggerSlotUpdates(player));
    }

    @Override
    protected void onDrag(@NonNull ViewerContext context, @NonNull InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        Set<Integer> slots = event.getRawSlots();

        if (slots.stream().allMatch(slot -> slot >= inventory.getSize())) {
            Player player = context.getPlayer();

            event.setCancelled(false);
            context.getViewer().setNextClickIn(0L);

            // Force trigger packet to add item worth lore.
            this.plugin.runTask(() -> {
                event.getInventorySlots().forEach(slot -> this.triggerSlotUpdate(player, slot));
            });
        }
    }

    @Override
    protected void onClose(@NonNull ViewerContext context, @NonNull InventoryCloseEvent event) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);
        data.products.forEach((itemStack, productData) -> {
            Product product = productData.product;
            int units = productData.units;
            int totalAmount = UnitUtils.unitsToAmount(product, units);

            Players.addItem(player, itemStack, totalAmount);
        });
        data.products.clear();
    }

    @Override
    public void handleClose(@NonNull Player player, @NonNull InventoryCloseEvent event,
                            @NonNull MenuRegistry menuRegistry) {
        super.handleClose(player, event, menuRegistry);
        this.triggerSlotUpdates(player); // A small workaround to not use #runTask in #onClose due to possible exception on server shutdown.
    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory,
                          @NonNull List<MenuItem> items) {

    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);

        int index = 0;
        for (Map.Entry<ItemStack, ProductData> entry : data.products.entrySet()) {
            if (index >= this.productSlots.length) break;

            Product product = entry.getValue().product;
            int units = entry.getValue().units;
            int unitSize = product.getUnitSize();

            int totalAmount = UnitUtils.unitsToAmount(product, units);
            while (totalAmount > 0) {
                if (index >= this.productSlots.length) break;

                ItemStack itemStack = new ItemStack(entry.getKey());
                int maxStackSize = itemStack.getMaxStackSize();
                int maxUnitsPerStack = maxStackSize / unitSize;
                int amount = UnitUtils.unitsToAmount(product, maxUnitsPerStack);

                itemStack.setAmount(Math.min(amount, totalAmount));
                totalAmount -= itemStack.getAmount();
                inventory.setItem(this.productSlots[index++], itemStack);
            }
        }

        this.plugin.runTask(() -> this.triggerSlotUpdates(player));
    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public boolean isImmuneSlot(@NonNull Player player, int slot) {
        if (this.isProductSlot(slot)) return false;

        MenuViewer viewer = this.getViewer(player);
        if (viewer == null) return true;

        InventoryView view = viewer.getCurrentView();
        if (view == null) return true;

        return slot < view.getTopInventory().getSize();
    }

    @Override
    public boolean isProductSlot(int slot) {
        return Lists.contains(this.productSlots, slot);
    }

    private void addItem(@NonNull ViewerContext context, @NonNull ItemStack clickedItem, boolean resetCursor,
                         @NonNull Consumer<@Nullable ItemStack> consumer) {
        //Player player = context.getPlayer();
        Product product = this.findProduct(context, clickedItem);
        if (product == null) return;

        int amount = clickedItem.getAmount();
        int units = UnitUtils.amountToUnits(product, amount);
        if (units <= 0) return;

        Data data = this.getObject(context);

        ItemStack keyStack = new ItemStack(clickedItem);
        keyStack.setAmount(1);

        ProductData current = data.products.get(keyStack);

        // Calculate menu space.
        int unitSize = product.getUnitSize();
        int maxStackSize = clickedItem.getMaxStackSize();
        int maxUnitsPerStack = maxStackSize / unitSize;
        int maxAllowedUnits = this.productSlots.length * maxUnitsPerStack;

        if (current != null) {
            maxAllowedUnits -= current.units;
        }

        for (ProductData other : data.products.values()) {
            if (other.product == product) continue;
            int otherUnitSize = other.product.getUnitSize();
            int otherMaxStackSize = other.product.getEffectivePreview().getMaxStackSize();
            int otherMaxUnitsPerStack = otherMaxStackSize / otherUnitSize;
            int otherUnitStacks = (int) Math.ceil((double) other.units / (double) otherMaxUnitsPerStack);

            maxAllowedUnits -= (otherUnitStacks * maxUnitsPerStack);
        }
        if (maxAllowedUnits <= 0) return;

        int finalUnits = units;

        if (current != null) {
            finalUnits += current.units();
        }
        data.products.put(keyStack, new ProductData(product, finalUnits));

        // Calculate leftovers
        int leftover = amount - UnitUtils.unitsToAmount(product, units);
        ItemStack left = null;
        if (leftover > 0) {
            left = new ItemStack(clickedItem);
            left.setAmount(leftover);
        }

        if (resetCursor) {
            Optional.ofNullable(context.getViewer().getCurrentView()).ifPresent(view -> view.setCursor(null));
        }

        context.getViewer().refresh();
        consumer.accept(left);
    }

    private void removeItem(@NonNull ViewerContext context, @NonNull ItemStack clickedItem,
                            @NonNull Consumer<@NonNull ItemStack> consumer) {
        Data data = this.getObject(context);

        ItemStack keyStack = new ItemStack(clickedItem);
        keyStack.setAmount(1);

        ProductData productData = data.products.remove(keyStack);
        if (productData == null) return;

        Product product = productData.product;

        int amount = clickedItem.getAmount();
        int units = UnitUtils.amountToUnits(product, amount);
        if (units <= 0) return;

        int currentUnits = productData.units;
        int afterUnits = currentUnits - units;
        if (afterUnits > 0) {
            data.products.put(keyStack, new ProductData(product, afterUnits));
        }

        context.getViewer().refresh();
        consumer.accept(new ItemStack(clickedItem));
    }

    private void triggerSlotUpdates(@NonNull Player player) {
        if (this.adapter == null) return;

        for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
            this.triggerSlotUpdate(player, slot);
        }
    }

    private void triggerSlotUpdate(@NonNull Player player, int slot) {
        if (this.adapter == null) return;

        ItemStack itemStack = player.getInventory().getItem(slot);
        if (itemStack == null || itemStack.getType().isAir()) return;

        this.adapter.callPlayerInventoryPacket(player, slot, itemStack);
    }

    @Nullable
    private Product findProduct(@NonNull ViewerContext context, @NonNull ItemStack itemStack) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);

        Product targetProduct = data.targetProduct;
        if (targetProduct != null) return targetProduct.getContent().isItemMatches(itemStack) ? targetProduct : null;

        Shop targetShop = data.targetShop;
        if (targetShop != null) return targetShop.getBestProduct(itemStack, TradeType.SELL, player);

        AbstractShopModule module = data.module;
        Set<Product> best = module.getShops(player).stream()
            .map(shop -> shop.getBestProduct(itemStack, TradeType.SELL, player))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return ShopUtils.getBestProduct(best, TradeType.SELL, player);
    }

    private boolean isSellable(@NonNull ViewerContext context, @NonNull ItemStack itemStack) {
        return this.findProduct(context, itemStack) != null;
    }

    private void handleSellOut(@NonNull ActionContext context) {
        Data data = this.getObject(context);
        if (data.products.isEmpty()) return;

        Player player = context.getPlayer();
        AbstractShopModule module = data.module;

        Inventory inventory = this.plugin.getServer().createInventory(null, 54);
        EPreparedTransaction.Builder transaction = EPreparedTransaction.builder(player, TradeType.SELL)
            .setStrict(false)
            .setUserInventory(inventory);

        data.products.forEach((itemStack, productData) -> {
            Product product = productData.product;
            int units = productData.units;
            int totalAmount = UnitUtils.unitsToAmount(product, units);

            ItemStack stack = new ItemStack(itemStack);
            stack.setAmount(totalAmount);

            inventory.addItem(stack).values().forEach(fail -> Players.addItem(player, fail));
            transaction.addProduct(product, units);
        });
        data.products.clear();

        module.proceedTransaction(transaction.build(), completed -> {
            // Add back to player all items that were not sold.
            for (ItemStack content : inventory.getContents()) {
                if (content != null && !content.getType().isAir()) {
                    Players.addItem(player, content);
                }
            }

            this.goBack(context);
        });
    }

    private void handleCancel(@NonNull ActionContext context) {
        this.goBack(context);
    }

    private void goBack(@NonNull ViewerContext context) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);

        if (data.targetShop != null) {
            data.targetShop.open(player, data.shopPage);
        }
        else if (data.targetProduct != null) {
            data.targetProduct.getShop().open(player, data.shopPage);
        }
        else {
            context.getViewer().closeMenu();
        }
    }

    @Nullable
    public ItemStack onSlotRender(@NonNull Player player, @NonNull ItemStack itemStack) {
        MenuViewer viewer = this.getViewer(player);
        if (viewer == null) return itemStack;

        if (itemStack.getType().isAir()) return itemStack;

        ViewerContext context = viewer.createContext();

        Product product = this.findProduct(context, itemStack);
        if (product == null) {
            return this.lockedIcon.getItemStack();
        }

        int amount = itemStack.getAmount();
        int units = UnitUtils.amountToUnits(product, amount);

        ItemStack modified = new ItemStack(itemStack);
        PlaceholderContext.Builder builder = PlaceholderContext.builder();
        List<String> lore = ItemUtil.getLoreSerialized(modified);

        if (units <= 0) {
            lore.add(this.amountText);
            builder.with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(product.getUnitSize()));
        }
        else {
            BalanceHolder worth = new BalanceHolder();
            worth.store(product.getCurrency(), product.getFinalSellPrice(player, units));

            lore.add(this.worthText);
            builder.with(ShopPlaceholders.GENERIC_WORTH, () -> worth.format(Lang.OTHER_PRICE_DELIMITER.text()));
        }

        ItemUtil.setLore(modified, builder.build().apply(lore));
        return modified;
    }
}
