package de.zfzfg.triggervolumes.listeners;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles events related to the selection tool.
 * Clears selections and stops particles when the tool is no longer held.
 * 
 * @author zfzfg
 */
public class SelectionToolListener implements Listener {

    private final TriggerVolumesPlugin plugin;

    /**
     * Creates a new SelectionToolListener.
     * 
     * @param plugin The plugin instance
     */
    public SelectionToolListener(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles when a player switches the item in their hand.
     * Clears selection if they switch away from the selection tool.
     * 
     * @param event The PlayerItemHeldEvent
     */
    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        // Check if they were holding the selection tool
        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        boolean wasHoldingTool = plugin.getSelectionManager().isSelectionTool(previousItem);
        boolean isHoldingTool = plugin.getSelectionManager().isSelectionTool(newItem);
        
        // If switching away from selection tool, clear selection
        if (wasHoldingTool && !isHoldingTool) {
            clearSelectionAndNotify(player);
        }
        
        // If switching to selection tool and has selection, restart particles
        if (!wasHoldingTool && isHoldingTool) {
            if (plugin.getSelectionManager().hasSelection(player)) {
                var selection = plugin.getSelectionManager().getSelection(player);
                if (selection.isComplete()) {
                    plugin.getParticleManager().startSelectionParticles(player);
                }
            }
        }
    }

    /**
     * Handles when a player drops an item.
     * Clears selection if they drop the selection tool.
     * 
     * @param event The PlayerDropItemEvent
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        
        // Check if they dropped the selection tool
        if (plugin.getSelectionManager().isSelectionTool(droppedItem)) {
            clearSelectionAndNotify(player);
        }
    }

    /**
     * Handles when a player picks up an item.
     * 
     * @param event The EntityPickupItemEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // Reserved for future functionality
        // Could auto-start selection particles if player picks up tool while holding slot
    }

    /**
     * Handles when a player leaves the server.
     * Cleans up their selection data.
     * 
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getSelectionManager().clearSelection(player);
    }

    /**
     * Clears a player's selection and notifies them.
     * 
     * @param player The player
     */
    private void clearSelectionAndNotify(Player player) {
        if (plugin.getSelectionManager().hasSelection(player)) {
            plugin.getSelectionManager().clearSelection(player);
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("selection-cleared"));
        }
    }
}
