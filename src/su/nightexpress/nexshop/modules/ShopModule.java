package su.nightexpress.nexshop.modules;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.module.AbstractModule;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;
import su.nightexpress.nexshop.modules.command.ModuleReloadCmd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ShopModule extends AbstractModule<ExcellentShop> {

    protected Logger logger;

    public ShopModule(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.logger = new Logger();

        if (this.moduleCommand != null) {
            this.moduleCommand.addDefaultCommand(new HelpSubCommand<>(this.plugin));
            this.moduleCommand.addChildren(new ModuleReloadCmd(this));
        }
    }

    @Override
    protected void onShutdown() {
        if (this.logger != null) {
            this.logger = null;
        }
    }

    @NotNull
    public Logger getLogger() {
        return this.logger;
    }

    public class Logger {

        private final boolean           outFile;
        private final boolean           outConsole;
        private final DateTimeFormatter dateFormat;
        private final String            format;

        public Logger() {
            String path = "Transaction_Logs.";

            this.outFile = cfg.getBoolean(path + "Output.File");
            this.outConsole = cfg.getBoolean(path + "Output.Console");
            this.dateFormat = DateTimeFormatter.ofPattern(cfg.getString(path + "Format.Date", "dd/MM/yyyy HH:mm:ss"));
            this.format = StringUT.color(cfg.getString(path + "Format.Purchase", "%type%: %player% - x%amount% of %item% for %price%&7 in %shop_name% shop."));
        }

        public void logTransaction(@NotNull AbstractShopPurchaseEvent event) {
            if (!this.outFile && !this.outConsole) return;

            Player player = event.getPlayer();
            IProductPrepared prepared = event.getPrepared();
            IShop shop = event.getShop();

            String format = this.format.replace("%player%", player.getName());
            format = prepared.replacePlaceholders().apply(format);
            format = shop.replacePlaceholders().apply(format);

            this.print(format);
        }

        private void print(@NotNull String text) {
            if (this.outConsole) {
                info(text);
            }
            if (this.outFile) {
                String date = LocalDateTime.now().format(this.dateFormat);
                String outFile = new StringBuilder()
                        .append("[").append(date).append("] ")
                        .append(StringUT.colorOff(text)).toString();

                BufferedWriter output;
                try {
                    output = new BufferedWriter(new FileWriter(getFullPath() + "transactions.log", true));
                    output.append(outFile);
                    output.newLine();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
