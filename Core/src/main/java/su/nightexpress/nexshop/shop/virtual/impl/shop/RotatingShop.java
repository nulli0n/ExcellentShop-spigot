package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.price.ProductPriceStorage;
import su.nightexpress.nexshop.data.rotation.ShopRotationData;
import su.nightexpress.nexshop.data.rotation.ShopRotationStorage;
import su.nightexpress.nexshop.data.stock.ProductStockStorage;
import su.nightexpress.nexshop.shop.util.TimeUtils;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.product.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.util.Placeholders;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RotatingShop extends VirtualShop<RotatingShop, RotatingProduct> {

    private RotationType rotationType;
    private int rotationInterval;
    private final Map<DayOfWeek, TreeSet<LocalTime>> rotationTimes;

    private int productMinAmount;
    private int productMaxAmount;
    private int[] productSlots;

    public RotatingShop(@NotNull VirtualShopModule module, @NotNull JYML cfg, @NotNull String id) {
        super(module, cfg, id);
        this.rotationTimes = new HashMap<>();

        this.placeholderMap
            .add(Placeholders.SHOP_PAGES, () -> {
                int limit = this.getProductSlots().length;
                int products = this.getData().getProducts().size();

                return String.valueOf((int) Math.ceil((double) products / (double) limit));
            })
            .add(Placeholders.SHOP_NEXT_ROTATION_DATE, () -> {
                LocalDateTime time = this.getNextRotationTime();
                if (time == null) return LangManager.getPlain(Lang.OTHER_NEVER);

                return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // TODO config
            })
            .add(Placeholders.SHOP_NEXT_ROTATION_IN, () -> {
                LocalDateTime next = this.getNextRotationTime();
                if (next == null) return LangManager.getPlain(Lang.OTHER_NEVER);

                return TimeUtil.formatTimeLeft(TimeUtil.toEpochMillis(next));
            })
            .add(Placeholders.SHOP_ROTATION_TYPE, () -> this.getRotationType().name())
            .add(Placeholders.SHOP_ROTATION_INTERVAL, () -> TimeUtil.formatTime(this.getRotationInterval() * 1000L))
            .add(Placeholders.SHOP_ROTATION_MIN_PRODUCTS, () -> NumberUtil.format(this.getProductMinAmount()))
            .add(Placeholders.SHOP_ROTATION_MAX_PRODUCTS, () -> NumberUtil.format(this.getProductMaxAmount()))
            .add(Placeholders.SHOP_ROTATION_PRODUCT_SLOTS, () -> Arrays.toString(this.getProductSlots()));
    }

    @Override
    @NotNull
    protected RotatingShop get() {
        return this;
    }

    @Override
    protected boolean loadAdditionalData() {
        this.setRotationType(cfg.getEnum("Rotation.Type", RotationType.class, RotationType.INTERVAL));
        this.setRotationInterval(cfg.getInt("Rotation.Interval", 86400));
        for (String sDay : cfg.getSection("Rotation.Fixed")) {
            DayOfWeek day = StringUtil.getEnum(sDay, DayOfWeek.class).orElse(null);
            if (day == null) continue;

            TreeSet<LocalTime> times = new TreeSet<>(TimeUtils.parseTimes(cfg.getStringList("Rotation.Fixed." + sDay)));
            this.getRotationTimes().put(day, times);
        }

        this.setProductMinAmount(cfg.getInt("Rotation.Products.Min_Amount"));
        this.setProductMaxAmount(cfg.getInt("Rotation.Products.Max_Amount"));
        this.setProductSlots(cfg.getIntArray("Rotation.Products.Slots"));
        return true;
    }

    @Override
    @NotNull
    protected RotatingProduct loadProduct(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        return VirtualProduct.read(cfg, path, id, RotatingProduct.class);
    }

    @Override
    protected void clearAdditionalData() {

    }

    @Override
    protected void saveAdditionalSettings() {
        cfg.set("Rotation.Type", this.getRotationType().name());
        cfg.set("Rotation.Interval", this.getRotationInterval());
        cfg.remove("Rotation.Fixed");
        this.getRotationTimes().forEach((day, times) -> {
            cfg.set("Rotation.Fixed." + day.name(), times.stream().map(time -> time.format(TimeUtils.TIME_FORMATTER)).toList());
        });

        cfg.set("Rotation.Products.Min_Amount", this.getProductMinAmount());
        cfg.set("Rotation.Products.Max_Amount", this.getProductMaxAmount());
        cfg.setIntArray("Rotation.Products.Slots", this.getProductSlots());
    }

    @Override
    protected void saveAdditionalProducts() {
        this.getProducts().forEach(product -> product.write(this.getConfigProducts(), "List." + product.getId()));
    }

    @Override
    @NotNull
    public VirtualShopType getType() {
        return VirtualShopType.ROTATING;
    }

    @NotNull
    public ShopRotationData getData() {
        ShopRotationData data = ShopRotationStorage.getData(this.getId());
        if (data == null) {
            data = new ShopRotationData(this.getId());
            ShopRotationStorage.createData(data);
        }
        return data;
    }

    @Nullable
    public LocalDateTime getNextRotationTime() {
        ShopRotationData data = this.getData();
        if (this.getRotationType() == RotationType.INTERVAL) {
            long latestRotation = data.getLatestRotation();
            long currentMs = System.currentTimeMillis();
            long diff = currentMs - latestRotation;
            if (diff < this.getRotationInterval() * 1000L) {
                long nextDate = System.currentTimeMillis() + (this.getRotationInterval() * 1000L - diff);
                return TimeUtil.getLocalDateTimeOf(nextDate);
            }
            return LocalDateTime.now();
        }
        else {
            if (this.getRotationTimes().isEmpty()) return null;

            LocalDate dateNow = LocalDate.now();
            while (!this.getRotationTimes().containsKey(dateNow.getDayOfWeek())) {
                dateNow = dateNow.plusDays(1);
            }

            TreeSet<LocalTime> times = this.getRotationTimes(dateNow.getDayOfWeek());
            if (times.isEmpty()) return null;

            LocalTime fit = times.ceiling(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
            if (fit == null) return null;

            return LocalDateTime.of(dateNow, fit);
        }
    }

    public void rotate() {
        ShopRotationData data = this.getData();
        data.setLatestRotation(System.currentTimeMillis());
        data.setProducts(this.generateRotationProducts());
        ShopRotationStorage.saveData(data);
    }

    public boolean tryRotate() {
        LocalDateTime nextRotate = this.getNextRotationTime();
        if (nextRotate == null) return false;

        LocalDateTime now = LocalDateTime.now();
        if (this.getRotationType() == RotationType.FIXED) {
            if (!now.toLocalTime().truncatedTo(ChronoUnit.MINUTES).equals(nextRotate.toLocalTime().truncatedTo(ChronoUnit.MINUTES))) return false;
        }
        else if (this.getRotationType() == RotationType.INTERVAL) {
            if (now.isBefore(nextRotate)) return false;
        }
        this.rotate();
        return true;
    }

    @NotNull
    public Set<String> generateRotationProducts() {
        int amount = Rnd.get(this.getProductMinAmount(), this.getProductMaxAmount());
        if (amount <= 0) return Collections.emptySet();

        Map<RotatingProduct, Double> products = new HashMap<>();
        this.getProducts().stream().filter(RotatingProduct::canRotate)
            .forEach(product -> products.put(product, product.getRotationChance()));

        Set<String> generated = new HashSet<>();
        while (amount > 0 && !products.isEmpty()) {
            RotatingProduct product = Rnd.getByWeight(products);
            ProductPriceStorage.deleteData(product);
            ProductStockStorage.deleteData(product);

            generated.add(product.getId());
            products.remove(product);
            amount--;
        }
        return generated;
    }

    @NotNull
    public RotationType getRotationType() {
        return rotationType;
    }

    public void setRotationType(@NotNull RotationType rotationType) {
        this.rotationType = rotationType;
    }

    public int getRotationInterval() {
        return rotationInterval;
    }

    public void setRotationInterval(int rotationInterval) {
        this.rotationInterval = Math.max(0, rotationInterval);
    }

    @NotNull
    public Map<DayOfWeek, TreeSet<LocalTime>> getRotationTimes() {
        return rotationTimes;
    }

    @NotNull
    public TreeSet<LocalTime> getRotationTimes(@NotNull DayOfWeek day) {
        return this.getRotationTimes().computeIfAbsent(day, k -> new TreeSet<>());
    }

    public int getProductMinAmount() {
        return productMinAmount;
    }

    public void setProductMinAmount(int productMinAmount) {
        this.productMinAmount = Math.max(0, productMinAmount);
    }

    public int getProductMaxAmount() {
        return productMaxAmount;
    }

    public void setProductMaxAmount(int productMaxAmount) {
        this.productMaxAmount = Math.max(0, productMaxAmount);
    }

    public int[] getProductSlots() {
        return productSlots;
    }

    public void setProductSlots(int[] productSlots) {
        this.productSlots = productSlots;
    }
}
