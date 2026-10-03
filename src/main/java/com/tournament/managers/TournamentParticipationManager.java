package com.tournament.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Holds player-presence rules separately from the tournament lifecycle manager. */
public class TournamentParticipationManager {
    private static final long COMBAT_LOG_LOCK_MILLIS = 60_000L;

    private final TournamentManager tournamentManager;
    private final Map<UUID, Long> combatLogUntil = new HashMap<>();

    public TournamentParticipationManager(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    public boolean isInTournamentWorld(Player player) {
        return tournamentManager.isTournamentRunning()
                && tournamentManager.getCurrentTournament() != null
                && player.getWorld().getName().equals(tournamentManager.getCurrentTournament().getWorldName());
    }

    public void recordCombatLog(UUID playerId) {
        combatLogUntil.put(playerId, System.currentTimeMillis() + COMBAT_LOG_LOCK_MILLIS);
    }

    public long getCombatLogRemainingMillis(UUID playerId) {
        Long until = combatLogUntil.get(playerId);
        if (until == null) return 0L;
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0) combatLogUntil.remove(playerId);
        return Math.max(0L, remaining);
    }
}
