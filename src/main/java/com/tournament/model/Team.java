package com.tournament.model;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import java.util.*;

public class Team {
	private String name;
	private ChatColor chatColor;
	private DyeColor dyeColor;
	private Color armorColor;
	private Set<UUID> activePlayers;
	private Set<UUID> spectatorPlayers;
	private SpawnLocation spawnLocation;
	private boolean eliminated;

	public Team(String name, ChatColor chatColor, DyeColor dyeColor, Color armorColor) {
		this.name = name;
		this.chatColor = chatColor;
		this.dyeColor = dyeColor;
		this.armorColor = armorColor;
		this.activePlayers = new HashSet<>();
		this.spectatorPlayers = new HashSet<>();
		this.eliminated = false;
	}

	public String getName() { return name; }
	public ChatColor getChatColor() { return chatColor; }
	public DyeColor getDyeColor() { return dyeColor; }
	public Color getArmorColor() { return armorColor; }
	public Set<UUID> getActivePlayers() { return activePlayers; }
	public Set<UUID> getSpectatorPlayers() { return spectatorPlayers; }
	public SpawnLocation getSpawnLocation() { return spawnLocation; }
	public void setSpawnLocation(SpawnLocation location) { this.spawnLocation = location; }
	public boolean isEliminated() { return eliminated; }
	public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

	public int getTotalPlayers() {
		return activePlayers.size() + spectatorPlayers.size();
	}

	public boolean isTeamEliminated() {
		return activePlayers.isEmpty();
	}

	public void addPlayer(UUID uuid, boolean asSpectator) {
		if (asSpectator) {
			spectatorPlayers.add(uuid);
		} else {
			activePlayers.add(uuid);
		}
	}

	public void removePlayer(UUID uuid) {
		activePlayers.remove(uuid);
		spectatorPlayers.remove(uuid);
	}

	public void playerToSpectator(UUID uuid) {
		if (activePlayers.remove(uuid)) {
			spectatorPlayers.add(uuid);
		}
	}

	public static class SpawnLocation {
		public double x, y, z;
		public float yaw, pitch;
		public String worldName;

		public SpawnLocation(double x, double y, double z, float yaw, float pitch, String worldName) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.worldName = worldName;
		}
	}
}
