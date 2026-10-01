package com.tournament.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ColorUtils {

	public static ItemStack colorizeLeatherArmor(ItemStack item, Color color) {
		if (item.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(color);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static Color dyeToColor(DyeColor dye) {
		return dye.getColor();
	}

	public static String chatColorToString(ChatColor color) {
		return color.toString();
	}
}
