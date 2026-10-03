package com.tournament.utils;

import com.tournament.model.Team;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class PlayerUtils {

	public static void applyTeamArmor(Player player, Team team) {
		Color armorColor = team.getArmorColor();

		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		helmet = ColorUtils.colorizeLeatherArmor(helmet, armorColor);

		player.getEquipment().setHelmet(helmet);
	}

	public static void giveWorldCompass(Player player) {
		// World compass will be in slot 1 (index 0 in inventory)
		ItemStack compass = new ItemStack(Material.COMPASS);
		compass.setAmount(1);
		player.getInventory().setItem(0, compass);
	}

	public static void setPlayerNameColor(Player player, Team team) {
		// This will be handled in the PlayerJoinListener with proper prefix/suffix
	}
}
