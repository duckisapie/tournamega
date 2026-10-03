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

/** Owns the tournament lifecycle and keeps all Bukkit world operations on the server thread. */
public class TournamentManager {
    private final TournamentPlugin plugin;
    private final TeamManager teamManager;
    private final KitManager kitManager;
    private final Map<LootChest, Kit> lootChests = new HashMap<>();
    private final Random random = new Random();
    private Tournament currentTournament;
    private int chestTaskId = -1;
    private int maxPlayersPerTeam;
    private final Map<UUID, Long> combatLogUntil = new HashMap<>();

    public TournamentManager(TournamentPlugin plugin, TeamManager teamManager, KitManager kitManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.kitManager = kitManager;
    }

    public void startTournament(Player starter) {
        if (currentTournament != null) return;
        Plugin multiverse = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiverse == null || !multiverse.isEnabled()) {
            starter.sendMessage(ChatColor.RED + "Multiverse-Core must be installed and enabled to start a tournament.");
            return;
        }
        currentTournament = new Tournament();
        currentTournament.setStatus(Tournament.TournamentStatus.STARTING);
        currentTournament.setWorldBorderSize(plugin.getConfig().getInt("tournament-world.world-border-radius", 300));
        int teamCount = teamManager.getAllTeams().size();
        maxPlayersPerTeam = teamCount == 0 ? 0 : (int) Math.ceil(Bukkit.getOnlinePlayers().size() / (double) teamCount);
        String worldName = "tournament_" + System.currentTimeMillis();
        // Delegate world creation to Multiverse so its world configuration and PerWorldInventory
        // integration are applied before players are moved into the tournament world.
        boolean commandAccepted = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create " + worldName + " normal");
        if (!commandAccepted) {
            currentTournament = null;
            starter.sendMessage(ChatColor.RED + "Multiverse-Core could not create the tournament world.");
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> finishStartingTournament(starter, worldName));
    }

    private void finishStartingTournament(Player starter, String worldName) {
        World world = Bukkit.getWorld(worldName);
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

        // Teleport first. PerWorldInventory changes a player's inventory during this transfer,
        // so the dyes are granted in showTeamSelection's delayed task, afterwards.
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
        String existingTeamId = teamManager.getPlayerTeam(player.getUniqueId());
        if (existingTeamId != null) {
            Team existingTeam = teamManager.getTeam(existingTeamId);
            Team.SpawnLocation spawn = existingTeam.getSpawnLocation();
            player.teleport(new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch));
            player.setFallDistance(0.0F);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }
        Location selection = createSafeSelectionPlatform(world);
        player.teleport(selection);
        player.setFallDistance(0.0F);
        player.setGameMode(GameMode.SURVIVAL);
        Bukkit.getScheduler().runTaskLater(plugin, () -> giveTeamSelectionItems(player), 2L);
    }

    private void giveTeamSelectionItems(Player player) {
        if (!isTournamentRunning() || !player.isOnline()
                || !player.getWorld().getName().equals(currentTournament.getWorldName())
                || teamManager.getPlayerTeam(player.getUniqueId()) != null) return;
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        // Leave displayed slot 1 (inventory index 0) empty for the server's world compass.
        int slot = 1;
        for (Team team : teamManager.getAllTeams().values()) {
            if (slot >= 9) break;
            ItemStack dye = new ItemStack(Material.valueOf(team.getDyeColor().name() + "_DYE"));
            ItemMeta meta = dye.getItemMeta();
            meta.setDisplayName(team.getChatColor() + team.getName());
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Left- or right-click to join this team"));
            dye.setItemMeta(meta);
            player.getInventory().setItem(slot++, dye);
        }
        player.getInventory().setHeldItemSlot(1);
        player.sendMessage(ChatColor.GOLD + "Choose your team with a dye from hotbar slot 2 onward. Hotbar slot 1 stays empty.");
    }

    private Location createSafeSelectionPlatform(World world) {
        Location spawn = world.getSpawnLocation();
        int y = Math.max(world.getHighestBlockYAt(spawn) + 20, world.getMinHeight() + 20);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ() + z).setType(Material.GLASS);
            }
        }
        return new Location(world, spawn.getBlockX() + .5D, y + 1, spawn.getBlockZ() + .5D);
    }

    public boolean selectTeam(Player player, DyeColor dyeColor) {
        if (!isTournamentRunning()) return false;
        String teamId = teamManager.getTeamIdByDye(dyeColor);
        if (teamId == null) return false;
        Team team = teamManager.getTeam(teamId);
        if (team.getTotalPlayers() >= maxPlayersPerTeam) {
            player.sendMessage(ChatColor.RED + "That team is full. Choose another team.");
            return false;
        }
        teamManager.addPlayerToTeam(player.getUniqueId(), teamId);
        player.getInventory().clear();
        PlayerUtils.applyTeamArmor(player, team);
        for (Kit kit : kitManager.getStartingKits()) {
            for (ItemStack item : kit.getItems()) giveItemOutsideSlotOne(player, item.clone());
        }
        Team.SpawnLocation spawn = team.getSpawnLocation();
        player.teleport(new Location(Bukkit.getWorld(spawn.worldName), spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch));
        player.setFallDistance(0.0F);
        player.setGameMode(GameMode.SURVIVAL);
        broadcastMessage(team.getChatColor() + player.getName() + ChatColor.YELLOW + " joined " + team.getChatColor() + team.getName());
        return true;
    }

    private void giveItemOutsideSlotOne(Player player, ItemStack item) {
        for (int slot = 1; slot < player.getInventory().getSize() && item.getAmount() > 0; slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current == null || current.getType().isAir()) {
                player.getInventory().setItem(slot, item);
                return;
            }
            if (current.isSimilar(item) && current.getAmount() < current.getMaxStackSize()) {
                int added = Math.min(item.getAmount(), current.getMaxStackSize() - current.getAmount());
                current.setAmount(current.getAmount() + added);
                item.setAmount(item.getAmount() - added);
            }
        }
        if (item.getAmount() > 0) player.getWorld().dropItemNaturally(player.getLocation(), item);
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
        combatLogUntil.clear();
        currentTournament = null;
        broadcastMessage(ChatColor.RED + "Tournament ended.");
    }

    public void handlePlayerDeath(Player player) { teamManager.playerToSpectator(player.getUniqueId()); }
    public void broadcastMessage(String message) { Bukkit.broadcastMessage(message); }
    public Tournament getCurrentTournament() { return currentTournament; }
    public TeamManager getTeamManager() { return teamManager; }
    public boolean isTournamentRunning() { return currentTournament != null && currentTournament.getStatus() == Tournament.TournamentStatus.RUNNING; }
    public boolean isInTournamentWorld(Player player) {
        return isTournamentRunning() && player.getWorld().getName().equals(currentTournament.getWorldName());
    }
    public void recordCombatLog(UUID playerId) { combatLogUntil.put(playerId, System.currentTimeMillis() + 60_000L); }
    public long getCombatLogRemainingMillis(UUID playerId) {
        Long until = combatLogUntil.get(playerId);
        if (until == null) return 0L;
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0) combatLogUntil.remove(playerId);
        return Math.max(0L, remaining);
    }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public void registerChestOpen(Location location) { }
}
