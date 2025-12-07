package de.zfzfg.triggervolumes.commands;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import de.zfzfg.triggervolumes.models.ActionType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for the /trigger command.
 * Provides suggestions for subcommands and their arguments.
 * 
 * @author zfzfg
 */
public class TriggerTabCompleter implements TabCompleter {

    private final TriggerVolumesPlugin plugin;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "tool", "create", "define", "delete", "list", "info",
            "setaction", "clearactions", "visualize", "show", "hide",
            "clone", "copypaste", "creategroup", "deletegroup", 
            "groupadd", "groupremove", "reload", "help"
    );

    private static final List<String> TRIGGER_TYPES = Arrays.asList("enter", "leave");

    private static final List<String> CLEAR_TYPES = Arrays.asList("enter", "leave", "all");

    private static final List<String> ACTION_TYPES = Arrays.stream(ActionType.values())
            .map(ActionType::name)
            .collect(Collectors.toList());

    /**
     * Creates a new TriggerTabCompleter.
     * 
     * @param plugin The plugin instance
     */
    public TriggerTabCompleter(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles tab completion for the /trigger command.
     * 
     * @param sender The command sender
     * @param command The command
     * @param alias The command alias
     * @param args The current arguments
     * @return List of completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("triggervolumes.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // Complete subcommands
            String partial = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            switch (subCommand) {
                case "create":
                    // Suggest a default name
                    completions.add("<name>");
                    break;
                case "delete":
                case "remove":
                case "info":
                case "visualize":
                case "show":
                case "hide":
                    // Complete with volume names
                    completions = plugin.getVolumeManager().getVolumeNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "clone":
                    // Complete with volume names for source (optional)
                    completions = plugin.getVolumeManager().getVolumeNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "copypaste":
                    // Complete with volume names for copy source
                    completions = plugin.getVolumeManager().getVolumeNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "setaction":
                case "clearactions":
                    // Complete with volume names and group names
                    completions.addAll(plugin.getVolumeManager().getVolumeNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList()));
                    completions.addAll(plugin.getVolumeManager().getGroupNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList()));
                    break;
                case "deletegroup":
                    // Complete with group names
                    completions = plugin.getVolumeManager().getGroupNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "groupadd":
                case "groupremove":
                    // Complete with group names
                    completions = plugin.getVolumeManager().getGroupNames().stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "creategroup":
                    // Suggest a default name
                    completions.add("<groupName>");
                    break;
                case "define":
                    completions.add("<name>");
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("setaction")) {
                // Complete with trigger types (enter/leave)
                String partial = args[2].toLowerCase();
                completions = TRIGGER_TYPES.stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("clearactions")) {
                // Complete with clear types (enter/leave/all)
                String partial = args[2].toLowerCase();
                completions = CLEAR_TYPES.stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("clone")) {
                // Complete with custom target name (optional)
                completions.add("<targetName>");
            } else if (subCommand.equals("copypaste")) {
                // Complete with volume names for paste target
                String partial = args[2].toLowerCase();
                completions = plugin.getVolumeManager().getVolumeNames().stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("groupadd") || subCommand.equals("groupremove")) {
                // Complete with volume names
                String partial = args[2].toLowerCase();
                completions = plugin.getVolumeManager().getVolumeNames().stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("creategroup")) {
                // Complete with volume names for first volume
                String partial = args[2].toLowerCase();
                completions = plugin.getVolumeManager().getVolumeNames().stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("define")) {
                completions.add("<x1>");
            }
        } else if (args.length >= 4) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("setaction") && args.length == 4) {
                // Complete with action types
                String partial = args[3].toUpperCase();
                completions = ACTION_TYPES.stream()
                        .filter(s -> s.startsWith(partial))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("setaction") && args.length == 5) {
                // Suggest value based on action type
                String actionType = args[3].toUpperCase();
                completions = getActionValueSuggestions(actionType);
            } else if (subCommand.equals("creategroup") && args.length >= 4) {
                // Complete with additional volume names
                String partial = args[args.length - 1].toLowerCase();
                completions = plugin.getVolumeManager().getVolumeNames().stream()
                        .filter(s -> s.startsWith(partial))
                        .filter(s -> !Arrays.asList(args).subList(2, args.length - 1).contains(s))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("define")) {
                switch (args.length) {
                    case 4:
                        completions.add("<y1>");
                        break;
                    case 5:
                        completions.add("<z1>");
                        break;
                    case 6:
                        completions.add("<x2>");
                        break;
                    case 7:
                        completions.add("<y2>");
                        break;
                    case 8:
                        completions.add("<z2>");
                        break;
                }
            }
        }

        return completions;
    }

    /**
     * Gets value suggestions based on action type.
     * 
     * @param actionType The action type
     * @return List of value suggestions
     */
    private List<String> getActionValueSuggestions(String actionType) {
        List<String> suggestions = new ArrayList<>();

        switch (actionType) {
            case "PLAYER_COMMAND":
                suggestions.add("/heal");
                suggestions.add("/spawn");
                break;
            case "CONSOLE_COMMAND":
                suggestions.add("give %player% diamond 1");
                suggestions.add("effect give %player% speed 30 1");
                break;
            case "MESSAGE":
                suggestions.add("&aWelcome!");
                suggestions.add("&cYou entered a restricted area!");
                break;
            case "TELEPORT":
                suggestions.add("0 64 0");
                suggestions.add("100 70 100 0 0");
                break;
        }

        return suggestions;
    }
}
