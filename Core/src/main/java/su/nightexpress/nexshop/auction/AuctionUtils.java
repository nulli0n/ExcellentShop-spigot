package su.nightexpress.nexshop.auction;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuctionUtils {

    public static void hideListingAttributes(@NotNull ItemStack itemStack) {
        if (AuctionConfig.LISTINGS_HIDE_ATTRIBUTES.get()) {
            ItemUtil.hideAttributes(itemStack);
            ItemUtil.editMeta(itemStack, meta -> meta.removeItemFlags(ItemFlag.values())); // quick fix for enchants hidden
        }
    }

    public static void fillDummy(@NotNull AuctionManager auctionManager) {
        String[] randoms = {"AquaticFlamesIV", "_silent_bunny_", "DefectIV", "Dinara777", "metalblaster99", "poolpony142",
            "OrganicPlasma", "sunfire81", "Cyan_Soul", "InvisibleShadow", "cutejune", "Ladykiller", "SHARkNESS", "HyBlox", "Radivil3",
            "VitorKhr", "Samara", "Katushka", "Dark_Night", "DjR2", "Nuforen", "Noob_Perforator", "LaserDance"};

        Map<UUID, String> owners = new HashMap<>();
        Stream.of(randoms).forEach(name -> owners.put(UUID.randomUUID(), name));

        Set<Material> materials = Stream.of(Material.values())
            .filter(Material::isItem).filter(Predicate.not(Material::isAir))
            .collect(Collectors.toSet());

        for (int i = 0; i < 20; i++) {
            UUID ownerId = Rnd.get(owners.keySet());

            String ownerName = owners.get(ownerId);

            ItemStack item = new ItemStack(Rnd.get(materials));
            item.setAmount(Rnd.get(1, item.getMaxStackSize()));

            if ((ItemUtil.isArmor(item) || ItemUtil.isSword(item) || ItemUtil.isTool(item)) && Rnd.chance(30D)) {
                for (int y = 0; y < Rnd.get(4); y++) {
                    Enchantment enchantment = Rnd.get(BukkitThing.getEnchantments());
                    item.addUnsafeEnchantment(enchantment, Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel()));
                }
            }

            //ItemHandler handler = ProductHandlerRegistry.forBukkitItem();
            //ItemPacker packer = handler.createPacker(item);
            PhysicalTyping typing = ProductTypes.fromItem(item, false);

            Currency currency = Rnd.get(auctionManager.getEnabledCurrencies());
            double price = NumberUtil.round((int) Rnd.getDouble(50, 10_000D));

            LocalDateTime created = LocalDateTime.now().minusDays(Rnd.get(5)).minusHours(Rnd.get(6)).minusMinutes(Rnd.get(30));
            long dateCreation = TimeUtil.toEpochMillis(created);
            long dateExpired = generateExpireDate(dateCreation);

            if (i < 15) {
                long deletionDate = generatePurgeDate(dateExpired);
                ActiveListing listing = new ActiveListing(UUID.randomUUID(), ownerId, ownerName, typing, currency, price, dateCreation, dateExpired, deletionDate);
                auctionManager.getListings().add(listing);
                auctionManager.getDatabase().addListing(listing);
            }
            else {
                LocalDateTime buyed = created.plusDays(Rnd.get(4)).plusHours(Rnd.get(4)).plusMinutes(Rnd.get(15));
                long buyDate = TimeUtil.toEpochMillis(buyed);
                long deletionDate = generatePurgeDate(buyDate);

                CompletedListing listing = new CompletedListing(UUID.randomUUID(), ownerId, ownerName, Rnd.get(randoms), typing, currency, price, dateCreation, buyDate, deletionDate, Rnd.nextBoolean());
                auctionManager.getListings().addCompleted(listing);
                auctionManager.getDatabase().addCompletedListing(listing);
            }
        }
    }

    public static boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    public static boolean isDisabledWorld(@NotNull String name) {
        return AuctionConfig.DISABLED_WORLDS.get().contains(name);
    }

    public static boolean isBadGamemode(@NotNull GameMode gameMode) {
        return AuctionConfig.DISABLED_GAMEMODES.get().contains(gameMode);
    }

    public static long generateExpireDate(long from) {
        return from + TimeUnit.MILLISECONDS.convert(AuctionConfig.LISTINGS_EXPIRE_TIME.get(), TimeUnit.SECONDS);
    }

    public static long generatePurgeDate(long from) {
        return from + TimeUnit.MILLISECONDS.convert(AuctionConfig.LISTINGS_PURGE_TIME.get(), TimeUnit.SECONDS);
    }

    public static double getSellTax(@NotNull Player player) {
        if (player.hasPermission(AuctionPerms.BYPASS_LISTING_TAX)) return 0D;

        return AuctionConfig.LISTINGS_SELL_TAX.get();
    }

    public static double getClaimTax(@NotNull Player player) {
        if (player.hasPermission(AuctionPerms.BYPASS_LISTING_TAX)) return 0D;

        return AuctionConfig.LISTINGS_CLAIM_TAX.get();
    }

    public static double getTax(@NotNull Currency currency, double price, double taxPercent) {
        double tax = price * (taxPercent / 100D);

        if (!currency.canHandleDecimals() || AuctionConfig.LISTINGS_FLOOR_PRICE.get()) {
            tax = Math.ceil(tax);
        }

        return tax;
    }

    public static double finePrice(double price) {
        price = Math.abs(price);

        if (AuctionConfig.LISTINGS_FLOOR_PRICE.get()) {
            return Math.floor(price);
        }

        return NumberUtil.round(price);
    }

    public static int getPossibleListings(@NotNull Player player) {
        return AuctionConfig.LISTINGS_PER_RANK.get().getGreatestOrNegative(player);
    }

    public static double getMaterialPriceMin(@NotNull Material material) {
        return getMaterialPriceRange(material).getMinValue();
    }

    public static double getMaterialPriceMax(@NotNull Material material) {
        return getMaterialPriceRange(material).getMaxValue();
    }

    @NotNull
    private static UniDouble getMaterialPriceRange(@NotNull Material material) {
        return AuctionConfig.LISTINGS_PRICE_PER_MATERIAL.get().getOrDefault(BukkitThing.toString(material), UniDouble.of(-1, -1));
    }

    public static double getCurrencyPriceMin(@NotNull Currency currency) {
        return getCurrencyPriceRange(currency).getMinValue();
    }

    public static double getCurrencyPriceMax(@NotNull Currency currency) {
        return getCurrencyPriceRange(currency).getMaxValue();
    }

    @NotNull
    private static UniDouble getCurrencyPriceRange(@NotNull Currency currency) {
        var map = AuctionConfig.LISTINGS_PRICE_PER_CURRENCY.get();

        return map.getOrDefault(currency.getInternalId(), map.getOrDefault(Placeholders.DEFAULT, UniDouble.of(-1, -1)));
    }
}
