package su.nightexpress.nexshop.shop.virtual.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.command.experimental.builder.SimpleFlagBuilder;
import su.nightexpress.nightcore.command.experimental.flag.FlagTypes;

public class CommandFlags {

    public static final String FORCE = "f";

    @NotNull
    public static SimpleFlagBuilder force() {
        return FlagTypes.simple(FORCE);
    }
}
