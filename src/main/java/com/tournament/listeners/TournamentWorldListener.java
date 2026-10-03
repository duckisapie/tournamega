package com.tournament.listeners;

import com.tournament.managers.TournamentParticipationManager;
import com.tournament.managers.TournamentManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Keeps tournament participants in the active tournament world until it is stopped. */
public class TournamentWorldListener implements Listener {
    private final TournamentParticipationManager participationManager;

    public TournamentWorldListener(TournamentParticipationManager participationManager) {
        this.participationManager = participationManager;
    private final TournamentManager tournamentManager;

    public TournamentWorldListener(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null && participationManager.isInTournamentWorld(event.getPlayer())
        if (event.getTo() != null && tournamentManager.isInTournamentWorld(event.getPlayer())
                && !event.getTo().getWorld().getName().equals(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot leave the tournament world while the tournament is running.");
        }
    }
}
