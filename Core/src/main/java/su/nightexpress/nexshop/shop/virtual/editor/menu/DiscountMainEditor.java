package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DiscountMainEditor extends EditorMenu<ExcellentShop, VirtualDiscount> {

    public DiscountMainEditor(@NotNull StaticShop shop, @NotNull VirtualDiscount discount) {
        super(shop.plugin(), discount, shop.getName() + ": Discount Editor", 45);

        this.addReturn(40).setClick((viewer, event) -> {
           this.plugin.runTask(task -> discount.getShop().getEditor().getDiscountEditor(shop).open(viewer.getPlayer(), 1));
        });

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.DISCOUNT_AMOUNT, 10).setClick((viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_AMOUNT, wrapper -> {
                discount.setDiscount(wrapper.asDouble());
                shop.saveSettings();
                return true;
            });
        });

        this.addItem(Material.REPEATER, VirtualLocales.DISCOUNT_DURATION, 12).setClick((viewer, event) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS, wrapper -> {
                discount.setDuration(wrapper.asInt());
                shop.saveSettings();
                return true;
            });
        });

        this.addItem(Material.DAYLIGHT_DETECTOR, VirtualLocales.DISCOUNT_DAYS, 14).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                discount.getDays().clear();
                this.save(viewer);
                return;
            }

            EditorManager.suggestValues(viewer.getPlayer(), CollectionsUtil.getEnumsList(DayOfWeek.class), true);
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_DAY, wrapper -> {
                DayOfWeek day = StringUtil.getEnum(wrapper.getTextRaw(), DayOfWeek.class).orElse(null);
                if (day == null) return true;

                discount.getDays().add(day);
                shop.saveSettings();
                return true;
            });
        });

        this.addItem(Material.CLOCK, VirtualLocales.DISCOUNT_TIMES, 16).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                discount.getTimes().clear();
                this.save(viewer);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, wrapper -> {
                try {
                    discount.getTimes().add(LocalTime.parse(wrapper.getTextRaw(), ShopUtils.TIME_FORMATTER));
                    shop.saveSettings();
                }
                catch (DateTimeParseException ignored) {}
                return true;
            });
        });

        this.getItems().forEach(menuItem -> {
            if (menuItem.getOptions().getDisplayModifier() == null) {
                menuItem.getOptions().setDisplayModifier((viewer, item) -> ItemUtil.replace(item, discount.replacePlaceholders()));
            }
        });
    }

    private void save(@NotNull MenuViewer viewer) {
        this.object.getShop().saveSettings();
        this.plugin.runTask(task -> this.open(viewer.getPlayer(), viewer.getPage()));
    }
}
