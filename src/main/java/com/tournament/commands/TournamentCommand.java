package com.tournament.commands;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TournamentManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TournamentCommand implements CommandExecutor {
	private TournamentPlugin plugin;
	private TournamentManager tournamentManager;

	public TournamentCommand(TournamentPlugin plugin, TournamentManager tournamentManager) {
		this.plugin = plugin;
		this.tournamentManager = tournamentManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("tournament.admin")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
			return true;
		}

		if (args.length == 0) {
			sendHelp(sender);
			return true;
		}

		String action = args[0].toLowerCase();

		switch (action) {
			case "start":
				handleStart(sender);
				return true;
			case "stop":
				handleStop(sender);
				return true;
			case "status":
				handleStatus(sender);
				return true;
			default:
				sendHelp(sender);
				return true;
		}
	}

	private void handleStart(CommandSender sender) {
		if (tournamentManager.isTournamentRunning()) {
			sender.sendMessage(ChatColor.RED + "A tournament is already running!");
			return;
		}

		sender.sendMessage(ChatColor.GOLD + "Starting tournament...");
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only a player can start a tournament because the starter joins team selection.");
			return;
		}
		tournamentManager.startTournament((Player) sender);
	}

	private void handleStop(CommandSender sender) {
		if (!tournamentManager.isTournamentRunning()) {
			sender.sendMessage(ChatColor.RED + "No tournament is running!");
			return;
		}

		sender.sendMessage(ChatColor.GOLD + "Stopping tournament...");
		tournamentManager.stopTournament();
	}

	private void handleStatus(CommandSender sender) {
		if (tournamentManager.isTournamentRunning()) {
			var tournament = tournamentManager.getCurrentTournament();
			sender.sendMessage(ChatColor.GREEN + "Tournament running in world: " + tournament.getWorldName());
		} else {
			sender.sendMessage(ChatColor.YELLOW + "No tournament is currently running!");
		}
	}

	private void sendHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "=== Tournament Commands ===");
		sender.sendMessage(ChatColor.GREEN + "/tournament start" + ChatColor.GRAY + " - Start a new tournament");
		sender.sendMessage(ChatColor.GREEN + "/tournament stop" + ChatColor.GRAY + " - Stop the current tournament");
		sender.sendMessage(ChatColor.GREEN + "/tournament status" + ChatColor.GRAY + " - Check tournament status");
	}
}
