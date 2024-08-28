package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.StringUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RotationTimesEditor extends EditorMenu<ShopPlugin, RotatingShop> implements AutoFilled<DayOfWeek> {

    private final VirtualShopModule module;

    public RotationTimesEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, "Rotation Times", 27);
        this.module = module;

        this.addReturn(22, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopEditor(viewer.getPlayer(), shop));
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<DayOfWeek> autoFill) {
        autoFill.setSlots(IntStream.range(1, 9).toArray());
        autoFill.setItems(Arrays.asList(DayOfWeek.values()));
        autoFill.setItemCreator(day -> {
            ItemStack item = new ItemStack(Material.CLOCK, day.ordinal() + 1);
            ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_ROTATION_DAY_TIMES).hideFlags().trimmed()
                .replace(Placeholders.GENERIC_NAME, () -> StringUtil.capitalizeFully(day.name()))
                .replace(Placeholders.GENERIC_TIME, () -> this.getObject(viewer).getRotationTimes(day).stream()
                    .map(time -> time.format(ShopUtils.TIME_FORMATTER)).collect(Collectors.joining("\n")))
                .writeMeta();
            return item;
        });
        autoFill.setClickAction(day -> (viewer1, event) -> {
            if (event.isLeftClick()) {
                this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, (dialog, wrapper) -> {
                    try {
                        this.getLink(viewer).getRotationTimes(day).add(LocalTime.parse(wrapper.getTextRaw(), ShopUtils.TIME_FORMATTER));
                        this.getLink(viewer).saveSettings();
                        this.runNextTick(() -> this.flush(viewer));
                    }
                    catch (DateTimeParseException ignored) {}
                    return true;
                });

            }
            else if (event.isRightClick()) {
                this.getObject(viewer).getRotationTimes(day).clear();
                this.getLink(viewer).saveSettings();
                this.runNextTick(() -> this.flush(viewer));
            }
        });
    }
}
