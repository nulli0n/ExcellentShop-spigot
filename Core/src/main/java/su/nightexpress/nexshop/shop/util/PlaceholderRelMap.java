package su.nightexpress.nexshop.shop.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlaceholderRelMap<T> {

    private final List<Pair<String, Function<T, String>>> keys;

    public PlaceholderRelMap() {
        this(new ArrayList<>());
    }

    public PlaceholderRelMap(@NotNull PlaceholderRelMap<T> other) {
        this(other.getKeys());
    }

    public PlaceholderRelMap(@NotNull List<Pair<String, Function<T, String>>> keys) {
        this.keys = new ArrayList<>(keys);
    }

    /*@NotNull
    public static <T> PlaceholderRelMap<T> fusion(@NotNull PlaceholderRelMap<T>... others) {
        PlaceholderRelMap<T> map = new PlaceholderRelMap<>();
        for (PlaceholderRelMap<T> other : others) {
            map.add(other);
        }
        return map;
    }*/

    @NotNull
    public List<Pair<String, Function<T, String>>> getKeys() {
        return keys;
    }

    @NotNull
    public PlaceholderRelMap<T> add(@NotNull PlaceholderMap other) {
        other.getKeys().forEach(pair -> {
            this.add(pair.getFirst(), player -> pair.getSecond().get()); // TODO Bad (?)
        });
        return this;
    }

    @NotNull
    public PlaceholderRelMap<T> add(@NotNull PlaceholderRelMap<T> other) {
        this.keys.addAll(other.getKeys());
        return this;
    }

    @NotNull
    @Deprecated
    public PlaceholderRelMap<T> add(@NotNull String key, @NotNull String replacer) {
        this.add(key, (object) -> replacer);
        return this;
    }

    @NotNull
    public PlaceholderRelMap<T> add(@NotNull String key, @NotNull Function<T, String> replacer) {
        this.getKeys().add(Pair.of(key, replacer));
        return this;
    }

    public void clear() {
        this.getKeys().clear();
    }

    @NotNull
    public PlaceholderMap toNormal(@Nullable T object) {
        List<Pair<String, Supplier<String>>> list = new ArrayList<>();
        this.keys.forEach(pair -> {
            list.add(Pair.of(pair.getFirst(), () -> pair.getSecond().apply(object)));
        });

        return new PlaceholderMap(list);
    }

    /*@NotNull
    public UnaryOperator<String> replacer(@Nullable T object) {
        List<Pair<String, Supplier<String>>> list = new ArrayList<>();
        this.keys.forEach(pair -> {
            list.add(Pair.of(pair.getFirst(), () -> pair.getSecond().apply(object)));
        });

        return str -> StringUtil.replaceEach(str, list);
    }*/
}
