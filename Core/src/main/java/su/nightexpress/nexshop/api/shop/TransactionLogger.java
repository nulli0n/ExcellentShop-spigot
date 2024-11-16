package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static su.nightexpress.nexshop.Placeholders.*;

public class TransactionLogger {

    public static final String FILE_NAME = "transactions.log";

    private final ShopModule        module;
    private final boolean           outFile;
    private final boolean           outConsole;
    private final DateTimeFormatter dateFormat;
    private final String            format;

    public TransactionLogger(@NotNull ShopModule module, @NotNull FileConfig config) {
        this.module = module;

        String path = "Transaction_Logs.";

        this.outFile = ConfigValue.create(path + "Output.File", true).read(config);

        this.outConsole = ConfigValue.create(path + "Output.Console", true).read(config);

        String datePattern = ConfigValue.create(path + "Format.Date", "dd/MM/yyyy HH:mm:ss").read(config);
        this.dateFormat = DateTimeFormatter.ofPattern(datePattern);

        this.format = ConfigValue.create(path + "Format.Purchase",
            GENERIC_TYPE + ": " + PLAYER_NAME + " - x" + GENERIC_AMOUNT + " of " + GENERIC_ITEM + " for " + GENERIC_PRICE + " in " + SHOP_NAME + " shop."
        ).read(config).replace("%player%", Placeholders.PLAYER_NAME);
    }

    public void logTransaction(@NotNull ShopTransactionEvent event) {
        if (!this.outFile && !this.outConsole) return;

        Player player = event.getPlayer();
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        Shop shop = product.getShop();

        String format = Placeholders.forPlayerWithPAPI(player).apply(this.format);
        format = result.replacePlaceholders().apply(format);
        format = shop.replacePlaceholders().apply(format);

        this.print(format);
    }

    private void print(@NotNull String text) {
        text = NightMessage.stripAll(text);

        if (this.outConsole) {
            this.module.info(text);
        }
        if (this.outFile) {
            String date = LocalDateTime.now().format(this.dateFormat);
            String outFile = "[" + date + "] " + text;

            File file = new File(this.module.getAbsolutePath(), FILE_NAME);
            BufferedWriter output;
            try {
                output = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true));
                output.append(outFile);
                output.newLine();
                output.close();
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
