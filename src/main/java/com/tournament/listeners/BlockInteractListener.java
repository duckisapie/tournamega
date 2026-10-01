package com.tournament.listeners;

import com.tournament.TournamentPlugin;
import com.tournament.managers.KitManager;
import com.tournament.managers.TournamentManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractListener implements Listener {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;
	private KitManager kitManager;

	public BlockInteractListener(TournamentPlugin plugin, TournamentManager tournamentManager, KitManager kitManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
		this.kitManager = kitManager;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block block = event.getClickedBlock();
		if (block == null || block.getType() != Material.CHEST) {
			return;
		}

		if (!tournamentManager.isTournamentRunning()) {
			return;
		}

		// Announce chest opening
		tournamentManager.registerChestOpen(block.getLocation());
	}
}
