package su.nightexpress.excellentshop.api.packet.display;

import org.jspecify.annotations.NonNull;

import java.util.List;

public interface DisplaySettings {

    boolean isHologramEnabled();

    boolean isHologramShadow();

    boolean isHologramSeeThrough();

    double getHologramScale();

    int getHologramLineWidth();

    int getHologramTextOpacity();

    int[] getHologramBackgroundColor();

    double getVisibleDistance();

    int getUpdateInterval();

    int getItemChangeInterval();

    @NonNull List<String> getAdminShopHologram();

    @NonNull List<String> getPlayerShopHologram();

    @NonNull List<String> getRentableShopHologram();

    @NonNull List<String> getUnconfiguredShopHologram();
}
