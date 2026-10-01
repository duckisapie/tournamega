package com.tournament.model;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class Kit {
	private String name;
	private double rarity; // 0.0 to 1.0
	private List<ItemStack> items;

	public Kit(String name, double rarity) {
		this.name = name;
		this.rarity = Math.max(0, Math.min(1, rarity));
		this.items = new ArrayList<>();
	}

	public String getName() { return name; }
	public double getRarity() { return rarity; }
	public List<ItemStack> getItems() { return items; }
	public void addItem(ItemStack item) { items.add(item); }

	public void setItems(List<ItemStack> items) {
		this.items = items;
	}
}
