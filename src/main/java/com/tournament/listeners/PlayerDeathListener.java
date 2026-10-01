package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentManager;
import com.tournament.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private TeamManager teamManager;

	public PlayerDeathListener(TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.teamManager = teamManager;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		if (!tournamentManager.isTournamentRunning()) {
			return;
		}

		Team team = teamManager.getTeamByPlayer(player);
		if (team == null) {
			return;
		}

		// Handle death - move to spectator and check if team is eliminated
		tournamentManager.handlePlayerDeath(player);
	}
}
