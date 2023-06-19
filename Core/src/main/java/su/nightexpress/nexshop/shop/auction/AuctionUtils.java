package su.nightexpress.nexshop.shop.auction;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuctionUtils {

    public static void fillDummy(@NotNull AuctionManager auctionManager) {
        List<ShopUser> users = auctionManager.plugin().getData().getUsers();
        String[] randoms = {"NekoGeko", "_silent_bunny_", "DefectIV", "Dinara777", "The_Metal", "poolpony142",
            "OrganicPlasma", "sunfire81", "CapCapCom", "PepsiCoca", "X_Mint_X", "Ladykiller", "SHARkNESS", "crazy95", "Radivil3",
            "VitorKhr", "RosieKei", "Samara", "Katushka", "Dark_Night", "marina007", "Nuforen", "Noob_Perforator", "LaserDance"};

        Map<UUID, String> owners = new HashMap<>();
        users.forEach(user -> owners.put(user.getId(), user.getName()));
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
                    Enchantment enchantment = Rnd.get(Enchantment.values());
                    item.addUnsafeEnchantment(enchantment, Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel()));
                }
            }

            Currency currency = Rnd.get(auctionManager.getCurrencies());
            double price = NumberUtil.round((int) Rnd.getDouble(50, 10_000D));

            LocalDateTime created = LocalDateTime.now().minusDays(Rnd.get(5)).minusHours(Rnd.get(6)).minusMinutes(Rnd.get(30));
            long dateCreation = TimeUtil.toEpochMillis(created);
            long dateExpired = dateCreation + AuctionConfig.LISTINGS_EXPIRE_IN;


            if (i < 15) {
                AuctionListing listing = new AuctionListing(UUID.randomUUID(), ownerId, ownerName, item, currency, price, dateCreation, dateExpired);
                auctionManager.addListing(listing);
                auctionManager.getDataHandler().addListing(listing);
            }
            else {
                LocalDateTime buyed = created.plusDays(Rnd.get(4)).plusHours(Rnd.get(4)).plusMinutes(Rnd.get(15));
                long buyDate = TimeUtil.toEpochMillis(buyed);

                AuctionCompletedListing listing = new AuctionCompletedListing(UUID.randomUUID(), ownerId, ownerName, Rnd.get(randoms), item, currency, price, dateCreation, Rnd.nextBoolean(), buyDate);
                auctionManager.addCompletedListing(listing);
                auctionManager.getDataHandler().addCompletedListing(listing);
            }
        }
    }

    public static double calculateTax(double price, double taxPercent) {
        return price * (taxPercent / 100D);
    }
}
