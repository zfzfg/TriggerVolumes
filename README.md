
# TriggerVolumes Plugin

A powerful Spigot/Paper plugin for creating 3D trigger volumes with customizable actions that execute when players enter or leave defined areas.

## Features

- **Selection System**: Tool-based selection of cubic regions with visual feedback
- **Trigger Volumes**: Store and manage named volumes with precise coordinates
- **Volume Groups**: Group multiple volumes together for batch operations
- **Clone & Copy**: Clone volumes with actions or copy actions between existing volumes
- **Enter/Leave Actions**: Execute different actions when players enter or leave volumes
- **Action Types**:
  - `PLAYER_COMMAND`: Execute commands as the player
  - `CONSOLE_COMMAND`: Execute commands from console
  - `MESSAGE`: Send colored messages to players
  - `TELEPORT`: Teleport players to specific coordinates
- **Particle Visualization**: Real-time particle effects showing volume boundaries
- **Cooldown System**: Prevent action spam with configurable cooldowns
- **Spatial Hashing**: Optimized performance for servers with hundreds of volumes
- **Multi-Language**: Support for 7 languages (EN, DE, FR, PL, RU, JA, ES)
- **Persistent Storage**: Volumes and actions saved in YAML format
- **Reload Command**: Update configurations without server restart

## Requirements

- **Server**: Spigot or Paper 1.19+
- **Java**: Version 17 or higher
- **Permissions Plugin**: Optional (for permission management)

## Installation

1. Download `TriggerVolumes-1.0.4.jar`
2. Place the file in your server's `plugins/` folder
3. Start or restart your server
4. Configuration files will be generated automatically in `plugins/TriggerVolumes/`

## Configuration

### Main Configuration (`config.yml`)

```yaml
# Language Configuration
language:
  default: en  # Available: en, de, fr, pl, ru, ja, es

# Particle Settings
particles:
  update-interval: 5      # Ticks between particle updates
  density: 0.5            # Distance between particles (blocks)
  visualization-duration: 30  # Duration in seconds

# Cooldown System
cooldowns:
  enabled: true
  default-cooldown: 3     # Cooldown in seconds

# Selection Tool
selection-tool:
  material: WOODEN_HOE
  name: "&6Selection Tool"
  lore:
    - "&7Left-click: Set position 1"
    - "&7Right-click: Set position 2"
```

### Changing Language

Edit `config.yml` and change the language setting:
```yaml
language:
  default: de  # Change to your preferred language code
```

Then reload: `/trigger reload`

## Commands

### Basic Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trigger tool` | Get the selection tool | `triggervolumes.admin` |
| `/trigger create <name>` | Create volume from selection | `triggervolumes.admin` |
| `/trigger define <name> <x1> <y1> <z1> <x2> <y2> <z2>` | Create volume with coordinates | `triggervolumes.admin` |
| `/trigger delete <name>` | Delete a volume | `triggervolumes.admin` |
| `/trigger clone [source] [name]` | Clone volume or create from selection | `triggervolumes.admin` |
| `/trigger copypaste <copy> <paste>` | Copy actions between volumes | `triggervolumes.admin` |
| `/trigger list` | List all volumes | `triggervolumes.admin` |
| `/trigger info <name>` | Show volume details and groups | `triggervolumes.admin` |
| `/trigger reload` | Reload configurations | `triggervolumes.admin` |
| `/trigger help` | Show help message | `triggervolumes.admin` |

### Action Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trigger setaction <name> enter <type> <value>` | Add enter action | `triggervolumes.admin` |
| `/trigger setaction <name> leave <type> <value>` | Add leave action | `triggervolumes.admin` |
| `/trigger clearactions <name> [enter\|leave\|all]` | Clear actions | `triggervolumes.admin` |

### Volume Groups Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trigger creategroup <name> <vol1> <vol2> [...]` | Create volume group (min 2) | `triggervolumes.admin` |
| `/trigger deletegroup <name>` | Delete group (volumes remain) | `triggervolumes.admin` |
| `/trigger setaction <group> <trigger> <type> <value>` | Apply action to all volumes in group | `triggervolumes.admin` |
| `/trigger clearactions <group> [enter\|leave\|all]` | Clear actions from group | `triggervolumes.admin` |

### Visualization Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trigger visualize <name>` | Show volume particles (30s) | `triggervolumes.admin` |
| `/trigger hide <name>` | Stop showing particles | `triggervolumes.admin` |

## Action Types

### PLAYER_COMMAND
Executes a command as if the player typed it.

**Examples:**
```
/trigger setaction MyVolume enter PLAYER_COMMAND /heal
/trigger setaction MyVolume enter PLAYER_COMMAND /spawn
/trigger setaction MyVolume leave PLAYER_COMMAND /back
```

**Note:** The player must have permission to execute the command!

### CONSOLE_COMMAND
Executes a command from the console (with OP privileges).

**Examples:**
```
/trigger setaction MyVolume enter CONSOLE_COMMAND give %player% diamond 1
/trigger setaction MyVolume enter CONSOLE_COMMAND effect give %player% speed 30 1
/trigger setaction MyVolume leave CONSOLE_COMMAND clear %player%
```

**Placeholders:**
- `%player%` - Player's name
- `%uuid%` - Player's UUID

### MESSAGE
Sends a colored message to the player.

**Examples:**
```
/trigger setaction MyVolume enter MESSAGE &aWelcome to the safe zone!
/trigger setaction MyVolume enter MESSAGE &6You are now in the &cDanger Zone&6!
/trigger setaction MyVolume leave MESSAGE &7You left the protected area.
```

**Color Codes:**
- `&0-9, a-f` - Standard Minecraft colors
- `&l` - Bold, `&o` - Italic, `&n` - Underline
- `&m` - Strikethrough, `&k` - Obfuscated
- `&r` - Reset formatting

### TELEPORT
Teleports the player to specified coordinates.

**Format:** `x y z [yaw] [pitch]`

**Examples:**
```
/trigger setaction MyVolume enter TELEPORT 100 64 200
/trigger setaction MyVolume enter TELEPORT 0 70 0 90 0
/trigger setaction MyVolume leave TELEPORT -50 65 -50 180 45
```

**Note:** Teleport coordinates are in the same world as the volume!

## Usage Workflow

### 1. Creating a Volume

#### Method A: Using the Selection Tool
```bash
# Get the selection tool
/trigger tool

# Select two corners by clicking blocks
# Left-click for position 1
# Right-click for position 2

# Create the volume
/trigger create MyFirstVolume
```

#### Method B: Using Coordinates
```bash
# Define volume with exact coordinates
/trigger define MyFirstVolume 100 60 100 150 80 150
```

### 2. Adding Actions

#### Enter Actions (triggered on entering)
```bash
# Send a welcome message
/trigger setaction MyFirstVolume enter MESSAGE &aWelcome!

# Give the player an item
/trigger setaction MyFirstVolume enter CONSOLE_COMMAND give %player% diamond 1

# Apply a potion effect
/trigger setaction MyFirstVolume enter CONSOLE_COMMAND effect give %player% speed 30 1
```

#### Leave Actions (triggered on leaving)
```bash
# Send a goodbye message
/trigger setaction MyFirstVolume leave MESSAGE &cGoodbye!

# Remove potion effects
/trigger setaction MyFirstVolume leave CONSOLE_COMMAND effect clear %player%

# Teleport player back
/trigger setaction MyFirstVolume leave TELEPORT 0 64 0
```

### 3. Testing and Visualization

```bash
# Visualize the volume with particles
/trigger visualize MyFirstVolume

# Walk into the volume - enter actions will execute
# Walk out of the volume - leave actions will execute

# Stop visualization
/trigger hide MyFirstVolume
```

### 4. Managing Volumes

```bash
# View all volumes
/trigger list

# Check volume details
/trigger info MyFirstVolume

# Clear specific actions
/trigger clearactions MyFirstVolume enter  # Clear only enter actions
/trigger clearactions MyFirstVolume leave  # Clear only leave actions
/trigger clearactions MyFirstVolume all    # Clear all actions

# Delete volume
/trigger delete MyFirstVolume
```

## Practical Examples

### Example 1: Safe Zone
Create a safe zone that heals players and prevents PvP:

```bash
/trigger create SafeZone
/trigger setaction SafeZone enter MESSAGE &a&lYou entered a safe zone!
/trigger setaction SafeZone enter CONSOLE_COMMAND effect give %player% regeneration 999999 1 true
/trigger setaction SafeZone enter CONSOLE_COMMAND effect give %player% resistance 999999 4 true
/trigger setaction SafeZone leave MESSAGE &cYou left the safe zone!
/trigger setaction SafeZone leave CONSOLE_COMMAND effect clear %player%
```

### Example 2: Shop Entrance
Display information when entering a shop:

```bash
/trigger create ShopEntrance
/trigger setaction ShopEntrance enter MESSAGE &6&l=== SHOP ===
/trigger setaction ShopEntrance enter MESSAGE &eWelcome to the market!
/trigger setaction ShopEntrance enter MESSAGE &7Type /shop to browse items
/trigger setaction ShopEntrance enter PLAYER_COMMAND /shop
```

### Example 3: Danger Zone
Warn players entering a dangerous area:

```bash
/trigger create DangerZone
/trigger setaction DangerZone enter MESSAGE &c&l⚠ WARNING ⚠
/trigger setaction DangerZone enter MESSAGE &cYou entered a dangerous area!
/trigger setaction DangerZone enter MESSAGE &cProceed with caution!
/trigger setaction DangerZone enter CONSOLE_COMMAND playsound entity.ender_dragon.growl player %player%
/trigger setaction DangerZone leave MESSAGE &aYou are now safe.
```

### Example 4: Teleport Hub
Create a portal that teleports players:

```bash
/trigger create TeleportHub
/trigger setaction TeleportHub enter MESSAGE &6Teleporting to spawn...
/trigger setaction TeleportHub enter TELEPORT 0 64 0 0 0
```

### Example 5: Resource Zone
Give players items when entering a mining area:

```bash
/trigger create MiningZone
/trigger setaction MiningZone enter MESSAGE &6You received mining equipment!
/trigger setaction MiningZone enter CONSOLE_COMMAND give %player% iron_pickaxe 1
/trigger setaction MiningZone enter CONSOLE_COMMAND give %player% torch 16
/trigger setaction MiningZone leave MESSAGE &7Your mining equipment was removed.
/trigger setaction MiningZone leave CONSOLE_COMMAND clear %player% iron_pickaxe
/trigger setaction MiningZone leave CONSOLE_COMMAND clear %player% torch
```

### Example 6: Cloning Volumes
Clone a volume's actions to a new area:

```bash
# Select new area with the tool
/trigger tool
# Left-click and right-click to select

# Clone an existing volume to the new selection
/trigger clone SafeZone MySafeZone2
# This creates MySafeZone2 with all actions from SafeZone

# Or create a volume without actions
/trigger clone
# Creates volume_1, volume_2, etc.

# Or clone with auto-generated name
/trigger clone SafeZone
# Creates SafeZone_clone, SafeZone_clone1, etc.
```

### Example 7: Copying Actions Between Volumes
Copy actions from one volume to another without selection:

```bash
# Copy all actions from DangerZone to AnotherDangerZone
/trigger copypaste DangerZone AnotherDangerZone
# Both enter and leave actions are copied
```

### Example 8: Volume Groups
Manage multiple volumes as a group:

```bash
# Create a group of shop volumes
/trigger creategroup AllShops Shop1 Shop2 Shop3

# Apply actions to all volumes in the group at once
/trigger setaction AllShops enter MESSAGE &6Welcome to the shop!
/trigger setaction AllShops enter CONSOLE_COMMAND effect give %player% glowing 10
/trigger setaction AllShops leave MESSAGE &7Thanks for visiting!

# Clear actions from all volumes in group
/trigger clearactions AllShops all

# Delete the group (volumes remain intact)
/trigger deletegroup AllShops
```

## Permissions

### Admin Permissions
- `triggervolumes.admin` - Access to all commands (default: OP)
  - Create, delete, and manage volumes
  - Set and clear actions
  - Visualize volumes
  - Reload configuration

### User Permissions
- `triggervolumes.use` - Trigger actions when entering/leaving volumes (default: all players)

### Permission Examples

**Using LuckPerms:**
```bash
# Give admin permission to specific player
/lp user PlayerName permission set triggervolumes.admin true

# Give admin permission to a group
/lp group admin permission set triggervolumes.admin true

# Remove trigger permission from guest group
/lp group guest permission set triggervolumes.use false
```

**Using PermissionsEx:**
```bash
# Give admin permission
/pex user PlayerName add triggervolumes.admin

# Give to group
/pex group admin add triggervolumes.admin
```

## Cooldown System

The cooldown system prevents actions from being triggered too frequently when players repeatedly enter/exit volumes.

### How it Works
- **Separate Cooldowns**: Enter and leave actions have independent cooldowns
- **Per-Volume**: Each volume has its own cooldown timer per player
- **Configurable**: Default cooldown time can be set in `config.yml`

### Configuration
```yaml
cooldowns:
  enabled: true           # Enable/disable cooldown system
  default-cooldown: 3     # Cooldown duration in seconds
```

### Behavior
- When a player triggers an action, they cannot trigger the same action type (enter/leave) for that volume again until the cooldown expires
- Cooldowns are cleared when players leave the server
- This prevents spam from players walking back and forth across volume boundaries

## Particle Visualization

Volumes can be visualized with colored particle effects to help with positioning and verification.

### Colors
- **Green** (VILLAGER_HAPPY): Volumes with actions
- **Red** (REDSTONE): Volumes without actions
- **Blue** (WATER_SPLASH): Currently being visualized

### Settings
```yaml
particles:
  update-interval: 5      # Ticks between updates (20 ticks = 1 second)
  density: 0.5            # Block spacing between particles
  visualization-duration: 30  # Auto-hide after X seconds
```

### Usage
```bash
# Show particles for 30 seconds
/trigger visualize MyVolume

# Stop showing particles
/trigger hide MyVolume
```

## Performance Optimization

The plugin uses **spatial hashing** to efficiently handle hundreds or thousands of volumes without performance impact.

### How It Works
- The world is divided into 16×16 block chunks (Minecraft's standard chunk size)
- Each volume is registered in all chunks it overlaps
- When checking if a player is in a volume, only volumes in the current chunk are checked

### Performance Comparison
| Number of Volumes | Without Optimization | With Spatial Hashing | Speedup |
|-------------------|---------------------|---------------------|----------|
| 10 volumes | 10 checks | 2-3 checks | 3-5× faster |
| 100 volumes | 100 checks | 5-8 checks | 12-20× faster |
| 1000 volumes | 1000 checks | 5-10 checks | 100-200× faster |

### Benefits
- **Scalable**: Handles thousands of volumes without lag
- **Efficient**: O(k) instead of O(n) where k = volumes per chunk
- **Automatic**: No configuration needed, works out of the box
- **Real-time**: Updates instantly when volumes are created/deleted

### Best Practices
- No special configuration required
- Works best when volumes don't span too many chunks
- Automatically rebuilds spatial index when needed

## File Structure

```
plugins/TriggerVolumes/
├── config.yml              # Main configuration
├── triggervolumes.yml      # Stored volumes and actions
└── lang/                   # Language files
    ├── en.yml             # English (default)
    ├── de.yml             # German
    ├── fr.yml             # French
    ├── pl.yml             # Polish
    ├── ru.yml             # Russian
    ├── ja.yml             # Japanese
    └── es.yml             # Spanish
```

## Volume Groups

Volume groups allow you to manage multiple volumes as a single unit, making it easy to apply the same actions to multiple areas.

### Creating Groups
```bash
# Create a group with at least 2 volumes
/trigger creategroup MyGroup Volume1 Volume2 Volume3
```

### Using Groups
Groups can be used wherever you would use a volume name:

```bash
# Apply actions to all volumes in a group
/trigger setaction MyGroup enter MESSAGE &aWelcome!
/trigger setaction MyGroup leave MESSAGE &cGoodbye!

# Clear actions from all volumes in a group
/trigger clearactions MyGroup enter
/trigger clearactions MyGroup all
```

### Managing Groups
```bash
# View which groups a volume belongs to
/trigger info Volume1
# Shows: "Groups: MyGroup, AnotherGroup"

# Delete a group (volumes are not deleted)
/trigger deletegroup MyGroup
```

### Key Features
- **Batch Operations**: Apply actions to multiple volumes simultaneously
- **Independent Volumes**: Deleting a group doesn't delete the volumes
- **Multiple Membership**: A volume can belong to multiple groups
- **Persistent**: Groups are saved and loaded with the plugin

## Storage Format

Volumes and groups are stored in `triggervolumes.yml`:

```yaml
volumes:
  MyVolume:
    world: world
    minX: 100.0
    minY: 60.0
    minZ: 100.0
    maxX: 150.0
    maxY: 80.0
    maxZ: 150.0
    enterActions:
      0:
        type: MESSAGE
        value: "&aWelcome!"
      1:
        type: CONSOLE_COMMAND
        value: "effect give %player% speed 30 1"
    leaveActions:
      0:
        type: MESSAGE
        value: "&cGoodbye!"
groups:
  MyGroup:
    name: MyGroup
    volumes:
      - Volume1
      - Volume2
      - Volume3
```

## Troubleshooting

### Actions Not Triggering

**Problem:** Player walks through volume but nothing happens

**Solutions:**
1. Check permissions: Player needs `triggervolumes.use`
2. Verify actions exist: `/trigger info <name>`
3. Check cooldown: Actions have a 3-second cooldown by default
4. Visualize volume: `/trigger visualize <name>` to verify boundaries
5. Check console for errors

### Selection Tool Not Working

**Problem:** Clicks don't register positions

**Solutions:**
1. Ensure you have the correct tool (default: Wooden Hoe)
2. Check you have `triggervolumes.admin` permission
3. Try getting a new tool: `/trigger tool`
4. Verify you're clicking blocks, not air

### Particles Not Showing

**Problem:** `/trigger visualize` doesn't show particles

**Solutions:**
1. Check your particle settings in client (Options > Video Settings > Particles)
2. Verify the volume exists: `/trigger info <name>`
3. Try standing closer to the volume
4. Check if another volume is being visualized (only one at a time)

### Commands Not Working

**Problem:** Commands return errors or don't work

**Solutions:**
1. Check permission: `triggervolumes.admin` for setup commands
2. Verify volume name is correct (case-insensitive)
3. Use quotes for values with spaces: `/trigger setaction MyVolume enter MESSAGE "Hello World"`
4. Check console for detailed error messages

### Performance Issues

**Problem:** Server lag with many volumes

**Solutions:**
1. The plugin uses spatial hashing (v1.0.3+) which handles thousands of volumes efficiently
2. Reduce particle density in `config.yml`
3. Increase particle update interval
4. Disable visualization when not needed
5. Increase cooldown times
6. Note: Performance issues with volumes are rare due to optimization

## Advanced Tips

### Using Placeholders
All actions support these placeholders:
- `%player%` - Player's name
- `%uuid%` - Player's UUID

Example:
```bash
/trigger setaction Welcome enter CONSOLE_COMMAND tellraw @a {"text":"Player %player% entered spawn!","color":"gold"}
```

### Multiple Actions
Volumes support unlimited actions per trigger type:
```bash
/trigger setaction MyVolume enter MESSAGE &aWelcome!
/trigger setaction MyVolume enter MESSAGE &eEnjoy your stay!
/trigger setaction MyVolume enter CONSOLE_COMMAND give %player% diamond 1
/trigger setaction MyVolume enter PLAYER_COMMAND /heal
```

All actions execute in the order they were added.

### Overlapping Volumes
Players can be in multiple volumes simultaneously. Each volume's actions will trigger independently when the player enters/leaves.

### World-Specific Volumes
Volumes are world-specific. A volume in "world" won't trigger for players in "world_nether".

### Volume Groups for Organization
Use groups to organize related volumes:
```bash
# Group all spawn protection zones
/trigger creategroup SpawnZones SpawnSafe1 SpawnSafe2 SpawnSafe3

# Group all shop areas
/trigger creategroup Shops MainShop ArmorShop FoodShop

# Apply consistent actions across related areas
/trigger setaction SpawnZones enter MESSAGE &aYou are now in spawn!
```

### Clone vs CopyPaste
- **Clone**: Creates a new volume from your selection + copies actions
  - Requires selection with tool
  - Creates new volume
  - Optional: specify source and custom name
  
- **CopyPaste**: Copies actions between existing volumes
  - No selection needed
  - Works with existing volumes only
  - Faster for duplicating actions

### Backup and Migration
Simply copy the `triggervolumes.yml` file to backup or transfer volumes to another server. Groups are also saved in this file.

## API for Developers

### Maven Dependency
```xml
<dependency>
    <groupId>de.zfzfg</groupId>
    <artifactId>TriggerVolumes</artifactId>
    <version>1.0.4</version>
    <scope>provided</scope>
</dependency>
```

### Getting the Plugin Instance
```java
TriggerVolumesPlugin plugin = (TriggerVolumesPlugin) Bukkit.getPluginManager().getPlugin("TriggerVolumes");
```

### Checking if a Location is in a Volume
```java
TriggerVolumeManager manager = plugin.getVolumeManager();
List<TriggerVolume> volumes = manager.getVolumesAtLocation(location);

if (!volumes.isEmpty()) {
    // Location is in at least one volume
}
```

### Creating a Volume Programmatically
```java
TriggerVolumeManager manager = plugin.getVolumeManager();
manager.createVolume("MyVolume", "world", 0, 60, 0, 100, 80, 100);

// Add actions
TriggerAction action = new TriggerAction(ActionType.MESSAGE, "&aHello!");
manager.addEnterAction("MyVolume", action);
```

## Support & Contributing

### Getting Help
- Check this documentation first
- Look for errors in console logs
- Ask in the Spigot forums
- Open an issue on GitHub

### Reporting Bugs
When reporting bugs, please include:
1. Server version (Spigot/Paper)
2. Plugin version
3. Full error message from console
4. Steps to reproduce the issue
5. Relevant configuration files

### Feature Requests
Have an idea? Open an issue on GitHub with:
- Clear description of the feature
- Use case / why it's needed
- Example of how it would work

## License

This plugin is provided as-is for private and public Minecraft servers.

## Credits

- **Author**: zfzfg
- **Version**: 1.0.4
- **Built with**: Maven, Spigot API, Java 17

---

**Made with ❤️ by zfzfg**

For updates and more plugins, visit our GitHub repository.
