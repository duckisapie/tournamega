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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.util.*;

public class TournamentManager {
    private final TournamentPlugin plugin;
    private final TeamManager teamManager;
    private final KitManager kitManager;
    private final Map<LootChest, Kit> lootChests = new HashMap<>();
    private final Set<UUID> playersInSelection = new HashSet<>();
    private final Map<UUID, Integer> selectionTaskIds = new HashMap<>();
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final List<Location> placedChestLocations = new ArrayList<>();
    private final Random random = new Random();
    private Tournament currentTournament;
    private int chestTaskId = -1;
    private int durationTaskId = -1;
    private int maxPlayersPerTeam;
    private int minChestDistance;
    private int chestSpawnRadius;

    public TournamentManager(TournamentPlugin plugin, TeamManager teamManager, KitManager kitManager) {
        this.plugin = plugin; this.teamManager = teamManager; this.kitManager = kitManager;
        this.minChestDistance = plugin.getConfig().getInt("tournament-world.chest.min-distance", 50);
        this.chestSpawnRadius = plugin.getConfig().getInt("tournament-world.chest.spawn-radius", 200);
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
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
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
        // Store original location when first entering tournament
        if (!originalLocations.containsKey(player.getUniqueId())) {
            originalLocations.put(player.getUniqueId(), player.getLocation().clone());
        }
        String existingTeamId = teamManager.getPlayerTeam(player.getUniqueId());
        if (existingTeamId != null) {
            Team existingTeam = teamManager.getTeam(existingTeamId);
            if (existingTeam != null && existingTeam.getSpawnLocation() != null) {
                Team.SpawnLocation spawn = existingTeam.getSpawnLocation();
                player.teleport(new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch));
            }
            player.setFallDistance(0.0F); player.setGameMode(GameMode.SURVIVAL); return;
        }
        playersInSelection.add(player.getUniqueId());
        player.teleport(createSafeSelectionPlatform(world)); player.setFallDistance(0.0F); player.setGameMode(GameMode.ADVENTURE);
        Bukkit.getScheduler().runTaskLater(plugin, () -> giveTeamSelectionItems(player), 2L);
        startSelectionTimeout(player);
    }
    private void startSelectionTimeout(Player player) {
        int timeoutSeconds = plugin.getConfig().getInt("tournament-world.team-selection-timeout-seconds", 30);
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (isTournamentRunning() && playersInSelection.contains(player.getUniqueId())) {
                playersInSelection.remove(player.getUniqueId());
                // Return to original world instead of spectator
                Location original = originalLocations.get(player.getUniqueId());
                if (original != null) {
                    player.teleport(original);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(ChatColor.GRAY + "Team selection timed out. Returned to your original world.");
                } else {
                    // Fallback to main world spawn
                    World mainWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                    if (mainWorld != null) {
                        player.teleport(mainWorld.getSpawnLocation());
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }
            selectionTaskIds.remove(player.getUniqueId());
        }, timeoutSeconds * 20L);
        selectionTaskIds.put(player.getUniqueId(), taskId);
    }
    public void cancelSelectionTimeout(UUID playerId) {
        Integer taskId = selectionTaskIds.remove(playerId);
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
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
        playersInSelection.remove(player.getUniqueId());
        cancelSelectionTimeout(player.getUniqueId());
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
        int radius = Math.max(1, chestSpawnRadius);
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(radius * 2 + 1) - radius, z = random.nextInt(radius * 2 + 1) - radius;
            int y = world.getHighestBlockYAt(x, z) + 1; 
            Location chestLoc = new Location(world, x, y, z);
            
            boolean tooClose = false;
            for (Location existing : placedChestLocations) {
                if (existing.getWorld() != world) continue;
                double distance = existing.distance(chestLoc);
                if (distance < minChestDistance) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;
            
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir() || !block.getRelative(0, 1, 0).getType().isAir() || block.getRelative(0, -1, 0).isPassable()) continue;
            block.setType(Material.CHEST);
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
            for (ItemStack item : kit.getItems()) chest.getBlockInventory().addItem(item.clone());
            chest.update(); lootChests.put(new LootChest(block.getLocation(), kit), kit);
            placedChestLocations.add(chestLoc);
            return;
        }
        for (int attempt = 0; attempt < 40; attempt++) {
            int x = random.nextInt(radius * 2 + 1) - radius, z = random.nextInt(radius * 2 + 1) - radius;
            int y = world.getHighestBlockYAt(x, z) + 1; Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir() || !block.getRelative(0, 1, 0).getType().isAir() || block.getRelative(0, -1, 0).isPassable()) continue;
            block.setType(Material.CHEST);
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
            for (ItemStack item : kit.getItems()) chest.getBlockInventory().addItem(item.clone());
            chest.update(); lootChests.put(new LootChest(block.getLocation(), kit), kit);
            placedChestLocations.add(new Location(world, x, y, z));
            return;
        }
    }
    public void stopTournament() {
        if (currentTournament == null) return;
        if (chestTaskId != -1) Bukkit.getScheduler().cancelTask(chestTaskId); chestTaskId = -1;
        if (durationTaskId != -1) Bukkit.getScheduler().cancelTask(durationTaskId); durationTaskId = -1;
        World world = Bukkit.getWorld(currentTournament.getWorldName());
        if (world != null) {
            for (Player player : new ArrayList<>(world.getPlayers())) {
                Location original = originalLocations.get(player.getUniqueId());
                if (original != null) {
                    player.teleport(original);
                } else {
                    World mainWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                    if (mainWorld != null) player.teleport(mainWorld.getSpawnLocation());
                }
                player.setGameMode(GameMode.SURVIVAL);
                player.setInvisible(false);
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
        lootChests.clear(); teamManager.resetTeams(); currentTournament = null; originalLocations.clear(); placedChestLocations.clear(); broadcastMessage(ChatColor.RED + "Tournament ended.");
    }
    public void handlePlayerDeath(Player player) {
        teamManager.playerToSpectator(player.getUniqueId());
        
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setCanPickupItems(false);
        
        Team team = teamManager.getTeamByPlayer(player);
        String teamName = team != null ? team.getChatColor() + team.getName() + ChatColor.RESET : ChatColor.GRAY + "Unknown";
        broadcastMessage(teamName + ChatColor.RED + " " + player.getName() + ChatColor.GRAY + " has died!");
        
        checkTournamentEnd();
    }
    public void broadcastMessage(String message) { Bukkit.broadcastMessage(message); }
    public Tournament getCurrentTournament() { return currentTournament; }
    public TeamManager getTeamManager() { return teamManager; }
    public boolean isTournamentRunning() { return currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING; }
    public boolean isInTournamentWorld(Player player) { return isTournamentRunning() && currentTournament.getWorldName() != null && player.getWorld().getName().equals(currentTournament.getWorldName()); }
    public boolean isInSelection(UUID playerId) { return playersInSelection.contains(playerId); }
    
    private void checkTournamentEnd() {
        if (!isTournamentRunning()) return;
        
        int aliveTeams = 0;
        int totalActivePlayers = 0;
        
        for (Team team : teamManager.getAllTeams().values()) {
            if (!team.isTeamEliminated()) {
                aliveTeams++;
                totalActivePlayers += team.getActivePlayers().size();
            }
        }
        
        // End tournament if only one team is alive or all players are dead
        if (aliveTeams <= 1 || totalActivePlayers <= 1) {
            Team winningTeam = null;
            for (Team team : teamManager.getAllTeams().values()) {
                if (!team.isTeamEliminated()) {
                    winningTeam = team;
                    break;
                }
            }
            
            if (winningTeam != null) {
                broadcastMessage(winningTeam.getChatColor() + winningTeam.getName() + ChatColor.GOLD + " has won the tournament!");
            } else {
                broadcastMessage(ChatColor.GOLD + "The tournament ended with no winners!");
            }
            
            stopTournament();
        }
    }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public void registerChestOpen(Location location) {}
}
