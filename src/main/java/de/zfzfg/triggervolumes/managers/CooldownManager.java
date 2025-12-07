package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages cooldowns for trigger volume activations.
 * Prevents repeated triggering when a player stays in or re-enters a volume.
 * 
 * @author zfzfg
 */
public class CooldownManager {

    private final TriggerVolumesPlugin plugin;
    
    // Tracks which volumes each player is currently in
    private final Map<UUID, Set<String>> playerInVolumes;
    
    // Tracks cooldowns: player UUID -> volume name -> last trigger time
    private final Map<UUID, Map<String, Long>> cooldowns;

    /**
     * Creates a new CooldownManager.
     * 
     * @param plugin The plugin instance
     */
    public CooldownManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.playerInVolumes = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    /**
     * Checks if a player has just entered a volume (debouncing).
     * 
     * @param player The player
     * @param volume The volume
     * @return True if this is a new entry
     */
    public boolean isNewEntry(Player player, TriggerVolume volume) {
        Set<String> inVolumes = playerInVolumes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        String volumeName = volume.getName().toLowerCase();
        
        if (inVolumes.contains(volumeName)) {
            return false;
        }
        
        inVolumes.add(volumeName);
        return true;
    }

    /**
     * Marks that a player has left a volume.
     * 
     * @param player The player
     * @param volume The volume
     */
    public void markExit(Player player, TriggerVolume volume) {
        Set<String> inVolumes = playerInVolumes.get(player.getUniqueId());
        if (inVolumes != null) {
            inVolumes.remove(volume.getName().toLowerCase());
        }
    }

    /**
     * Updates which volumes a player is in based on their current location.
     * 
     * @param player The player
     * @param currentVolumes The volumes the player is currently in
     */
    public void updatePlayerVolumes(Player player, List<TriggerVolume> currentVolumes) {
        Set<String> inVolumes = playerInVolumes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        
        // Find volumes the player has left
        Set<String> currentNames = new HashSet<>();
        for (TriggerVolume volume : currentVolumes) {
            currentNames.add(volume.getName().toLowerCase());
        }
        
        // Remove volumes player is no longer in
        inVolumes.removeIf(name -> !currentNames.contains(name));
    }

    /**
     * Checks if a player can trigger a volume (not on cooldown).
     * 
     * @param player The player
     * @param volume The volume
     * @return True if the player can trigger the volume
     */
    public boolean canTrigger(Player player, TriggerVolume volume) {
        boolean cooldownEnabled = plugin.getConfig().getBoolean("cooldowns.enabled", true);
        if (!cooldownEnabled) {
            return true;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return true;
        }
        
        Long lastTrigger = playerCooldowns.get(volume.getName().toLowerCase());
        if (lastTrigger == null) {
            return true;
        }
        
        int defaultCooldown = plugin.getConfig().getInt("cooldowns.default-cooldown", 3);
        long cooldownMs = defaultCooldown * 1000L;
        return System.currentTimeMillis() - lastTrigger >= cooldownMs;
    }

    /**
     * Sets the cooldown for a player and volume.
     * 
     * @param player The player
     * @param volume The volume
     */
    public void setCooldown(Player player, TriggerVolume volume) {
        boolean cooldownEnabled = plugin.getConfig().getBoolean("cooldowns.enabled", true);
        if (!cooldownEnabled) {
            return;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        playerCooldowns.put(volume.getName().toLowerCase(), System.currentTimeMillis());
    }

    /**
     * Clears all cooldowns for a player.
     * 
     * @param player The player
     */
    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
        playerInVolumes.remove(player.getUniqueId());
    }

    /**
     * Gets the remaining cooldown time in seconds.
     * 
     * @param player The player
     * @param volume The volume
     * @return Remaining cooldown in seconds, or 0 if no cooldown
     */
    public int getRemainingCooldown(Player player, TriggerVolume volume) {
        boolean cooldownEnabled = plugin.getConfig().getBoolean("cooldowns.enabled", true);
        if (!cooldownEnabled) {
            return 0;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }
        
        Long lastTrigger = playerCooldowns.get(volume.getName().toLowerCase());
        if (lastTrigger == null) {
            return 0;
        }
        
        int defaultCooldown = plugin.getConfig().getInt("cooldowns.default-cooldown", 3);
        long cooldownMs = defaultCooldown * 1000L;
        long elapsed = System.currentTimeMillis() - lastTrigger;
        long remaining = cooldownMs - elapsed;
        
        return remaining > 0 ? (int) (remaining / 1000) + 1 : 0;
    }
}
