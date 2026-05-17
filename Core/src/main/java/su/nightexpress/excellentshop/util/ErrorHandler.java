package su.nightexpress.excellentshop.util;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopAPI;
import su.nightexpress.nightcore.config.FileConfig;

@Deprecated
public class ErrorHandler {

    public static void configError(@NonNull String text, @NonNull FileConfig config, @NonNull String path) {
        ShopAPI.getPlugin().error(text + " Found in '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
    }
}
