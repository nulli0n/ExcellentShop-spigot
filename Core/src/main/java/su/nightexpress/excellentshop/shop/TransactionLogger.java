package su.nightexpress.excellentshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.Module;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.ShopFiles;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogger {

    private static final String BUY_ENTRY  = "%s bought %s item(s) total worth %s:";
    private static final String SELL_ENTRY = "%s sold %s item(s) total worth %s:";
    private static final String ITEM_ENTRY = "  > %s x %s | Name: '%s' | Id: '%s' | Shop: '%s' | Worth: %s";

    private final Module            module;
    private final boolean           outFile;
    private final boolean           outConsole;
    private final DateTimeFormatter dateFormat;
    private final Path              path;

    public TransactionLogger(@NonNull Module module, @NonNull FileConfig config) {
        this.module = module;

        String path = "Transaction_Logs.";

        this.outFile = ConfigValue.create(path + "Output.File", true).read(config);
        this.outConsole = ConfigValue.create(path + "Output.Console", true).read(config);

        String datePattern = ConfigValue.create(path + "Format.Date", "dd/MM/yyyy HH:mm:ss").read(config);

        this.dateFormat = DateTimeFormatter.ofPattern(datePattern);
        this.path = this.module.getPath().resolve(ShopFiles.LOG_TRANSACTIONS);
    }

    public void logTransaction(@NonNull ECompletedTransaction transaction) {
        if (!this.outFile && !this.outConsole) return;

        Player player = transaction.player();
        TradeType type = transaction.type();

        String prefix = switch (type) {
            case BUY -> BUY_ENTRY;
            case SELL -> SELL_ENTRY;
        };

        List<String> logs = new ArrayList<>();

        logs.add(prefix.formatted(
            player.getName(),
            transaction.countTotalAmount(),
            transaction.worth().format((currency, value) -> value + " " + currency.getName(), ", ")
        ));

        transaction.items().forEach(item -> {
            Product product = item.product();
            ItemStack preview = product.getEffectivePreview();

            logs.add(ITEM_ENTRY.formatted(
                UnitUtils.unitsToAmount(item.product(), item.units()),
                BukkitThing.getValue(preview.getType()),
                NightMessage.stripTags(ItemUtil.getNameSerialized(preview)),
                product.getId(),
                product.getShop().getId(),
                item.price().format((currency, value) -> value + " " + currency.getName(), ", "))
            );
        });

        this.print(logs);
    }

    private void print(@NonNull List<String> text) {
        if (this.outConsole) {
            text.forEach(this.module::info);
        }
        if (this.outFile) {
            String date = TimeUtil.getCurrentDateTime().format(this.dateFormat);
            String logEntry = "[" + date + "] " + String.join(System.lineSeparator(), text) + System.lineSeparator();

            try {
                Files.writeString(this.path, logEntry, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
