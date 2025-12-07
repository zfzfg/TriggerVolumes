package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player selections for creating trigger volumes.
 * Selections are temporary and only exist while the player holds the selection tool.
 * 
 * @author zfzfg
 */
public class SelectionManager {

    private final TriggerVolumesPlugin plugin;
    private final Map<UUID, Selection> selections;
    private final String toolName;
    private final Material toolMaterial;

    /**
     * Creates a new SelectionManager.
     * 
     * @param plugin The plugin instance
     */
    public SelectionManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.selections = new HashMap<>();
        
        // Load tool settings from config
        String materialName = plugin.getConfig().getString("selection-tool.material", "WOODEN_HOE");
        this.toolMaterial = Material.valueOf(materialName);
        this.toolName = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("selection-tool.name", "&6Selection Tool"));
    }

    /**
     * Gets or creates a selection for a player.
     * 
     * @param player The player
     * @return The player's selection
     */
    public Selection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    /**
     * Checks if a player has a selection.
     * 
     * @param player The player
     * @return True if the player has a selection
     */
    public boolean hasSelection(Player player) {
        return selections.containsKey(player.getUniqueId());
    }

    /**
     * Clears a player's selection.
     * 
     * @param player The player
     */
    public void clearSelection(Player player) {
        Selection selection = selections.remove(player.getUniqueId());
        if (selection != null) {
            // Stop particle visualization for this selection
            plugin.getParticleManager().stopSelectionParticles(player);
        }
    }

    /**
     * Creates and gives the selection tool to a player.
     * 
     * @param player The player to give the tool to
     */
    public void giveSelectionTool(Player player) {
        ItemStack tool = createSelectionTool();
        player.getInventory().addItem(tool);
    }

    /**
     * Creates a new selection tool item.
     * 
     * @return The selection tool ItemStack
     */
    public ItemStack createSelectionTool() {
        ItemStack tool = new ItemStack(toolMaterial);
        ItemMeta meta = tool.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(toolName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: Set Position 1");
            lore.add(ChatColor.GRAY + "Right-click: Set Position 2");
            lore.add("");
            lore.add(ChatColor.YELLOW + "TriggerVolumes Selection Tool");
            meta.setLore(lore);
            
            tool.setItemMeta(meta);
        }
        
        return tool;
    }

    /**
     * Checks if an item is the selection tool.
     * 
     * @param item The item to check
     * @return True if the item is a selection tool
     */
    public boolean isSelectionTool(ItemStack item) {
        if (item == null || item.getType() != toolMaterial) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(toolName);
    }

    /**
     * Checks if a player is holding the selection tool.
     * 
     * @param player The player to check
     * @return True if the player is holding the selection tool
     */
    public boolean isHoldingSelectionTool(Player player) {
        return isSelectionTool(player.getInventory().getItemInMainHand());
    }

    /**
     * Gets the selection tool material.
     * 
     * @return The tool material
     */
    public Material getToolMaterial() {
        return toolMaterial;
    }

    /**
     * Gets the selection tool display name.
     * 
     * @return The tool display name
     */
    public String getToolName() {
        return toolName;
    }
}
