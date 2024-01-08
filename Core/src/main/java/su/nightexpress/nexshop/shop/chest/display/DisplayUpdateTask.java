package su.nightexpress.nexshop.shop.chest.display;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;

public class DisplayUpdateTask extends AbstractTask<ExcellentShop> {

    private final DisplayHandler handler;

    public DisplayUpdateTask(@NotNull ExcellentShop plugin, @NotNull DisplayHandler handler) {
        super(plugin, ChestConfig.DISPLAY_UPDATE_INTERVAL.get(), true);
        this.handler = handler;
    }

    @Override
    public void action() {
        this.handler.update();
    }
}
