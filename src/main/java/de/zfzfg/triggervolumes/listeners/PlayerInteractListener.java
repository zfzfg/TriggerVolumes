package de.zfzfg.triggervolumes.listeners;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.Selection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles player interactions with the selection tool.
 * Sets position 1 on left-click and position 2 on right-click.
 * 
 * @author zfzfg
 */
public class PlayerInteractListener implements Listener {

    private final TriggerVolumesPlugin plugin;

    /**
     * Creates a new PlayerInteractListener.
     * 
     * @param plugin The plugin instance
     */
    public PlayerInteractListener(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player interactions for selection tool usage.
     * 
     * @param event The PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has permission
        if (!player.hasPermission("triggervolumes.admin")) {
            return;
        }
        
        // Check if player is holding the selection tool
        if (!plugin.getSelectionManager().isSelectionTool(event.getItem())) {
            return;
        }
        
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        
        // Handle left-click (pos1) and right-click (pos2)
        if (action == Action.LEFT_CLICK_BLOCK && block != null) {
            event.setCancelled(true);
            setPosition1(player, block.getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK && block != null) {
            event.setCancelled(true);
            setPosition2(player, block.getLocation());
        } else if (action == Action.LEFT_CLICK_AIR) {
            event.setCancelled(true);
            // Use player's target block for air clicks
            Block targetBlock = player.getTargetBlockExact(100);
            if (targetBlock != null) {
                setPosition1(player, targetBlock.getLocation());
            }
        } else if (action == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            // Use player's target block for air clicks
            Block targetBlock = player.getTargetBlockExact(100);
            if (targetBlock != null) {
                setPosition2(player, targetBlock.getLocation());
            }
        }
    }

    /**
     * Sets position 1 for a player's selection.
     * 
     * @param player The player
     * @param location The location to set
     */
    private void setPosition1(Player player, Location location) {
        Selection selection = plugin.getSelectionManager().getSelection(player);
        selection.setPos1(location.clone());
        
        String message = plugin.getLanguageManager().getMessageWithPrefix("pos1-set",
                "%x%", String.valueOf(location.getBlockX()),
                "%y%", String.valueOf(location.getBlockY()),
                "%z%", String.valueOf(location.getBlockZ()));
        player.sendMessage(message);
        
        // Start or update particle visualization
        if (selection.isComplete()) {
            plugin.getParticleManager().startSelectionParticles(player);
        }
    }

    /**
     * Sets position 2 for a player's selection.
     * 
     * @param player The player
     * @param location The location to set
     */
    private void setPosition2(Player player, Location location) {
        Selection selection = plugin.getSelectionManager().getSelection(player);
        selection.setPos2(location.clone());
        
        String message = plugin.getLanguageManager().getMessageWithPrefix("pos2-set",
                "%x%", String.valueOf(location.getBlockX()),
                "%y%", String.valueOf(location.getBlockY()),
                "%z%", String.valueOf(location.getBlockZ()));
        player.sendMessage(message);
        
        // Start or update particle visualization
        if (selection.isComplete()) {
            plugin.getParticleManager().startSelectionParticles(player);
        }
    }
}
