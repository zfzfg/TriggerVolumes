package de.zfzfg.triggervolumes.listeners;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.managers.CooldownManager;
import de.zfzfg.triggervolumes.models.TriggerAction;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

/**
 * Handles player movement and triggers actions when players enter or leave trigger volumes.
 * Implements debouncing to prevent repeated triggering while inside a volume.
 * 
 * @author zfzfg
 */
public class PlayerMoveListener implements Listener {

    private final TriggerVolumesPlugin plugin;
    private final CooldownManager enterCooldownManager;
    private final CooldownManager leaveCooldownManager;
    
    // Track which volumes each player is currently in
    private final Map<UUID, Set<String>> playerVolumes;

    /**
     * Creates a new PlayerMoveListener.
     * 
     * @param plugin The plugin instance
     */
    public PlayerMoveListener(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.enterCooldownManager = new CooldownManager(plugin);
        this.leaveCooldownManager = new CooldownManager(plugin);
        this.playerVolumes = new HashMap<>();
    }

    /**
     * Handles player movement and checks for trigger volume entries and exits.
     * 
     * @param event The PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved to a new block
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) {
            return;
        }
        
        // Optimization: only check when moving to a new block
        if (from.getBlockX() == to.getBlockX() && 
            from.getBlockY() == to.getBlockY() && 
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        checkVolumeTransitions(player, to);
    }

    /**
     * Handles player teleportation and checks for trigger volume entries and exits.
     * 
     * @param event The PlayerTeleportEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        
        Player player = event.getPlayer();
        // Delay the check slightly to ensure teleport has completed
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                checkVolumeTransitions(player, player.getLocation());
            }
        }, 1L);
    }

    /**
     * Checks if a player has entered or left any volumes and triggers appropriate actions.
     * 
     * @param player The player to check
     * @param location The player's current location
     */
    private void checkVolumeTransitions(Player player, Location location) {
        // Get volumes at the new location
        List<TriggerVolume> volumesAtLocation = plugin.getVolumeManager().getVolumesAtLocation(location);
        
        // Get the set of volumes the player was in (make a copy to avoid concurrent modification)
        UUID playerId = player.getUniqueId();
        Set<String> previousVolumes = playerVolumes.computeIfAbsent(playerId, k -> new HashSet<>());
        Set<String> previousVolumesCopy = new HashSet<>(previousVolumes);
        Set<String> currentVolumeNames = new HashSet<>();
        
        // Build current volume names set
        for (TriggerVolume volume : volumesAtLocation) {
            currentVolumeNames.add(volume.getName().toLowerCase());
        }
        
        // Check for exits first (volumes player was in but is no longer in)
        for (String previousVolumeName : previousVolumesCopy) {
            if (!currentVolumeNames.contains(previousVolumeName)) {
                // Player just left this volume
                TriggerVolume volume = plugin.getVolumeManager().getVolume(previousVolumeName);
                if (volume != null) {
                    onPlayerLeaveVolume(player, volume);
                }
            }
        }
        
        // Then check for new entries
        for (TriggerVolume volume : volumesAtLocation) {
            String volumeName = volume.getName().toLowerCase();
            
            // Check if this is a new entry
            if (!previousVolumesCopy.contains(volumeName)) {
                // Player just entered this volume
                onPlayerEnterVolume(player, volume);
            }
        }
        
        // Update the tracked volumes by replacing the set
        playerVolumes.put(playerId, currentVolumeNames);
    }

    /**
     * Called when a player enters a trigger volume.
     * 
     * @param player The player
     * @param volume The volume the player entered
     */
    private void onPlayerEnterVolume(Player player, TriggerVolume volume) {
        // Check if player has permission to trigger
        if (!player.hasPermission("triggervolumes.use")) {
            return;
        }
        
        // Check cooldown for enter actions
        if (!enterCooldownManager.canTrigger(player, volume)) {
            return;
        }
        
        // Execute all enter actions for this volume
        List<TriggerAction> actions = volume.getEnterActions();
        for (TriggerAction action : actions) {
            action.execute(player);
        }
        
        // Set cooldown
        if (!actions.isEmpty()) {
            enterCooldownManager.setCooldown(player, volume);
        }
    }

    /**
     * Called when a player leaves a trigger volume.
     * 
     * @param player The player
     * @param volume The volume the player left
     */
    private void onPlayerLeaveVolume(Player player, TriggerVolume volume) {
        // Check if player has permission to trigger
        if (!player.hasPermission("triggervolumes.use")) {
            return;
        }
        
        // Check cooldown for leave actions
        if (!leaveCooldownManager.canTrigger(player, volume)) {
            return;
        }
        
        // Execute all leave actions for this volume
        List<TriggerAction> actions = volume.getLeaveActions();
        for (TriggerAction action : actions) {
            action.execute(player);
        }
        
        // Set cooldown
        if (!actions.isEmpty()) {
            leaveCooldownManager.setCooldown(player, volume);
        }
    }

    /**
     * Cleans up player data when they leave the server.
     * 
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playerVolumes.remove(playerId);
        enterCooldownManager.clearCooldowns(event.getPlayer());
        leaveCooldownManager.clearCooldowns(event.getPlayer());
    }
}
