package com.tournament.model;

import org.bukkit.Location;

public class LootChest {
	private Location location;
	private Kit kit;
	private boolean opened;

	public LootChest(Location location, Kit kit) {
		this.location = location;
		this.kit = kit;
		this.opened = false;
	}

	public Location getLocation() { return location; }
	public Kit getKit() { return kit; }
	public boolean isOpened() { return opened; }
	public void setOpened(boolean opened) { this.opened = opened; }
}
