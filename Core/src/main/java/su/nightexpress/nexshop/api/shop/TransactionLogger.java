package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public TransactionLogger(@NotNull ShopModule module) {
        this.module = module;

        String path = "Transaction_Logs.";
        JYML cfg = module.getConfig();

        this.outFile = JOption.create(path + "Output.File", true).read(cfg);

        this.outConsole = JOption.create(path + "Output.Console", true).read(cfg);

        String datePattern = JOption.create(path + "Format.Date", "dd/MM/yyyy HH:mm:ss").read(cfg);
        this.dateFormat = DateTimeFormatter.ofPattern(datePattern);

        this.format = JOption.create(path + "Format.Purchase",
            GENERIC_TYPE + ": " + PLAYER_NAME + " - x" + GENERIC_AMOUNT + " of " + GENERIC_ITEM + " for " + GENERIC_PRICE + " in " + SHOP_NAME + " shop."
        ).read(cfg);
    }

    public void logTransaction(@NotNull ShopTransactionEvent event) {
        if (!this.outFile && !this.outConsole) return;

        Player player = event.getPlayer();
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        Shop shop = product.getShop();

        String format = Placeholders.forPlayer(player).apply(this.format.replace("%player%", player.getName()));
        format = result.replacePlaceholders().apply(format);
        format = shop.replacePlaceholders().apply(format);

        this.print(format);
    }

    private void print(@NotNull String text) {
        text = Colorizer.restrip(text);

        if (this.outConsole) {
            this.module.info(text);
        }
        if (this.outFile) {
            String date = LocalDateTime.now().format(this.dateFormat);
            String outFile = "[" + date + "] " + text;

            File file = new File(this.module.getAbsolutePath(), FILE_NAME);
            BufferedWriter output;
            try {
                output = new BufferedWriter(new FileWriter(file, true));
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
