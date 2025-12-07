package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.Selection;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages particle effects for visualizing selections and trigger volumes.
 * 
 * @author zfzfg
 */
public class ParticleManager {

    private final TriggerVolumesPlugin plugin;
    private final Map<UUID, BukkitTask> selectionParticleTasks;
    private final Map<String, BukkitTask> volumeParticleTasks;
    private final Map<String, Long> volumeVisualizationStart;
    
    private final int updateInterval;
    private final double density;
    private final int visualizationDuration;
    
    // Colors for volume visualization
    private static final Color[] VOLUME_COLORS = {
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.PURPLE,
        Color.AQUA
    };

    /**
     * Creates a new ParticleManager.
     * 
     * @param plugin The plugin instance
     */
    public ParticleManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.selectionParticleTasks = new HashMap<>();
        this.volumeParticleTasks = new HashMap<>();
        this.volumeVisualizationStart = new HashMap<>();
        
        // Load settings from config
        this.updateInterval = plugin.getConfig().getInt("particles.update-interval", 5);
        this.density = plugin.getConfig().getDouble("particles.density", 0.5);
        this.visualizationDuration = plugin.getConfig().getInt("particles.visualization-duration", 30);
    }

    /**
     * Starts showing selection particles for a player.
     * 
     * @param player The player to show particles to
     */
    public void startSelectionParticles(Player player) {
        stopSelectionParticles(player);
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    selectionParticleTasks.remove(player.getUniqueId());
                    return;
                }
                
                Selection selection = plugin.getSelectionManager().getSelection(player);
                if (selection == null || !selection.isComplete()) {
                    return;
                }
                
                if (!plugin.getSelectionManager().isHoldingSelectionTool(player)) {
                    return;
                }
                
                World world = player.getWorld();
                if (!world.getName().equals(selection.getWorldName())) {
                    return;
                }
                
                drawSelectionBox(player, selection, Color.WHITE);
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
        
        selectionParticleTasks.put(player.getUniqueId(), task);
    }

    /**
     * Stops showing selection particles for a player.
     * 
     * @param player The player to stop particles for
     */
    public void stopSelectionParticles(Player player) {
        BukkitTask task = selectionParticleTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Starts visualizing a trigger volume.
     * 
     * @param volumeName The name of the volume to visualize
     * @param player The player to show particles to
     */
    public void startVolumeVisualization(String volumeName, Player player) {
        TriggerVolume volume = plugin.getVolumeManager().getVolume(volumeName);
        if (volume == null) {
            return;
        }
        
        String key = volumeName.toLowerCase();
        stopVolumeVisualization(volumeName);
        
        // Get color based on volume index
        int colorIndex = getVolumeColorIndex(volumeName);
        Color color = VOLUME_COLORS[colorIndex % VOLUME_COLORS.length];
        
        volumeVisualizationStart.put(key, System.currentTimeMillis());
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Check for timeout
                long startTime = volumeVisualizationStart.getOrDefault(key, System.currentTimeMillis());
                if (System.currentTimeMillis() - startTime > visualizationDuration * 1000L) {
                    stopVolumeVisualization(volumeName);
                    return;
                }
                
                World world = plugin.getServer().getWorld(volume.getWorldName());
                if (world == null) {
                    return;
                }
                
                // Show to all players in the world within range
                for (Player p : world.getPlayers()) {
                    Location center = volume.getCenter(world);
                    if (p.getLocation().distance(center) < 100) {
                        drawVolumeBox(p, volume, color);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
        
        volumeParticleTasks.put(key, task);
    }

    /**
     * Stops visualizing a trigger volume.
     * 
     * @param volumeName The name of the volume
     */
    public void stopVolumeVisualization(String volumeName) {
        String key = volumeName.toLowerCase();
        BukkitTask task = volumeParticleTasks.remove(key);
        if (task != null) {
            task.cancel();
        }
        volumeVisualizationStart.remove(key);
    }

    /**
     * Checks if a volume is being visualized.
     * 
     * @param volumeName The name of the volume
     * @return True if the volume is being visualized
     */
    public boolean isVisualizingVolume(String volumeName) {
        return volumeParticleTasks.containsKey(volumeName.toLowerCase());
    }

    /**
     * Stops all particle tasks.
     */
    public void stopAllTasks() {
        for (BukkitTask task : selectionParticleTasks.values()) {
            task.cancel();
        }
        selectionParticleTasks.clear();
        
        for (BukkitTask task : volumeParticleTasks.values()) {
            task.cancel();
        }
        volumeParticleTasks.clear();
        volumeVisualizationStart.clear();
    }

    /**
     * Draws a selection box with particles.
     * 
     * @param player The player to show particles to
     * @param selection The selection to draw
     * @param color The particle color
     */
    private void drawSelectionBox(Player player, Selection selection, Color color) {
        double minX = selection.getMinX();
        double minY = selection.getMinY();
        double minZ = selection.getMinZ();
        double maxX = selection.getMaxX() + 1;
        double maxY = selection.getMaxY() + 1;
        double maxZ = selection.getMaxZ() + 1;
        
        World world = player.getWorld();
        
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
        
        // Draw all 12 edges of the box
        drawLine(player, world, minX, minY, minZ, maxX, minY, minZ, dust);
        drawLine(player, world, minX, minY, minZ, minX, maxY, minZ, dust);
        drawLine(player, world, minX, minY, minZ, minX, minY, maxZ, dust);
        
        drawLine(player, world, maxX, maxY, maxZ, minX, maxY, maxZ, dust);
        drawLine(player, world, maxX, maxY, maxZ, maxX, minY, maxZ, dust);
        drawLine(player, world, maxX, maxY, maxZ, maxX, maxY, minZ, dust);
        
        drawLine(player, world, minX, maxY, minZ, maxX, maxY, minZ, dust);
        drawLine(player, world, minX, maxY, minZ, minX, maxY, maxZ, dust);
        
        drawLine(player, world, maxX, minY, minZ, maxX, maxY, minZ, dust);
        drawLine(player, world, maxX, minY, minZ, maxX, minY, maxZ, dust);
        
        drawLine(player, world, minX, minY, maxZ, maxX, minY, maxZ, dust);
        drawLine(player, world, minX, minY, maxZ, minX, maxY, maxZ, dust);
    }

    /**
     * Draws a trigger volume box with particles.
     * 
     * @param player The player to show particles to
     * @param volume The volume to draw
     * @param color The particle color
     */
    private void drawVolumeBox(Player player, TriggerVolume volume, Color color) {
        double minX = volume.getMinX();
        double minY = volume.getMinY();
        double minZ = volume.getMinZ();
        double maxX = volume.getMaxX() + 1;
        double maxY = volume.getMaxY() + 1;
        double maxZ = volume.getMaxZ() + 1;
        
        World world = player.getWorld();
        
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
        
        // Draw all 12 edges of the box
        drawLine(player, world, minX, minY, minZ, maxX, minY, minZ, dust);
        drawLine(player, world, minX, minY, minZ, minX, maxY, minZ, dust);
        drawLine(player, world, minX, minY, minZ, minX, minY, maxZ, dust);
        
        drawLine(player, world, maxX, maxY, maxZ, minX, maxY, maxZ, dust);
        drawLine(player, world, maxX, maxY, maxZ, maxX, minY, maxZ, dust);
        drawLine(player, world, maxX, maxY, maxZ, maxX, maxY, minZ, dust);
        
        drawLine(player, world, minX, maxY, minZ, maxX, maxY, minZ, dust);
        drawLine(player, world, minX, maxY, minZ, minX, maxY, maxZ, dust);
        
        drawLine(player, world, maxX, minY, minZ, maxX, maxY, minZ, dust);
        drawLine(player, world, maxX, minY, minZ, maxX, minY, maxZ, dust);
        
        drawLine(player, world, minX, minY, maxZ, maxX, minY, maxZ, dust);
        drawLine(player, world, minX, minY, maxZ, minX, maxY, maxZ, dust);
    }

    /**
     * Draws a line of particles between two points.
     * 
     * @param player The player to show particles to
     * @param world The world to draw in
     * @param x1 Start X
     * @param y1 Start Y
     * @param z1 Start Z
     * @param x2 End X
     * @param y2 End Y
     * @param z2 End Z
     * @param dust The dust options for the particle
     */
    private void drawLine(Player player, World world, double x1, double y1, double z1,
                          double x2, double y2, double z2, Particle.DustOptions dust) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        int particles = (int) (distance / density);
        
        if (particles == 0) particles = 1;
        
        double dx = (x2 - x1) / particles;
        double dy = (y2 - y1) / particles;
        double dz = (z2 - z1) / particles;
        
        for (int i = 0; i <= particles; i++) {
            Location loc = new Location(world, x1 + dx * i, y1 + dy * i, z1 + dz * i);
            player.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }

    /**
     * Gets a consistent color index for a volume based on its name.
     * 
     * @param volumeName The volume name
     * @return The color index
     */
    private int getVolumeColorIndex(String volumeName) {
        // Get all volume names sorted for consistent coloring
        List<String> names = new ArrayList<>(plugin.getVolumeManager().getVolumeNames());
        Collections.sort(names);
        return names.indexOf(volumeName.toLowerCase());
    }
}
