package su.nightexpress.nexshop.shop.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.api.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.module.ShopModule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionLogger {

    private final ShopModule module;
    private final boolean    outFile;
    private final boolean           outConsole;
    private final DateTimeFormatter dateFormat;
    private final String            format;

    public TransactionLogger(@NotNull ShopModule module) {
        this.module = module;

        String path = "Transaction_Logs.";
        JYML cfg = module.getConfig();

        this.outFile = cfg.getBoolean(path + "Output.File");
        this.outConsole = cfg.getBoolean(path + "Output.Console");
        this.dateFormat = DateTimeFormatter.ofPattern(cfg.getString(path + "Format.Date", "dd/MM/yyyy HH:mm:ss"));
        this.format = Colorizer.apply(cfg.getString(path + "Format.Purchase", "%type%: %player% - x%amount% of %item% for %price%&7 in %shop_name% shop."));
    }

    public void logTransaction(@NotNull ShopTransactionEvent<?> event) {
        if (!this.outFile && !this.outConsole) return;

        Player player = event.getPlayer();
        TransactionResult result = event.getResult();
        Product<?, ?, ?> product = result.getProduct();
        Shop<?, ?> shop = product.getShop();

        String format = this.format.replace("%player%", player.getName());
        format = result.replacePlaceholders().apply(format);
        format = shop.replacePlaceholders().apply(format);

        this.print(Colorizer.strip(format));
    }

    private void print(@NotNull String text) {
        if (this.outConsole) {
            this.module.info(text);
        }
        if (this.outFile) {
            String date = LocalDateTime.now().format(this.dateFormat);
            String outFile = "[" + date + "] " + Colorizer.restrip(text);

            BufferedWriter output;
            try {
                output = new BufferedWriter(new FileWriter(this.module.getAbsolutePath() + "/transactions.log", true));
                output.append(outFile);
                output.newLine();
                output.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
