package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RotationTimesEditor extends EditorMenu<ExcellentShop, RotatingShop> implements AutoPaged<DayOfWeek> {

    public RotationTimesEditor(@NotNull ExcellentShop plugin, @NotNull RotatingShop shop) {
        super(plugin, shop, shop.getName() + ": Rotation Times", 27);

        this.addReturn(22).setClick((viewer, event) -> {
            shop.getEditor().openNextTick(viewer, 1);
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public int[] getObjectSlots() {
        return IntStream.range(1, 9).toArray();
    }

    @Override
    @NotNull
    public List<DayOfWeek> getObjects(@NotNull Player player) {
        return Arrays.asList(DayOfWeek.values());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull DayOfWeek day) {
        ItemStack item = new ItemStack(Material.CLOCK, day.ordinal() + 1);
        ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_ROTATION_DAY_TIMES).hideFlags().trimmed()
            .replace(Placeholders.GENERIC_NAME, () -> StringUtil.capitalizeFully(day.name()))
            .replace(Placeholders.GENERIC_TIME, () -> object.getRotationTimes(day).stream()
                .map(time -> time.format(ShopUtils.TIME_FORMATTER)).collect(Collectors.joining("\n")))
            .writeMeta();
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull DayOfWeek day) {
        return (viewer, event) -> {
            if (event.isLeftClick()) {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, wrapper -> {
                    try {
                        this.object.getRotationTimes(day).add(LocalTime.parse(wrapper.getTextRaw(), ShopUtils.TIME_FORMATTER));
                        this.object.saveSettings();
                        this.openNextTick(viewer, viewer.getPage());
                    }
                    catch (DateTimeParseException ignored) {}
                    return true;
                });

            }
            else if (event.isRightClick()) {
                this.object.getRotationTimes(day).clear();
                this.object.saveSettings();
                this.openNextTick(viewer, viewer.getPage());
            }
        };
    }
}
