package dev.projectg.crossplatforms;

import dev.projectg.crossplatforms.config.mapping.GeneralConfig;
import dev.projectg.crossplatforms.reloadable.Reloadable;
import dev.projectg.crossplatforms.reloadable.ReloadableRegistry;

public class Logger implements Reloadable {

    private static final Logger LOGGER = new Logger(CrossplatForms.getInstance());

    private final CrossplatForms plugin;
    private boolean debug;

    public static Logger getLogger() {
        return LOGGER;
    }

    private Logger(CrossplatForms plugin) {
        this.plugin = plugin;
        debug = plugin.getConfigManager().getConfig(GeneralConfig.class).isEnableDebug();
        ReloadableRegistry.registerReloadable(this);
    }

    public void log(Level level, String message) {
        switch (level) {
            default: // intentional fallthrough
            case INFO:
                info(message);
                break;
            case WARN:
                warn(message);
                break;
            case SEVERE:
                severe(message);
                break;
            case DEBUG:
                debug(message);
                break;
        }
    }
    public void info(String message) {
        plugin.getLogger().info(message);
    }
    public void warn(String message) {
        plugin.getLogger().warning(message);
    }
    public void severe(String message) {
        plugin.getLogger().severe(message);
    }
    public void debug(String message) {
        if (debug) {
            plugin.getLogger().info(message);
        }
    }

    public enum Level {
        INFO,
        WARN,
        SEVERE,
        DEBUG
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean reload() {
        debug = plugin.getConfigManager().getConfig(GeneralConfig.class).isEnableDebug();
        return true;
    }
}
