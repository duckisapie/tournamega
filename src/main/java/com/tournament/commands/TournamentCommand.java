package com.tournament.commands;

import com.tournament.TournamentPlugin;
import com.tournament.managers.TournamentManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TournamentCommand implements CommandExecutor {
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("^(\\d+)([hdwm])$", Pattern.CASE_INSENSITIVE);

    private final TournamentPlugin plugin;
    private final TournamentManager tournamentManager;

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

        String action = args[0].toLowerCase(Locale.ROOT);

        switch (action) {
            case "start":
                handleStart(sender, args);
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

    private void handleStart(CommandSender sender, String[] args) {
        if (tournamentManager.isTournamentRunning()) {
            sender.sendMessage(ChatColor.RED + "A tournament is already running!");
            return;
        }

        if (args.length > 2) {
            sender.sendMessage(
                    ChatColor.RED + "Usage: /tournament start [numberh|numberd|numberw|numberm]"
            );
            return;
        }

        long durationMillis = 0L;

        if (args.length == 2) {
            Long parsedDuration = parseDuration(args[1]);

            if (parsedDuration == null) {
                sender.sendMessage(
                        ChatColor.RED
                                + "Invalid duration. Use a positive number followed by h, d, w, or m (for example: 2h)."
                );
                return;
            }

            durationMillis = parsedDuration;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    ChatColor.RED
                            + "Only a player can start a tournament because the starter joins team selection."
            );
            return;
        }

        sender.sendMessage(
                ChatColor.GOLD
                        + "Starting tournament..."
                        + (durationMillis > 0 ? " Time limit: " + args[1] : "")
        );

        tournamentManager.startTournament(player, durationMillis);
    }

    private Long parseDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input.toLowerCase(Locale.ROOT));

        if (!matcher.matches()) {
            return null;
        }

        try {
            long amount = Long.parseLong(matcher.group(1));

            if (amount <= 0) {
                return null;
            }

            long unitMillis = switch (matcher.group(2).charAt(0)) {
                case 'h' -> 60L * 60L * 1000L;
                case 'd' -> 24L * 60L * 60L * 1000L;
                case 'w' -> 7L * 24L * 60L * 60L * 1000L;
                case 'm' -> 30L * 24L * 60L * 60L * 1000L;
                default -> 0L;
            };

            return Math.multiplyExact(amount, unitMillis);
        } catch (NumberFormatException | ArithmeticException exception) {
            return null;
        }
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
            sender.sendMessage(
                    ChatColor.GREEN
                            + "Tournament running in world: "
                            + tournament.getWorldName()
            );
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No tournament is currently running!");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Tournament Commands ===");
        sender.sendMessage(
                ChatColor.GREEN
                        + "/tournament start [2h|2d|2w|2m]"
                        + ChatColor.GRAY
                        + " - Start a tournament, optionally with a time limit"
        );
        sender.sendMessage(
                ChatColor.GREEN
                        + "/tournament stop"
                        + ChatColor.GRAY
                        + " - Stop the current tournament"
        );
        sender.sendMessage(
                ChatColor.GREEN
                        + "/tournament status"
                        + ChatColor.GRAY
                        + " - Check tournament status"
        );
    }
}
