package com.tournament;

import com.tournament.managers.KitManager;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentManager;
import com.tournament.commands.TournamentCommand;
import com.tournament.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TournamentPlugin extends JavaPlugin {

	private TeamManager teamManager;
	private KitManager kitManager;
	private TournamentManager tournamentManager;

	@Override
	public void onEnable() {
		// Save default config if it doesn't exist
		saveDefaultConfig();

		// Initialize managers
		this.teamManager = new TeamManager(this);
		this.kitManager = new KitManager(this);
		this.tournamentManager = new TournamentManager(this, teamManager, kitManager);

		// Register commands
		getCommand("tournament").setExecutor(new TournamentCommand(this, tournamentManager));

		// Register event listeners
		Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this, tournamentManager, teamManager), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, tournamentManager, teamManager), this);
		Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this, tournamentManager, teamManager), this);
		Bukkit.getPluginManager().registerEvents(new BlockInteractListener(this, tournamentManager, kitManager), this);
		Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(this, tournamentManager, teamManager), this);
		Bukkit.getPluginManager().registerEvents(new CompassClickListener(this, tournamentManager, teamManager), this);

		getLogger().info("TournamentPlugin enabled successfully!");
	}

	@Override
	public void onDisable() {
		// Stop any active tournament
		if (tournamentManager != null) {
			tournamentManager.stopTournament();
		}

		getLogger().info("TournamentPlugin disabled!");
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}

	public KitManager getKitManager() {
		return kitManager;
	}

	public TournamentManager getTournamentManager() {
		return tournamentManager;
	}
}
