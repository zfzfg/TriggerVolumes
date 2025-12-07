package de.zfzfg.triggervolumes.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents an action that can be executed when a player enters a trigger volume.
 * 
 * @author zfzfg
 */
public class TriggerAction {

    private final ActionType type;
    private final String value;

    /**
     * Creates a new TriggerAction.
     * 
     * @param type The type of action
     * @param value The action value (command, message, or coordinates)
     */
    public TriggerAction(ActionType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the action type.
     * 
     * @return The action type
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Gets the action value.
     * 
     * @return The action value
     */
    public String getValue() {
        return value;
    }

    /**
     * Executes this action for the given player.
     * 
     * @param player The player to execute the action for
     */
    public void execute(Player player) {
        String processedValue = value.replace("%player%", player.getName())
                                     .replace("%uuid%", player.getUniqueId().toString());
        
        switch (type) {
            case PLAYER_COMMAND:
                executePlayerCommand(player, processedValue);
                break;
            case CONSOLE_COMMAND:
                executeConsoleCommand(processedValue);
                break;
            case MESSAGE:
                sendMessage(player, processedValue);
                break;
            case TELEPORT:
                teleportPlayer(player, processedValue);
                break;
        }
    }

    /**
     * Executes a command as the player.
     * 
     * @param player The player to execute as
     * @param command The command to execute
     */
    private void executePlayerCommand(Player player, String command) {
        // Remove leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        player.performCommand(command);
    }

    /**
     * Executes a command from the console.
     * 
     * @param command The command to execute
     */
    private void executeConsoleCommand(String command) {
        // Remove leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * Sends a message to the player with color code support.
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    private void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Teleports the player to the specified coordinates.
     * 
     * @param player The player to teleport
     * @param coords The coordinates in format "x y z" or "x y z yaw pitch"
     */
    private void teleportPlayer(Player player, String coords) {
        String[] parts = coords.split(" ");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                
                float yaw = player.getLocation().getYaw();
                float pitch = player.getLocation().getPitch();
                
                if (parts.length >= 5) {
                    yaw = Float.parseFloat(parts[3]);
                    pitch = Float.parseFloat(parts[4]);
                }
                
                Location targetLocation = new Location(player.getWorld(), x, y, z, yaw, pitch);
                player.teleport(targetLocation);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid teleport coordinates!");
            }
        }
    }

    @Override
    public String toString() {
        return "TriggerAction{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
