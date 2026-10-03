package com.tournament;

import com.tournament.commands.TournamentCommand;
import com.tournament.listeners.*;
import com.tournament.managers.KitManager;
import com.tournament.managers.TeamManager;
import com.tournament.managers.TournamentManager;
import com.tournament.managers.TournamentParticipationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TournamentPlugin extends JavaPlugin {

    private TeamManager teamManager;
    private KitManager kitManager;
    private TournamentManager tournamentManager;
    private TournamentParticipationManager participationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.teamManager = new TeamManager(this);
        this.kitManager = new KitManager(this);
        this.tournamentManager = new TournamentManager(this, teamManager, kitManager);
        this.participationManager = new TournamentParticipationManager(tournamentManager);

        getCommand("tournament").setExecutor(new TournamentCommand(this, tournamentManager));

        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this, tournamentManager, teamManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, tournamentManager, teamManager, participationManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this, tournamentManager, teamManager, participationManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockInteractListener(this, tournamentManager, kitManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(this, tournamentManager, teamManager), this);
        Bukkit.getPluginManager().registerEvents(new CompassClickListener(this, tournamentManager, teamManager), this);
        Bukkit.getPluginManager().registerEvents(new TeamSelectionListener(tournamentManager), this);
        Bukkit.getPluginManager().registerEvents(new SlotOneListener(tournamentManager), this);
        Bukkit.getPluginManager().registerEvents(new TournamentWorldListener(tournamentManager), this);

        getLogger().info("TournamentPlugin enabled successfully!");
    }

    @Override
    public void onDisable() {
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
