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
import org.bukkit.plugin.Plugin;
import java.util.*;

public class TournamentManager {
    private final TournamentPlugin plugin;
    private final TeamManager teamManager;
    private final KitManager kitManager;
    private final Map<LootChest, Kit> lootChests = new HashMap<>();
    private final Random random = new Random();
    private Tournament currentTournament;
    private int chestTaskId = -1;
    private int durationTaskId = -1;
    private int maxPlayersPerTeam;

    public TournamentManager(TournamentPlugin plugin, TeamManager teamManager, KitManager kitManager) {
        this.plugin = plugin; this.teamManager = teamManager; this.kitManager = kitManager;
    }
    public void startTournament(Player starter) { startTournament(starter, 0L); }
    public void startTournament(Player starter, long durationMillis) {
        if (currentTournament != null) return;
        Plugin multiverse = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiverse == null || !multiverse.isEnabled()) {
            starter.sendMessage(ChatColor.RED + "Multiverse-Core must be installed and enabled to start a tournament."); return;
        }
        String worldName = "tournament_" + System.currentTimeMillis();
        int teamCount = teamManager.getAllTeams().size();
        maxPlayersPerTeam = teamCount == 0 ? 0 : Math.max(1, (int) Math.ceil(Bukkit.getOnlinePlayers().size() / (double) teamCount));
        currentTournament = new Tournament();
        currentTournament.setStatus(Tournament.TournamentStatus.STARTING);
        currentTournament.setWorldName(worldName);
        currentTournament.setWorldBorderSize(plugin.getConfig().getInt("tournament-world.world-border-radius", 300));
        if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create " + worldName + " normal")) {
            currentTournament = null;
            starter.sendMessage(ChatColor.RED + "Multiverse-Core could not create the tournament world."); return;
        }
        waitForWorldAndStart(starter, worldName, durationMillis, 0);
    }
    private void waitForWorldAndStart(Player starter, String worldName, long durationMillis, int attempt) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null && attempt < 100) { waitForWorldAndStart(starter, worldName, durationMillis, attempt + 1); return; }
            if (world == null) {
                currentTournament = null;
                starter.sendMessage(ChatColor.RED + "The tournament world was not loaded by Multiverse-Core."); return;
            }
            finishStartingTournament(starter, world, durationMillis);
        }, attempt == 0 ? 1L : 2L);
    }
    private void finishStartingTournament(Player starter, World world, long durationMillis) {
        if (currentTournament == null) return;
        currentTournament.setStartTime(System.currentTimeMillis());
        world.setDifficulty(Difficulty.HARD); world.setPVP(true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        WorldBorder border = world.getWorldBorder(); border.setCenter(0, 0); border.setSize(currentTournament.getWorldBorderSize() * 2.0D);
        setupTeamSpawns(world);
        currentTournament.setStatus(Tournament.TournamentStatus.RUNNING);
        startEndlessChestSpawner(); if (durationMillis > 0) startDurationTimer(durationMillis);
        for (Player player : Bukkit.getOnlinePlayers()) showTeamSelection(player);
        broadcastMessage(ChatColor.GOLD + "Tournament started. Select a team using a dye in your hotbar.");
    }
    private void startDurationTimer(long durationMillis) {
        long delayTicks = Math.max(1L, durationMillis / 50L);
        durationTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (isTournamentRunning()) { broadcastMessage(ChatColor.GOLD + "The tournament time limit has been reached."); stopTournament(); }
        }, delayTicks);
    }
    private void setupTeamSpawns(World world) {
        Map<String, Team> teams = teamManager.getAllTeams(); int index = 0;
        for (Team team : teams.values()) {
            double angle = (Math.PI * 2 * index++) / Math.max(1, teams.size());
            int x = (int) Math.round(Math.cos(angle) * currentTournament.getWorldBorderSize() * 0.70D);
            int z = (int) Math.round(Math.sin(angle) * currentTournament.getWorldBorderSize() * 0.70D);
            int y = world.getHighestBlockYAt(x, z) + 1;
            team.setSpawnLocation(new Team.SpawnLocation(x + 0.5D, y, z + 0.5D, 0, 0, world.getName()));
            team.setEliminated(false);
        }
    }
    public void showTeamSelection(Player player) {
        if (!isTournamentRunning()) return;
        World world = Bukkit.getWorld(currentTournament.getWorldName()); if (world == null) return;
        String existingTeamId = teamManager.getPlayerTeam(player.getUniqueId());
        if (existingTeamId != null) {
            Team existingTeam = teamManager.getTeam(existingTeamId);
            if (existingTeam != null && existingTeam.getSpawnLocation() != null) {
                Team.SpawnLocation spawn = existingTeam.getSpawnLocation();
                player.teleport(new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch));
            }
            player.setFallDistance(0.0F); player.setGameMode(GameMode.SURVIVAL); return;
        }
        player.teleport(createSafeSelectionPlatform(world)); player.setFallDistance(0.0F); player.setGameMode(GameMode.ADVENTURE);
        Bukkit.getScheduler().runTaskLater(plugin, () -> giveTeamSelectionItems(player), 2L);
    }
    private void giveTeamSelectionItems(Player player) {
        if (!isTournamentRunning() || !player.isOnline() || !player.getWorld().getName().equals(currentTournament.getWorldName())
                || teamManager.getPlayerTeam(player.getUniqueId()) != null) return;
        player.getInventory().clear(); player.getInventory().setArmorContents(null); PlayerUtils.giveWorldCompass(player);
        int slot = 1;
        for (Team team : teamManager.getAllTeams().values()) {
            if (slot >= 9) break;
            ItemStack dye = new ItemStack(Material.valueOf(team.getDyeColor().name() + "_DYE"));
            ItemMeta meta = dye.getItemMeta();
            if (meta != null) { meta.setDisplayName(team.getChatColor() + team.getName()); meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to join this team")); dye.setItemMeta(meta); }
            player.getInventory().setItem(slot++, dye);
        }
        player.getInventory().setHeldItemSlot(1);
        player.sendMessage(ChatColor.GOLD + "Choose your team with a dye. Hotbar slot 1 is reserved for the compass.");
    }
    private Location createSafeSelectionPlatform(World world) {
        Location spawn = world.getSpawnLocation();
        int y = Math.max(world.getHighestBlockYAt(spawn) + 20, world.getMinHeight() + 20);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ() + z).setType(Material.GLASS);
        return new Location(world, spawn.getBlockX() + 0.5D, y + 1, spawn.getBlockZ() + 0.5D);
    }
    public boolean selectTeam(Player player, DyeColor dyeColor) {
        if (!isTournamentRunning()) return false;
        String teamId = teamManager.getTeamIdByDye(dyeColor); if (teamId == null) return false;
        Team team = teamManager.getTeam(teamId); if (team == null) return false;
        if (team.getTotalPlayers() >= maxPlayersPerTeam) { player.sendMessage(ChatColor.RED + "That team is full. Choose another team."); return false; }
        teamManager.addPlayerToTeam(player.getUniqueId(), teamId); player.getInventory().clear(); PlayerUtils.applyTeamArmor(player, team);
        for (Kit kit : kitManager.getStartingKits()) for (ItemStack item : kit.getItems()) giveItemOutsideSlotOne(player, item.clone());
        Team.SpawnLocation spawn = team.getSpawnLocation();
        if (spawn != null) { World world = Bukkit.getWorld(spawn.worldName); if (world != null) player.teleport(new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch)); }
        player.setFallDistance(0.0F); player.setGameMode(GameMode.SURVIVAL);
        broadcastMessage(team.getChatColor() + player.getName() + ChatColor.YELLOW + " joined " + team.getChatColor() + team.getName());
        return true;
    }
    private void giveItemOutsideSlotOne(Player player, ItemStack item) {
        for (int slot = 1; slot < player.getInventory().getSize() && item.getAmount() > 0; slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current == null || current.getType().isAir()) { player.getInventory().setItem(slot, item); return; }
            if (current.isSimilar(item) && current.getAmount() < current.getMaxStackSize()) {
                int added = Math.min(item.getAmount(), current.getMaxStackSize() - current.getAmount());
                current.setAmount(current.getAmount() + added); item.setAmount(item.getAmount() - added);
            }
        }
        if (item.getAmount() > 0) player.getWorld().dropItemNaturally(player.getLocation(), item);
    }
    private void startEndlessChestSpawner() {
        int interval = Math.max(20, plugin.getConfig().getInt("tournament-world.chest.spawn-interval-ticks", 1200));
        chestTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> { if (isTournamentRunning()) placeRandomChest(); }, 20L, interval);
    }
    private void placeRandomChest() {
        World world = Bukkit.getWorld(currentTournament.getWorldName()); Kit kit = kitManager.getRandomKit(); if (world == null || kit == null) return;
        int radius = Math.max(1, currentTournament.getWorldBorderSize() - 2);
        for (int attempt = 0; attempt < 40; attempt++) {
            int x = random.nextInt(radius * 2 + 1) - radius, z = random.nextInt(radius * 2 + 1) - radius;
            int y = world.getHighestBlockYAt(x, z) + 1; Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir() || !block.getRelative(0, 1, 0).getType().isAir() || block.getRelative(0, -1, 0).isPassable()) continue;
            block.setType(Material.CHEST);
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
            for (ItemStack item : kit.getItems()) chest.getBlockInventory().addItem(item.clone());
            chest.update(); lootChests.put(new LootChest(block.getLocation(), kit), kit); return;
        }
    }
    public void stopTournament() {
        if (currentTournament == null) return;
        if (chestTaskId != -1) Bukkit.getScheduler().cancelTask(chestTaskId); chestTaskId = -1;
        if (durationTaskId != -1) Bukkit.getScheduler().cancelTask(durationTaskId); durationTaskId = -1;
        World world = Bukkit.getWorld(currentTournament.getWorldName());
        if (world != null) {
            World mainWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (mainWorld != null) for (Player player : new ArrayList<>(world.getPlayers())) { player.teleport(mainWorld.getSpawnLocation()); player.setGameMode(GameMode.SURVIVAL); }
        }
        lootChests.clear(); teamManager.resetTeams(); currentTournament = null; broadcastMessage(ChatColor.RED + "Tournament ended.");
    }
    public void handlePlayerDeath(Player player) { teamManager.playerToSpectator(player.getUniqueId()); player.setGameMode(GameMode.SPECTATOR); }
    public void broadcastMessage(String message) { Bukkit.broadcastMessage(message); }
    public Tournament getCurrentTournament() { return currentTournament; }
    public TeamManager getTeamManager() { return teamManager; }
    public boolean isTournamentRunning() { return currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING; }
    public boolean isInTournamentWorld(Player player) { return isTournamentRunning() && currentTournament.getWorldName() != null && player.getWorld().getName().equals(currentTournament.getWorldName()); }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public void registerChestOpen(Location location) {}
}
