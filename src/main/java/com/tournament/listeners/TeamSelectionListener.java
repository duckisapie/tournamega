package com.tournament.listeners;

import com.tournament.managers.TournamentManager;
import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TeamSelectionListener implements Listener {
    private final TournamentManager tournamentManager;
    public TeamSelectionListener(TournamentManager tournamentManager) { this.tournamentManager = tournamentManager; }

    @EventHandler
    public void onSelect(PlayerInteractEvent event) {
        if (!tournamentManager.isInTournamentWorld(event.getPlayer())) return;
        if (tournamentManager.getTeamManager().getPlayerTeam(event.getPlayer().getUniqueId()) != null) return;
        if (!tournamentManager.isInSelection(event.getPlayer().getUniqueId())) return;
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_DYE")) return;
        try {
            DyeColor dye = DyeColor.valueOf(item.getType().name().replace("_DYE", ""));
            if (tournamentManager.selectTeam(event.getPlayer(), dye)) event.setCancelled(true);
        } catch (IllegalArgumentException ignored) {}
    }
}
