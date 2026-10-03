package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentParticipationManager;
import com.tournament.managers.TournamentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private TeamManager teamManager;
	private TournamentParticipationManager participationManager;

	public PlayerQuitListener(TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager, TournamentParticipationManager participationManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.teamManager = teamManager;
		this.participationManager = participationManager;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (!tournamentManager.isTournamentRunning()) {
			return;
		}

		if (participationManager.isInTournamentWorld(player)) {
			participationManager.recordCombatLog(player.getUniqueId());
		if (tournamentManager.isInTournamentWorld(player)) {
			tournamentManager.recordCombatLog(player.getUniqueId());
		}
	}
}
