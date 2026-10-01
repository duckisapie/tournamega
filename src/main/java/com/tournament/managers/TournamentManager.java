package com.tournament.managers;

import com.tournament.TournamentPlugin;
import com.tournament.model.LootChest;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.model.Kit;
import com.tournament.utils.ColorUtils;
import com.tournament.utils.PlayerUtils;
import com.tournament.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TournamentManager {
	private TournamentPlugin plugin;
	private TeamManager teamManager;
	private KitManager kitManager;
	private Tournament currentTournament;
	private Map<LootChest, Kit> lootChests;
	private List<UUID> onlinePlayers;

	public TournamentManager(TournamentPlugin plugin, TeamManager teamManager, KitManager kitManager) {
		this.plugin = plugin;
		this.teamManager = teamManager;
		this.kitManager = kitManager;
		this.lootChests = new HashMap<>();
		this.onlinePlayers = new ArrayList<>();
	}

	public void startTournament() {
		if (currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING) {
			plugin.getLogger().warning("Tournament already running!");
			return;
		}

		currentTournament = new Tournament();
		currentTournament.setStatus(Tournament.TournamentStatus.STARTING);

		// Create the world
		String worldName = "Tournament_" + System.currentTimeMillis();
		plugin.getLogger().info("Creating tournament world: " + worldName);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			createTournamentWorld(worldName);

			Bukkit.getScheduler().runTask(plugin, () -> {
				currentTournament.setWorldName(worldName);
				currentTournament.setStartTime(System.currentTimeMillis());

				// Setup teams and spawns
				setupTeamSpawns(worldName);

				// Place loot chests
				placeLootChests(worldName);

				currentTournament.setStatus(Tournament.TournamentStatus.RUNNING);
				broadcastMessage(ChatColor.GOLD + "Tournament started! World: " + worldName);
			});
		});
	}

	private void createTournamentWorld(String worldName) {
		try {
			WorldCreator creator = new WorldCreator(worldName);
			creator.type(WorldType.FLAT);
			creator.generator("CleanroomGenerator");

			World world = Bukkit.createWorld(creator);
			if (world != null) {
				world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
				world.setDifficulty(Difficulty.HARD);
				world.setPVP(true);

				WorldBorder border = world.getWorldBorder();
				border.setCenter(0, 0);
				border.setSize(currentTournament.getWorldBorderSize() * 2);

				plugin.getLogger().info("Tournament world created: " + worldName);
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Failed to create tournament world: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void setupTeamSpawns(String worldName) {
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			plugin.getLogger().severe("World not found: " + worldName);
			return;
		}

		Map<String, Team> teams = teamManager.getAllTeams();
		Random random = new Random();
		int teamsCount = teams.size();

		int index = 0;
		for (String teamId : teams.keySet()) {
			Team team = teams.get(teamId);

			// Generate random spawn location within world border
			double angle = (index * 360.0) / teamsCount;
			double distance = currentTournament.getWorldBorderSize() * 0.7;
			double x = distance * Math.cos(Math.toRadians(angle));
			double z = distance * Math.sin(Math.toRadians(angle));

			// Find ground level
			int y = 64;
			Location spawnLoc = new Location(world, x, y, z);

			Team.SpawnLocation spawn = new Team.SpawnLocation(x, y, z, 0, 0, worldName);
			team.setSpawnLocation(spawn);
			team.setEliminated(false);

			plugin.getLogger().info("Set spawn for team " + team.getName() + " at (" + x + ", " + y + ", " + z + ")");

			index++;
		}
	}

	private void placeLootChests(String worldName) {
		World world = Bukkit.getWorld(worldName);
		if (world == null) return;

		Random random = new Random();
		int chestCount = plugin.getConfig().getInt("chest.count", 25);
		int maxAttempts = chestCount * 3;
		int placed = 0;

		for (int i = 0; i < maxAttempts && placed < chestCount; i++) {
			int x = random.nextInt(currentTournament.getWorldBorderSize() * 2) - currentTournament.getWorldBorderSize();
			int z = random.nextInt(currentTournament.getWorldBorderSize() * 2) - currentTournament.getWorldBorderSize();
			int y = 64;

			Location loc = new Location(world, x, y, z);
			Block block = loc.getBlock();

			if (block.getType() == Material.AIR) {
				block.setType(Material.CHEST);

				Kit kit = kitManager.getRandomKit();
				if (kit != null) {
					LootChest lootChest = new LootChest(loc, kit);
					lootChests.put(lootChest, kit);

					// Fill chest with loot
					org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
					for (ItemStack item : kit.getItems()) {
						chest.getBlockInventory().addItem(item.clone());
					}
					chest.update();

					placed++;
				}
			}
		}

		plugin.getLogger().info("Placed " + placed + " loot chests");
	}

	public void stopTournament() {
		if (currentTournament == null) return;

		currentTournament.setStatus(Tournament.TournamentStatus.ENDED);

		// Delete the tournament world
		if (currentTournament.getWorldName() != null) {
			World world = Bukkit.getWorld(currentTournament.getWorldName());
			if (world != null) {
				for (Player player : world.getPlayers()) {
					player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
				}
			}
		}

		lootChests.clear();
		currentTournament = null;
		teamManager.resetTeams();
		broadcastMessage(ChatColor.RED + "Tournament ended!");
	}

	public void handlePlayerDeath(Player player) {
		if (currentTournament == null || currentTournament.getStatus() != Tournament.TournamentStatus.RUNNING) {
			return;
		}

		Team team = teamManager.getTeamByPlayer(player);
		if (team != null) {
			teamManager.playerToSpectator(player.getUniqueId());
			player.setGameMode(GameMode.SPECTATOR);

			// Keep compass in inventory for world selection
			PlayerUtils.giveWorldCompass(player);

			broadcastMessage(ChatColor.YELLOW + player.getName() + " has been eliminated!");

			if (team.isTeamEliminated()) {
				broadcastMessage(ChatColor.RED + "Team " + team.getName() + " has been completely eliminated!");
				team.setEliminated(true);

				// Show spectators the world selection interface
				for (UUID spectatorUUID : team.getSpectatorPlayers()) {
					Player spectator = Bukkit.getPlayer(spectatorUUID);
					if (spectator != null) {
						showWorldSelectionInterface(spectator, team);
					}
				}
			}
		}
	}

	private void showWorldSelectionInterface(Player player, Team team) {
		// Creates an interface for players to choose world after team elimination
		// This will be enhanced with proper GUI in next phase if needed
		broadcastMessage(ChatColor.RED + "Team " + team.getName().toUpperCase() + " is eliminated! You may:"
				+ ChatColor.YELLOW + " 1) Click compass to change worlds " + ChatColor.GRAY + " or "
				+ ChatColor.YELLOW + "2) Stay and spectate");
	}

	public void broadcastMessage(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	public Tournament getCurrentTournament() {
		return currentTournament;
	}

	public boolean isTournamentRunning() {
		return currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING;
	}

	public void registerChestOpen(Location location) {
		for (LootChest chest : lootChests.keySet()) {
			if (chest.getLocation().equals(location)) {
				if (!chest.isOpened()) {
					chest.setOpened(true);
					Kit kit = lootChests.get(chest);
					broadcastMessage(ChatColor.YELLOW + "A chest containing " + ChatColor.AQUA + kit.getName() + ChatColor.YELLOW + " has been opened!");
				}
				break;
			}
		}
	}
}
