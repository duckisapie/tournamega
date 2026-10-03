package com.tournament.listeners;

import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentParticipationManager;
import com.tournament.managers.TournamentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final TournamentManager tournamentManager;
    private final TournamentParticipationManager participationManager;
    public PlayerQuitListener(com.tournament.TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager, TournamentParticipationManager participationManager) {
        this.tournamentManager = tournamentManager; this.participationManager = participationManager;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (tournamentManager.isTournamentRunning() && participationManager.isInTournamentWorld(player)) participationManager.recordCombatLog(player.getUniqueId());
    }
}
