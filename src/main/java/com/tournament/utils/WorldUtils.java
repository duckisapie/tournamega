package com.tournament.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class WorldUtils {

	public static void setupWorldBorder(World world, int size) {
		WorldBorder border = world.getWorldBorder();
		border.setCenter(0, 0);
		border.setSize(size * 2);
	}

	public static World getOrCreateWorld(String worldName) {
		World world = Bukkit.getWorld(worldName);
		return world;
	}

	public static boolean worldExists(String worldName) {
		return Bukkit.getWorld(worldName) != null;
	}

	public static void deleteWorld(String worldName) {
		World world = Bukkit.getWorld(worldName);
		if (world != null) {
			for (org.bukkit.entity.Player player : world.getPlayers()) {
				player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
			}
			Bukkit.unloadWorld(world, false);
		}
	}
}
