package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Ownable {

    @NotNull UUID getOwnerId();
}
