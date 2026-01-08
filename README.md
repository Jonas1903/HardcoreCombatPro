# HardcoreCombatPro

A robust combat mechanics plugin for Minecraft Paper 1.21.8 servers, featuring universal combat tagging, safe zone system, and hardcore restrictions.

## Features

### 1. Universal Combat Logic

**Trigger Events:**
- **Receiving Damage:** Players receive a combat tag for 15 seconds when taking damage from any source (fall, fire, lava, mobs, players, void, cactus, etc.)
- **Dealing Damage:** Players receive a combat tag for 15 seconds when dealing damage to any living entity

**Tracking:**
- Efficient combat status management using `HashMap<UUID, Long>`
- Combat tags automatically expire after 15 seconds
- Real-time countdown display via Action Bar

**Death Reset:**
- Player deaths automatically reset combat tags
- Respawn without combat restrictions

**Admin Bypass:**
- Players with `combat.admin` permission or OP status are excluded from all combat restrictions

### 2. Combat Visuals & UI

**Action Bar Display:**
- Dynamic combat countdown timer (e.g., `Combat Status: 12s remaining`)
- Updates every second using Adventure API
- Color-coded messages using MiniMessage format

### 3. Custom Safe Zone System

**Cuboid Safe Zone:**
- Define a single cuboid safe zone using two location points
- Configured via `config.yml` or in-game commands

**Commands:**
- `/ct set1` - Set the first corner of the safe zone (requires `combat.admin` permission)
- `/ct set2` - Set the second corner of the safe zone (requires `combat.admin` permission)

**Player Entry Blocking:**
- Tagged players (non-admin) cannot enter the safe zone
- Movement is canceled with a notification message

**Particle Wall:**
- Visual particle effects (FLAME/REDSTONE) display around safe zone edges
- Activates when tagged players come within 5 blocks of the boundary
- Updates every 5 ticks for smooth visuals

### 4. Hard Game Restrictions

**Command Blocking:**
- Tagged players cannot execute any commands during combat
- Prevents teleportation and other exploits

**Combat Log:**
- Players who disconnect while tagged are instantly killed (`setHealth(0)`)
- Prevents combat logging exploitation

## Technical Details

**Requirements:**
- Minecraft Paper 1.21.8 (or compatible versions)
- Java 21

**Technology Stack:**
- Modern Java 21 features (Records, switch patterns)
- Adventure API for text components
- MiniMessage for text formatting
- No external dependencies beyond Paper

**Classes:**
- `HardcoreCombatPro.java` - Main plugin class, command handling
- `CombatManager.java` - Combat timer management and data storage
- `ZoneManager.java` - Safe zone logic and particle effects
- `CombatListener.java` - Event handling for damage, movement, and restrictions

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Configure settings in `plugins/HardcoreCombatPro/config.yml`
5. Use `/ct set1` and `/ct set2` to define your safe zone

## Configuration

```yaml
# Combat tag duration in seconds
combat-duration: 15

# Safe zone settings
safe-zone:
  enabled: true
  particles:
    enabled: true
    type: "FLAME"
    detection-distance: 5
```

## Permissions

- `combat.admin` - Bypass all combat restrictions and access zone commands (default: op)

## Building from Source

```bash
git clone https://github.com/Jonas1903/HardcoreCombatPro.git
cd HardcoreCombatPro
mvn clean package
```

The compiled JAR will be in the `target` directory.

## License

This project is licensed under standard open source terms.