package dev.projectg.crossplatforms.spigot.common;

import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.handler.FormPlayer;
import dev.projectg.crossplatforms.handler.PlaceholderHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.events.ExpansionsLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public class PlaceholderAPIHandler implements PlaceholderHandler, Listener {

    public PlaceholderAPIHandler(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onExpansionsLoaded(ExpansionsLoadedEvent event) {
        if (!PlaceholderAPI.isRegistered("Player")) {
            Logger.getLogger().warn("PlaceholderAPI is installed but the Player extension is not installed! %player_name% and %player_uuid% will NOT be resolved. Please install the Player extension.");
        }
    }

    /**
     * Returns the inputted text with placeholders set, if PlaceholderAPI is loaded. If not, it returns the same text.
     * @param player The player
     * @param text The text
     * @return the formatted text.
     */
    public String setPlaceholders(@Nonnull FormPlayer player, @Nonnull String text) {
        if (text.isEmpty()) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders((Player) player.getHandle(), text);
    }
}
