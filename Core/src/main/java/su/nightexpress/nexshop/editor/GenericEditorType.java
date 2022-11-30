package su.nightexpress.nexshop.editor;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;

import java.util.ArrayList;
import java.util.List;

public enum GenericEditorType implements EditorButtonType {

    PRODUCT_FREE_SLOT(Material.GREEN_STAINED_GLASS_PANE, "FREE SLOT",
        EditorButtonType.info("Drop item on me to add it as a product!")),
    PRODUCT_RESERVED_SLOT(Material.BARRIER, "RESERVED SLOT",
        EditorButtonType.info("This slot is already occuppied by a shop product.")),
    PRODUCT_CHANGE_PRICE_TYPE(Material.COMPARATOR, "Price Type",
        EditorButtonType.current(Placeholders.PRODUCT_PRICE_TYPE),
        EditorButtonType.info("Sets the product price type. Depends on a price type, this menu will have different settings."),
        EditorButtonType.warn("When changing price type, previous settings will be WIPED OUT!"),
        EditorButtonType.click("Left-Click to &fToggle")),
    PRODUCT_CHANGE_PRICE_FLAT_BUY(Material.LIME_DYE, "Buy Price",
        EditorButtonType.current(Placeholders.PRODUCT_PRICE_BUY),
        EditorButtonType.info("Sets the product buy price."),
        EditorButtonType.note("When price is disabled (negative value), players will be unable to buy it."),
        EditorButtonType.click("Left-Click to &fEdit\nRight-Click to &fDisable")),
    PRODUCT_CHANGE_PRICE_FLAT_SELL(Material.PINK_DYE, "Sell Price",
        EditorButtonType.current(Placeholders.PRODUCT_PRICE_SELL),
        EditorButtonType.info("Sets the product sell price."),
        EditorButtonType.note("When price is disabled (negative value), players will be unable to sell it."),
        EditorButtonType.click("Left-Click to &fEdit\nRight-Click to &fDisable")),
    PRODUCT_CHANGE_PRICE_FLOAT_BUY(Material.LIME_DYE, "Buy Price",
        EditorButtonType.current("Min: &f" + Placeholders.PRODUCT_PRICER_BUY_MIN + "\nMax: &f" + Placeholders.PRODUCT_PRICER_BUY_MAX),
        EditorButtonType.info("Sets the product float buy price. The final buy price will be generated in a range of min. and max. values."),
        EditorButtonType.note("When price is disabled (negative value), players will be unable to buy it."),
        EditorButtonType.click("Left-Click to &fEdit Min\nRight-Click to &fEdit Max")),
    PRODUCT_CHANGE_PRICE_FLOAT_SELL(Material.PINK_DYE, "Sell Price",
        EditorButtonType.current("Min: &f" + Placeholders.PRODUCT_PRICER_SELL_MIN + "\nMax: &f" + Placeholders.PRODUCT_PRICER_SELL_MAX),
        EditorButtonType.info("Sets the product float sell price. The final sell price will be generated in a range of min. and max. values."),
        EditorButtonType.note("When price is disabled (negative value), players will be unable to sell it."),
        EditorButtonType.click("Left-Click to &fEdit Min\nRight-Click to &fEdit Max")),
    PRODUCT_CHANGE_PRICE_FLOAT_REFRESH(Material.CLOCK, "Refresh Settings",
        EditorButtonType.current("Days: &f" + Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_DAYS + "\nTimes: &f\n" + Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_TIMES),
        EditorButtonType.info("Sets conditions, when the product price will be refreshed. Until that, the previous generated price will be used."),
        EditorButtonType.warn("You have to provide at least ONE day and time for the float price feature to work."),
        EditorButtonType.click("Left-Click to &fAdd Day\nShift-Left to &fClear Days\nRight-Click to &fAdd Time\nShift-Right to &fClear Times")),
    PRODUCT_CHANGE_PRICE_FLOAT_BUY_MIN,
    PRODUCT_CHANGE_PRICE_FLOAT_BUY_MAX,
    PRODUCT_CHANGE_PRICE_FLOAT_SELL_MIN,
    PRODUCT_CHANGE_PRICE_FLOAT_SELL_MAX,
    PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_DAYS,
    PRODUCT_CHANGE_PRICE_FLOAT_REFRESH_TIMES,
    PRODUCT_CHANGE_PRICE_DYNAMIC_BUY(Material.LIME_DYE, "Buy Price",
        EditorButtonType.current("Min: &f" + Placeholders.PRODUCT_PRICER_BUY_MIN + "\nMax: &f" + Placeholders.PRODUCT_PRICER_BUY_MAX),
        EditorButtonType.info("Sets limit for the product buy price. The final buy price will never exceed the limit you set."),
        EditorButtonType.click("Left-Click to &fEdit Min\nRight-Click to &fEdit Max")),
    PRODUCT_CHANGE_PRICE_DYNAMIC_SELL(Material.PINK_DYE, "Sell Price",
        EditorButtonType.current("Min: &f" + Placeholders.PRODUCT_PRICER_SELL_MIN + "\nMax: &f" + Placeholders.PRODUCT_PRICER_SELL_MAX),
        EditorButtonType.info("Sets limit for the product sell price. The final sell price will never exceed the limit you set."),
        EditorButtonType.click("Left-Click to &fEdit Min\nRight-Click to &fEdit Max")),
    PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL(Material.GRAY_DYE, "Initial Price",
        EditorButtonType.current("Buy: &f" + Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_BUY + "\nSell: &f" + Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_SELL),
        EditorButtonType.info("Sets initial price for the product. These values will be used as default ones, and to calculate further product price."),
        EditorButtonType.click("Left-Click to &fEdit Buy\nRight-Click to &fEdit Sell")),
    PRODUCT_CHANGE_PRICE_DYNAMIC_STEP(Material.REPEATER, "Price Step",
        EditorButtonType.current("Buy: &f" + Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_BUY + "\nSell: &f" + Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_SELL),
        EditorButtonType.info("Sets price step for the product. The buy/sell price will be ajdusted at specified amount when this product is pruchased/sold."),
        EditorButtonType.note("More purchases - Higher prices.\nMore sales - Lower prices."),
        EditorButtonType.click("Left-Click to &fEdit Buy\nRight-Click to &fEdit Sell")),
    PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MIN,
    PRODUCT_CHANGE_PRICE_DYNAMIC_BUY_MAX,
    PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MIN,
    PRODUCT_CHANGE_PRICE_DYNAMIC_SELL_MAX,
    PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_SELL,
    PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL_BUY,
    PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_SELL,
    PRODUCT_CHANGE_PRICE_DYNAMIC_STEP_BUY;

    private final Material     material;
    private       String       name;
    private       List<String> lore;

    GenericEditorType() {
        this(Material.AIR, "", "");
    }

    GenericEditorType(@NotNull Material material, @NotNull String name, @NotNull String... lores) {
        this.material = material;
        this.setName(name);
        this.setLore(EditorButtonType.fineLore(lores));
    }

    @NotNull
    @Override
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = StringUtil.color(name);
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    public void setLore(@NotNull List<String> lore) {
        this.lore = StringUtil.color(new ArrayList<>(lore));
    }
}
