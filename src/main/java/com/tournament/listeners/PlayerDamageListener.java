package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentManager;
import com.tournament.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private TeamManager teamManager;

	public PlayerDamageListener(TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.teamManager = teamManager;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player damaged = (Player) event.getEntity();
		Player damager = null;

		if (event.getDamager() instanceof Player) {
			damager = (Player) event.getDamager();
		}

		if (!tournamentManager.isTournamentRunning()) {
			return;
		}

		// Prevent damage within own team would go here if we wanted team-friendly fire control
		// For now, allow all PvP
	}
}
