package su.nightexpress.nexshop.shop.menu;

import org.jetbrains.annotations.NotNull;

public record Breadcumb<T>(@NotNull T source, int page) {

}
