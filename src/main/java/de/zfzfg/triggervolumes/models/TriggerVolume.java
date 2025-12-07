package de.zfzfg.triggervolumes.models;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a 3D trigger volume in the world.
 * A trigger volume is a cuboid area that can execute actions when players enter or leave it.
 * 
 * @author zfzfg
 */
public class TriggerVolume {

    private final String name;
    private final String worldName;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;
    private final List<TriggerAction> enterActions;
    private final List<TriggerAction> leaveActions;
    
    // Keep legacy actions list for backwards compatibility
    @Deprecated
    private final List<TriggerAction> actions;

    /**
     * Creates a new TriggerVolume.
     * 
     * @param name The unique name of the volume
     * @param worldName The name of the world this volume is in
     * @param x1 First X coordinate
     * @param y1 First Y coordinate
     * @param z1 First Z coordinate
     * @param x2 Second X coordinate
     * @param y2 Second Y coordinate
     * @param z2 Second Z coordinate
     */
    public TriggerVolume(String name, String worldName, double x1, double y1, double z1, 
                         double x2, double y2, double z2) {
        this.name = name;
        this.worldName = worldName;
        // Ensure min/max are correctly sorted
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.enterActions = new ArrayList<>();
        this.leaveActions = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    /**
     * Gets the name of this trigger volume.
     * 
     * @return The volume name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the world name this volume is in.
     * 
     * @return The world name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Gets the minimum X coordinate.
     * 
     * @return The minimum X coordinate
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Gets the minimum Y coordinate.
     * 
     * @return The minimum Y coordinate
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Gets the minimum Z coordinate.
     * 
     * @return The minimum Z coordinate
     */
    public double getMinZ() {
        return minZ;
    }

    /**
     * Gets the maximum X coordinate.
     * 
     * @return The maximum X coordinate
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Gets the maximum Y coordinate.
     * 
     * @return The maximum Y coordinate
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Gets the maximum Z coordinate.
     * 
     * @return The maximum Z coordinate
     */
    public double getMaxZ() {
        return maxZ;
    }

    /**
     * Gets all actions associated with this volume (legacy - returns enter actions).
     * 
     * @return List of trigger actions
     * @deprecated Use {@link #getEnterActions()} or {@link #getLeaveActions()} instead
     */
    @Deprecated
    public List<TriggerAction> getActions() {
        return enterActions;
    }

    /**
     * Gets all enter actions associated with this volume.
     * 
     * @return List of enter trigger actions
     */
    public List<TriggerAction> getEnterActions() {
        return enterActions;
    }

    /**
     * Gets all leave actions associated with this volume.
     * 
     * @return List of leave trigger actions
     */
    public List<TriggerAction> getLeaveActions() {
        return leaveActions;
    }

    /**
     * Adds an action to this volume (legacy - adds to enter actions).
     * 
     * @param action The action to add
     * @deprecated Use {@link #addEnterAction(TriggerAction)} or {@link #addLeaveAction(TriggerAction)} instead
     */
    @Deprecated
    public void addAction(TriggerAction action) {
        enterActions.add(action);
    }

    /**
     * Adds an enter action to this volume.
     * 
     * @param action The action to add
     */
    public void addEnterAction(TriggerAction action) {
        enterActions.add(action);
    }

    /**
     * Adds a leave action to this volume.
     * 
     * @param action The action to add
     */
    public void addLeaveAction(TriggerAction action) {
        leaveActions.add(action);
    }

    /**
     * Clears all actions from this volume (legacy - clears enter actions).
     * 
     * @deprecated Use {@link #clearEnterActions()} or {@link #clearLeaveActions()} instead
     */
    @Deprecated
    public void clearActions() {
        enterActions.clear();
    }

    /**
     * Clears all enter actions from this volume.
     */
    public void clearEnterActions() {
        enterActions.clear();
    }

    /**
     * Clears all leave actions from this volume.
     */
    public void clearLeaveActions() {
        leaveActions.clear();
    }

    /**
     * Clears all actions (both enter and leave) from this volume.
     */
    public void clearAllActions() {
        enterActions.clear();
        leaveActions.clear();
    }

    /**
     * Checks if a location is inside this trigger volume.
     * Uses block coordinates to match the visual representation.
     * A block extends from X.0 to X.999, so we add 1 to max coordinates.
     * 
     * @param location The location to check
     * @return True if the location is inside the volume
     */
    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        // Add 1 to max coordinates to match block boundaries
        // This ensures the trigger area matches the particle visualization
        return x >= minX && x < maxX + 1 &&
               y >= minY && y < maxY + 1 &&
               z >= minZ && z < maxZ + 1;
    }

    /**
     * Gets the center location of this volume.
     * 
     * @param world The world to create the location in
     * @return The center location
     */
    public Location getCenter(World world) {
        return new Location(world,
                (minX + maxX) / 2,
                (minY + maxY) / 2,
                (minZ + maxZ) / 2);
    }

    /**
     * Gets the volume size in blocks.
     * 
     * @return The volume size
     */
    public double getVolume() {
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }

    @Override
    public String toString() {
        return "TriggerVolume{" +
                "name='" + name + '\'' +
                ", world='" + worldName + '\'' +
                ", min=(" + minX + ", " + minY + ", " + minZ + ")" +
                ", max=(" + maxX + ", " + maxY + ", " + maxZ + ")" +
                ", enterActions=" + enterActions.size() +
                ", leaveActions=" + leaveActions.size() +
                '}';
    }
}
