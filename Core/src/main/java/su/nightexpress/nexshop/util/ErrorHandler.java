package su.nightexpress.nexshop.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.ShopAPI;
import su.nightexpress.nightcore.config.FileConfig;

@Deprecated
public class ErrorHandler {

    public static void configError(@NotNull String text, @NotNull FileConfig config, @NotNull String path) {
        ShopAPI.getPlugin().error(text + " Found in '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
    }
}
