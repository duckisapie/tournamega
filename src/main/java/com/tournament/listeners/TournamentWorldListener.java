package com.tournament.listeners;

import com.tournament.managers.TournamentManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Keeps players in the active tournament world until the tournament is stopped. */
public class TournamentWorldListener implements Listener {

    private final TournamentManager tournamentManager;

    public TournamentWorldListener(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }

        if (!tournamentManager.isInTournamentWorld(event.getPlayer())) {
            return;
        }

        if (!event.getTo().getWorld().getName()
                .equals(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    "You cannot leave the tournament world while the tournament is running."
            );
        }
    }
}
