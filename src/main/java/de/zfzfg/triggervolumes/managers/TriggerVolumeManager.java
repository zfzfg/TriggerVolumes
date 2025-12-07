package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.ActionType;
import de.zfzfg.triggervolumes.models.TriggerAction;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all trigger volumes including loading, saving, and CRUD operations.
 * 
 * @author zfzfg
 */
public class TriggerVolumeManager {

    private final TriggerVolumesPlugin plugin;
    private final Map<String, TriggerVolume> volumes;
    private final File volumesFile;
    private FileConfiguration volumesConfig;

    /**
     * Creates a new TriggerVolumeManager.
     * 
     * @param plugin The plugin instance
     */
    public TriggerVolumeManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.volumes = new HashMap<>();
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
        
        plugin.getLogger().info("Loaded " + volumes.size() + " trigger volumes.");
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
     * 
     * @param location The location to check
     * @return List of volumes containing the location
     */
    public List<TriggerVolume> getVolumesAtLocation(Location location) {
        List<TriggerVolume> result = new ArrayList<>();
        for (TriggerVolume volume : volumes.values()) {
            if (volume.contains(location)) {
                result.add(volume);
            }
        }
        return result;
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
}
