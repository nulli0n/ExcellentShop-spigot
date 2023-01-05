package su.nightexpress.nexshop.shop.chest.editor;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;

import java.util.ArrayList;
import java.util.List;

public enum ChestEditorType implements EditorButtonType {

    PRODUCT_OBJECT(Material.EMERALD, "%product_preview_name%",
        EditorButtonType.click("Left to &fEdit\nShift-Right to &fRemove &7(No Undo)")),
    PRODUCT_CHANGE_CURRENCY(Material.EMERALD, "Product Currency",
        EditorButtonType.current(Placeholders.PRODUCT_CURRENCY),
        EditorButtonType.info("Sets the product currency."),
        EditorButtonType.click("Left-Click to &fEdit")),
    PRODUCT_CHANGE_PRICE(Material.NAME_TAG, "Price Manager",
        EditorButtonType.current("Buy: &f" + Placeholders.PRODUCT_PRICE_BUY + "\nSell: &f" + Placeholders.PRODUCT_PRICE_SELL),
        EditorButtonType.info("Here you can change product price type and set prices."),
        EditorButtonType.click("Left-Click to &fNavigate\n[Q] Key to &fRefresh")),
    ;

    private final Material material;
    private           String   name;
    private       List<String> lore;

    ChestEditorType() {
        this(Material.AIR, "", "");
    }

    ChestEditorType(@NotNull Material material, @NotNull String name, @NotNull String... lores) {
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
