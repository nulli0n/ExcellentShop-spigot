package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.data.object.RotationData;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.message.LangMessage;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RotatingShop extends AbstractVirtualShop<RotatingProduct> {

    private final Map<DayOfWeek, TreeSet<LocalTime>> rotationTimes;

    private RotationData rotationData;
    private RotationType rotationType;
    private int          rotationInterval;
    private boolean      locked;

    private int   productMinAmount;
    private int   productMaxAmount;
    private int[] productSlots;

    public RotatingShop(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull File file, @NotNull String id) {
        super(plugin, module, file, id);
        this.rotationTimes = new HashMap<>();
        this.locked = true;
        this.rotationData = new RotationData(this.getId());
        this.placeholderMap.add(Placeholders.forRotatingShop(this));
    }

    public void loadData() {
        this.rotationData = this.plugin.getData().getVirtualDataHandler().getRotationData(this);
        if (this.rotationData == null) {
            this.rotationData = new RotationData(this.getId());
            this.plugin.getData().getVirtualDataHandler().insertRotationData(this.getData());
        }
        this.locked = false;
        this.tryRotate();
    }

    @Override
    protected boolean loadAdditional(@NotNull FileConfig config) {
        this.setRotationType(config.getEnum("Rotation.Type", RotationType.class, RotationType.INTERVAL));
        this.setRotationInterval(config.getInt("Rotation.Interval", 86400));
        for (String sDay : config.getSection("Rotation.Fixed")) {
            DayOfWeek day = StringUtil.getEnum(sDay, DayOfWeek.class).orElse(null);
            if (day == null) continue;

            TreeSet<LocalTime> times = new TreeSet<>(ShopUtils.parseTimes(config.getStringList("Rotation.Fixed." + sDay)));
            this.getRotationTimes().put(day, times);
        }

        this.setProductMinAmount(config.getInt("Rotation.Products.Min_Amount"));
        this.setProductMaxAmount(config.getInt("Rotation.Products.Max_Amount"));
        this.setProductSlots(config.getIntArray("Rotation.Products.Slots"));
        return true;
    }

    @Override
    public void addProduct(@NotNull Product product) {
        if (product instanceof RotatingProduct rotatingProduct) {
            this.addProduct(rotatingProduct);
        }
    }

    @Override
    @NotNull
    public RotatingProduct createProduct(@NotNull String id, @NotNull Currency currency,
                                         @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        return new RotatingProduct(this.plugin, id, this, currency, handler, packer);
    }

    @Override
    protected void saveAdditionalSettings(@NotNull FileConfig config) {
        config.set("Rotation.Type", this.getRotationType().name());
        config.set("Rotation.Interval", this.getRotationInterval());
        config.remove("Rotation.Fixed");
        this.getRotationTimes().forEach((day, times) -> {
            config.set("Rotation.Fixed." + day.name(), times.stream().map(time -> time.format(ShopUtils.TIME_FORMATTER)).toList());
        });

        config.set("Rotation.Products.Min_Amount", this.getProductMinAmount());
        config.set("Rotation.Products.Max_Amount", this.getProductMaxAmount());
        config.setIntArray("Rotation.Products.Slots", this.getProductSlots());
    }

    @Override
    protected void saveAdditionalProducts() {
        this.getProducts().forEach(this::writeProduct);
    }

    @Override
    protected void writeProduct(@NotNull RotatingProduct product) {
        product.write(this.configProducts, this.getProductSavePath(product));
    }

    @Override
    @NotNull
    public ShopType getType() {
        return ShopType.ROTATING;
    }

    @NotNull
    public RotationData getData() {
        return this.rotationData;
    }

    @Nullable
    public LocalDateTime getNextRotationTime() {
        if (this.locked) return null;

        RotationData data = this.getData();
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
        RotationData data = this.getData();
        data.setLatestRotation(System.currentTimeMillis());
        data.setProducts(this.generateRotationProducts());
        this.plugin.getData().getVirtualDataHandler().saveRotationData(data);
    }

    public boolean tryRotate() {
        if (this.locked) return false;

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

        LangMessage notify = VirtualLang.SHOP_ROTATION_NOTIFY.getMessage()
            .replace(this.replacePlaceholders())
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(this.productSlots.length));

        // Back to main server thread.
        this.plugin.runTask(task -> {
            this.plugin.getServer().getOnlinePlayers().forEach(player -> {
                if (!this.canAccess(player, false)) return;
                notify.send(player);

                this.module.updateShopMenu(player, this);
            });
        });

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
            this.getPricer().deleteData(product);
            this.getStock().deleteGlobalData(product);

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
