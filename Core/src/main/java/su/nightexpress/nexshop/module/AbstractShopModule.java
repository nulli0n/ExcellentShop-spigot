package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class AbstractShopModule extends AbstractManager<ExcellentShop> {

    private final String id;
    private final String name;
    protected final ModuleCommand<AbstractShopModule> command;
    protected final JYML cfg;

    public AbstractShopModule(@NotNull ExcellentShop plugin, @NotNull String id, @NotNull String[] aliases) {
        super(plugin);
        this.id = id;
        this.name = StringUtil.capitalizeUnderscored(this.getId());
        this.command = new ModuleCommand<>(this, aliases, (String) null);
        this.cfg = JYML.loadOrExtract(plugin, this.getLocalPath(), "settings.yml");
    }

    @Override
    protected void onLoad() {
        // ---------- MOVE OUT OF /MODULES/ START ----------
        File dirOld = new File(plugin.getDataFolder().getAbsolutePath() + "/modules/" + this.getId());
        File dirNew = new File(plugin.getDataFolder().getAbsolutePath() + "/" + this.getId());
        if (dirOld.exists() && !dirNew.exists()) {
            try {
                Files.move(dirOld.toPath(), dirNew.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        // ---------- MOVE OUT OF /MODULES/ END ----------

        this.command.addDefaultCommand(new HelpSubCommand<>(this.plugin));
        this.command.addChildren(new ModuleReloadCommand<>(this));

        this.plugin.getCommandManager().registerCommand(this.command);
    }

    @Override
    protected void onShutdown() {
        this.plugin.getCommandManager().unregisterCommand(this.command);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public JYML getConfig() {
        return this.cfg;
    }

    @NotNull
    public final String getLocalPath() {
        return this.getId();
    }

    @NotNull
    public final String getAbsolutePath() {
        return this.plugin.getDataFolder() + "/" + this.getLocalPath();
    }

    @NotNull
    private String buildLog(@NotNull String msg) {
        return "[" + this.getName() + "] " + msg;
    }

    public final void info(@NotNull String msg) {
        this.plugin.info(this.buildLog(msg));
    }

    public final void warn(@NotNull String msg) {
        this.plugin.warn(this.buildLog(msg));
    }

    public final void error(@NotNull String msg) {
        this.plugin.error(this.buildLog(msg));
    }
}
