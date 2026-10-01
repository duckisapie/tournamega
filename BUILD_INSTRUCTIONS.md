# Tournament Plugin - Build Instructions

## Prerequisites
You need to have one of the following installed:
- Maven 3.6+ (recommended)
- Gradle 7.0+
- Java Development Kit (JDK) 11+

## Building with Maven

If Maven is installed on your system:

```bash
cd /path/to/NewRepo
mvn clean package
```

The built JAR will be located at: `target/tournament-plugin-1.0.0.jar`

## Building with Gradle

If Maven is not available, use Gradle:

1. Make sure you have Gradle installed or use the Gradle wrapper (if gradlew exists):

```bash
cd /path/to/NewRepo
./gradlew build
```

Or on Windows:
```bash
gradlew.bat build
```

If gradlew doesn't exist, you can install Gradle globally and run:
```bash
gradle build
```

The built JAR will be located at: `build/libs/TournamentPlugin-1.0.0.jar`

## Installation

1. Copy the built JAR to your Spigot/Bukkit server's `plugins/` directory
2. Ensure Multiverse-Core plugin is also installed
3. Restart your server
4. The plugin will generate a default `config.yml` in `plugins/TournamentPlugin/`
5. Edit the configuration as needed
6. Restart the server again to apply config changes

## Configuration

The `config.yml` file allows you to:
- Define teams with specific colors
- Create and customize loot kits with rarity values
- Adjust world border size
- Configure tournament announcements

## Running Commands

Admin must have `tournament.admin` permission (ops by default):

```
/tournament start     - Start a new tournament
/tournament stop      - Stop the current tournament
/tournament status    - Check tournament status
```

## Troubleshooting

### Plugin doesn't load
- Ensure Multiverse-Core is installed
- Check server logs for error messages
- Verify Java version is 11 or higher

### Teams not showing
- Edit the config.yml and add team definitions
- Restart the server
- Check that color names are valid (RED, BLUE, GREEN, YELLOW, etc.)

### Chests not spawning
- Check the `chest.count` setting in config.yml
- Ensure kits are properly defined
- Look at server logs for any errors

### GeyserMC Not Showing Colors
- Ensure you have the latest GeyserMC and Floodgate installed
- Adventure API and platform-bukkit dependency must be on the classpath
- The plugin uses `net.kyori.adventure` for color formatting
