package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.ActionType;
import de.zfzfg.triggervolumes.models.TriggerAction;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import de.zfzfg.triggervolumes.models.VolumeGroup;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all trigger volumes including loading, saving, and CRUD operations.
 * Also manages volume groups for batch operations.
 * 
 * @author zfzfg
 */
public class TriggerVolumeManager {

    private final TriggerVolumesPlugin plugin;
    private final Map<String, TriggerVolume> volumes;
    private final Map<String, VolumeGroup> groups;
    private final File volumesFile;
    private FileConfiguration volumesConfig;
    
    // Spatial hashing for performance optimization
    private final Map<String, Map<Long, List<TriggerVolume>>> spatialHash;
    private static final int CHUNK_SIZE = 16; // Minecraft chunk size

    /**
     * Creates a new TriggerVolumeManager.
     * 
     * @param plugin The plugin instance
     */
    public TriggerVolumeManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.volumes = new HashMap<>();
        this.groups = new HashMap<>();
        this.spatialHash = new HashMap<>();
        this.volumesFile = new File(plugin.getDataFolder(), "triggervolumes.yml");
    }

    /**
     * Loads all volumes from the configuration file.
     */
    public void loadVolumes() {
        if (!volumesFile.exists()) {
            plugin.saveResource("triggervolumes.yml", false);
        }
        
        volumesConfig = YamlConfiguration.loadConfiguration(volumesFile);
        volumes.clear();
        
        ConfigurationSection volumesSection = volumesConfig.getConfigurationSection("volumes");
        if (volumesSection == null) {
            return;
        }
        
        for (String name : volumesSection.getKeys(false)) {
            ConfigurationSection volumeSection = volumesSection.getConfigurationSection(name);
            if (volumeSection == null) continue;
            
            String worldName = volumeSection.getString("world");
            double minX = volumeSection.getDouble("minX");
            double minY = volumeSection.getDouble("minY");
            double minZ = volumeSection.getDouble("minZ");
            double maxX = volumeSection.getDouble("maxX");
            double maxY = volumeSection.getDouble("maxY");
            double maxZ = volumeSection.getDouble("maxZ");
            
            TriggerVolume volume = new TriggerVolume(name, worldName, minX, minY, minZ, maxX, maxY, maxZ);
            
            // Load enter actions
            ConfigurationSection enterActionsSection = volumeSection.getConfigurationSection("enterActions");
            if (enterActionsSection != null) {
                for (String actionKey : enterActionsSection.getKeys(false)) {
                    ConfigurationSection actionSection = enterActionsSection.getConfigurationSection(actionKey);
                    if (actionSection == null) continue;
                    
                    String typeStr = actionSection.getString("type");
                    String value = actionSection.getString("value");
                    
                    try {
                        ActionType type = ActionType.valueOf(typeStr);
                        volume.addEnterAction(new TriggerAction(type, value));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid action type: " + typeStr);
                    }
                }
            }
            
            // Load leave actions
            ConfigurationSection leaveActionsSection = volumeSection.getConfigurationSection("leaveActions");
            if (leaveActionsSection != null) {
                for (String actionKey : leaveActionsSection.getKeys(false)) {
                    ConfigurationSection actionSection = leaveActionsSection.getConfigurationSection(actionKey);
                    if (actionSection == null) continue;
                    
                    String typeStr = actionSection.getString("type");
                    String value = actionSection.getString("value");
                    
                    try {
                        ActionType type = ActionType.valueOf(typeStr);
                        volume.addLeaveAction(new TriggerAction(type, value));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid action type: " + typeStr);
                    }
                }
            }
            
            // Legacy support: Load old "actions" section as enter actions
            ConfigurationSection actionsSection = volumeSection.getConfigurationSection("actions");
            if (actionsSection != null && enterActionsSection == null) {
                for (String actionKey : actionsSection.getKeys(false)) {
                    ConfigurationSection actionSection = actionsSection.getConfigurationSection(actionKey);
                    if (actionSection == null) continue;
                    
                    String typeStr = actionSection.getString("type");
                    String value = actionSection.getString("value");
                    
                    try {
                        ActionType type = ActionType.valueOf(typeStr);
                        volume.addEnterAction(new TriggerAction(type, value));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid action type: " + typeStr);
                    }
                }
            }
            
            volumes.put(name.toLowerCase(), volume);
        }
        
        // Load groups
        ConfigurationSection groupsSection = volumesConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                List<String> volumeNames = groupsSection.getStringList(groupName);
                if (volumeNames != null && !volumeNames.isEmpty()) {
                    groups.put(groupName.toLowerCase(), new VolumeGroup(groupName, volumeNames));
                }
            }
        }
        
        // Rebuild spatial hash after loading
        rebuildSpatialHash();
        
        plugin.getLogger().info("Loaded " + volumes.size() + " trigger volumes and " + groups.size() + " groups.");
    }

    /**
     * Saves all volumes to the configuration file.
     */
    public void saveVolumes() {
        volumesConfig = new YamlConfiguration();
        
        ConfigurationSection volumesSection = volumesConfig.createSection("volumes");
        
        for (TriggerVolume volume : volumes.values()) {
            ConfigurationSection volumeSection = volumesSection.createSection(volume.getName());
            
            volumeSection.set("world", volume.getWorldName());
            volumeSection.set("minX", volume.getMinX());
            volumeSection.set("minY", volume.getMinY());
            volumeSection.set("minZ", volume.getMinZ());
            volumeSection.set("maxX", volume.getMaxX());
            volumeSection.set("maxY", volume.getMaxY());
            volumeSection.set("maxZ", volume.getMaxZ());
            
            // Save enter actions
            ConfigurationSection enterActionsSection = volumeSection.createSection("enterActions");
            List<TriggerAction> enterActions = volume.getEnterActions();
            for (int i = 0; i < enterActions.size(); i++) {
                TriggerAction action = enterActions.get(i);
                ConfigurationSection actionSection = enterActionsSection.createSection(String.valueOf(i));
                actionSection.set("type", action.getType().name());
                actionSection.set("value", action.getValue());
            }
            
            // Save leave actions
            ConfigurationSection leaveActionsSection = volumeSection.createSection("leaveActions");
            List<TriggerAction> leaveActions = volume.getLeaveActions();
            for (int i = 0; i < leaveActions.size(); i++) {
                TriggerAction action = leaveActions.get(i);
                ConfigurationSection actionSection = leaveActionsSection.createSection(String.valueOf(i));
                actionSection.set("type", action.getType().name());
                actionSection.set("value", action.getValue());
            }
        }
        
        // Save groups
        ConfigurationSection groupsSection = volumesConfig.createSection("groups");
        for (VolumeGroup group : groups.values()) {
            groupsSection.set(group.getName(), group.getVolumeNames());
        }
        
        try {
            volumesConfig.save(volumesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save trigger volumes: " + e.getMessage());
        }
    }

    /**
     * Creates a new trigger volume.
     * 
     * @param name The unique name for the volume
     * @param worldName The world name
     * @param x1 First X coordinate
     * @param y1 First Y coordinate
     * @param z1 First Z coordinate
     * @param x2 Second X coordinate
     * @param y2 Second Y coordinate
     * @param z2 Second Z coordinate
     * @return True if created successfully
     */
    public boolean createVolume(String name, String worldName, double x1, double y1, double z1,
                                double x2, double y2, double z2) {
        String key = name.toLowerCase();
        if (volumes.containsKey(key)) {
            return false;
        }
        
        TriggerVolume volume = new TriggerVolume(name, worldName, x1, y1, z1, x2, y2, z2);
        volumes.put(key, volume);
        rebuildSpatialHash(); // Update spatial hash
        saveVolumes();
        return true;
    }

    /**
     * Deletes a trigger volume.
     * 
     * @param name The name of the volume to delete
     * @return True if deleted successfully
     */
    public boolean deleteVolume(String name) {
        String key = name.toLowerCase();
        if (!volumes.containsKey(key)) {
            return false;
        }
        
        volumes.remove(key);
        rebuildSpatialHash(); // Update spatial hash
        saveVolumes();
        return true;
    }

    /**
     * Gets a trigger volume by name.
     * 
     * @param name The name of the volume
     * @return The TriggerVolume, or null if not found
     */
    public TriggerVolume getVolume(String name) {
        return volumes.get(name.toLowerCase());
    }

    /**
     * Checks if a volume with the given name exists.
     * 
     * @param name The name to check
     * @return True if the volume exists
     */
    public boolean volumeExists(String name) {
        return volumes.containsKey(name.toLowerCase());
    }

    /**
     * Gets all trigger volumes.
     * 
     * @return Collection of all volumes
     */
    public Collection<TriggerVolume> getAllVolumes() {
        return volumes.values();
    }

    /**
     * Gets all volume names.
     * 
     * @return Set of all volume names
     */
    public Set<String> getVolumeNames() {
        return volumes.keySet();
    }

    /**
     * Gets all volumes that contain the given location.
     * Uses spatial hashing for improved performance with many volumes.
     * 
     * @param location The location to check
     * @return List of volumes containing the location
     */
    public List<TriggerVolume> getVolumesAtLocation(Location location) {
        List<TriggerVolume> result = new ArrayList<>();
        
        if (location == null || location.getWorld() == null) {
            return result;
        }
        
        String worldName = location.getWorld().getName();
        Map<Long, List<TriggerVolume>> worldHash = spatialHash.get(worldName);
        
        if (worldHash == null) {
            return result;
        }
        
        // Get chunk hash for the location
        long chunkHash = getChunkHash(location);
        List<TriggerVolume> candidates = worldHash.get(chunkHash);
        
        if (candidates == null) {
            return result;
        }
        
        // Check only volumes in the same chunk
        for (TriggerVolume volume : candidates) {
            if (volume.contains(location)) {
                result.add(volume);
            }
        }
        
        return result;
    }
    
    /**
     * Calculates a hash for the chunk containing the location.
     * 
     * @param location The location
     * @return The chunk hash
     */
    private long getChunkHash(Location location) {
        int chunkX = (int) Math.floor(location.getX() / CHUNK_SIZE);
        int chunkZ = (int) Math.floor(location.getZ() / CHUNK_SIZE);
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
    
    /**
     * Rebuilds the spatial hash for all volumes.
     * Called after loading volumes or when volumes change.
     */
    private void rebuildSpatialHash() {
        spatialHash.clear();
        
        for (TriggerVolume volume : volumes.values()) {
            String worldName = volume.getWorldName();
            Map<Long, List<TriggerVolume>> worldHash = spatialHash.computeIfAbsent(worldName, k -> new HashMap<>());
            
            // Calculate all chunks the volume spans
            int minChunkX = (int) Math.floor(volume.getMinX() / CHUNK_SIZE);
            int maxChunkX = (int) Math.floor((volume.getMaxX() + 1) / CHUNK_SIZE);
            int minChunkZ = (int) Math.floor(volume.getMinZ() / CHUNK_SIZE);
            int maxChunkZ = (int) Math.floor((volume.getMaxZ() + 1) / CHUNK_SIZE);
            
            // Add volume to all chunks it spans
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long chunkHash = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
                    worldHash.computeIfAbsent(chunkHash, k -> new ArrayList<>()).add(volume);
                }
            }
        }
    }

    /**
     * Adds an enter action to a volume.
     * 
     * @param volumeName The name of the volume
     * @param action The action to add
     * @return True if added successfully
     */
    public boolean addEnterAction(String volumeName, TriggerAction action) {
        TriggerVolume volume = getVolume(volumeName);
        if (volume == null) {
            return false;
        }
        
        volume.addEnterAction(action);
        saveVolumes();
        return true;
    }

    /**
     * Adds a leave action to a volume.
     * 
     * @param volumeName The name of the volume
     * @param action The action to add
     * @return True if added successfully
     */
    public boolean addLeaveAction(String volumeName, TriggerAction action) {
        TriggerVolume volume = getVolume(volumeName);
        if (volume == null) {
            return false;
        }
        
        volume.addLeaveAction(action);
        saveVolumes();
        return true;
    }

    /**
     * Adds an action to a volume (legacy - adds to enter actions).
     * 
     * @param volumeName The name of the volume
     * @param action The action to add
     * @return True if added successfully
     * @deprecated Use {@link #addEnterAction(String, TriggerAction)} or {@link #addLeaveAction(String, TriggerAction)} instead
     */
    @Deprecated
    public boolean addAction(String volumeName, TriggerAction action) {
        return addEnterAction(volumeName, action);
    }

    /**
     * Clears all enter actions from a volume.
     * 
     * @param volumeName The name of the volume
     * @return True if cleared successfully
     */
    public boolean clearEnterActions(String volumeName) {
        TriggerVolume volume = getVolume(volumeName);
        if (volume == null) {
            return false;
        }
        
        volume.clearEnterActions();
        saveVolumes();
        return true;
    }

    /**
     * Clears all leave actions from a volume.
     * 
     * @param volumeName The name of the volume
     * @return True if cleared successfully
     */
    public boolean clearLeaveActions(String volumeName) {
        TriggerVolume volume = getVolume(volumeName);
        if (volume == null) {
            return false;
        }
        
        volume.clearLeaveActions();
        saveVolumes();
        return true;
    }

    /**
     * Clears all actions (both enter and leave) from a volume.
     * 
     * @param volumeName The name of the volume
     * @return True if cleared successfully
     */
    public boolean clearAllActions(String volumeName) {
        TriggerVolume volume = getVolume(volumeName);
        if (volume == null) {
            return false;
        }
        
        volume.clearAllActions();
        saveVolumes();
        return true;
    }

    /**
     * Clears all actions from a volume (legacy - clears enter actions).
     * 
     * @param volumeName The name of the volume
     * @return True if cleared successfully
     * @deprecated Use {@link #clearEnterActions(String)}, {@link #clearLeaveActions(String)}, or {@link #clearAllActions(String)} instead
     */
    @Deprecated
    public boolean clearActions(String volumeName) {
        return clearAllActions(volumeName);
    }

    // ========== Group Management Methods ==========

    /**
     * Creates a new volume group.
     * 
     * @param groupName The name of the group
     * @param volumeNames List of volume names to include (minimum 2)
     * @return True if created successfully, false if group exists or less than 2 volumes
     */
    public boolean createGroup(String groupName, List<String> volumeNames) {
        String key = groupName.toLowerCase();
        if (groups.containsKey(key)) {
            return false;
        }
        
        if (volumeNames.size() < 2) {
            return false;
        }
        
        // Verify all volumes exist
        for (String volumeName : volumeNames) {
            if (!volumeExists(volumeName)) {
                return false;
            }
        }
        
        groups.put(key, new VolumeGroup(groupName, volumeNames));
        saveVolumes();
        return true;
    }

    /**
     * Deletes a volume group (does not delete the volumes).
     * 
     * @param groupName The name of the group to delete
     * @return True if deleted successfully
     */
    public boolean deleteGroup(String groupName) {
        String key = groupName.toLowerCase();
        if (!groups.containsKey(key)) {
            return false;
        }
        
        groups.remove(key);
        saveVolumes();
        return true;
    }

    /**
     * Gets a volume group by name.
     * 
     * @param groupName The name of the group
     * @return The VolumeGroup, or null if not found
     */
    public VolumeGroup getGroup(String groupName) {
        return groups.get(groupName.toLowerCase());
    }

    /**
     * Checks if a group exists.
     * 
     * @param groupName The name to check
     * @return True if the group exists
     */
    public boolean groupExists(String groupName) {
        return groups.containsKey(groupName.toLowerCase());
    }

    /**
     * Adds a volume to an existing group.
     * 
     * @param groupName The name of the group
     * @param volumeName The name of the volume to add
     * @return True if added successfully, false if group doesn't exist, volume doesn't exist, or volume already in group
     */
    public boolean addVolumeToGroup(String groupName, String volumeName) {
        String key = groupName.toLowerCase();
        if (!groups.containsKey(key)) {
            return false;
        }
        
        if (!volumeExists(volumeName)) {
            return false;
        }
        
        VolumeGroup group = groups.get(key);
        if (group.containsVolume(volumeName)) {
            return false; // Already in group
        }
        
        group.addVolume(volumeName);
        saveVolumes();
        return true;
    }

    /**
     * Removes a volume from a group.
     * 
     * @param groupName The name of the group
     * @param volumeName The name of the volume to remove
     * @return True if removed successfully, false if group doesn't exist or volume not in group
     */
    public boolean removeVolumeFromGroup(String groupName, String volumeName) {
        String key = groupName.toLowerCase();
        if (!groups.containsKey(key)) {
            return false;
        }
        
        VolumeGroup group = groups.get(key);
        if (!group.containsVolume(volumeName)) {
            return false; // Not in group
        }
        
        group.removeVolume(volumeName);
        
        // Delete group if it has less than 2 volumes
        if (group.getVolumeNames().size() < 2) {
            groups.remove(key);
        }
        
        saveVolumes();
        return true;
    }

    /**
     * Gets all group names.
     * 
     * @return Set of all group names
     */
    public Set<String> getGroupNames() {
        return groups.keySet();
    }

    /**
     * Checks if a name is a volume or a group.
     * 
     * @param name The name to check
     * @return True if it's a volume, false if it's a group or doesn't exist
     */
    public boolean isVolume(String name) {
        return volumeExists(name);
    }

    /**
     * Clones all actions from one volume to another.
     * 
     * @param sourceVolumeName The source volume to copy from
     * @param targetVolumeName The target volume to copy to
     * @return True if cloned successfully
     */
    public boolean cloneActions(String sourceVolumeName, String targetVolumeName) {
        TriggerVolume source = getVolume(sourceVolumeName);
        TriggerVolume target = getVolume(targetVolumeName);
        
        if (source == null || target == null) {
            return false;
        }
        
        // Clear existing actions
        target.clearAllActions();
        
        // Copy enter actions
        for (TriggerAction action : source.getEnterActions()) {
            target.addEnterAction(new TriggerAction(action.getType(), action.getValue()));
        }
        
        // Copy leave actions
        for (TriggerAction action : source.getLeaveActions()) {
            target.addLeaveAction(new TriggerAction(action.getType(), action.getValue()));
        }
        
        saveVolumes();
        return true;
    }

    /**
     * Gets all groups that contain a specific volume.
     * 
     * @param volumeName The volume to check
     * @return List of group names containing this volume
     */
    public List<String> getGroupsForVolume(String volumeName) {
        List<String> groupNames = new ArrayList<>();
        
        for (Map.Entry<String, VolumeGroup> entry : groups.entrySet()) {
            if (entry.getValue().containsVolume(volumeName)) {
                groupNames.add(entry.getValue().getName());
            }
        }
        
        return groupNames;
    }
}
