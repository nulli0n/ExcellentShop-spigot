package su.nightexpress.nexshop.api.shop.packer;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandPacker extends ProductPacker {

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);
}
