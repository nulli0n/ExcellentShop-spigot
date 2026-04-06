package su.nightexpress.excellentshop.feature.playershop.display;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.display.DisplaySettings;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrapper;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.List;

public class PlayerShopDisplaySettings extends AbstractConfig implements DisplaySettings {

    private final ConfigProperty<Integer> updateInterval = this.addProperty(ConfigTypes.INT, "Display.Update_Interval",
        1,
        "Sets how often (in seconds) shop displays will render for players."
    );

    private final ConfigProperty<Integer> itemChangeInterval = this.addProperty(ConfigTypes.INT, "Display.Item_Change_Interval",
        5,
        "Sets how often (in seconds) item displayed in the showcase will change.",
        "[*] Must be divisible by the Update_Interval value."
    );

    private final ConfigProperty<Integer> visibleDistance = this.addProperty(ConfigTypes.INT, "Display.Visible_Distance",
        10,
        "Sets shop display visibility distance."
    );

    private final ConfigProperty<Boolean> hologramEnabled = this.addProperty(ConfigTypes.BOOLEAN, "Display.Hologram.Enabled",
        true,
        "Controls whether shops will have client-side holograms displaying its name and prices."
    );

    private final ConfigProperty<Double> hologramScale = this.addProperty(ConfigTypes.DOUBLE, "Display.Hologram.Scale",
        0.8,
        "Sets hologram text scale.",
        "[Default is 0.8]"
    );

    private final ConfigProperty<Integer> hologramLineWidth = this.addProperty(ConfigTypes.INT, "Display.Hologram.LineWidth",
        200,
        "Maximum line width used to split lines.",
        "[Default is 200]"
    );

    private final ConfigProperty<Integer> hologramTextOpacity = this.addProperty(ConfigTypes.INT, "Display.Hologram.TextOpacity",
        -1,
        "Alpha value of rendered text. Value ranges from 0 to 255. Values up to 3 are treated as fully opaque (255).",
        "The text rendering is discarded for values between 4 and 26. Defaults to -1, which represents 255 and is completely opaque.",
        "[Default is -1]"
    );

    private final ConfigProperty<Boolean> hologramSeeThrough = this.addProperty(ConfigTypes.BOOLEAN, "Display.Hologram.SeeThrough",
        false,
        "Whether the text be visible through blocks.",
        "[Default is false]"
    );

    private final ConfigProperty<Boolean> hologramShadow = this.addProperty(ConfigTypes.BOOLEAN, "Display.Hologram.Shadow",
        true,
        "Whether the text is displayed with shadow.",
        "[Default is true]"
    );

    private final ConfigProperty<int[]> hologramBackgroundColor = this.addProperty(ConfigTypes.INT_ARRAY, "Display.Hologram.BackgroundColor",
        new int[]{64, 0, 0, 0},
        "The background color, arranged by [A,R,G,B]. Where: A = Alpha (opacity), R = Red, G = Green, B = Blue.",
        "[Default is 64,0,0,0]"
    );

    private final ConfigProperty<List<String>> hologramAdminShop = this.addProperty(ConfigTypes.STRING_LIST, "Display.Hologram.Content.AdminShop",
        getDefaultShopHologram(),
        "Hologram text for Admin Shops."
    );

    private final ConfigProperty<List<String>> hologramPlayerShop = this.addProperty(ConfigTypes.STRING_LIST, "Display.Hologram.Content.PlayerShop",
        getDefaultShopHologram(),
        "Hologram text for Player Shops."
    );

    private final ConfigProperty<List<String>> hologramRentable = this.addProperty(ConfigTypes.STRING_LIST, "Display.Hologram.Content.Rent",
        Lists.newList(
            TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("FOR RENT"),
            TagWrappers.GRAY.wrap(ShopPlaceholders.CHEST_SHOP_RENT_DURATION),
            TagWrappers.GRAY.wrap(ShopPlaceholders.CHEST_SHOP_RENT_PRICE)
        ),
        "Hologram text for shops set for Rent."
    );

    private final ConfigProperty<List<String>> hologramAbsent = this.addProperty(ConfigTypes.STRING_LIST, "Display.Hologram.Content.Empty",
        Lists.newList(
            TagWrappers.RED.wrap("< Not Configured >")
        ),
        "Hologram text for shops without products added."
    );

    @NonNull
    private static List<String> getDefaultShopHologram() {
        List<String> list = new ArrayList<>();

        TagWrapper gray = TagWrappers.GRAY;
        TagWrapper white = TagWrappers.WHITE;

        String buyText = TagWrappers.GREEN.wrap("B: " + TagWrappers.SOFT_GREEN.wrap("{buy_price}"));
        String sellText = TagWrappers.RED.wrap("S: " + TagWrappers.SOFT_RED.wrap("{sell_price}"));
        String stockText = gray.wrap("Stock: ") + white.wrap(ShopPlaceholders.PRODUCT_STOCK) + " " + gray.wrap("(" + white.wrap("+" + ShopPlaceholders.PRODUCT_SPACE));

        list.add(ShopPlaceholders.SHOP_NAME);
        list.add("<if_admin_shop>" + TagWrappers.RED.wrap("(Admin Shop)") + "</if_admin_shop>");
        list.add(gray.wrap(ShopPlaceholders.PRODUCT_PREVIEW_NAME));
        list.add("<if_buyable>" + buyText + "</if_buyable><if_sellable> " + sellText + "</if_sellable>");
        list.add("<if_has_stock>" + stockText + "</if_has_stock>");

        return list;
    }

    @Override
    public boolean isHologramEnabled() {
        return this.hologramEnabled.get();
    }

    @Override
    public boolean isHologramShadow() {
        return this.hologramShadow.get();
    }

    @Override
    public boolean isHologramSeeThrough() {
        return this.hologramSeeThrough.get();
    }

    @Override
    public double getHologramScale() {
        return this.hologramScale.get();
    }

    @Override
    public int getHologramLineWidth() {
        return this.hologramLineWidth.get();
    }

    @Override
    public int getHologramTextOpacity() {
        return this.hologramTextOpacity.get();
    }

    @Override
    public int[] getHologramBackgroundColor() {
        return this.hologramBackgroundColor.get();
    }

    @Override
    public double getVisibleDistance() {
        return this.visibleDistance.get();
    }

    @Override
    public int getUpdateInterval() {
        return this.updateInterval.get();
    }

    @Override
    public int getItemChangeInterval() {
        return this.itemChangeInterval.get();
    }

    @Override
    @NonNull
    public List<String> getAdminShopHologram() {
        return List.copyOf(this.hologramAdminShop.get());
    }

    @Override
    @NonNull
    public List<String> getPlayerShopHologram() {
        return List.copyOf(this.hologramPlayerShop.get());
    }

    @Override
    @NonNull
    public List<String> getRentableShopHologram() {
        return List.copyOf(this.hologramRentable.get());
    }

    @Override
    @NonNull
    public List<String> getUnconfiguredShopHologram() {
        return List.copyOf(this.hologramAbsent.get());
    }
}
