package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShopSettings implements Writeable {

    public static final int DEFAULT_LAYOUT_PAGE = 0;

    public static final int MAX_PAGES = 100;
    public static final int MIN_PAGES = 1;

    private final Map<Integer, String> pageLayouts;

    private Set<String>  aliases;
    private String       name;
    private List<String> description;
    private NightItem    icon;
    private boolean      permissionRequired;
    private boolean      buyingAllowed;
    private boolean      sellingAllowed;
    private Set<Integer> menuSlots;
    private int          pages;
    private boolean      paginatedLayouts;

    public ShopSettings() {
        this.icon = NightItem.fromType(Material.CHEST);
        this.pageLayouts = new TreeMap<>();
        this.aliases = new HashSet<>();
        this.description = new ArrayList<>();
        this.menuSlots = new HashSet<>();
    }

    @NotNull
    public ShopSettings load(@NotNull FileConfig config, @NotNull String path) {
        if (config.contains(path + ".Transaction_Allowed")) {
            boolean oldBuy = config.getBoolean(path + ".Transaction_Allowed.BUY");
            boolean oldSell = config.getBoolean(path + ".Transaction_Allowed.SELL");
            config.set(path + ".Buying", oldBuy);
            config.set(path + ".Selling", oldSell);
            config.remove(path + ".Transaction_Allowed");
        }
        if (config.contains(path + ".Layout.Name")) {
            String oldDefault = config.getString(path + ".Layout.Name", Placeholders.DEFAULT);
            config.set(path + ".Layout.ByPage." + DEFAULT_LAYOUT_PAGE, oldDefault);
            config.remove(path + ".Layout.Name");
        }

        this.setAliases(config.getStringSet(path + ".Aliases"));
        this.setName(config.getString(path + ".Name", "Null"));
        this.setDescription(config.getStringList(path + ".Description"));
        this.setIcon(config.getCosmeticItem(path + ".Icon"));
        this.setPermissionRequired(config.getBoolean(path + ".Permission_Required", false));
        this.setBuyingAllowed(config.getBoolean(path + ".Buying", true));
        this.setSellingAllowed(config.getBoolean(path + ".Selling", true));
        this.setPages(config.getInt(path + ".Pages", 1));

        this.setMenuSlots(IntStream.of(config.getIntArray(path + ".MainMenu.Slot")).boxed().collect(Collectors.toSet()));

        this.setPaginatedLayouts(ConfigValue.create(path + ".Layout.Paginated", true).read(config));
        config.getSection(path + ".Layout.ByPage").forEach(sId -> {
            int page = NumberUtil.getIntegerAbs(sId, -1);
            if (page < 0) return;

            this.pageLayouts.put(page, config.getString(path + ".Layout.ByPage." + sId, Placeholders.DEFAULT));
        });

        return this;
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Aliases", this.aliases);
        config.set(path + ".Name", this.name);
        config.set(path + ".Description", this.description);
        config.set(path + ".Icon", this.icon);
        config.set(path + ".Permission_Required", this.permissionRequired);
        config.set(path + ".Buying", this.buyingAllowed);
        config.set(path + ".Selling", this.sellingAllowed);
        config.set(path + ".Pages", this.pages);

        config.setIntArray(path + ".MainMenu.Slot", this.menuSlots.stream().mapToInt(i -> i).toArray());

        config.set(path + ".Layout.Paginated", this.paginatedLayouts);
        config.remove(path + ".Layout.ByPage");
        this.pageLayouts.forEach((page, lName) -> config.set(path + ".Layout.ByPage." + page, lName));
    }

    @NotNull
    public Set<String> getAliases() {
        return this.aliases;
    }

    public void setAliases(@NotNull Set<String> aliases) {
        this.aliases = new HashSet<>(aliases);
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    public void setDescription(@NotNull List<String> description) {
        this.description = description;
    }

    @NotNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    public void setIcon(@NotNull NightItem icon) {
        this.icon = icon.copy().setAmount(1);
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public void setPermissionRequired(boolean isPermissionRequired) {
        this.permissionRequired = isPermissionRequired;
    }

    public boolean isBuyingAllowed() {
        return this.buyingAllowed;
    }

    public void setBuyingAllowed(boolean buyingAllowed) {
        this.buyingAllowed = buyingAllowed;
    }

    public boolean isSellingAllowed() {
        return this.sellingAllowed;
    }

    public void setSellingAllowed(boolean sellingAllowed) {
        this.sellingAllowed = sellingAllowed;
    }

    @NotNull
    public Set<Integer> getMenuSlots() {
        return this.menuSlots;
    }

    public void setMenuSlots(@NotNull Set<Integer> menuSlots) {
        this.menuSlots = menuSlots;
        this.menuSlots.removeIf(slot -> slot < 0);
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = Math.clamp(pages, MIN_PAGES, MAX_PAGES);
    }

    public boolean isPaginatedLayouts() {
        return this.paginatedLayouts;
    }

    public void setPaginatedLayouts(boolean paginatedLayouts) {
        this.paginatedLayouts = paginatedLayouts;
    }

    @NotNull
    public String getLayout(int page) {
        return this.paginatedLayouts && this.pageLayouts.containsKey(page) ? this.pageLayouts.get(page) : this.pageLayouts.computeIfAbsent(DEFAULT_LAYOUT_PAGE, k -> Placeholders.DEFAULT);
    }

    public void setPageLayout(int page, @NotNull String layoutName) {
        this.pageLayouts.put(page, layoutName.toLowerCase());
    }

    public void removePageLayout(int page) {
        if (page == DEFAULT_LAYOUT_PAGE) {
            this.setPageLayout(page, Placeholders.DEFAULT);
        }
        else {
            this.pageLayouts.remove(page);
        }
    }
}
