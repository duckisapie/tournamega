# Tournament Plugin - Quick Start Guide

## Installation (30 seconds)

1. **Download**: Get the JAR from the `target/` or `build/libs/` folder after building
2. **Copy**: Place JAR in your server's `plugins/` folder
3. **Restart**: Restart your Spigot/Bukkit server
4. **Done**: Plugin creates config automatically

## First Tournament (2 minutes)

```bash
# In Minecraft game (as admin/op):
/tournament start

# Wait for message: "Tournament started! World: Tournament_[timestamp]"
# Players join and are auto-spawned at team locations
```

## Config Quick Tips

Edit `plugins/TournamentPlugin/config.yml`:

```yaml
# Add more teams
teams:
  red:
	name: "Red Team"
	color: "RED"
  blue:
	name: "Blue Team"
	color: "BLUE"
  green:
	name: "Green Team"
	color: "GREEN"

# Customize kits (rarity 0.0-1.0, higher = more common)
kits:
  starter:
	name: "Starting Items"
	rarity: 0.9
	items:
	  1:
		material: "STONE_PICKAXE"
		amount: 1
	  2:
		material: "ACACIA_LOG"
		amount: 32

# Adjust world size and chest count
tournament-world:
  chest:
	count: 30  # More chests = more loot
  world-border-radius: 300  # 600 block total width
```

## Commands Reference

```
/tournament start         Start tournament (creates world)
/tournament stop          End tournament (deletes world)
/tournament status        Check if running

# Permissions
tournament.admin          Allows all commands (ops by default)
tournament.join           Permission to join tournament
```

## During Gameplay

- **Players join**: Automatically assigned to teams
- **Team colors**: Players see team name in colored text
- **Armor colors**: Leather armor matches team color
- **Compass**: In slot 1, only usable after team dies
- **Chests**: Contain random kits, opening is announced
- **Death**: Player becomes spectator until team eliminated
- **Spectate**: Left-click compass to leave or stay and watch

## Troubleshooting

### "Tournament already running!"
- Using `/tournament start` twice
- Solution: Use `/tournament stop`, wait, then start again

### "No teams configured!"
- Config file has no teams section
- Solution: Add teams to `config.yml` and restart server

### No players spawning
- Teams not properly configured
- Solution: Use `/tournament stop`, fix config, restart server, try again

### Chests empty
- No kits defined in config
- Solution: Add kits section to config with items

### Colors not showing on Bedrock
- GeyserMC not installed
- Solution: Install GeyserMC + Floodgate

## Default Configuration

The plugin generates a working config with:
- 4 teams (Red, Blue, Green, Yellow)
- 4 starter kits (Starter, Combat, Building, Utilities)
- 25 loot chests around map
- 300 block world border radius

No changes needed - just run `/tournament start`!

## Tips for Admins

1. **Test mode**: Create small tournaments first to test
2. **Player counts**: Use 4-16 players for best experience
3. **Kit balance**: Adjust rarity values (0.9 = common, 0.1 = rare)
4. **World size**: For 8+ teams, increase `world-border-radius` to 400+
5. **Chest distribution**: About 1 chest per 100 blocks² is good
6. **Announcement spam**: Disable announcements in config if needed
7. **Backup worlds**: Use Multiverse backup commands before tournaments

## Advanced: Adding Custom Materials

Valid Minecraft materials (Spigot 1.20):
- Ores: COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE
- Blocks: OAK_LOG, STONE, DIRT, GRASS_BLOCK, SAND, GRAVEL
- Items: DIAMOND_SWORD, STONE_PICKAXE, BOW, ARROW
- Food: APPLE, STEAK, BREAD, GOLDEN_APPLE, ENDER_PEARL
- Tools: STONE_AXE, WOODEN_PICKAXE, DIAMOND_PICKAXE

Example kit:
```yaml
premium:
  name: "Premium Kit"
  rarity: 0.2
  items:
	1:
	  material: "DIAMOND_SWORD"
	  amount: 1
	2:
	  material: "BOW"
	  amount: 1
	3:
	  material: "ARROW"
	  amount: 64
```

## Need Help?

1. Check server logs: `logs/latest.log`
2. Verify config syntax (YAML is space-sensitive)
3. Ensure Multiverse-Core plugin is running: `/mv list`
4. Try default config again: Delete `config.yml` and restart

Enjoy your tournaments! 🎮
