package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.util.TimeUtils;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DiscountMainEditor extends EditorMenu<ExcellentShop, VirtualDiscount> {

    public DiscountMainEditor(@NotNull VirtualShop shop, @NotNull VirtualDiscount discount) {
        super(shop.plugin(), discount, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        this.addReturn(40).setClick((viewer, event) -> {
           this.plugin.runTask(task -> discount.getShop().getEditor().getDiscountEditor().open(viewer.getPlayer(), 1));
        });

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.DISCOUNT_AMOUNT, 10).setClick((viewer, event) -> {
            this.startEdit(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_AMOUNT), chat -> {
                discount.setDiscount(StringUtil.getDouble(Colorizer.strip(chat.getMessage()), 0D));
                shop.saveSettings();
                return true;
            });
        });

        this.addItem(Material.REPEATER, VirtualLocales.DISCOUNT_DURATION, 12).setClick((viewer, event) -> {
            this.startEdit(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS), chat -> {
                discount.setDuration(StringUtil.getInteger(Colorizer.strip(chat.getMessage()), 0));
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
            this.startEdit(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_DAY), chat -> {
                DayOfWeek day = StringUtil.getEnum(Colorizer.strip(chat.getMessage()), DayOfWeek.class).orElse(null);
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

            this.startEdit(viewer.getPlayer(), plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_TIME), chat -> {
                try {
                    discount.getTimes().add(LocalTime.parse(Colorizer.strip(chat.getMessage()), TimeUtils.TIME_FORMATTER));
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
