package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.Placeholders;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public enum VirtualEditorType implements EditorButtonType {

    DISCOUNT_OBJECT(Material.GOLD_NUGGET, "Shop Discount",
        EditorButtonType.info("Discount: &f" + Placeholders.DISCOUNT_CONFIG_AMOUNT + "%\nDays: \n" + Placeholders.DISCOUNT_CONFIG_DAYS + "\nTimes: \n" + Placeholders.DISCOUNT_CONFIG_TIMES),
        EditorButtonType.click("Left-Click to &fEdit\nShift-Right to &fDelete &7(No Undo)")),
    DISCOUNT_CREATE(Material.ANVIL, "Create Discount",
        EditorButtonType.info("Create a new discount config for the shop."),
        EditorButtonType.click("Left-Click to &fCreate")),
    DISCOUNT_CHANGE_DISCOUNT(Material.GOLD_NUGGET, "Discount Amount",
        EditorButtonType.current(Placeholders.DISCOUNT_CONFIG_AMOUNT + "%"),
        EditorButtonType.info("Sets the discount amount (in percent)."),
        EditorButtonType.click("Left-Click to &fEdit")),
    DISCOUNT_CHANGE_DURATION(Material.REPEATER, "Duration",
        EditorButtonType.current(Placeholders.DISCOUNT_CONFIG_DURATION),
        EditorButtonType.info("For how long this discount will have effect?"),
        EditorButtonType.click("Left-Click to &fEdit")),
    DISCOUNT_CHANGE_DAY(Material.DAYLIGHT_DETECTOR, "Activation Days",
        EditorButtonType.current(Placeholders.DISCOUNT_CONFIG_DAYS),
        EditorButtonType.info("A list of days, when this discount will be activated."),
        EditorButtonType.warn("You have to set at least ONE day and time for the discount to work!"),
        EditorButtonType.click("Left-Click to &fAdd Day\nRight-Click to &fClear")),
    DISCOUNT_CHANGE_TIME(Material.CLOCK, "Activation Times",
        EditorButtonType.current(Placeholders.DISCOUNT_CONFIG_TIMES),
        EditorButtonType.info("A list of times, when this discount will be activated."),
        EditorButtonType.warn("You have to set at least ONE day and time for the discount to work!"),
        EditorButtonType.click("Left-Click to &fAdd Time\nRight-Click to &fClear")),
    ;

    private final Material     material;
    private       String       name;
    private       List<String> lore;

    VirtualEditorType() {
        this(Material.AIR, "", "");
    }

    VirtualEditorType(@NotNull Material material, @NotNull String name, @NotNull String... lores) {
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
        this.name = Colorizer.apply(name);
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    public void setLore(@NotNull List<String> lore) {
        this.lore = Colorizer.apply(new ArrayList<>(lore));
    }
}
