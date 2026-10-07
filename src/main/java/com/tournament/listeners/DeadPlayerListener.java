package com.tournament.listeners;

import com.tournament.managers.TournamentManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeadPlayerListener implements Listener {
    private final TournamentManager tournamentManager;

    public DeadPlayerListener(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!tournamentManager.isTournamentRunning()) return;
        if (!tournamentManager.isInTournamentWorld(player)) return;
        
        if (player.getGameMode() == GameMode.CREATIVE && player.isInvisible()) {
            event.setRespawnLocation(player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!tournamentManager.isTournamentRunning()) return;
        if (!tournamentManager.isInTournamentWorld(player)) return;
        
        if (player.getGameMode() == GameMode.CREATIVE && player.isInvisible()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!tournamentManager.isTournamentRunning()) return;
        if (!tournamentManager.isInTournamentWorld(player)) return;
        
        if (player.getGameMode() == GameMode.CREATIVE && player.isInvisible()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!tournamentManager.isTournamentRunning()) return;
        if (!tournamentManager.isInTournamentWorld(player)) return;
        
        if (player.getGameMode() == GameMode.CREATIVE && player.isInvisible()) {
            event.setCancelled(true);
        }
    }
}
