# Tournament Plugin - Complete Implementation

A command-based survival tournament plugin for modern Bukkit/Spigot servers. It creates an isolated world, lets every online player choose a team, and continuously adds random loot chests during the game.

## Features

### Core Tournament Features
- ✅ **Team Selection**: `/tournament start` sends the admin and all online players to a sky lobby with one team dye per hotbar slot, beginning at slot 1. Left- or right-click a dye to join that team.
- ✅ **Team Hat**: Players receive a leather helmet dyed to their selected team's colour.
- ✅ **Starting Kits**: A kit with `rarity: 1.0` is issued when a player selects their team; it is never used as chest loot.
- ✅ **Endless Loot**: One chest is safely placed on open ground at a random location each configured interval for the duration of the tournament.
- ✅ **Elimination System**: Dead players enter spectator mode until their entire team is eliminated
- ✅ **World Selection**: Eliminated players can leave tournament or stay as spectators
- ✅ **Auto-Generated Worlds**: `/tournament start` creates new isolated tournament world
- ✅ **Announcements**: Chat announcements for player deaths, team eliminations, and chest discoveries
- ✅ **GeyserMC Support**: Compatible with Bedrock players (uses Adventure API for colors)

### Configuration System
- Teams with custom colors (RED, BLUE, GREEN, YELLOW, etc.)
- Multiple kit definitions with items and rarity weights
- World border radius configuration
- Endless loot chest spawn interval configuration
- Customizable announcements

### Admin Commands
```
/tournament start    - Start a new tournament (creates world, spawns teams)
/tournament stop     - Stop current tournament and cleanup
/tournament status   - Check if tournament is running
```

## Project Structure

```
tournament-plugin/
├── pom.xml                                    # Maven build config
├── build.gradle                               # Gradle build config
├── BUILD_INSTRUCTIONS.md                      # Build setup guide
├── README.md                                  # This file
├── src/
│   ├── main/
│   │   ├── java/com/tournament/
│   │   │   ├── TournamentPlugin.java         # Main plugin entry point
│   │   │   ├── commands/
│   │   │   │   └── TournamentCommand.java    # /tournament command handler
│   │   │   ├── listeners/
│   │   │   │   ├── PlayerJoinListener.java   # Team assignment & spawn
│   │   │   │   ├── PlayerDeathListener.java  # Death handling & spectator
│   │   │   │   ├── PlayerQuitListener.java   # Cleanup on logout
│   │   │   │   ├── BlockInteractListener.java # Chest opening
│   │   │   │   ├── CompassClickListener.java  # World selection interface
│   │   │   │   └── PlayerDamageListener.java # PvP handling
│   │   │   ├── managers/
│   │   │   │   ├── TournamentManager.java    # Core tournament logic
│   │   │   │   ├── TeamManager.java          # Team & player tracking
│   │   │   │   └── KitManager.java           # Loot kit management
│   │   │   ├── model/
│   │   │   │   ├── Tournament.java           # Tournament state POJO
│   │   │   │   ├── Team.java                 # Team definition & tracking
│   │   │   │   ├── Kit.java                  # Loot kit definition
│   │   │   │   └── LootChest.java            # Chest wrapper
│   │   │   └── utils/
│   │   │       ├── ColorUtils.java           # Color management
│   │   │       ├── WorldUtils.java           # World utilities
│   │   │       └── PlayerUtils.java          # Player utilities
│   │   └── resources/
│   │       ├── plugin.yml                    # Plugin manifest
│   │       └── config.yml                    # Default configuration
```

## Installation & Setup

### Prerequisites
- Minecraft Spigot/Bukkit server (1.17+)
- Java 21 or higher
- Maven or Gradle (for building)
- (Optional) GeyserMC + Floodgate for Bedrock support

### Step 1: Build the Plugin

**With Maven:**
```bash
cd /path/to/tournament-plugin
mvn clean package
```

**With Gradle:**
```bash
cd /path/to/tournament-plugin
gradle build
```

See `BUILD_INSTRUCTIONS.md` for detailed build instructions.

### Step 2: Install the Plugin

1. Copy the built JAR to your Spigot server's `plugins/` directory:
   - Maven: `target/tournament-plugin-1.0.0.jar`
   - Gradle: `build/libs/TournamentPlugin-1.0.0.jar`

2. Restart your server

### Step 3: Configure the Plugin

The first run creates `plugins/TournamentPlugin/config.yml`. Edit this file to:

```yaml
# Define your teams
teams:
  red:
	name: "Red Team"
	color: "RED"
  blue:
	name: "Blue Team"
	color: "BLUE"

# Define loot kits with rarity
kits:
  starter:
	name: "Starter Kit"
	rarity: 1.0  # given to every player after team selection
	items:
	  1:
		material: "STONE_PICKAXE"
		amount: 1
```

4. Restart the server to apply changes

## Usage Guide

### Starting a Tournament

1. Admin runs `/tournament start`
2. Plugin automatically:
   - Creates new world (`Tournament_[timestamp]`)
   - Creates a normal survival world
   - Creates world border (300 blocks from spawn by default)
   - Teleports everyone, including the command issuer, to a sky team-selection lobby
   - Gives team-selection dyes in hotbar slots starting from slot 1

3. Each player left- or right-clicks a dye to select their team
4. Players spawn at their selected team's location with a team-coloured leather hat and all `rarity: 1.0` kits

### During Tournament

- **Survival gameplay**: All normal Minecraft mechanics work
- **Mining/Crafting**: Full survival mode features available
- **PvP**: Combat enabled between teams
- **Loot**: Chests found announce kit type to server
- **Deaths**: Eliminated players go to spectator mode, still see action

### Team Elimination

When a player dies:
1. They enter spectator mode
2. Server announces: `[TEAM_COLOR] Player has been eliminated!`
3. If their entire team dies:
   - Server announces: `[TEAM_COLOR] Team has been completely eliminated!`
   - Surviving spectators see: "Team eliminated! Click compass to change worlds or stay to spectate"

### Leaving Tournament

Spectating players (team eliminated) can:
1. Click the compass in their hotbar
2. Teleported to main world spawn
3. Removed from tournament tracking
4. **Cannot rejoin** the same tournament world

## Color Support

### Supported Team Colors
Valid colors in config:
- RED, BLUE, GREEN, YELLOW, ORANGE, PURPLE, PINK, BROWN, BLACK, WHITE, LIME, CYAN, LIGHT_GRAY, GRAY

### Chat Display
Team names appear in chat with their assigned color:
```
[Red Team] Player has been eliminated!
```

### Bedrock Player Support
- Colors display correctly for Bedrock players via GeyserMC
- Uses Kyori Adventure API for compatibility
- Colored leather armor visible to all clients

## Configuration Reference

### teams
Define tournament teams with their display name and color:
```yaml
teams:
  team_id:
	name: "Display Name"
	color: "COLOR_NAME"
```

### kits
Define loot kits with items and rarity (0.0-1.0):
```yaml
kits:
  kit_id:
	name: "Kit Name"
	rarity: 0.5
	items:
	  1:
		material: "MATERIAL_NAME"
		amount: NUMBER
```

### tournament-world
- `chest.spawn-interval-ticks`: Ticks between random chest spawns (default: 1200); chests continue spawning until the tournament stops
- `world-border-radius`: World border radius in blocks (default: 300)

## Troubleshooting

### Plugin won't start
- Check Multiverse-Core is installed: `/mv list`
- Check server logs for "TournamentPlugin enabled successfully!"
- Verify Java is 11+ with: `java -version`

### Teams not showing
- Edit `plugins/TournamentPlugin/config.yml`
- Add team entries in the `teams` section
- Restart server
- Run `/tournament stop` then `/tournament start`

### No loot chests spawning
- Check `chest.count` in config (must be > 0)
- Verify kits are defined in config
- Check server logs for errors
- Try increasing `chest.count` value

### Colors not working on Bedrock
- Ensure GeyserMC and Floodgate are installed and running
- Verify Adventure API version is compatible (4.14.0)
- Check that players can see colored items on Java clients first

### Players can re-join tournament world
- Ensure they clicked compass to leave spectator mode
- Confirm they were teleported to main world (`/where` or `/getpos`)
- Tournament world is time-limited until server restart by design

## API & Extensibility

The plugin uses a manager-based architecture for easy extension:

### TournamentManager
- `startTournament()` - Initiate new tournament
- `stopTournament()` - End current tournament
- `handlePlayerDeath(Player)` - Spectator logic
- `broadcastMessage(Component)` - Send colored messages
- `isTournamentRunning()` - Check status

### TeamManager
- `getTeam(String)` - Get team by ID
- `addPlayerToTeam(UUID, String)` - Assign player
- `playerToSpectator(UUID)` - Move player to spectator
- `resetTeams()` - Clear all team data

### KitManager
- `getRandomKit()` - Rarity-weighted random selection
- `getKitItems(String)` - Get items from kit
- `getAllKits()` - List all configured kits

## Performance Notes

- Async world generation to prevent server lag
- Efficient player tracking with UUID-based maps
- Chest placement uses random iteration (not O(n²))
- Adventure API color rendering is lightweight

## Known Limitations

- Tournament world is deleted when server restarts
- Players cannot rejoin same tournament instance
- Spectators cannot mine/build (Spectator mode restriction)
- One tournament per server at a time

## Future Enhancement Ideas

- GUI-based kit selection for dead players
- Tournament brackets and round management
- Respawn timers for downed players
- Custom world presets (Nether, End, etc.)
- Tournament statistics and leaderboards
- Economy integration (prize pool)
- Custom spawn point selection UI

## Support & Issues

If you encounter any issues:

1. Check server logs for errors: `logs/latest.log`
2. Verify all plugins are updated
3. Test with a fresh config.yml
4. Ensure all dependencies are installed

## License

This project is created for Minecraft server administrators.

## Version History

- **1.0.0** - Initial release
  - Core tournament system
  - Team management
  - Custom loot kits
  - Spectator mode
  - GeyserMC support
