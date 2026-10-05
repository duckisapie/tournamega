package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TournamentManager;
import com.tournament.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class CompassClickListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private TeamManager teamManager;

	public CompassClickListener(TournamentPlugin plugin, TournamentManager tournamentManager, TeamManager teamManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.teamManager = teamManager;
	}

	@EventHandler
	public void onCompassClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		// Check if player is holding compass in slot 1
		if (item == null || item.getType() != Material.COMPASS) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Check if player is in spectator mode (team eliminated or no team selected)
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// Check if player is in selection mode and hasn't chosen a team yet
				if (!tournamentManager.isInSelection(player.getUniqueId())) {
					return;
				}
			}

			if (!tournamentManager.isTournamentRunning()) {
				return;
			}

			// Teleport to main world (default spawn)
			Location mainSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
			player.teleport(mainSpawn);

			// Remove from tournament
			teamManager.removePlayer(player.getUniqueId());
			tournamentManager.cancelSelectionTimeout(player.getUniqueId());
			player.setGameMode(GameMode.SURVIVAL);

			plugin.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has left the tournament!");
		}
	}
}
