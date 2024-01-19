package su.nightexpress.nexshop.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public class PlaceholderHook {

    private static Expansion expansion;

    public static void setup(@NotNull ExcellentShop plugin) {
        if (expansion == null) {
            expansion = new Expansion(plugin);
            expansion.register();
        }
    }

    public static void shutdown() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    private static class Expansion extends PlaceholderExpansion {

        private final ExcellentShop plugin;

        public Expansion(@NotNull ExcellentShop plugin) {
            this.plugin = plugin;
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return plugin.getName().toLowerCase();
        }

        @Override
        @NotNull
        public String getAuthor() {
            return plugin.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (params.startsWith("auction_")) {
                AuctionManager module = this.plugin.getAuction();
                if (module == null) return null;

                String subParams = params.substring("auction_".length());

                if (subParams.equalsIgnoreCase("max_listings")) {
                    return NumberUtil.format(module.getListingsMaximum(player));
                }
            }
            else if (params.startsWith("chestshop_")) {
                ChestShopModule module = this.plugin.getChestShop();
                if (module == null) return null;

                String subParams = params.substring("chestshop_".length());
                if (subParams.equalsIgnoreCase("max_shops")) {
                    return NumberUtil.format(ChestUtils.getShopLimit(player));
                }
                if (subParams.equalsIgnoreCase("products_per_shop")) {
                    return NumberUtil.format(ChestUtils.getProductLimit(player));
                }
            }
            else if (params.startsWith("virtualshop_")) {
                VirtualShopModule module = this.plugin.getVirtualShop();
                if (module == null) return null;

                String subParams = params.substring("virtualshop_".length());
                if (subParams.equalsIgnoreCase("sell_multiplier")) {
                    return NumberUtil.format(VirtualShopModule.getSellMultiplier(player));
                }
            }

            return super.onPlaceholderRequest(player, params);
        }
    }
}
