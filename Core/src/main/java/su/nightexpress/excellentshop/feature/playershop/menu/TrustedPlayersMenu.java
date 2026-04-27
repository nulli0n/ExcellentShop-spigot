package su.nightexpress.excellentshop.feature.playershop.menu;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.core.ChestLang;
import su.nightexpress.excellentshop.feature.playershop.dialog.PSDialogKeys;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemPopulator;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.user.UserInfo;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.profile.PlayerProfiles;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class TrustedPlayersMenu extends AbstractObjectMenu<ChestShop> {

    private static final int[] DEFAULT_TRUSTED_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private static final IconLocale TRUSTED_ICON = ChestLang.iconBuilder("UI.TrustedPlayers.Profile")
        .accentColor(TagWrappers.GOLD)
        .name(CommonPlaceholders.PLAYER_NAME)
        .appendInfo("This player is a trusted player.")
        .br()
        .appendClick("Press " + TagWrappers.KEY.apply("key.drop") + " to remove.")
        .build();

    private final ChestShopModule module;

    private ItemPopulator<UserInfo> trustedPlayerPopulator;

    public TrustedPlayersMenu(@NonNull NightPlugin plugin, @NonNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X4, "Trusted Players", ChestShop.class);
        this.module = module;
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(0, 36).toArray());
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, DEFAULT_TRUSTED_SLOTS);

        this.addNextPageButton(32);
        this.addPreviousPageButton(29);
        this.addBackButton(this::handleBack, 27);

        this.addDefaultButton("add_trusted", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ANVIL)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Add Player"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Click to add trusted player.")
                    ))
                    .hideAllComponents()
                )
                .action(this::handleAdd)
                .build()
            )
            .slots(31)
            .build()
        );
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {
        int[] trustedSlots = config.get(ConfigTypes.INT_ARRAY, "Trusted.Slots", DEFAULT_TRUSTED_SLOTS);

        this.trustedPlayerPopulator = ItemPopulator.builder(UserInfo.class)
            .slots(trustedSlots)
            .itemProvider((context, profile) -> NightItem.fromType(Material.PLAYER_HEAD)
                .hideAllComponents()
                .setPlayerProfile(PlayerProfiles.createProfile(profile.id(), profile.name()))
                .localized(TRUSTED_ICON)
                .replace(builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, profile::name)
                )
            )
            .actionProvider(profile -> context -> this.handleTrusted(context, profile))
            .build();
    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory,
                          @NonNull List<MenuItem> items) {

        ChestShop shop = this.getObject(context);
        List<UserInfo> trustedPlayers = shop.getTrustedPlayers()
            .values()
            .stream()
            .sorted(Comparator.comparing(UserInfo::name))
            .toList();

        this.trustedPlayerPopulator.populateTo(context, trustedPlayers, items);
    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

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

    private void handleBack(@NonNull ActionContext context) {
        ChestShop shop = this.getObject(context);
        Player player = context.getPlayer();

        this.module.openShopSettings(player, shop);
    }

    private void handleAdd(@NonNull ActionContext context) {
        ChestShop shop = this.getObject(context);
        Player player = context.getPlayer();

        this.plugin.showDialog(player, PSDialogKeys.SHOP_ADD_TRUSTED_PLAYER, shop, () -> context.getViewer().refresh());
    }

    private void handleTrusted(@NonNull ActionContext context, @NonNull UserInfo profile) {
        ChestShop shop = this.getObject(context);
        Player player = context.getPlayer();

        InventoryClickEvent event = context.getEvent();
        if (event.getClick() == ClickType.DROP) {
            this.module.removeTrustedPlayer(player, shop, profile.id(), () -> context.getViewer().refresh());
        }
    }
}
