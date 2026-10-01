package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentManager;
import com.tournament.model.Team;
import com.tournament.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private TeamManager teamManager;

	public PlayerJoinListener(TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.teamManager = teamManager;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!tournamentManager.isTournamentRunning()) {
			return;
		}

		var tournament = tournamentManager.getCurrentTournament();
		String worldName = tournament.getWorldName();

		// Assign player to first available team
		String assignedTeam = null;
		for (String teamId : teamManager.getAllTeams().keySet()) {
			assignedTeam = teamId;
			break;
		}

		if (assignedTeam == null) {
			player.sendMessage(ChatColor.RED + "No teams configured!");
			return;
		}

		teamManager.addPlayerToTeam(player.getUniqueId(), assignedTeam);
		Team team = teamManager.getTeam(assignedTeam);

		// Teleport to team spawn location
		if (team.getSpawnLocation() != null) {
			Team.SpawnLocation spawn = team.getSpawnLocation();
			Location spawnLoc = new Location(
					plugin.getServer().getWorld(spawn.worldName),
					spawn.x, spawn.y + 1, spawn.z,
					spawn.yaw, spawn.pitch
			);
			player.teleport(spawnLoc);
		}

		// Apply team armor
		PlayerUtils.applyTeamArmor(player, team);

		// Give world compass in slot 1
		PlayerUtils.giveWorldCompass(player);

		// Set game mode to survival
		player.setGameMode(GameMode.SURVIVAL);

		// Announce player join with team color
		plugin.getServer().broadcastMessage(
				ChatColor.RED + "[" + team.getName() + "] " + ChatColor.YELLOW + player.getName() + " has joined the tournament!"
		);
	}
}
