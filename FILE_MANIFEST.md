# Tournament Plugin - File Structure & Manifest

## Build Configuration Files
- `pom.xml` - Maven build configuration with Spigot, Adventure, Multiverse dependencies
- `build.gradle` - Gradle build configuration (alternative to Maven)

## Plugin Resources
- `src/main/resources/plugin.yml` - Bukkit plugin manifest
- `src/main/resources/config.yml` - Default configuration template

## Main Plugin Class
- `src/main/java/com/tournament/TournamentPlugin.java` - Entry point, manager initialization, event registration

## Command Handler
- `src/main/java/com/tournament/commands/TournamentCommand.java` - /tournament command implementation

## Core Managers
- `src/main/java/com/tournament/managers/TournamentManager.java` - Tournament lifecycle, world generation, announcement system
- `src/main/java/com/tournament/managers/TeamManager.java` - Team definition, player assignment, elimination tracking
- `src/main/java/com/tournament/managers/KitManager.java` - Loot kit loading, rarity-weighted selection

## Data Models
- `src/main/java/com/tournament/model/Tournament.java` - Tournament state and metadata
- `src/main/java/com/tournament/model/Team.java` - Team definition with players and elimination status
- `src/main/java/com/tournament/model/Kit.java` - Loot kit with items and rarity
- `src/main/java/com/tournament/model/LootChest.java` - Physical chest representation

## Event Listeners
- `src/main/java/com/tournament/listeners/PlayerJoinListener.java` - Player spawn, team assignment, armor application
- `src/main/java/com/tournament/listeners/PlayerDeathListener.java` - Death handling, spectator mode activation
- `src/main/java/com/tournament/listeners/PlayerQuitListener.java` - Logout cleanup
- `src/main/java/com/tournament/listeners/BlockInteractListener.java` - Chest opening announcements
- `src/main/java/com/tournament/listeners/CompassClickListener.java` - World selection interface
- `src/main/java/com/tournament/listeners/PlayerDamageListener.java` - PvP event handling

## Utility Classes
- `src/main/java/com/tournament/utils/ColorUtils.java` - Leather armor coloring, color conversion
- `src/main/java/com/tournament/utils/WorldUtils.java` - World border setup, world utilities
- `src/main/java/com/tournament/utils/PlayerUtils.java` - Team armor application, inventory management

## Documentation
- `README.md` - Complete feature documentation, installation, usage guide
- `QUICK_START.md` - 2-minute setup guide for server administrators
- `BUILD_INSTRUCTIONS.md` - Detailed Maven/Gradle build instructions
- `IMPLEMENTATION_SUMMARY.md` - Technical implementation overview
- `FILE_MANIFEST.md` - This file

## Summary

**Total Files: 26**
- Java Source Files: 12
- Configuration Files: 2
- Build Files: 2
- Resource Files: 0 (plugin.yml, config.yml counted above)
- Documentation Files: 5
- Manifest Files: 1 (build.gradle also included)

## Class Hierarchy

```
com.tournament
├── TournamentPlugin
├── commands
│   └── TournamentCommand
├── listeners
│   ├── PlayerJoinListener
│   ├── PlayerDeathListener
│   ├── PlayerQuitListener
│   ├── BlockInteractListener
│   ├── CompassClickListener
│   └── PlayerDamageListener
├── managers
│   ├── TournamentManager
│   ├── TeamManager
│   └── KitManager
├── model
│   ├── Tournament
│   ├── Team
│   ├── Kit
│   └── LootChest
└── utils
	├── ColorUtils
	├── WorldUtils
	└── PlayerUtils
```

## Lines of Code (Approximate)

- TournamentPlugin.java: 70 lines
- TournamentManager.java: 200 lines
- TeamManager.java: 100 lines
- KitManager.java: 100 lines
- Event Listeners (6 files): 180 lines total
- Data Models (4 files): 120 lines total
- Utilities (3 files): 80 lines total
- Commands: 80 lines
- Config files: 100 lines

**Total: ~1,000 lines of production code**

## Building & Running

1. **Build:** `mvn clean package` or `gradle build`
2. **Output:** `target/tournament-plugin-1.0.0.jar` or `build/libs/TournamentPlugin-1.0.0.jar`
3. **Deploy:** Copy to `plugins/` folder in Spigot server
4. **Start:** `/tournament start`

See BUILD_INSTRUCTIONS.md for detailed build process.
