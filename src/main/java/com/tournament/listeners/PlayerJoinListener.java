package com.tournament.listeners;

import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentParticipationManager;
import com.tournament.managers.TournamentManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final com.tournament.TournamentPlugin plugin;
    private final TournamentManager tournamentManager;
    private final TournamentParticipationManager participationManager;
    public PlayerJoinListener(com.tournament.TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager, TournamentParticipationManager participationManager) { this.plugin = plugin; this.tournamentManager = tournamentManager; this.participationManager = participationManager; }
    @EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
        if (!tournamentManager.isTournamentRunning()) return;
        long remaining = participationManager.getCombatLogRemainingMillis(event.getPlayer().getUniqueId());
        if (remaining > 0) {
            event.getPlayer().kickPlayer(ChatColor.RED + "Combat logging is disabled. Rejoin in " + ((remaining + 999) / 1000) + " seconds.");
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> tournamentManager.showTeamSelection(event.getPlayer()), 1L);
    }
}
