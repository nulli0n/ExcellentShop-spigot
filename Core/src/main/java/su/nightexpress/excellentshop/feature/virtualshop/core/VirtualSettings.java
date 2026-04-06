package su.nightexpress.excellentshop.feature.virtualshop.core;

import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.product.click.ProductClickSettings;
import su.nightexpress.nexshop.module.ShopModuleSettings;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrapper;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.*;

public class VirtualSettings extends AbstractConfig implements ShopModuleSettings {

    private static final ConfigType<Map<TradeStatus, List<String>>> TRADE_STATUS_LORE_TYPE = ConfigTypes.forMap(
        str -> Enums.get(str, TradeStatus.class),
        Enum::name,
        ConfigTypes.STRING_LIST
    );

    private final ConfigProperty<Map<TradeStatus, List<String>>> guiProductDisplayMasterInfo = this.addProperty(TRADE_STATUS_LORE_TYPE, "GUI.Product.Display.Master",
        getDefaultProducePriceInfo(),
        ""
    );

    private final ConfigProperty<String> guiProductDisplayComponentPriceTrendUp = this.addProperty(ConfigTypes.STRING, "GUI.Product.Display.Component.price_trend.up",
        TagWrappers.GREEN.wrap("↑ " + ShopPlaceholders.GENERIC_TREND + "%")
    );

    private final ConfigProperty<String> guiProductDisplayComponentPriceTrendDown = this.addProperty(ConfigTypes.STRING, "GUI.Product.Display.Component.price_trend.down",
        TagWrappers.RED.wrap("↓ " + ShopPlaceholders.GENERIC_TREND + "%")
    );

    private final ConfigProperty<ProductClickSettings> guiProductClickSettings = this.addProperty(ProductClickSettings.CONFIG_TYPE, "GUI.Product-Click-Settings",
        getDefaultProductClickSettings(),
        "Controls GUI behavior when clicking shop products."
    );


    private final ConfigProperty<Boolean> guiBuyingCloseAfterPurcahse = this.addProperty(ConfigTypes.BOOLEAN, "GUI.Buying.Close-After-Purchase",
        false,
        "Controls whether Buying Menu should be closed after purchase instead of return back to Shop Menu."
    );

    @NonNull
    private static Map<TradeStatus, List<String>> getDefaultProducePriceInfo() {
        Map<TradeStatus, List<String>> map = new LinkedHashMap<>();

        TagWrapper dgray = TagWrappers.DARK_GRAY;
        TagWrapper gray = TagWrappers.GRAY;
        TagWrapper white = TagWrappers.WHITE;
        TagWrapper orange = TagWrappers.ORANGE;
        TagWrapper blue = TagWrappers.BLUE;

        List<String> buyInfo = Lists.newList(
            TagWrappers.GREEN.wrap("┃ Buy Info:"),
            dgray.wrap(" » ") + gray.wrap("Price:") + " " + white.wrap("{buy_price}"),
            "<if_dynamic_price>" + dgray.wrap(" » ") + gray.wrap("Trend:") + " " + white.wrap("{buy_price_trend}") + "</if_dynamic_price>"
        );

        List<String> sellInfo = Lists.newList(
            TagWrappers.RED.wrap("┃ Sell Info:"),
            dgray.wrap(" » ") + gray.wrap("Reward:") + " " + white.wrap("{sell_price}"),
            dgray.wrap(" » ") + gray.wrap("Sell All:") + " " + white.wrap("{sell_all_price}"),
            "<if_dynamic_price>" + dgray.wrap(" » ") + gray.wrap("Trend:") + " " + white.wrap("{sell_price_trend}") + "</if_dynamic_price>"
        );

        List<String> stockInfo = Lists.newList(
            "<if_has_stock>" + TagWrappers.SOFT_AQUA.wrap("┃ Stock:") + "</if_has_stock>",
            "<if_has_stock>" + dgray.wrap(" » ") + gray.wrap("Current:") + " " + white.wrap(ShopPlaceholders.PRODUCT_STOCK) + gray.wrap("/") + white.wrap(ShopPlaceholders.PRODUCT_CAPACITY) + "</if_has_stock>",
            "<if_stock_resettable>" + dgray.wrap(" » ") + gray.wrap("Auto-Refill:") + " " + white.wrap("{stock_reset_time}") + "</if_stock_resettable>"
        );

        List<String> limitsInfo = Lists.newList(
            "<if_has_limits>" + TagWrappers.ORANGE.wrap("┃ Limits:") + "</if_has_limits>",
            "<if_has_buy_limit>" + dgray.wrap(" » ") + gray.wrap("Buy Limit:") + " " + orange.wrap("{buy_limit_current}" + gray.wrap("/") + ShopPlaceholders.PRODUCT_BUY_LIMIT) + "</if_has_buy_limit>",
            "<if_has_sell_limit>" + dgray.wrap(" » ") + gray.wrap("Sell Limit:") + " " + orange.wrap("{sell_limit_current}" + gray.wrap("/") + ShopPlaceholders.PRODUCT_SELL_LIMIT) + "</if_has_sell_limit>",
            "<if_limit_resettable>" + dgray.wrap(" » ") + gray.wrap("Auto-Reset:") + " " + white.wrap("{limit_reset_time}") + "</if_limit_resettable>"
        );

        List<String> none = new ArrayList<>();

        List<String> buyableOnly = new ArrayList<>();
        buyableOnly.add(ShopPlaceholders.PRODUCT_PREVIEW_LORE);
        buyableOnly.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        buyableOnly.addAll(buyInfo);
        buyableOnly.add(ShopPlaceholders.EMPTY_IF_BELOW);
        buyableOnly.addAll(stockInfo);
        buyableOnly.add(ShopPlaceholders.EMPTY_IF_BELOW);
        buyableOnly.addAll(limitsInfo);
        buyableOnly.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        buyableOnly.add(blue.wrap("➥ Actions:"));
        buyableOnly.add(dgray.wrap(" » ") + blue.wrap("Left-Click") + " " + gray.wrap("→") + " " + white.wrap("Open Buy Menu"));
        buyableOnly.add(dgray.wrap(" » ") + blue.wrap("Right-Click") + " " + gray.wrap("→") + " " + white.wrap("Buy x 1"));
        buyableOnly.add(dgray.wrap(" » ") + blue.wrap("Key " + TagWrappers.KEY.apply("key.hotbar.1") + "-" + TagWrappers.KEY.apply("key.hotbar.9")) + " " + gray.wrap("→") + " " + white.wrap("Quick Buy"));

        List<String> sellableOnly = new ArrayList<>();
        sellableOnly.add(ShopPlaceholders.PRODUCT_PREVIEW_LORE);
        sellableOnly.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        sellableOnly.addAll(sellInfo);
        sellableOnly.add(ShopPlaceholders.EMPTY_IF_BELOW);
        sellableOnly.addAll(stockInfo);
        sellableOnly.add(ShopPlaceholders.EMPTY_IF_BELOW);
        sellableOnly.addAll(limitsInfo);
        sellableOnly.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        sellableOnly.add(blue.wrap("➥ Actions:"));
        sellableOnly.add(dgray.wrap(" » ") + blue.wrap("Left-Click") + " " + gray.wrap("→") + " " + white.wrap("Open Sell Menu"));
        sellableOnly.add(dgray.wrap(" » ") + blue.wrap("Right-Click") + " " + gray.wrap("→") + " " + white.wrap("Sell x 1"));
        sellableOnly.add(dgray.wrap(" » ") + blue.wrap("Key " + TagWrappers.KEY.apply("key.hotbar.1") + "-" + TagWrappers.KEY.apply("key.hotbar.9")) + " " + gray.wrap("→") + " " + white.wrap("Quick Sell"));
        sellableOnly.add(dgray.wrap(" » ") + blue.wrap("Control + " + TagWrappers.KEY.apply("key.drop")) + " " + gray.wrap("→") + " " + white.wrap("Sell All"));

        List<String> both = new ArrayList<>();
        both.add(ShopPlaceholders.PRODUCT_PREVIEW_LORE);
        both.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        both.addAll(buyInfo);
        both.add(ShopPlaceholders.EMPTY_IF_BELOW);
        both.addAll(sellInfo);
        both.add(ShopPlaceholders.EMPTY_IF_BELOW);
        both.addAll(stockInfo);
        both.add(ShopPlaceholders.EMPTY_IF_BELOW);
        both.addAll(limitsInfo);
        both.add(ShopPlaceholders.EMPTY_IF_ABOVE);
        both.add(blue.wrap("➥ Actions:"));
        both.add(dgray.wrap(" » ") + blue.wrap("Left-Click") + " " + gray.wrap("→") + " " + white.wrap("Open Buy Menu"));
        both.add(dgray.wrap(" » ") + blue.wrap("Right-Click") + " " + gray.wrap("→") + " " + white.wrap("Open Sell Menu"));
        both.add(dgray.wrap(" » ") + blue.wrap(TagWrappers.KEY.apply("key.sneak") + " + LMB") + " " + gray.wrap("→") + " " + white.wrap("Buy x 1"));
        both.add(dgray.wrap(" » ") + blue.wrap(TagWrappers.KEY.apply("key.sneak") + " + RMB") + " " + gray.wrap("→") + " " + white.wrap("Sell x 1"));
        both.add(dgray.wrap(" » ") + blue.wrap("Control + " + TagWrappers.KEY.apply("key.drop")) + " " + gray.wrap("→") + " " + white.wrap("Sell All"));


        none.add(TagWrappers.RED.wrap("[ Item Unconfigured ]"));

        map.put(TradeStatus.BUYABLE, buyableOnly);
        map.put(TradeStatus.SELLABLE, sellableOnly);
        map.put(TradeStatus.BUYABLE_AND_SELLABLE, both);
        map.put(TradeStatus.UNAVAILABLE, none);

        return map;
    }

    @NonNull
    private static ProductClickSettings getDefaultProductClickSettings() {
        Map<TradeStatus, Map<ClickType, ProductClickAction>> keyMappings = new LinkedHashMap<>();

        keyMappings.put(TradeStatus.BUYABLE_AND_SELLABLE, Map.of(
            ClickType.LEFT, ProductClickAction.OPEN_BUY_MENU,
            ClickType.RIGHT, ProductClickAction.OPEN_SELL_MENU,
            ClickType.SHIFT_LEFT, ProductClickAction.BUY_ONE,
            ClickType.SHIFT_RIGHT, ProductClickAction.SELL_ONE,
            ClickType.CONTROL_DROP, ProductClickAction.SELL_ALL
        ));

        keyMappings.put(TradeStatus.BUYABLE, Map.of(
            ClickType.LEFT, ProductClickAction.OPEN_BUY_MENU,
            ClickType.RIGHT, ProductClickAction.BUY_ONE,
            ClickType.NUMBER_KEY, ProductClickAction.BUY_ONE
        ));

        keyMappings.put(TradeStatus.SELLABLE, Map.of(
            ClickType.LEFT, ProductClickAction.OPEN_SELL_MENU,
            ClickType.RIGHT, ProductClickAction.SELL_ONE,
            ClickType.NUMBER_KEY, ProductClickAction.SELL_ONE,
            ClickType.CONTROL_DROP, ProductClickAction.SELL_ALL
        ));

        return new ProductClickSettings(keyMappings);
    }

    @NonNull
    public String getProductDisplayComponentPriceTrendUp() {
        return this.guiProductDisplayComponentPriceTrendUp.get();
    }

    @NonNull
    public String getProductDisplayComponentPriceTrendDown() {
        return this.guiProductDisplayComponentPriceTrendDown.get();
    }

    @Override
    @NonNull
    public List<String> getProductDisplayMasterInfo(@NonNull TradeStatus status) {
        return this.guiProductDisplayMasterInfo.get().getOrDefault(status, Collections.emptyList());
    }

    @Override
    @NonNull
    public ProductClickSettings getProductClickSettings() {
        return this.guiProductClickSettings.get();
    }

    @Override
    public boolean isBuyingMenuCloseAfterPurchase() {
        return this.guiBuyingCloseAfterPurcahse.get();
    }
}
