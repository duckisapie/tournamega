package com.tournament.model;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class Tournament {
	private UUID id;
	private String worldName;
	private long startTime;
	private TournamentStatus status;
	private Map<String, Team> teams;
	private int worldBorderSize;

	public Tournament() {
		this.id = UUID.randomUUID();
		this.teams = new HashMap<>();
		this.status = TournamentStatus.CREATED;
		this.worldBorderSize = 300;
	}

	public UUID getId() { return id; }
	public String getWorldName() { return worldName; }
	public void setWorldName(String name) { this.worldName = name; }
	public long getStartTime() { return startTime; }
	public void setStartTime(long time) { this.startTime = time; }
	public TournamentStatus getStatus() { return status; }
	public void setStatus(TournamentStatus status) { this.status = status; }
	public Map<String, Team> getTeams() { return teams; }
	public int getWorldBorderSize() { return worldBorderSize; }
	public void setWorldBorderSize(int size) { this.worldBorderSize = size; }

	public enum TournamentStatus {
		CREATED, STARTING, RUNNING, ENDED
	}
}
