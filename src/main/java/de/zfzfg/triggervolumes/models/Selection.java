package de.zfzfg.triggervolumes.models;

import org.bukkit.Location;

/**
 * Represents a player's selection of two points for creating a trigger volume.
 * This is a temporary selection that exists only while the player holds the selection tool.
 * 
 * @author zfzfg
 */
public class Selection {

    private Location pos1;
    private Location pos2;

    /**
     * Creates a new empty Selection.
     */
    public Selection() {
        this.pos1 = null;
        this.pos2 = null;
    }

    /**
     * Gets the first position.
     * 
     * @return The first position, or null if not set
     */
    public Location getPos1() {
        return pos1;
    }

    /**
     * Sets the first position.
     * 
     * @param pos1 The first position
     */
    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    /**
     * Gets the second position.
     * 
     * @return The second position, or null if not set
     */
    public Location getPos2() {
        return pos2;
    }

    /**
     * Sets the second position.
     * 
     * @param pos2 The second position
     */
    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    /**
     * Checks if both positions are set.
     * 
     * @return True if both positions are set
     */
    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    /**
     * Checks if both positions are in the same world.
     * 
     * @return True if both positions are in the same world
     */
    public boolean isSameWorld() {
        if (!isComplete()) {
            return false;
        }
        return pos1.getWorld().equals(pos2.getWorld());
    }

    /**
     * Clears the selection.
     */
    public void clear() {
        pos1 = null;
        pos2 = null;
    }

    /**
     * Gets the minimum X coordinate.
     * 
     * @return The minimum X coordinate
     */
    public double getMinX() {
        return Math.min(pos1.getX(), pos2.getX());
    }

    /**
     * Gets the minimum Y coordinate.
     * 
     * @return The minimum Y coordinate
     */
    public double getMinY() {
        return Math.min(pos1.getY(), pos2.getY());
    }

    /**
     * Gets the minimum Z coordinate.
     * 
     * @return The minimum Z coordinate
     */
    public double getMinZ() {
        return Math.min(pos1.getZ(), pos2.getZ());
    }

    /**
     * Gets the maximum X coordinate.
     * 
     * @return The maximum X coordinate
     */
    public double getMaxX() {
        return Math.max(pos1.getX(), pos2.getX());
    }

    /**
     * Gets the maximum Y coordinate.
     * 
     * @return The maximum Y coordinate
     */
    public double getMaxY() {
        return Math.max(pos1.getY(), pos2.getY());
    }

    /**
     * Gets the maximum Z coordinate.
     * 
     * @return The maximum Z coordinate
     */
    public double getMaxZ() {
        return Math.max(pos1.getZ(), pos2.getZ());
    }

    /**
     * Gets the world name of the selection.
     * 
     * @return The world name
     */
    public String getWorldName() {
        if (pos1 != null && pos1.getWorld() != null) {
            return pos1.getWorld().getName();
        }
        if (pos2 != null && pos2.getWorld() != null) {
            return pos2.getWorld().getName();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Selection{" +
                "pos1=" + (pos1 != null ? pos1.toVector() : "null") +
                ", pos2=" + (pos2 != null ? pos2.toVector() : "null") +
                '}';
    }
}
