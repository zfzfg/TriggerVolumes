package de.zfzfg.triggervolumes;

import de.zfzfg.triggervolumes.commands.TriggerCommand;
import de.zfzfg.triggervolumes.commands.TriggerTabCompleter;
import de.zfzfg.triggervolumes.listeners.PlayerInteractListener;
import de.zfzfg.triggervolumes.listeners.PlayerMoveListener;
import de.zfzfg.triggervolumes.listeners.SelectionToolListener;
import de.zfzfg.triggervolumes.managers.LanguageManager;
import de.zfzfg.triggervolumes.managers.ParticleManager;
import de.zfzfg.triggervolumes.managers.SelectionManager;
import de.zfzfg.triggervolumes.managers.TriggerVolumeManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for TriggerVolumes.
 * This plugin allows administrators to define 3D trigger volumes
 * with automated actions when players enter them.
 * 
 * @author zfzfg
 * @version 1.0.0
 */
public class TriggerVolumesPlugin extends JavaPlugin {

    private static TriggerVolumesPlugin instance;
    
    private TriggerVolumeManager volumeManager;
    private SelectionManager selectionManager;
    private ParticleManager particleManager;
    private LanguageManager languageManager;

    /**
     * Called when the plugin is enabled.
     * Initializes all managers, listeners, and commands.
     */
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        languageManager = new LanguageManager(this);
        languageManager.load();
        
        volumeManager = new TriggerVolumeManager(this);
        selectionManager = new SelectionManager(this);
        particleManager = new ParticleManager(this);
        
        // Load volumes from storage
        volumeManager.loadVolumes();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new SelectionToolListener(this), this);
        
        // Register commands
        TriggerCommand triggerCommand = new TriggerCommand(this);
        getCommand("trigger").setExecutor(triggerCommand);
        getCommand("trigger").setTabCompleter(new TriggerTabCompleter(this));
        
        getLogger().info("TriggerVolumes has been enabled!");
    }

    /**
     * Called when the plugin is disabled.
     * Saves all data and cleans up resources.
     */
    @Override
    public void onDisable() {
        // Save volumes to storage
        if (volumeManager != null) {
            volumeManager.saveVolumes();
        }
        
        // Stop particle tasks
        if (particleManager != null) {
            particleManager.stopAllTasks();
        }
        
        getLogger().info("TriggerVolumes has been disabled!");
    }

    /**
     * Gets the plugin instance.
     * 
     * @return The plugin instance
     */
    public static TriggerVolumesPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the TriggerVolume manager.
     * 
     * @return The TriggerVolumeManager instance
     */
    public TriggerVolumeManager getVolumeManager() {
        return volumeManager;
    }

    /**
     * Gets the Selection manager.
     * 
     * @return The SelectionManager instance
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Gets the Particle manager.
     * 
     * @return The ParticleManager instance
     */
    public ParticleManager getParticleManager() {
        return particleManager;
    }

    /**
     * Gets the Language manager.
     * 
     * @return The LanguageManager instance
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
