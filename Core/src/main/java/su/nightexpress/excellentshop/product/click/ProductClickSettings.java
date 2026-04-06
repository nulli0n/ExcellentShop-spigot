package su.nightexpress.excellentshop.product.click;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.util.Enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProductClickSettings implements Writeable {

    public static final ConfigType<ProductClickSettings> CONFIG_TYPE = ConfigType.of(ProductClickSettings::read, FileConfig::set);

    private final Map<TradeStatus, Map<ClickType, ProductClickAction>> keyMappings;

    public ProductClickSettings(@NonNull Map<TradeStatus, Map<ClickType, ProductClickAction>> keyMappings) {
        this.keyMappings = keyMappings;
    }

    @NonNull
    public static ProductClickSettings read(@NonNull FileConfig config, @NonNull String path) {
        Map<TradeStatus, Map<ClickType, ProductClickAction>> keyMappings = new HashMap<>();

        String mappingsPath = path + ".Key-Mappings";
        config.getSection(mappingsPath).forEach(statusName -> {
            TradeStatus status = Enums.get(statusName, TradeStatus.class);
            if (status == null) return;

            String statusPath = mappingsPath + "." + statusName;
            Map<ClickType, ProductClickAction> clicksMap = new HashMap<>();

            config.getSection(statusPath).forEach(clickName -> {
                ClickType clickType = Enums.get(clickName, ClickType.class);
                if (clickType == null) return;

                ProductClickAction action = config.getEnum(statusPath + "." + clickName, ProductClickAction.class, ProductClickAction.NONE);
                clicksMap.put(clickType, action);
            });

            keyMappings.put(status, clicksMap);
        });

        return new ProductClickSettings(keyMappings);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.remove(path + ".Key-Mappings");

        this.keyMappings.forEach((status, clicksMap) -> {
            clicksMap.forEach((clickType, action) -> {
                config.set(path + ".Key-Mappings." + status.name() + "." + clickType.name(), action.name());
            });
        });
    }

    @NonNull
    public ProductClickAction getClickAction(@NonNull TradeStatus status, @NonNull ClickType clickType) {
        return this.keyMappings.getOrDefault(status, Collections.emptyMap()).getOrDefault(clickType, ProductClickAction.NONE);
    }
}
