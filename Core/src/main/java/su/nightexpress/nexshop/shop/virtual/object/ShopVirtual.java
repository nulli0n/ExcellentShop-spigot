package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.AbstractTimed;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.*;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyId;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.ShopDiscount;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopMain;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShopVirtual extends AbstractShop<IProductVirtual> implements IShopVirtual {

    private final JYML configProducts;
    private final JYML configView;

    private final BankVirtual bank;
    private       int         pages;
    private boolean     isPermissionRequired;
    private ItemStack   icon;
    private int[]       citizensIds;

    private AbstractShopView<IShopVirtual> view;
    private EditorShopMain                 editor;

    public ShopVirtual(@NotNull VirtualShop virtualShop, @NotNull JYML cfg) {
        super(virtualShop.plugin(), cfg);

        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "products.yml");
        this.configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "view.yml");
        this.configView.addMissing("Title", StringUtil.capitalizeFully(this.getId()));
        this.configView.addMissing("Size", 54);
        this.configView.saveChanges();

        this.bank = new BankVirtual(this);

        this.setName(cfg.getString("Name", this.configView.getString("Title", this.getId())));
        this.setPages(cfg.getInt("Pages", 1));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));
        this.setIcon(cfg.getItem("Icon"));
        this.setCitizensIds(cfg.getIntArray("Citizens.Attached_NPC"));

        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }

        for (String sId : cfg.getSection("Discounts.Custom")) {
            String path = "Discounts.Custom." + sId + ".";
            double dDiscount = cfg.getDouble(path + "Discount", 1D);
            if (dDiscount <= 0) continue;

            Set<DayOfWeek> days = AbstractTimed.parseDays(cfg.getString(path + "Times.Days", ""));
            Set<LocalTime[]> times = AbstractTimed.parseTimes(cfg.getStringList(path + "Times.Times"));
            IShopDiscount discount = new ShopDiscount(days, times, dDiscount);
            this.getDiscounts().add(discount);
        }

        for (String sId : this.configProducts.getSection("List")) {
            String path = "List." + sId + ".";

            String path2 = path + "Shop_View.";
            ItemStack shopPreview = configProducts.getItemEncoded(path2 + "Preview");
            if (shopPreview == null || shopPreview.getType().isAir()) {
                plugin.error("Invalid product preview of '" + sId + "' in '" + getId() + "' shop!");
                continue;
            }
            int shopSlot = configProducts.getInt(path2 + "Slot", -1);
            int shopPage = configProducts.getInt(path2 + "Page", -1);

            path2 = path + "Discount.";
            boolean isDiscountAllowed = configProducts.getBoolean(path2 + "Allowed");

            path2 = path + "Purchase.";
            String currencyId = configProducts.getString(path2 + "Currency", CurrencyId.VAULT);
            ICurrency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency == null) {
                plugin.error("Invalid product currency of '" + sId + "' in '" + getId() + "' shop!");
                continue;
            }

            Map<TradeType, int[]> limit = new HashMap<>();
            Map<TradeType, double[]> priceMinMax = new HashMap<>();
            boolean itemMetaEnabled = configProducts.getBoolean(path2 + "Item_Meta_Enabled");
            for (TradeType tradeType : TradeType.values()) {
                String path3 = path2 + tradeType.name() + ".";
                double priceMin = configProducts.getDouble(path3 + "Price_Min");
                double priceMax = configProducts.getDouble(path3 + "Price_Max");

                priceMinMax.put(tradeType, new double[]{priceMin, priceMax});

                String path4 = path + "Limit." + tradeType.name() + ".";
                int buyLimitAmount = configProducts.getInt(path4 + "Amount", -1);
                int buyLimitCooldown = configProducts.getInt(path4 + "Cooldown", 0);
                limit.put(tradeType, new int[]{buyLimitAmount, buyLimitCooldown});
            }

            path2 = path + "Purchase.Randomizer.";
            boolean isRndEnabled = configProducts.getBoolean(path2 + "Enabled");
            Set<DayOfWeek> rndDays = AbstractTimed.parseDays(configProducts.getString(path2 + "Times.Days", ""));
            Set<LocalTime[]> rndTimes = AbstractTimed.parseTimes(configProducts.getStringList(path2 + "Times.Times"));

            path2 = path + "Reward.";
            ItemStack rewardItem = configProducts.getItemEncoded(path2 + "Item");
            List<String> rewardCommands = configProducts.getStringList(path2 + "Commands");

            IProductPricer pricer = new ProductPricer(priceMinMax, isRndEnabled, rndDays, rndTimes);

            IProductVirtual product = new ProductVirtual(
                this, sId,

                shopPreview, rewardItem, currency, pricer, isDiscountAllowed, itemMetaEnabled, shopSlot, shopPage,
                limit,

                rewardCommands);

            this.getProductMap().put(product.getId(), product);
        }

        this.setupView();
    }

    public ShopVirtual(@NotNull VirtualShop virtualShop, @NotNull String path) {
        this(virtualShop, new JYML(new File(path)));
        this.setIcon(new ItemStack(Material.STONE));
        this.save();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
            .replace(Placeholders.SHOP_VIRTUAL_PERMISSION_NODE, Perms.VIRTUAL_SHOP + this.getId())
            .replace(Placeholders.SHOP_VIRTUAL_PERMISSION_REQUIRED, LangManager.getBoolean(this.isPermissionRequired()))
            .replace(Placeholders.SHOP_VIRTUAL_ICON_NAME, ItemUtil.getItemName(this.getIcon()))
            .replace(Placeholders.SHOP_VIRTUAL_PAGES, String.valueOf(this.getPages()))
            .replace(Placeholders.SHOP_VIRTUAL_VIEW_SIZE, String.valueOf(this.getView().getSize()))
            .replace(Placeholders.SHOP_VIRTUAL_VIEW_TITLE, this.getView().getTitle())
            .replace(Placeholders.SHOP_VIRTUAL_NPC_IDS, String.join(", ", IntStream.of(this.getCitizensIds()).boxed()
                .map(String::valueOf).toList()))
        );
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.clear();
            this.view = null;
        }
        if (this.products != null) {
            this.products.values().forEach(IProduct::clear);
            this.products.clear();
        }
    }

    @Override
    public void onSave() {
        configView.set("Title", this.view.getTitle());
        configView.set("Size", this.view.getSize());
        configView.saveChanges();

        cfg.set("Name", this.getName());
        cfg.set("Pages", this.getPages());
        cfg.set("Permission_Required", this.isPermissionRequired());
        this.purchaseAllowed.forEach((type, isAllowed) -> {
            cfg.set("Transaction_Allowed." + type.name(), isAllowed);
        });
        cfg.setItem("Icon", this.getIcon());
        cfg.setIntArray("Citizens.Attached_NPC", this.getCitizensIds());

        int c = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        cfg.set("Discounts.Custom", null);
        for (IShopDiscount discount : this.getDiscounts()) {
            String path = "Discounts.Custom." + (c++) + ".";
            cfg.set(path + "Discount", discount.getDiscountRaw());
            cfg.set(path + "Times.Days", discount.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            cfg.set(path + "Times.Times", discount.getTimes().stream()
                .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                .collect(Collectors.toList()));
        }


        configProducts.set("List", null);
        for (IProductVirtual shopProduct : this.getProducts()) {
            String path = "List." + shopProduct.getId() + ".";

            configProducts.setItemEncoded(path + "Shop_View.Preview", shopProduct.getPreview());
            configProducts.set(path + "Shop_View.Slot", shopProduct.getSlot());
            configProducts.set(path + "Shop_View.Page", shopProduct.getPage());
            configProducts.set(path + "Discount.Allowed", shopProduct.isDiscountAllowed());

            for (TradeType tradeType : TradeType.values()) {
                configProducts.set(path + "Limit." + tradeType.name() + ".Amount", shopProduct.getLimitAmount(tradeType));
                configProducts.set(path + "Limit." + tradeType.name() + ".Cooldown", shopProduct.getLimitCooldown(tradeType));
            }

            configProducts.set(path + "Purchase.Currency", shopProduct.getCurrency().getId());
            configProducts.set(path + "Purchase.Item_Meta_Enabled", shopProduct.isItemMetaEnabled());
            IProductPricer pricer = shopProduct.getPricer();
            for (TradeType buyType : TradeType.values()) {
                String path2 = path + "Purchase." + buyType.name() + ".";
                configProducts.set(path2 + "Price_Min", pricer.getPriceMin(buyType));
                configProducts.set(path2 + "Price_Max", pricer.getPriceMax(buyType));
            }

            String path3 = path + "Purchase.Randomizer.";
            configProducts.set(path3 + "Enabled", pricer.isRandomizerEnabled());
            configProducts.set(path3 + "Times.Days", pricer.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            configProducts.set(path3 + "Times.Times", pricer.getTimes().stream()
                .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                .collect(Collectors.toList()));

            configProducts.setItemEncoded(path + "Reward.Item", shopProduct.getItem());
            configProducts.set(path + "Reward.Commands", shopProduct.getCommands());
        }
        configProducts.saveChanges();
    }

    /*@Override
    public double getShopBalance(@NotNull IShopCurrency currency) {
        return -1D;
    }

    @Override
    public void takeFromShopBalance(@NotNull IShopCurrency currency, double amount) {

    }

    @Override
    public void addToShopBalance(@NotNull IShopCurrency currency, double amount) {

    }*/

    @NotNull
    public JYML getConfigProducts() {
        return this.configProducts;
    }

    @NotNull
    public JYML getConfigView() {
        return this.configView;
    }

    @Override
    @NotNull
    public EditorShopMain getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopMain(this.plugin(), this);
        }
        return this.editor;
    }

    @Override
    public void setupView() {
        this.configView.reload();
        this.view = new ShopVirtualView(this, this.getConfigView());
        // this.getEditor().rebuild();
        this.getEditor().getEditorViewDesign().setTitle(this.getView().getTitle());
        this.getEditor().getEditorViewDesign().setSize(this.getView().getSize());

        this.getEditor().getEditorProducts().setTitle(this.getView().getTitle());
        this.getEditor().getEditorProducts().setSize(this.getView().getSize());
    }

    @Override
    @NotNull
    public AbstractShopView<IShopVirtual> getView() {
        return this.view;
    }

    @Override
    @Deprecated
    public void open(@NotNull Player player, int page) {
        if (!this.hasPermission(player)) {
            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return;
        }

        VirtualShop virtualShop = plugin.getVirtualShop();
        if (virtualShop != null && !virtualShop.isShopAllowed(player)) {
            return;
        }
        this.getView().open(player, page);
    }

    @NotNull
    @Override
    public BankVirtual getBank() {
        return this.bank;
    }

    @Override
    public boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;
        return player.hasPermission(Perms.VIRTUAL_SHOP + this.getId());
    }

    @Override
    public boolean isPermissionRequired() {
        return this.isPermissionRequired;
    }

    @Override
    public void setPermissionRequired(boolean isPermissionRequired) {
        this.isPermissionRequired = isPermissionRequired;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return new ItemStack(this.icon);
    }

    @Override
    public void setIcon(@NotNull ItemStack icon) {
        this.icon = new ItemStack(icon);
        this.icon.setAmount(1);
    }

    @Override
    public int getPages() {
        return this.pages;
    }

    @Override
    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
    }

    @Override
    public int[] getCitizensIds() {
        return this.citizensIds;
    }

    @Override
    public void setCitizensIds(int[] npcIds) {
        this.citizensIds = npcIds;
    }
}
