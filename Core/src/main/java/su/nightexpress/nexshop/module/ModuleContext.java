package su.nightexpress.nexshop.module;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.nexshop.user.UserManager;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogRegistry;

import java.nio.file.Path;

public record ModuleContext(
    @NonNull ShopPlugin plugin,
    @NonNull DataHandler dataHandler,
    @NonNull DataManager dataManager,
    @NonNull UserManager userManager,
    @NonNull DialogRegistry dialogRegistry,
    @NonNull String id,
    @NonNull Path path,
    @NonNull ModuleDefinition definition
    ) {

}
