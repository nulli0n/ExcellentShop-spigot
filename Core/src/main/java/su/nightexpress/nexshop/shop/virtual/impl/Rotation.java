package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.random.Rnd;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Rotation implements Writeable {

    private static final NightItem DEFAULT_ICON = NightItem.asCustomHead("a70216baf1b9675f805dfdf95db043afe6f881c82b25937e46b15068e8f3e882");

    private final String      id;
    private final VirtualShop shop;

    private final Map<Integer, Set<Integer>>         slotsByPageMap;
    private final Map<String, RotationItem>          itemMap;
    private final Map<DayOfWeek, TreeSet<LocalTime>> rotationTimes;

    private NightItem icon;
    private RotationType rotationType;
    private int          rotationInterval;

    public Rotation(@NotNull String id, @NotNull VirtualShop shop) {
        this.id = id;
        this.shop = shop;
        this.slotsByPageMap = new HashMap<>();
        this.itemMap = new HashMap<>();
        this.rotationTimes = new HashMap<>();

        this.setIcon(DEFAULT_ICON);
        this.setRotationType(RotationType.INTERVAL);
        this.setRotationInterval(86400);
    }

    public void load(@NotNull FileConfig config, @NotNull String path) {
        config.getSection(path + ".SlotsByPage").forEach(pageStr -> {
            int page = NumberUtil.getIntegerAbs(pageStr);
            if (page == 0) return;

            Set<Integer> slots = IntStream.of(config.getIntArray(path + ".SlotsByPage." + pageStr)).boxed().collect(Collectors.toCollection(LinkedHashSet::new));
            this.setSlots(slots, page);
        });

        config.getSection(path + ".Items").forEach(productId -> {
            VirtualProduct product = this.shop.getProductById(productId);
            if (product == null || !product.isRotating()) return;

            double weight = config.getDouble(path + ".Items." + productId + ".Weight");

            RotationItem item = new RotationItem(productId, weight);
            this.addItem(item);
        });

        this.setIcon(config.getCosmeticItem(path + ".Icon", DEFAULT_ICON));
        this.setRotationType(config.getEnum(path + ".Refresh.Type", RotationType.class, RotationType.INTERVAL));
        this.setRotationInterval(config.getInt(path + ".Refresh.Interval", 86400));
        for (String sDay : config.getSection(path + ".Refresh.Fixed")) {
            DayOfWeek day = StringUtil.getEnum(sDay, DayOfWeek.class).orElse(null);
            if (day == null) continue;

            TreeSet<LocalTime> times = new TreeSet<>(ShopUtils.parseTimes(config.getStringList(path + ".Refresh.Fixed." + sDay)));
            this.rotationTimes.put(day, times);
        }
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.remove(path + ".SlotsByPage");
        this.slotsByPageMap.forEach((page, slots) -> {
            if (slots.isEmpty()) return;

            config.setIntArray(path + ".SlotsByPage." + page, slots.stream().mapToInt(i -> i).toArray());
        });

        config.remove(path + ".Items");
        this.itemMap.forEach((itemId, item) -> {
            config.set(path + ".Items." + itemId + ".Weight", item.getWeight());
        });

        config.set(path + ".Icon", this.icon);
        config.set(path + ".Refresh.Type", this.rotationType.name());
        config.set(path + ".Refresh.Interval", this.rotationInterval);
        config.remove(path + ".Refresh.Fixed");
        this.rotationTimes.forEach((day, times) -> {
            config.set(path + ".Refresh.Fixed." + day.name(), times.stream().map(time -> time.format(ShopUtils.TIME_FORMATTER)).toList());
        });
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.ROTATION.replacer(this);
    }

    @NotNull
    public Map<Integer, List<String>> generateRotationProducts() {
        Map<Integer, List<String>> map = new HashMap<>();

        for (int page = 1; page < this.shop.getPages() + 1; page++) {
            Set<Integer> slots = this.getSlots(page);
            if (slots.isEmpty()) continue;

            Map<RotationItem, Double> itemWeights = new HashMap<>();
            this.shop.getValidProducts().forEach(product -> {
                RotationItem item = this.getItemById(product.getId());
                if (item == null) return;
                if (item.getWeight() <= 0D) return;

                itemWeights.put(item, item.getWeight());
            });

            List<String> productIds = new ArrayList<>();
            int amount = slots.size();

            while (amount > 0 && !itemWeights.isEmpty()) {
                RotationItem item = Rnd.getByWeight(itemWeights);

                productIds.add(item.getProductId());
                itemWeights.remove(item);
                amount--;
            }
            if (productIds.isEmpty()) continue;

            map.put(page, productIds);
        }

        return map;
    }

    public long createNextRotationTimestamp() {
        if (this.getRotationType() == RotationType.INTERVAL) {
            return TimeUtil.createFutureTimestamp(this.rotationInterval);
        }

        if (this.rotationTimes.isEmpty()) return 0L;

        int count = 0;
        while (count < 8) {
            LocalDate dateLookup = LocalDate.now().plusDays(count++);

            LocalTime fit = this.findTime(dateLookup);
            if (fit != null) {
                return TimeUtil.toEpochMillis(LocalDateTime.of(dateLookup, fit));
            }
        }

        return 0L;
    }

    @Nullable
    private LocalTime findTime(@NotNull LocalDate dateLookup) {
        TreeSet<LocalTime> times = this.rotationTimes.get(dateLookup.getDayOfWeek());
        if (times == null || times.isEmpty()) return null;

        LocalTime fit;
        if (dateLookup.getDayOfYear() != LocalDate.now().getDayOfYear()) {
            fit = times.stream().min(LocalTime::compareTo).orElse(null);
        }
        else {
            fit = times.ceiling(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        }
        return fit;
    }

    public void addSlot(int page, int slot) {
        this.slotsByPageMap.computeIfAbsent(page, k -> new LinkedHashSet<>()).add(slot);
    }

    public void removeSlot(int page, int slot) {
        this.slotsByPageMap.computeIfAbsent(page, k -> new LinkedHashSet<>()).remove(slot);
    }

    @NotNull
    public Set<Integer> getSlots(int page) {
        return this.slotsByPageMap.getOrDefault(page, Collections.emptySet());
    }

    public void setSlots(@NotNull Set<Integer> slots, int page) {
        this.slotsByPageMap.put(page, slots);
    }

    public int countAllSlots() {
        return this.slotsByPageMap.values().stream().mapToInt(Set::size).sum();
    }

    public int countItems() {
        return this.itemMap.size();
    }

    public void addItem(@NotNull RotationItem item) {
        this.itemMap.put(item.getProductId(), item);
    }

    public void removeItem(@NotNull RotationItem item) {
        this.itemMap.remove(item.getProductId());
    }

    public boolean hasProduct(@NotNull VirtualProduct product) {
        return this.hasItem(product.getId());
    }

    public boolean hasItem(@NotNull String productId) {
        return this.getItemById(productId.toLowerCase()) != null;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public VirtualShop getShop() {
        return this.shop;
    }

    @NotNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    public void setIcon(@NotNull NightItem icon) {
        this.icon = icon.copy().ignoreNameAndLore();
    }

    @NotNull
    public Map<Integer, Set<Integer>> getSlotsByPageMap() {
        return this.slotsByPageMap;
    }

    @NotNull
    public Map<String, RotationItem> getItemMap() {
        return this.itemMap;
    }

    @NotNull
    public Set<RotationItem> getItems() {
        return new HashSet<>(this.itemMap.values());
    }

    @Nullable
    public RotationItem getItemById(@NotNull String id) {
        return this.itemMap.get(id.toLowerCase());
    }

    @NotNull
    public RotationType getRotationType() {
        return this.rotationType;
    }

    public void setRotationType(@NotNull RotationType rotationType) {
        this.rotationType = rotationType;
    }

    public int getRotationInterval() {
        return this.rotationInterval;
    }

    public void setRotationInterval(int rotationInterval) {
        this.rotationInterval = Math.max(0, rotationInterval);
    }

    @NotNull
    public Map<DayOfWeek, TreeSet<LocalTime>> getRotationTimes() {
        return this.rotationTimes;
    }

    @NotNull
    public TreeSet<LocalTime> getRotationTimes(@NotNull DayOfWeek day) {
        return this.rotationTimes.computeIfAbsent(day, k -> new TreeSet<>());
    }
}
