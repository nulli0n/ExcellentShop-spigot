package su.nightexpress.nexshop.currency;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class CurrencyId {

    public static final String EXP           = "exp";
    public static final String VAULT         = "vault";
    public static final String PLAYER_POINTS = "player_points";
    public static final String GAME_POINTS   = "game_points";
    public static final String GEMS_ECONOMY  = "gemseconomy";

    @NotNull
    public static String[] values() {
        return new String[]{EXP, VAULT, PLAYER_POINTS, GAME_POINTS, GEMS_ECONOMY};
    }

    @NotNull
    public static Stream<String> stream() {
        return Arrays.stream(values());
    }

}
