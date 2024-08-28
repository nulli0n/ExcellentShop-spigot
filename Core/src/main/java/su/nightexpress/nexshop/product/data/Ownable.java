package su.nightexpress.nexshop.product.data;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Deprecated
public interface Ownable {

    @NotNull UUID getOwnerId();
}
