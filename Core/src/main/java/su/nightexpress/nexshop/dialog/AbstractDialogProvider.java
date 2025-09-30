package su.nightexpress.nexshop.dialog;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.util.Players;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractDialogProvider<T> implements DialogProvider<T>, LangContainer {

    protected static final String ACTION_APPLY = "accept";
    protected static final String ACTION_RESET = "reset";
    protected static final String ACTION_BACK  = "exit";

    protected final ShopPlugin plugin;

    public AbstractDialogProvider(@NotNull ShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void showNextTick(@NotNull Player user, @NotNull T source) {
        this.plugin.runTask(task -> this.show(user, source));
    }

    @Override
    public void close(@NotNull Player user) {
        Players.closeDialog(user);
    }

    @Override
    public void closeAndThen(@NotNull Player player, T source, @NotNull BiConsumer<Player, T> consumer) {
        this.close(player);
        consumer.accept(player, source);
    }

    @Override
    public void closeAndThen(@NotNull Player player, T source, @NotNull Consumer<Player> consumer) {
        this.close(player);
        consumer.accept(player);
    }

    @Override
    public void closeAndThen(@NotNull Player player, T source, @NotNull Runnable runnable) {
        this.close(player);
        runnable.run();
    }
}
