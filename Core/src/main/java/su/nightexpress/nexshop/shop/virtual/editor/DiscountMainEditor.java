package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DiscountMainEditor extends EditorMenu<ShopPlugin, VirtualDiscount> {

    private final VirtualShopModule module;

    public DiscountMainEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Discount Editor"), MenuSize.CHEST_45);
        this.module = module;

        this.addReturn(40, (viewer, event, discount) -> {
            this.runNextTick(() -> this.module.openDiscountsEditor(viewer.getPlayer(), discount.getShop()));
        });

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.DISCOUNT_AMOUNT, 10, (viewer, event, discount)  -> {
            this.handleInput(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_AMOUNT.text(), (dialog, input) -> {
                discount.setDiscount(input.asDouble());
                discount.getShop().markDirty();
                return true;
            });
        });

        this.addItem(Material.REPEATER, VirtualLocales.DISCOUNT_DURATION, 12, (viewer, event, discount) -> {
            this.handleInput(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_SECONDS.text(), (dialog, input) -> {
                discount.setDuration(input.asInt());
                discount.getShop().markDirty();
                return true;
            });
        });

        this.addItem(Material.DAYLIGHT_DETECTOR, VirtualLocales.DISCOUNT_DAYS, 14, (viewer, event, discount)  -> {
            if (event.isRightClick()) {
                discount.getDays().clear();
                this.saveAndFlush(viewer, discount.getShop());
                return;
            }

            this.handleInput(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_DAY.text(), (dialog, input) -> {
                DayOfWeek day = StringUtil.getEnum(input.getTextRaw(), DayOfWeek.class).orElse(null);
                if (day == null) return true;

                discount.getDays().add(day);
                discount.getShop().markDirty();
                return true;
            }).setSuggestions(Lists.getEnums(DayOfWeek.class), true);
        });

        this.addItem(Material.CLOCK, VirtualLocales.DISCOUNT_TIMES, 16, (viewer, event, discount)  -> {
            if (event.isRightClick()) {
                discount.getTimes().clear();
                this.saveAndFlush(viewer, discount.getShop());
                return;
            }

            this.handleInput(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_TIME.text(), (dialog, input) -> {
                try {
                    discount.getTimes().add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                    discount.getShop().markDirty();
                }
                catch (DateTimeParseException ignored) {}
                return true;
            });
        });

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).replacePlaceholders());
        }));
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        shop.markDirty();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
