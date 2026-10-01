package com.tournament.managers;

import com.tournament.TournamentPlugin;
import com.tournament.model.Team;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {
	private TournamentPlugin plugin;
	private Map<String, Team> teams;
	private Map<UUID, String> playerTeamMap;

	public TeamManager(TournamentPlugin plugin) {
		this.plugin = plugin;
		this.teams = new HashMap<>();
		this.playerTeamMap = new HashMap<>();
		loadTeamsFromConfig();
	}

	private void loadTeamsFromConfig() {
		ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");
		if (teamsSection == null) {
			plugin.getLogger().warning("No teams configured in config.yml");
			return;
		}

		for (String teamKey : teamsSection.getKeys(false)) {
			ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamKey);
			String teamName = teamSection.getString("name", teamKey);
			String colorStr = teamSection.getString("color", "WHITE").toUpperCase();

			try {
				ChatColor chatColor = ChatColor.valueOf(colorStr);
				DyeColor dyeColor = DyeColor.valueOf(colorStr);
				Color armorColor = dyeColor.getColor();

				Team team = new Team(teamName, chatColor, dyeColor, armorColor);
				teams.put(teamKey, team);
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("Invalid color for team " + teamKey + ": " + colorStr);
			}
		}

		plugin.getLogger().info("Loaded " + teams.size() + " teams from config");
	}

	public Team getTeam(String teamId) {
		return teams.get(teamId);
	}

	public Map<String, Team> getAllTeams() {
		return new HashMap<>(teams);
	}

	public void addPlayerToTeam(UUID playerUUID, String teamId) {
		Team team = teams.get(teamId);
		if (team != null) {
			team.addPlayer(playerUUID, false);
			playerTeamMap.put(playerUUID, teamId);
		}
	}

	public String getPlayerTeam(UUID playerUUID) {
		return playerTeamMap.get(playerUUID);
	}

	public void removePlayer(UUID playerUUID) {
		String teamId = playerTeamMap.remove(playerUUID);
		if (teamId != null && teams.containsKey(teamId)) {
			teams.get(teamId).removePlayer(playerUUID);
		}
	}

	public void playerToSpectator(UUID playerUUID) {
		String teamId = playerTeamMap.get(playerUUID);
		if (teamId != null && teams.containsKey(teamId)) {
			teams.get(teamId).playerToSpectator(playerUUID);
		}
	}

	public void resetTeams() {
		for (Team team : teams.values()) {
			team.getActivePlayers().clear();
			team.getSpectatorPlayers().clear();
			team.setEliminated(false);
		}
		playerTeamMap.clear();
	}

	public Team getTeamByPlayer(Player player) {
		String teamId = playerTeamMap.get(player.getUniqueId());
		return teamId != null ? teams.get(teamId) : null;
	}

	public boolean isTeamEliminated(String teamId) {
		Team team = teams.get(teamId);
		return team != null && team.isTeamEliminated();
	}

	public int getEliminatedTeamCount() {
		int count = 0;
		for (Team team : teams.values()) {
			if (team.isTeamEliminated()) {
				count++;
			}
		}
		return count;
	}
}
