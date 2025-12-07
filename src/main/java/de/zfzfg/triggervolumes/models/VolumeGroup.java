package de.zfzfg.triggervolumes.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of trigger volumes that can be managed together.
 * Groups allow applying actions to multiple volumes at once.
 * 
 * @author zfzfg
 */
public class VolumeGroup {

    private final String name;
    private final List<String> volumeNames;

    /**
     * Creates a new VolumeGroup.
     * 
     * @param name The unique name of the group
     * @param volumeNames List of volume names in this group
     */
    public VolumeGroup(String name, List<String> volumeNames) {
        this.name = name;
        this.volumeNames = new ArrayList<>(volumeNames);
    }

    /**
     * Gets the name of this group.
     * 
     * @return The group name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all volume names in this group.
     * 
     * @return List of volume names
     */
    public List<String> getVolumeNames() {
        return new ArrayList<>(volumeNames);
    }

    /**
     * Checks if a volume is in this group.
     * 
     * @param volumeName The volume name to check
     * @return True if the volume is in this group
     */
    public boolean containsVolume(String volumeName) {
        return volumeNames.stream()
                .anyMatch(name -> name.equalsIgnoreCase(volumeName));
    }

    /**
     * Gets the number of volumes in this group.
     * 
     * @return The volume count
     */
    public int size() {
        return volumeNames.size();
    }

    /**
     * Adds a volume to this group.
     * 
     * @param volumeName The volume name to add
     * @return True if the volume was added, false if it already exists
     */
    public boolean addVolume(String volumeName) {
        if (containsVolume(volumeName)) {
            return false;
        }
        volumeNames.add(volumeName);
        return true;
    }

    /**
     * Removes a volume from this group.
     * 
     * @param volumeName The volume name to remove
     * @return True if the volume was removed
     */
    public boolean removeVolume(String volumeName) {
        return volumeNames.removeIf(name -> name.equalsIgnoreCase(volumeName));
    }

    @Override
    public String toString() {
        return "VolumeGroup{" +
                "name='" + name + '\'' +
                ", volumes=" + volumeNames.size() +
                '}';
    }
}
