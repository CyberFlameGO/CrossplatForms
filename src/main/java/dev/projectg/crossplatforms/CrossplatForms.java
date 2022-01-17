package dev.projectg.crossplatforms;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import dev.projectg.crossplatforms.command.CommandOrigin;
import dev.projectg.crossplatforms.command.FormsCommand;
import dev.projectg.crossplatforms.command.SpigotCommandOrigin;
import dev.projectg.crossplatforms.command.defaults.DefaultCommands;
import dev.projectg.crossplatforms.command.defaults.HelpCommand;
import dev.projectg.crossplatforms.command.defaults.ListCommand;
import dev.projectg.crossplatforms.config.ConfigManager;
import dev.projectg.crossplatforms.config.GeneralConfig;
import dev.projectg.crossplatforms.item.AccessItemRegistry;
import dev.projectg.crossplatforms.item.InventoryManager;
import dev.projectg.crossplatforms.interfacing.java.JavaMenuListeners;
import dev.projectg.crossplatforms.interfacing.bedrock.BedrockFormRegistry;
import dev.projectg.crossplatforms.interfacing.java.JavaMenuRegistry;
import dev.projectg.crossplatforms.handler.BedrockHandler;
import dev.projectg.crossplatforms.handler.FloodgateHandler;
import dev.projectg.crossplatforms.handler.GeyserHandler;
import dev.projectg.crossplatforms.handler.ServerHandler;
import dev.projectg.crossplatforms.handler.SpigotServerHandler;
import dev.projectg.crossplatforms.reloadable.ReloadableRegistry;
import dev.projectg.crossplatforms.utils.FileUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.session.auth.AuthType;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Getter
public class CrossplatForms extends JavaPlugin {
    @Getter
    private static CrossplatForms instance;

    private String branch = "unknown";
    private String commit = "unknown";

    private ConfigManager configManager;
    private ServerHandler serverHandler;
    private BedrockHandler bedrockHandler;

    private AccessItemRegistry accessItemRegistry;
    private BedrockFormRegistry bedrockFormRegistry;
    private JavaMenuRegistry javaMenuRegistry;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        ReloadableRegistry.clear();
        Logger logger = Logger.getLogger();

        serverHandler = new SpigotServerHandler(Bukkit.getServer());

        try {
            Properties gitProperties = new Properties();
            gitProperties.load(FileUtils.getResource("git.properties"));
            branch = gitProperties.getProperty("git.branch", "unknown");
            commit = gitProperties.getProperty("git.commit.id.abbrev", "unknown");
            logger.info("Branch: " + branch + ", Commit: " + commit);
        } catch (Exception e) {
            logger.warn("Unable to load resource: git.properties");
            e.printStackTrace();
        }

        if (serverHandler.isPluginEnabled("floodgate")) {
            if (serverHandler.isPluginEnabled("Geyser-Spigot") && GeyserImpl.getInstance().getConfig().getRemote().getAuthType() != AuthType.FLOODGATE ) {
                logger.warn("Floodgate is installed but auth-type in Geyser's config is not set to Floodgate! Ignoring Floodgate.");
                bedrockHandler = new GeyserHandler();
            } else {
                bedrockHandler = new FloodgateHandler();
            }
        } else if (serverHandler.isPluginEnabled("Geyser-Spigot")) {
            bedrockHandler = new GeyserHandler();
            logger.warn("Floodgate is recommended and more stable!");
        } else {
            logger.severe("Geyser or Floodgate is required! Disabling.");
            return;
        }

        if (!serverHandler.isPluginEnabled("PlaceholderAPI")) {
            logger.warn("This plugin works best with PlaceholderAPI! Since you don't have it installed, only %player_name% and %player_uuid% will work in the GeyserHub config!");
        }

        long configTime = System.currentTimeMillis();
        configManager = new ConfigManager(getDataFolder(), logger);
        if (!configManager.loadAllConfigs()) {
            logger.severe("A severe configuration error occurred, which will lead to significant parts of this plugin not loading. Please repair the config and run /forms reload or restart the server.");
        }
        logger.setDebug(configManager.getConfig(GeneralConfig.class).map(GeneralConfig::isEnableDebug).orElse(false));
        logger.debug("Took " + (System.currentTimeMillis() - configTime) + "ms to load config files.");

        // Bungee channel for selector
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Load forms
        long registryTime = System.currentTimeMillis();
        accessItemRegistry = new AccessItemRegistry(configManager, serverHandler);
        bedrockFormRegistry = new BedrockFormRegistry(configManager, serverHandler);
        javaMenuRegistry = new JavaMenuRegistry(configManager, serverHandler);
        logger.debug("Took " + (System.currentTimeMillis() - registryTime) + "ms to setup registries.");

        long commandTime = System.currentTimeMillis();

        CommandManager<CommandOrigin> commandManager;
        try {
            commandManager = new BukkitCommandManager<>(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    (SpigotCommandOrigin::new),
                    origin -> (CommandSender) origin.getHandle());
        } catch (Exception e) {
            logger.severe("Failed to create CommandManager, stopping");
            e.printStackTrace();
            return;
        }

        Command.Builder<CommandOrigin> defaultBuilder = commandManager.commandBuilder("forms");

        commandManager.command(defaultBuilder
                .permission("crossplatforms.base")
                .handler((context -> {
                    CommandOrigin origin = context.getSender();
                    try {
                        if (origin.hasPermission(ListCommand.PERMISSION)) {
                            logger.debug("Executing /forms list from /forms");
                            commandManager.executeCommand(origin, "forms list").get();
                        } else if (origin.hasPermission(HelpCommand.PERMISSION)) {
                            logger.debug("Executing /forms help from /forms");
                            commandManager.executeCommand(origin, "forms help").get();
                        } else {
                            origin.sendMessage(Logger.Level.INFO, "Please specify sub command");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }))
                .build());

        for (FormsCommand command : new DefaultCommands(this).getCommands()) {
            command.register(commandManager, defaultBuilder);
        }

        logger.debug("Took " + (System.currentTimeMillis() - commandTime) + "ms to setup commands.");

        // Listeners for the Bedrock and Java menus
        Bukkit.getServer().getPluginManager().registerEvents(
                new InventoryManager(
                        accessItemRegistry,
                        bedrockFormRegistry,
                        javaMenuRegistry,
                        bedrockHandler),
                this);

        Bukkit.getServer().getPluginManager().registerEvents(new JavaMenuListeners(javaMenuRegistry), this);

        logger.info("Took " + (System.currentTimeMillis() - start) + "ms to boot CrossplatForms.");
    }
}
