package de.zfzfg.triggervolumes.commands;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.ActionType;
import de.zfzfg.triggervolumes.models.Selection;
import de.zfzfg.triggervolumes.models.TriggerAction;
import de.zfzfg.triggervolumes.models.TriggerVolume;
import de.zfzfg.triggervolumes.models.VolumeGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Main command handler for the /trigger command and all its subcommands.
 * 
 * @author zfzfg
 */
public class TriggerCommand implements CommandExecutor {

    private final TriggerVolumesPlugin plugin;

    /**
     * Creates a new TriggerCommand.
     * 
     * @param plugin The plugin instance
     */
    public TriggerCommand(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the /trigger command and routes to appropriate subcommands.
     * 
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     * @return True if the command was handled
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "tool":
                return handleTool(sender);
            case "create":
                return handleCreate(sender, args);
            case "define":
                return handleDefine(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "setaction":
                return handleSetAction(sender, args);
            case "clearactions":
                return handleClearActions(sender, args);
            case "visualize":
            case "show":
                return handleVisualize(sender, args);
            case "hide":
                return handleHide(sender, args);
            case "clone":
                return handleClone(sender, args);
            case "copypaste":
                return handleCopyPaste(sender, args);
            case "creategroup":
                return handleCreateGroup(sender, args);
            case "deletegroup":
                return handleDeleteGroup(sender, args);
            case "groupadd":
                return handleGroupAdd(sender, args);
            case "groupremove":
                return handleGroupRemove(sender, args);
            case "reload":
                return handleReload(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Handles the /trigger tool command.
     * Gives the player a selection tool.
     * 
     * @param sender The command sender
     * @return True if successful
     */
    private boolean handleTool(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (!player.hasPermission("triggervolumes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        plugin.getSelectionManager().giveSelectionTool(player);
        player.sendMessage(getMessage("tool-given"));
        return true;
    }

    /**
     * Handles the /trigger create <name> command.
     * Creates a new trigger volume from the current selection.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (!player.hasPermission("triggervolumes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /trigger create <name>");
            return true;
        }

        String name = args[1];
        Selection selection = plugin.getSelectionManager().getSelection(player);

        if (!selection.isComplete()) {
            player.sendMessage(getMessage("no-selection"));
            return true;
        }

        if (!selection.isSameWorld()) {
            player.sendMessage(ChatColor.RED + "Both positions must be in the same world!");
            return true;
        }

        if (plugin.getVolumeManager().volumeExists(name)) {
            player.sendMessage(getMessage("volume-already-exists").replace("%name%", name));
            return true;
        }

        boolean success = plugin.getVolumeManager().createVolume(
                name,
                selection.getWorldName(),
                selection.getMinX(),
                selection.getMinY(),
                selection.getMinZ(),
                selection.getMaxX(),
                selection.getMaxY(),
                selection.getMaxZ()
        );

        if (success) {
            player.sendMessage(getMessage("volume-created").replace("%name%", name));
            // Don't clear selection - let the player keep it for visualization
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create volume!");
        }

        return true;
    }

    /**
     * Handles the /trigger define <name> <x1> <y1> <z1> <x2> <y2> <z2> command.
     * Creates a new trigger volume with specified coordinates.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleDefine(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (!player.hasPermission("triggervolumes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 8) {
            player.sendMessage(ChatColor.RED + "Usage: /trigger define <name> <x1> <y1> <z1> <x2> <y2> <z2>");
            return true;
        }

        String name = args[1];

        if (plugin.getVolumeManager().volumeExists(name)) {
            player.sendMessage(getMessage("volume-already-exists").replace("%name%", name));
            return true;
        }

        try {
            double x1 = Double.parseDouble(args[2]);
            double y1 = Double.parseDouble(args[3]);
            double z1 = Double.parseDouble(args[4]);
            double x2 = Double.parseDouble(args[5]);
            double y2 = Double.parseDouble(args[6]);
            double z2 = Double.parseDouble(args[7]);

            boolean success = plugin.getVolumeManager().createVolume(
                    name,
                    player.getWorld().getName(),
                    x1, y1, z1,
                    x2, y2, z2
            );

            if (success) {
                player.sendMessage(getMessage("volume-created").replace("%name%", name));
            } else {
                player.sendMessage(ChatColor.RED + "Failed to create volume!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates! Please enter valid numbers.");
        }

        return true;
    }

    /**
     * Handles the /trigger delete <name> command.
     * Deletes a trigger volume.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger delete <name>");
            return true;
        }

        String name = args[1];

        if (!plugin.getVolumeManager().volumeExists(name)) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        // Stop visualization if active
        plugin.getParticleManager().stopVolumeVisualization(name);

        boolean success = plugin.getVolumeManager().deleteVolume(name);

        if (success) {
            sender.sendMessage(getMessage("volume-deleted").replace("%name%", name));
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete volume!");
        }

        return true;
    }

    /**
     * Handles the /trigger list command.
     * Lists all trigger volumes.
     * 
     * @param sender The command sender
     * @return True if successful
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        Collection<TriggerVolume> volumes = plugin.getVolumeManager().getAllVolumes();

        if (volumes.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No trigger volumes defined.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Trigger Volumes (" + volumes.size() + ") ===");
        for (TriggerVolume volume : volumes) {
            sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + volume.getName() + 
                    ChatColor.GRAY + " (World: " + volume.getWorldName() + 
                    ", Enter: " + volume.getEnterActions().size() + 
                    ", Leave: " + volume.getLeaveActions().size() + ")");
        }

        return true;
    }

    /**
     * Handles the /trigger info <name> command.
     * Shows detailed information about a trigger volume.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger info <name>");
            return true;
        }

        String name = args[1];
        TriggerVolume volume = plugin.getVolumeManager().getVolume(name);

        if (volume == null) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Volume: " + volume.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.WHITE + volume.getWorldName());
        sender.sendMessage(ChatColor.YELLOW + "Min: " + ChatColor.WHITE + 
                String.format("(%.1f, %.1f, %.1f)", volume.getMinX(), volume.getMinY(), volume.getMinZ()));
        sender.sendMessage(ChatColor.YELLOW + "Max: " + ChatColor.WHITE + 
                String.format("(%.1f, %.1f, %.1f)", volume.getMaxX(), volume.getMaxY(), volume.getMaxZ()));
        sender.sendMessage(ChatColor.YELLOW + "Volume: " + ChatColor.WHITE + 
                String.format("%.0f blocks", volume.getVolume()));
        
        // Show groups this volume is in
        List<String> groupNames = plugin.getVolumeManager().getGroupsForVolume(name);
        if (!groupNames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Groups: " + ChatColor.WHITE + 
                    String.join(", ", groupNames));
        }

        // Show enter actions
        if (volume.getEnterActions().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Enter Actions: " + ChatColor.GRAY + "None");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Enter Actions:");
            for (int i = 0; i < volume.getEnterActions().size(); i++) {
                TriggerAction action = volume.getEnterActions().get(i);
                sender.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + 
                        ChatColor.AQUA + action.getType().name() + 
                        ChatColor.WHITE + ": " + action.getValue());
            }
        }

        // Show leave actions
        if (volume.getLeaveActions().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Leave Actions: " + ChatColor.GRAY + "None");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Leave Actions:");
            for (int i = 0; i < volume.getLeaveActions().size(); i++) {
                TriggerAction action = volume.getLeaveActions().get(i);
                sender.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + 
                        ChatColor.AQUA + action.getType().name() + 
                        ChatColor.WHITE + ": " + action.getValue());
            }
        }

        return true;
    }

    /**
     * Handles the /trigger setaction <name> <enter|leave> <type> <value> command.
     * Adds an action to a trigger volume or group.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleSetAction(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger setaction <name|group> <enter|leave> <type> <value>");
            sender.sendMessage(ChatColor.GRAY + "Triggers: enter, leave");
            sender.sendMessage(ChatColor.GRAY + "Types: PLAYER_COMMAND, CONSOLE_COMMAND, MESSAGE, TELEPORT");
            return true;
        }

        String name = args[1];
        String triggerStr = args[2].toLowerCase();
        String typeStr = args[3].toUpperCase();

        // Check if it's a group or volume
        boolean isGroup = plugin.getVolumeManager().groupExists(name);
        boolean isVolume = plugin.getVolumeManager().volumeExists(name);
        
        if (!isGroup && !isVolume) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        // Validate trigger type (enter or leave)
        boolean isEnter;
        if (triggerStr.equals("enter")) {
            isEnter = true;
        } else if (triggerStr.equals("leave")) {
            isEnter = false;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid trigger! Use 'enter' or 'leave'.");
            return true;
        }

        ActionType type;
        try {
            type = ActionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid action type! Valid types: PLAYER_COMMAND, CONSOLE_COMMAND, MESSAGE, TELEPORT");
            return true;
        }

        // Join the remaining args as the value
        String value = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        TriggerAction action = new TriggerAction(type, value);
        
        if (isGroup) {
            // Apply to all volumes in the group
            VolumeGroup group = plugin.getVolumeManager().getGroup(name);
            int successCount = 0;
            
            for (String volumeName : group.getVolumeNames()) {
                boolean success;
                if (isEnter) {
                    success = plugin.getVolumeManager().addEnterAction(volumeName, action);
                } else {
                    success = plugin.getVolumeManager().addLeaveAction(volumeName, action);
                }
                if (success) successCount++;
            }
            
            String triggerName = isEnter ? "enter" : "leave";
            sender.sendMessage(ChatColor.GREEN + "Added " + triggerName + " action to " + successCount + " volumes in group " + ChatColor.YELLOW + name);
        } else {
            // Apply to single volume
            boolean success;
            if (isEnter) {
                success = plugin.getVolumeManager().addEnterAction(name, action);
            } else {
                success = plugin.getVolumeManager().addLeaveAction(name, action);
            }

            if (success) {
                String triggerName = isEnter ? "enter" : "leave";
                sender.sendMessage(getMessage("action-set")
                        .replace("%name%", name)
                        .replace("%trigger%", triggerName));
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to set action!");
            }
        }

        return true;
    }

    /**
     * Handles the /trigger clearactions <name> [enter|leave|all] command.
     * Clears actions from a trigger volume or group.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleClearActions(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger clearactions <name|group> [enter|leave|all]");
            return true;
        }

        String name = args[1];

        // Check if it's a group or volume
        boolean isGroup = plugin.getVolumeManager().groupExists(name);
        boolean isVolume = plugin.getVolumeManager().volumeExists(name);
        
        if (!isGroup && !isVolume) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        String triggerType = args.length >= 3 ? args[2].toLowerCase() : "all";
        
        if (isGroup) {
            // Apply to all volumes in the group
            VolumeGroup group = plugin.getVolumeManager().getGroup(name);
            int successCount = 0;
            
            for (String volumeName : group.getVolumeNames()) {
                boolean success = false;
                
                switch (triggerType) {
                    case "enter":
                        success = plugin.getVolumeManager().clearEnterActions(volumeName);
                        break;
                    case "leave":
                        success = plugin.getVolumeManager().clearLeaveActions(volumeName);
                        break;
                    case "all":
                    default:
                        success = plugin.getVolumeManager().clearAllActions(volumeName);
                        break;
                }
                
                if (success) successCount++;
            }
            
            sender.sendMessage(ChatColor.GREEN + "Cleared " + triggerType + " actions from " + successCount + " volumes in group " + ChatColor.YELLOW + name);
        } else {
            // Apply to single volume
            boolean success;
            String clearedType;
            
            switch (triggerType) {
                case "enter":
                    success = plugin.getVolumeManager().clearEnterActions(name);
                    clearedType = "enter";
                    break;
                case "leave":
                    success = plugin.getVolumeManager().clearLeaveActions(name);
                    clearedType = "leave";
                    break;
                case "all":
                default:
                    success = plugin.getVolumeManager().clearAllActions(name);
                    clearedType = "all";
                    break;
            }

            if (success) {
                sender.sendMessage(ChatColor.GREEN + "Cleared " + clearedType + " actions from volume " + ChatColor.YELLOW + name);
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to clear actions!");
            }
        }

        return true;
    }

    /**
     * Handles the /trigger visualize <name> command.
     * Shows particles for a trigger volume.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleVisualize(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("triggervolumes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /trigger visualize <name>");
            return true;
        }

        String name = args[1];

        if (!plugin.getVolumeManager().volumeExists(name)) {
            player.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        if (plugin.getParticleManager().isVisualizingVolume(name)) {
            player.sendMessage(ChatColor.YELLOW + "Volume " + name + " is already being visualized!");
            return true;
        }

        plugin.getParticleManager().startVolumeVisualization(name, player);
        player.sendMessage(ChatColor.GREEN + "Visualizing volume " + ChatColor.YELLOW + name + 
                ChatColor.GREEN + ". Use /trigger hide " + name + " to stop.");

        return true;
    }

    /**
     * Handles the /trigger hide <name> command.
     * Stops showing particles for a trigger volume.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleHide(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger hide <name>");
            return true;
        }

        String name = args[1];

        if (!plugin.getVolumeManager().volumeExists(name)) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", name));
            return true;
        }

        plugin.getParticleManager().stopVolumeVisualization(name);
        sender.sendMessage(ChatColor.GREEN + "Stopped visualizing volume " + ChatColor.YELLOW + name);

        return true;
    }

    /**
     * Handles the /trigger clone [sourceVolume] [targetName] command.
     * Clones all actions from source volume to a new volume from selection.
     * If sourceVolume is omitted, only creates volume from selection.
     * If targetName is provided, uses it instead of auto-generated name.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleClone(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("triggervolumes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        Selection selection = plugin.getSelectionManager().getSelection(player);

        if (selection == null || !selection.isComplete()) {
            player.sendMessage(getMessage("selection-incomplete"));
            return true;
        }

        // Check if source volume is provided
        String sourceVolumeName = null;
        String targetVolumeName = null;
        
        if (args.length >= 2) {
            sourceVolumeName = args[1];
            
            if (!plugin.getVolumeManager().volumeExists(sourceVolumeName)) {
                player.sendMessage(getMessage("volume-not-found").replace("%name%", sourceVolumeName));
                return true;
            }
        }
        
        // Check if custom target name is provided
        if (args.length >= 3) {
            targetVolumeName = args[2];
            
            if (plugin.getVolumeManager().volumeExists(targetVolumeName)) {
                player.sendMessage(getMessage("volume-already-exists-error").replace("%name%", targetVolumeName));
                return true;
            }
        } else if (sourceVolumeName != null) {
            // Generate a unique name for the cloned volume
            targetVolumeName = sourceVolumeName + "_clone";
            int counter = 1;
            while (plugin.getVolumeManager().volumeExists(targetVolumeName)) {
                targetVolumeName = sourceVolumeName + "_clone" + counter;
                counter++;
            }
        } else {
            // No source and no custom name - generate default name
            targetVolumeName = "volume_1";
            int counter = 1;
            while (plugin.getVolumeManager().volumeExists(targetVolumeName)) {
                counter++;
                targetVolumeName = "volume_" + counter;
            }
        }

        // Create the new volume
        boolean created = plugin.getVolumeManager().createVolume(
                targetVolumeName,
                selection.getWorldName(),
                selection.getMinX(), selection.getMinY(), selection.getMinZ(),
                selection.getMaxX(), selection.getMaxY(), selection.getMaxZ()
        );

        if (!created) {
            player.sendMessage(ChatColor.RED + "Failed to create target volume!");
            return true;
        }

        // Clone actions if source volume was provided
        if (sourceVolumeName != null) {
            boolean cloned = plugin.getVolumeManager().cloneActions(sourceVolumeName, targetVolumeName);

            if (cloned) {
                TriggerVolume sourceVolume = plugin.getVolumeManager().getVolume(sourceVolumeName);
                player.sendMessage(getMessage("volume-cloned")
                        .replace("%source%", sourceVolumeName)
                        .replace("%target%", targetVolumeName));
                player.sendMessage(getMessage("actions-cloned")
                        .replace("%enter%", String.valueOf(sourceVolume.getEnterActions().size()))
                        .replace("%leave%", String.valueOf(sourceVolume.getLeaveActions().size())));
            } else {
                player.sendMessage(ChatColor.RED + "Failed to clone actions!");
            }
        } else {
            player.sendMessage(getMessage("volume-created-from-selection").replace("%name%", targetVolumeName));
        }

        return true;
    }

    /**
     * Handles the /trigger copypaste <copyVolume> <pasteVolume> command.
     * Copies all actions from one volume to another without needing a selection.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleCopyPaste(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger copypaste <copyVolume> <pasteVolume>");
            sender.sendMessage(ChatColor.GRAY + "Copies all actions from copyVolume to pasteVolume.");
            return true;
        }

        String copyVolumeName = args[1];
        String pasteVolumeName = args[2];

        if (!plugin.getVolumeManager().volumeExists(copyVolumeName)) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", copyVolumeName));
            return true;
        }

        if (!plugin.getVolumeManager().volumeExists(pasteVolumeName)) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", pasteVolumeName));
            return true;
        }

        boolean success = plugin.getVolumeManager().cloneActions(copyVolumeName, pasteVolumeName);

        if (success) {
            TriggerVolume copyVolume = plugin.getVolumeManager().getVolume(copyVolumeName);
            sender.sendMessage(getMessage("actions-copied")
                    .replace("%source%", copyVolumeName)
                    .replace("%target%", pasteVolumeName));
            sender.sendMessage(getMessage("actions-cloned")
                    .replace("%enter%", String.valueOf(copyVolume.getEnterActions().size()))
                    .replace("%leave%", String.valueOf(copyVolume.getLeaveActions().size())));
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to copy actions!");
        }

        return true;
    }

    /**
     * Handles the /trigger creategroup <groupName> <volume1> <volume2> ... command.
     * Creates a group of volumes for batch operations.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleCreateGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger creategroup <groupName> <volume1> <volume2> [volume3...]");
            sender.sendMessage(ChatColor.GRAY + "Minimum 2 volumes required.");
            return true;
        }

        String groupName = args[1];

        if (plugin.getVolumeManager().groupExists(groupName)) {
            sender.sendMessage(ChatColor.RED + "Group " + ChatColor.YELLOW + groupName + ChatColor.RED + " already exists!");
            return true;
        }

        // Collect volume names
        List<String> volumeNames = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String volumeName = args[i];
            
            if (!plugin.getVolumeManager().volumeExists(volumeName)) {
                sender.sendMessage(ChatColor.RED + "Volume " + ChatColor.YELLOW + volumeName + ChatColor.RED + " does not exist!");
                return true;
            }
            
            volumeNames.add(volumeName);
        }

        boolean success = plugin.getVolumeManager().createGroup(groupName, volumeNames);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Created group " + ChatColor.YELLOW + groupName + 
                    ChatColor.GREEN + " with " + volumeNames.size() + " volumes:");
            for (String volumeName : volumeNames) {
                sender.sendMessage(ChatColor.GRAY + "  - " + volumeName);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to create group! Make sure you have at least 2 volumes.");
        }

        return true;
    }

    /**
     * Handles the /trigger deletegroup <groupName> command.
     * Deletes a volume group (volumes remain).
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleDeleteGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger deletegroup <groupName>");
            return true;
        }

        String groupName = args[1];

        if (!plugin.getVolumeManager().groupExists(groupName)) {
            sender.sendMessage(ChatColor.RED + "Group " + ChatColor.YELLOW + groupName + ChatColor.RED + " does not exist!");
            return true;
        }

        VolumeGroup group = plugin.getVolumeManager().getGroup(groupName);
        boolean success = plugin.getVolumeManager().deleteGroup(groupName);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Deleted group " + ChatColor.YELLOW + groupName);
            sender.sendMessage(ChatColor.GRAY + "The " + group.getVolumeNames().size() + " volumes in the group remain intact.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete group!");
        }

        return true;
    }

    /**
     * Handles the /trigger groupadd <groupName> <volumeName> command.
     * Adds a volume to an existing group.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleGroupAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger groupadd <groupName> <volumeName>");
            return true;
        }

        String groupName = args[1];
        String volumeName = args[2];

        if (!plugin.getVolumeManager().groupExists(groupName)) {
            sender.sendMessage(getMessage("group-not-found").replace("%name%", groupName));
            return true;
        }

        if (!plugin.getVolumeManager().volumeExists(volumeName)) {
            sender.sendMessage(getMessage("volume-not-found").replace("%name%", volumeName));
            return true;
        }

        boolean success = plugin.getVolumeManager().addVolumeToGroup(groupName, volumeName);

        if (success) {
            sender.sendMessage(getMessage("volume-added-to-group")
                    .replace("%volume%", volumeName)
                    .replace("%group%", groupName));
        } else {
            sender.sendMessage(getMessage("volume-already-in-group")
                    .replace("%volume%", volumeName)
                    .replace("%group%", groupName));
        }

        return true;
    }

    /**
     * Handles the /trigger groupremove <groupName> <volumeName> command.
     * Removes a volume from a group.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if successful
     */
    private boolean handleGroupRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /trigger groupremove <groupName> <volumeName>");
            return true;
        }

        String groupName = args[1];
        String volumeName = args[2];

        if (!plugin.getVolumeManager().groupExists(groupName)) {
            sender.sendMessage(getMessage("group-not-found").replace("%name%", groupName));
            return true;
        }

        boolean success = plugin.getVolumeManager().removeVolumeFromGroup(groupName, volumeName);

        if (success) {
            VolumeGroup group = plugin.getVolumeManager().getGroup(groupName);
            if (group == null) {
                // Group was deleted because it had less than 2 volumes
                sender.sendMessage(getMessage("volume-removed-from-group-deleted")
                        .replace("%volume%", volumeName)
                        .replace("%group%", groupName));
            } else {
                sender.sendMessage(getMessage("volume-removed-from-group")
                        .replace("%volume%", volumeName)
                        .replace("%group%", groupName));
            }
        } else {
            sender.sendMessage(getMessage("volume-not-in-group")
                    .replace("%volume%", volumeName)
                    .replace("%group%", groupName));
        }

        return true;
    }

    /**
     * Handles the /trigger reload command.
     * Reloads the plugin configuration and language files.
     * 
     * @param sender The command sender
     * @return True if successful
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("triggervolumes.admin")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        try {
            // Reload config
            plugin.reloadConfig();
            
            // Reload language files
            plugin.getLanguageManager().reload();
            
            // Reload volumes
            plugin.getVolumeManager().loadVolumes();
            
            sender.sendMessage(getMessage("plugin-reloaded"));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading plugin: " + e.getMessage());
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Sends the help message to a command sender.
     * 
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== TriggerVolumes Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/trigger tool" + ChatColor.GRAY + " - Get the selection tool");
        sender.sendMessage(ChatColor.YELLOW + "/trigger create <name>" + ChatColor.GRAY + " - Create volume from selection");
        sender.sendMessage(ChatColor.YELLOW + "/trigger define <name> <x1> <y1> <z1> <x2> <y2> <z2>" + ChatColor.GRAY + " - Create with coordinates");
        sender.sendMessage(ChatColor.YELLOW + "/trigger delete <name>" + ChatColor.GRAY + " - Delete a volume");
        sender.sendMessage(ChatColor.YELLOW + "/trigger clone [sourceVolume] [targetName]" + ChatColor.GRAY + " - Clone volume or create from selection");
        sender.sendMessage(ChatColor.YELLOW + "/trigger copypaste <copyVolume> <pasteVolume>" + ChatColor.GRAY + " - Copy actions between volumes");
        sender.sendMessage(ChatColor.YELLOW + "/trigger list" + ChatColor.GRAY + " - List all volumes");
        sender.sendMessage(ChatColor.YELLOW + "/trigger info <name>" + ChatColor.GRAY + " - Show volume details");
        sender.sendMessage(ChatColor.YELLOW + "/trigger setaction <name|group> <enter|leave> <type> <value>" + ChatColor.GRAY + " - Add action");
        sender.sendMessage(ChatColor.YELLOW + "/trigger clearactions <name|group> [enter|leave|all]" + ChatColor.GRAY + " - Clear actions");
        sender.sendMessage(ChatColor.YELLOW + "/trigger creategroup <groupName> <vol1> <vol2> ..." + ChatColor.GRAY + " - Create volume group");
        sender.sendMessage(ChatColor.YELLOW + "/trigger deletegroup <groupName>" + ChatColor.GRAY + " - Delete volume group");
        sender.sendMessage(ChatColor.YELLOW + "/trigger groupadd <groupName> <volumeName>" + ChatColor.GRAY + " - Add volume to group");
        sender.sendMessage(ChatColor.YELLOW + "/trigger groupremove <groupName> <volumeName>" + ChatColor.GRAY + " - Remove volume from group");
        sender.sendMessage(ChatColor.YELLOW + "/trigger visualize <name>" + ChatColor.GRAY + " - Show volume particles");
        sender.sendMessage(ChatColor.YELLOW + "/trigger hide <name>" + ChatColor.GRAY + " - Hide volume particles");
        sender.sendMessage(ChatColor.YELLOW + "/trigger reload" + ChatColor.GRAY + " - Reload plugin configuration");
        sender.sendMessage(ChatColor.GRAY + "Triggers: enter (on entering), leave (on leaving)");
        sender.sendMessage(ChatColor.GRAY + "Action types: PLAYER_COMMAND, CONSOLE_COMMAND, MESSAGE, TELEPORT");
    }

    /**
     * Gets a message from the language manager.
     * 
     * @param key The message key
     * @return The formatted message
     */
    private String getMessage(String key) {
        return plugin.getLanguageManager().getMessageWithPrefix(key);
    }

    /**
     * Gets a message from the language manager with replacements.
     * 
     * @param key The message key
     * @param replacements Placeholder replacements
     * @return The formatted message
     */
    @SuppressWarnings("unused")
    private String getMessage(String key, String... replacements) {
        return plugin.getLanguageManager().getMessageWithPrefix(key, replacements);
    }
}
