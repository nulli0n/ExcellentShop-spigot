package su.nightexpress.nexshop.module;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.module.AbstractModule;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.IProductPrepared;
import su.nightexpress.nexshop.api.shop.IShop;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;
import su.nightexpress.nexshop.module.command.ModuleReloadCmd;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ShopModule extends AbstractModule<ExcellentShop> {

    protected JYML                          cfg;
    protected ShopModuleCommand<ShopModule> moduleCommand;
    protected Logger                        logger;

    public ShopModule(@NotNull ExcellentShop plugin, @NotNull String id) {
        super(plugin, id);
    }

    @Override
    protected void onLoad() {
        this.cfg = JYML.loadOrExtract(plugin, this.getPath() + "settings.yml");
        this.logger = new Logger();

        this.registerCommands();
    }

    @Override
    protected void onShutdown() {
        this.unregisterCommands();
        if (this.logger != null) {
            this.logger = null;
        }
    }

    @NotNull
    public JYML getConfig() {
        return this.cfg;
    }

    @NotNull
    public Logger getLogger() {
        return this.logger;
    }

    private void registerCommands() {
        String alias = cfg.getString("Command_Aliases");
        if (alias == null) return;

        String[] aliases = alias.split(",");
        if (aliases.length == 0 || aliases[0].isEmpty()) return;

        this.moduleCommand = new ShopModuleCommand<>(this, aliases);
        this.moduleCommand.addDefaultCommand(new HelpSubCommand<>(this.plugin));
        this.moduleCommand.addChildren(new ModuleReloadCmd(this));
        this.plugin.getCommandManager().registerCommand(this.moduleCommand);
    }

    private void unregisterCommands() {
        if (this.moduleCommand == null) return;

        this.plugin.getCommandManager().unregisterCommand(this.moduleCommand);
        this.moduleCommand = null;
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
            this.format = StringUtil.color(cfg.getString(path + "Format.Purchase", "%type%: %player% - x%amount% of %item% for %price%&7 in %shop_name% shop."));
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
                String outFile = "[" + date + "] " + StringUtil.colorOff(text);

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
