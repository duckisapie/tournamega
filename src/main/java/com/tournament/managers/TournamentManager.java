package com.tournament.managers;

import com.tournament.TournamentPlugin;
import com.tournament.model.Kit;
import com.tournament.model.LootChest;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/** Owns the tournament lifecycle and keeps all Bukkit world operations on the server thread. */
public class TournamentManager {
    private final TournamentPlugin plugin;
    private final TeamManager teamManager;
    private final KitManager kitManager;
    private final Map<LootChest, Kit> lootChests = new HashMap<>();
    private final Random random = new Random();
    private Tournament currentTournament;
    private int chestTaskId = -1;

    public TournamentManager(TournamentPlugin plugin, TeamManager teamManager, KitManager kitManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.kitManager = kitManager;
    }

    public void startTournament(Player starter) {
        if (currentTournament != null) return;
        currentTournament = new Tournament();
        currentTournament.setStatus(Tournament.TournamentStatus.STARTING);
        currentTournament.setWorldBorderSize(plugin.getConfig().getInt("tournament-world.world-border-radius", 300));
        String worldName = "tournament_" + System.currentTimeMillis();
        World world = Bukkit.createWorld(new WorldCreator(worldName).environment(World.Environment.NORMAL));
        if (world == null) {
            currentTournament = null;
            starter.sendMessage(ChatColor.RED + "Could not create the tournament world.");
            return;
        }
        currentTournament.setWorldName(worldName);
        currentTournament.setStartTime(System.currentTimeMillis());
        world.setDifficulty(Difficulty.HARD);
        world.setPVP(true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(currentTournament.getWorldBorderSize() * 2.0D);
        setupTeamSpawns(world);
        currentTournament.setStatus(Tournament.TournamentStatus.RUNNING);
        startEndlessChestSpawner();

        // The command issuer is included even if they were not previously in the world.
        for (Player player : Bukkit.getOnlinePlayers()) showTeamSelection(player);
        broadcastMessage(ChatColor.GOLD + "Tournament started. Select a team using a dye in your hotbar.");
    }

    private void setupTeamSpawns(World world) {
        Map<String, Team> teams = teamManager.getAllTeams();
        int index = 0;
        for (Team team : teams.values()) {
            double angle = (Math.PI * 2 * index++) / Math.max(1, teams.size());
            int x = (int) Math.round(Math.cos(angle) * currentTournament.getWorldBorderSize() * .70D);
            int z = (int) Math.round(Math.sin(angle) * currentTournament.getWorldBorderSize() * .70D);
            int y = world.getHighestBlockYAt(x, z) + 1;
            team.setSpawnLocation(new Team.SpawnLocation(x + .5D, y, z + .5D, 0, 0, world.getName()));
            team.setEliminated(false);
        }
    }

    public void showTeamSelection(Player player) {
        if (!isTournamentRunning()) return;
        World world = Bukkit.getWorld(currentTournament.getWorldName());
        if (world == null) return;
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        int slot = 0; // Minecraft's displayed slot 1 is inventory index 0.
        for (Team team : teamManager.getAllTeams().values()) {
            if (slot >= 9) break;
            ItemStack dye = new ItemStack(Material.valueOf(team.getDyeColor().name() + "_DYE"));
            ItemMeta meta = dye.getItemMeta();
            meta.setDisplayName(team.getChatColor() + team.getName());
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Left- or right-click to join this team"));
            dye.setItemMeta(meta);
            player.getInventory().setItem(slot++, dye);
        }
        Location selection = world.getSpawnLocation().clone().add(.5D, 45D, .5D);
        player.teleport(selection);
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(ChatColor.GOLD + "Choose your team with a dye from hotbar slot 1 onward.");
    }

    public boolean selectTeam(Player player, DyeColor dyeColor) {
        if (!isTournamentRunning()) return false;
        String teamId = teamManager.getTeamIdByDye(dyeColor);
        if (teamId == null) return false;
        Team team = teamManager.getTeam(teamId);
        teamManager.addPlayerToTeam(player.getUniqueId(), teamId);
        player.getInventory().clear();
        PlayerUtils.applyTeamArmor(player, team);
        for (Kit kit : kitManager.getStartingKits()) {
            for (ItemStack item : kit.getItems()) player.getInventory().addItem(item.clone());
        }
        Team.SpawnLocation spawn = team.getSpawnLocation();
        player.teleport(new Location(Bukkit.getWorld(spawn.worldName), spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch));
        player.setGameMode(GameMode.SURVIVAL);
        broadcastMessage(team.getChatColor() + player.getName() + ChatColor.YELLOW + " joined " + team.getChatColor() + team.getName());
        return true;
    }

    private void startEndlessChestSpawner() {
        int interval = Math.max(20, plugin.getConfig().getInt("tournament-world.chest.spawn-interval-ticks", 1200));
        chestTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (isTournamentRunning()) placeRandomChest();
        }, 20L, interval);
    }

    private void placeRandomChest() {
        World world = Bukkit.getWorld(currentTournament.getWorldName());
        Kit kit = kitManager.getRandomKit();
        if (world == null || kit == null) return;
        int radius = currentTournament.getWorldBorderSize() - 2;
        for (int attempt = 0; attempt < 40; attempt++) {
            int x = random.nextInt(radius * 2 + 1) - radius;
            int z = random.nextInt(radius * 2 + 1) - radius;
            int y = world.getHighestBlockYAt(x, z) + 1;
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir() || !block.getRelative(0, 1, 0).getType().isAir() || block.getRelative(0, -1, 0).isPassable()) continue;
            block.setType(Material.CHEST);
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
            for (ItemStack item : kit.getItems()) chest.getBlockInventory().addItem(item.clone());
            chest.update();
            lootChests.put(new LootChest(block.getLocation(), kit), kit);
            return;
        }
    }

    public void stopTournament() {
        if (currentTournament == null) return;
        if (chestTaskId != -1) Bukkit.getScheduler().cancelTask(chestTaskId);
        chestTaskId = -1;
        World world = Bukkit.getWorld(currentTournament.getWorldName());
        if (world != null) for (Player player : world.getPlayers()) player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        lootChests.clear();
        teamManager.resetTeams();
        currentTournament = null;
        broadcastMessage(ChatColor.RED + "Tournament ended.");
    }

    public void handlePlayerDeath(Player player) { teamManager.playerToSpectator(player.getUniqueId()); }
    public void broadcastMessage(String message) { Bukkit.broadcastMessage(message); }
    public Tournament getCurrentTournament() { return currentTournament; }
    public TeamManager getTeamManager() { return teamManager; }
    public boolean isTournamentRunning() { return currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING; }
    public void registerChestOpen(Location location) { }
}
