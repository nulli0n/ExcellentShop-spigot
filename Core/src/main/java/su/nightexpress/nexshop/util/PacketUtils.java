package su.nightexpress.nexshop.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.packet.PacketLibrary;

import java.util.Optional;

public class PacketUtils {

    private static PacketLibrary library;

    public static void register(@NonNull PacketLibrary library) {
        PacketUtils.library = library;
       // PacketUtils.library.setup();
    }

    public static void clear() {
        if (library != null) {
            //library.shutdown();
            library = null;
        }
    }

    @NonNull
    public static Optional<PacketLibrary> library() {
        return Optional.ofNullable(library);
    }

    @Nullable
    public static PacketLibrary getLibrary() {
        return library;
    }
}
