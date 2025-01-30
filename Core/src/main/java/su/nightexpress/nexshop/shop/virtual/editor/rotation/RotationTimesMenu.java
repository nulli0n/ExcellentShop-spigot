package su.nightexpress.nexshop.shop.virtual.editor.rotation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class RotationTimesMenu extends LinkedMenu<ShopPlugin, Rotation> implements Filled<DayOfWeek> {

    private static final String[] SKULL_DAY = {
        "a8432a5756a04ebf062d72a6f31bd62e8f4d82a92120336ae1972fe18d3870ba",
        "7e50c7097994313d9432142da7651dc6dd633587e2e1dd9a562abbc7878efb65",
        "5dd22db8c6e238fb8cc0819d02a65403297d63b67c6c7ce6b43bc829189837f4",
        "854c1ded92319bd83573f0f0041e730338eb7bb7997eb71ff583c2908323888e",
        "54dac7cf2017a2aefcdf29dc3832d407cbd9c8b6ba0e51a0a3169f6ffb62c015",
        "ded9bedcc1d1c48caee5728e1ef9b008d5a5d30d2e14c1c7b849e8d8553b5257",
        "90bba8d56bb6b906683b18fba481d58fa0bb7d3b3b18c6452e9257df542f53aa"
    };

    //private final VirtualShopModule module;

    public RotationTimesMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X4, VirtualLang.EDITOR_TITLE_ROTATION_TIMES.getString());
        //this.module = module;

        this.addItem(MenuItem.buildReturn(this, 31, (viewer, event) -> {
            this.runNextTick(() -> module.openRotationOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));
    }

    @Override
    @NotNull
    public MenuFiller<DayOfWeek> createFiller(@NotNull MenuViewer viewer) {
        Rotation rotation = this.getLink(viewer);

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(10, 18).toArray())
            .setItems(Arrays.asList(DayOfWeek.values()))
            .setItemCreator(day -> {
                return NightItem.asCustomHead(SKULL_DAY[day.ordinal()])
                    .localized(VirtualLocales.ROTATION_DAY_TIME_OBJECT)
                    .setHideComponents(true)
                    .replacement(replacer -> replacer
                        .replace(Placeholders.GENERIC_NAME, () -> StringUtil.capitalizeFully(day.name()))
                        .replace(Placeholders.GENERIC_TIME, () -> rotation.getRotationTimes(day).stream()
                            .map(time -> Lang.goodEntry(time.format(ShopUtils.TIME_FORMATTER))).collect(Collectors.joining("\n")))
                    );
            })
            .setItemClick(day -> (viewer1, event) -> {
                if (event.isLeftClick()) {
                    this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_TIME, input -> {
                        try {
                            rotation.getRotationTimes(day).add(LocalTime.parse(input.getTextRaw(), ShopUtils.TIME_FORMATTER));
                            rotation.getShop().saveRotations();
                            this.runNextTick(() -> this.flush(viewer));
                        }
                        catch (DateTimeParseException ignored) {}
                        return true;
                    }));

                }
                else if (event.isRightClick()) {
                    rotation.getRotationTimes(day).clear();
                    rotation.getShop().saveRotations();
                    this.runNextTick(() -> this.flush(viewer));
                }
            })
            .build();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
