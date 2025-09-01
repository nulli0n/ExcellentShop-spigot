package su.nightexpress.nexshop.dialog;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface DialogProvider<T> {

    void show(@NotNull Player player, @NotNull T source);

    void close(@NotNull Player player);

    void closeAndThen(@NotNull Player player, T source, @NotNull BiConsumer<Player, T> consumer);

    void closeAndThen(@NotNull Player player, T source, @NotNull Consumer<Player> consumer);

    void closeAndThen(@NotNull Player player, T source, @NotNull Runnable runnable);
}
