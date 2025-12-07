# TriggerVolumes - Quick Start Guide

This guide will help you get started with TriggerVolumes in just a few minutes.

## Table of Contents

1. [First Steps](#first-steps)
2. [Basic Usage](#basic-usage)
3. [Action Types Explained](#action-types-explained)
4. [Common Use Cases](#common-use-cases)
5. [Tips & Tricks](#tips--tricks)

---

## First Steps

### 1. Install the Plugin

1. Download `TriggerVolumes-1.0.0.jar`
2. Place it in your `plugins/` folder
3. Restart your server
4. Give yourself admin permission: `/lp user YourName permission set triggervolumes.admin true`

### 2. Get the Selection Tool

```bash
/trigger tool
```

You'll receive a **Wooden Hoe** (by default). This is your selection tool:
- **Left-click** a block to set **Position 1**
- **Right-click** a block to set **Position 2**

### 3. Create Your First Volume

1. Select two opposite corners of your desired area
2. Create the volume:
   ```bash
   /trigger create MyFirstVolume
   ```

### 4. Add an Action

Add a welcome message that appears when someone enters:
```bash
/trigger setaction MyFirstVolume enter MESSAGE &aWelcome to my area!
```

### 5. Test It!

Walk into the volume - you should see the message appear!

---

## Basic Usage

### Creating Volumes

#### Method 1: Selection Tool (Recommended)
```bash
# Get the tool
/trigger tool

# Click two corners with the tool
# Then create the volume
/trigger create VolumeName
```

#### Method 2: Direct Coordinates
```bash
/trigger define VolumeName 100 60 100 200 80 200
#                          └─────┬─────┘ └─────┬─────┘
#                           Position 1    Position 2
```

### Adding Actions

**Enter actions** (when player enters):
```bash
/trigger setaction VolumeName enter <TYPE> <value>
```

**Leave actions** (when player leaves):
```bash
/trigger setaction VolumeName leave <TYPE> <value>
```

**Action Types:**
- `MESSAGE` - Show a message
- `PLAYER_COMMAND` - Run command as player
- `CONSOLE_COMMAND` - Run command as console
- `TELEPORT` - Teleport the player

### Managing Volumes

```bash
# List all volumes
/trigger list

# View volume details
/trigger info VolumeName

# Visualize with particles (30 seconds)
/trigger visualize VolumeName

# Stop visualization
/trigger hide VolumeName

# Clear actions
/trigger clearactions VolumeName enter    # Clear enter actions only
/trigger clearactions VolumeName leave    # Clear leave actions only
/trigger clearactions VolumeName all      # Clear all actions

# Delete volume
/trigger delete VolumeName
```

---

## Action Types Explained

### 1. MESSAGE
**What it does:** Sends a colored message to the player

**Syntax:**
```bash
/trigger setaction <volume> <enter|leave> MESSAGE <message>
```

**Examples:**
```bash
# Simple message
/trigger setaction Spawn enter MESSAGE Welcome to spawn!

# With colors
/trigger setaction Spawn enter MESSAGE &aWelcome &6to &cspawn!

# Multiple colors and formatting
/trigger setaction Shop enter MESSAGE &l&6=== SHOP === &r&eClick items to buy!
```

**Color Codes:**
- `&0` = Black, `&1` = Dark Blue, `&2` = Dark Green, `&3` = Dark Aqua
- `&4` = Dark Red, `&5` = Dark Purple, `&6` = Gold, `&7` = Gray
- `&8` = Dark Gray, `&9` = Blue, `&a` = Green, `&b` = Aqua
- `&c` = Red, `&d` = Light Purple, `&e` = Yellow, `&f` = White

**Formatting:**
- `&l` = Bold, `&o` = Italic, `&n` = Underline
- `&m` = Strikethrough, `&k` = Magic/Obfuscated
- `&r` = Reset all formatting

---

### 2. PLAYER_COMMAND
**What it does:** Executes a command as if the player typed it

**Syntax:**
```bash
/trigger setaction <volume> <enter|leave> PLAYER_COMMAND <command>
```

**Important:** The player MUST have permission for the command!

**Examples:**
```bash
# Make player open their enderchest
/trigger setaction Storage enter PLAYER_COMMAND /enderchest

# Make player teleport to spawn
/trigger setaction Portal enter PLAYER_COMMAND /spawn

# Make player open a menu (requires menu plugin)
/trigger setaction Menu enter PLAYER_COMMAND /menu open
```

**Use Cases:**
- Opening GUIs or menus
- Player-based teleports
- Personal commands that need player context

---

### 3. CONSOLE_COMMAND
**What it does:** Executes a command from console (with OP privileges)

**Syntax:**
```bash
/trigger setaction <volume> <enter|leave> CONSOLE_COMMAND <command>
```

**Placeholders:**
- `%player%` - Replaced with player's name
- `%uuid%` - Replaced with player's UUID

**Examples:**
```bash
# Give the player items
/trigger setaction Gift enter CONSOLE_COMMAND give %player% diamond 5

# Apply potion effects
/trigger setaction Speed enter CONSOLE_COMMAND effect give %player% speed 30 2

# Run multiple commands (add multiple actions)
/trigger setaction Buff enter CONSOLE_COMMAND effect give %player% strength 60 1
/trigger setaction Buff enter CONSOLE_COMMAND effect give %player% speed 60 1
/trigger setaction Buff enter CONSOLE_COMMAND effect give %player% regeneration 60 1

# Remove effects when leaving
/trigger setaction Buff leave CONSOLE_COMMAND effect clear %player%

# Execute complex commands
/trigger setaction Arena enter CONSOLE_COMMAND gamemode adventure %player%
/trigger setaction Arena leave CONSOLE_COMMAND gamemode survival %player%
```

**Use Cases:**
- Giving items/effects
- Changing gamemode
- Running admin commands
- Broadcasting messages
- Economy commands (money give/take)

---

### 4. TELEPORT
**What it does:** Teleports the player to coordinates

**Syntax:**
```bash
# Basic (coordinates only)
/trigger setaction <volume> <enter|leave> TELEPORT <x> <y> <z>

# Advanced (with rotation)
/trigger setaction <volume> <enter|leave> TELEPORT <x> <y> <z> <yaw> <pitch>
```

**Parameters:**
- `x y z` - Target coordinates (can be decimals)
- `yaw` - Horizontal rotation (0-360, optional)
  - 0 = South, 90 = West, 180 = North, 270 = East
- `pitch` - Vertical rotation (-90 to 90, optional)
  - -90 = Up, 0 = Straight, 90 = Down

**Examples:**
```bash
# Simple teleport
/trigger setaction Portal enter TELEPORT 0 64 0

# Teleport with specific rotation (facing north)
/trigger setaction Portal enter TELEPORT 100 65 200 180 0

# Teleport looking down at 45 degrees
/trigger setaction Drop enter TELEPORT 0 100 0 0 45

# Create a one-way portal
/trigger setaction PortalIn enter TELEPORT 1000 64 1000
/trigger setaction PortalIn enter MESSAGE &6Teleported to the Nether Hub!
```

**Use Cases:**
- Portal systems
- Spawn points
- Jail/timeout areas
- Event locations
- Secret areas

---

## Common Use Cases

### 🏠 Spawn Protection Zone

Create a safe zone at spawn with healing and protection:

```bash
# Create the volume
/trigger create SpawnZone

# Enter effects
/trigger setaction SpawnZone enter MESSAGE &a&lWelcome to Spawn!
/trigger setaction SpawnZone enter MESSAGE &7You are now protected
/trigger setaction SpawnZone enter CONSOLE_COMMAND effect give %player% regeneration 999999 1 true
/trigger setaction SpawnZone enter CONSOLE_COMMAND effect give %player% resistance 999999 4 true

# Leave effects
/trigger setaction SpawnZone leave MESSAGE &cYou left the safe zone
/trigger setaction SpawnZone leave CONSOLE_COMMAND effect clear %player%
```

---

### 🛒 Shop Entrance

Welcome players and show them shop commands:

```bash
/trigger create ShopEntry

/trigger setaction ShopEntry enter MESSAGE &6&m                    &r
/trigger setaction ShopEntry enter MESSAGE &6&l     SHOP
/trigger setaction ShopEntry enter MESSAGE &e  Welcome to the market!
/trigger setaction ShopEntry enter MESSAGE &7  Type /shop to browse
/trigger setaction ShopEntry enter MESSAGE &6&m                    &r

/trigger setaction ShopEntry leave MESSAGE &7Thanks for shopping!
```

---

### ⚔️ PvP Arena

Enable PvP mode when entering, disable when leaving:

```bash
/trigger create PvPArena

# On enter: Adventure mode, clear inventory, give kit
/trigger setaction PvPArena enter MESSAGE &c&lYou entered the PvP Arena!
/trigger setaction PvPArena enter CONSOLE_COMMAND gamemode adventure %player%
/trigger setaction PvPArena enter CONSOLE_COMMAND clear %player%
/trigger setaction PvPArena enter CONSOLE_COMMAND give %player% iron_sword 1
/trigger setaction PvPArena enter CONSOLE_COMMAND give %player% bow 1
/trigger setaction PvPArena enter CONSOLE_COMMAND give %player% arrow 64

# On leave: Survival mode, clear arena items, heal
/trigger setaction PvPArena leave MESSAGE &aYou left the PvP Arena
/trigger setaction PvPArena leave CONSOLE_COMMAND gamemode survival %player%
/trigger setaction PvPArena leave CONSOLE_COMMAND clear %player%
/trigger setaction PvPArena leave CONSOLE_COMMAND effect give %player% instant_health 1 10
```

---

### 🎵 Music Zone

Play music using note blocks or resource packs:

```bash
/trigger create MusicZone

/trigger setaction MusicZone enter MESSAGE &dYou entered the music zone
/trigger setaction MusicZone enter CONSOLE_COMMAND playsound minecraft:music.creative player %player%

/trigger setaction MusicZone leave MESSAGE &7Music stopped
/trigger setaction MusicZone leave CONSOLE_COMMAND stopsound %player% music
```

---

### 🚪 Portal System

Create a network of portals:

```bash
# Portal 1 -> Portal 2
/trigger create Portal1
/trigger setaction Portal1 enter MESSAGE &6Teleporting...
/trigger setaction Portal1 enter TELEPORT 1000 64 1000 0 0

# Portal 2 -> Portal 1 (return)
/trigger create Portal2
/trigger setaction Portal2 enter MESSAGE &6Returning...
/trigger setaction Portal2 enter TELEPORT 0 64 0 180 0
```

---

### ⛏️ Mining Buff Zone

Give mining buffs in designated mining areas:

```bash
/trigger create MiningZone

/trigger setaction MiningZone enter MESSAGE &6⛏ &eMining buffs activated!
/trigger setaction MiningZone enter CONSOLE_COMMAND effect give %player% haste 999999 2 true
/trigger setaction MiningZone enter CONSOLE_COMMAND effect give %player% night_vision 999999 0 true

/trigger setaction MiningZone leave MESSAGE &7Mining buffs removed
/trigger setaction MiningZone leave CONSOLE_COMMAND effect clear %player% haste
/trigger setaction MiningZone leave CONSOLE_COMMAND effect clear %player% night_vision
```

---

### 🏆 Achievement Area

Reward players for finding secret areas:

```bash
/trigger create SecretArea

/trigger setaction SecretArea enter MESSAGE &6&l✦ SECRET FOUND! ✦
/trigger setaction SecretArea enter MESSAGE &aYou discovered a hidden area!
/trigger setaction SecretArea enter CONSOLE_COMMAND give %player% diamond 10
/trigger setaction SecretArea enter CONSOLE_COMMAND give %player% emerald 5
/trigger setaction SecretArea enter CONSOLE_COMMAND advancement grant %player% only custom:secret_finder
```

---

### 🚫 Restricted Area

Prevent unauthorized access:

```bash
/trigger create Restricted

/trigger setaction Restricted enter MESSAGE &c&l⚠ RESTRICTED AREA ⚠
/trigger setaction Restricted enter MESSAGE &cYou do not have access!
/trigger setaction Restricted enter TELEPORT 0 64 0
```

Note: This only works if the player has `triggervolumes.use` permission. For better security, remove this permission from players who shouldn't enter.

---

### 🎮 Mini-Game Lobby

Auto-join mini-game when entering lobby:

```bash
/trigger create GameLobby

/trigger setaction GameLobby enter MESSAGE &a&lJoining game lobby...
/trigger setaction GameLobby enter CONSOLE_COMMAND gamemode adventure %player%
/trigger setaction GameLobby enter PLAYER_COMMAND /game join

/trigger setaction GameLobby leave MESSAGE &7Left game lobby
/trigger setaction GameLobby leave PLAYER_COMMAND /game leave
/trigger setaction GameLobby leave CONSOLE_COMMAND gamemode survival %player%
```

---

## Tips & Tricks

### 💡 Multiple Messages

You can add multiple messages to create multi-line notifications:

```bash
/trigger setaction MyVolume enter MESSAGE &6&m                    &r
/trigger setaction MyVolume enter MESSAGE &e&l   WELCOME!
/trigger setaction MyVolume enter MESSAGE &7  Important information here
/trigger setaction MyVolume enter MESSAGE &6&m                    &r
```

### 💡 Testing Actions

Use `/trigger visualize` to see the volume boundaries while testing:

```bash
/trigger visualize MyVolume
# Walk in and out to test
/trigger hide MyVolume
```

### 💡 Checking Volume Info

Forgot what actions a volume has? Check with:

```bash
/trigger info MyVolume
```

This shows:
- Coordinates
- World name
- All enter actions
- All leave actions

### 💡 Cooldown Bypass

Actions have a 3-second cooldown by default. If you walk out and back in quickly, actions won't trigger again until the cooldown expires. This is normal behavior to prevent spam.

### 💡 Overlapping Volumes

You can have multiple volumes in the same area! Each will trigger independently:

```bash
# Large outer volume
/trigger create OuterZone
/trigger setaction OuterZone enter MESSAGE &eEntered outer zone

# Small inner volume
/trigger create InnerZone
/trigger setaction InnerZone enter MESSAGE &6Entered inner zone
```

Walking through will trigger both!

### 💡 Using Quotes

If your message or command has special characters, use quotes:

```bash
# Without quotes (wrong)
/trigger setaction Test enter MESSAGE Hello World!
# Only "Hello" is used, "World!" is ignored

# With quotes (correct)
/trigger setaction Test enter MESSAGE "Hello World!"
```

### 💡 Tab Completion

Use **TAB** to auto-complete:
- Volume names
- Command parameters
- Action types

Just type `/trigger setaction ` and press TAB!

### 💡 Color Preview

Test colors quickly:
```bash
/trigger setaction Test enter MESSAGE &a=&b=&c=&d=&e=&f=&0=&1=&2=
```

### 💡 Particle Colors

- **Green particles** = Volume has actions
- **Red particles** = Volume is empty (no actions)
- Use this to quickly identify which volumes need actions

### 💡 World-Specific

Remember: Volumes only work in the world they were created in!

- Create in "world" → Works in "world" only
- Create in "world_nether" → Works in "world_nether" only

### 💡 Backup Your Work

The file `plugins/TriggerVolumes/triggervolumes.yml` contains all your volumes. Back it up regularly!

```bash
# Backup command (Linux)
cp plugins/TriggerVolumes/triggervolumes.yml plugins/TriggerVolumes/triggervolumes_backup.yml
```

---

## Next Steps

Now that you understand the basics:

1. **Experiment** with different action combinations
2. **Create** a portal system for your server
3. **Set up** protected zones for important areas
4. **Design** mini-game lobbies with auto-join
5. **Build** an economy system with shop entry messages

For more advanced features and troubleshooting, see the full [README_EN.md](README_EN.md)

---

**Need Help?**
- Check the [README.md](README.md) for detailed documentation
- Look at console logs for error messages
- Test with `/trigger visualize` to see volume boundaries

**Happy Building! 🎮**
