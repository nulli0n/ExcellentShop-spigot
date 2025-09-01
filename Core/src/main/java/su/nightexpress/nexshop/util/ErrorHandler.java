package su.nightexpress.nexshop.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nightcore.config.FileConfig;

public class ErrorHandler {

    // TODO Plugin instance

    public static void configError(@NotNull String text, @NotNull FileConfig config, @NotNull String path) {
        ShopAPI.getPlugin().error(text + " Found in '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
    }
}
