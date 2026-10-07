package com.tournament.listeners;

import com.tournament.managers.TournamentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class SlotOneListener implements Listener {
    private final TournamentManager tournamentManager;
    public SlotOneListener(TournamentManager tournamentManager) { this.tournamentManager = tournamentManager; }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !tournamentManager.isInTournamentWorld(player)) return;
        if ((event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory && event.getSlot() == 0) || event.getHotbarButton() == 0) {
            // Allow spectators and dead players (creative mode) to use the compass in slot 0
            if (player.getGameMode() != org.bukkit.GameMode.SPECTATOR && player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !tournamentManager.isInTournamentWorld(player)) return;
        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == topSize + 27) {
                // Allow spectators and dead players (creative mode) to move items in slot 0
                if (player.getGameMode() != org.bukkit.GameMode.SPECTATOR && player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldSlotChange(PlayerItemHeldEvent event) {
        if (tournamentManager.isInTournamentWorld(event.getPlayer()) && event.getNewSlot() == 0) {
            // Allow spectators and dead players (creative mode) to use the compass in slot 0
            if (event.getPlayer().getGameMode() != org.bukkit.GameMode.SPECTATOR && event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!tournamentManager.isInTournamentWorld(event.getPlayer())) return;
        if (event.getPlayer().getInventory().getHeldItemSlot() == 0) {
            // Allow spectators and dead players (creative mode) to drop the compass
            if (event.getPlayer().getGameMode() != org.bukkit.GameMode.SPECTATOR && event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player) || !tournamentManager.isInTournamentWorld(player)) return;
        event.setCancelled(true);
        ItemStack remaining = event.getItem().getItemStack().clone();
        for (int slot = 1; slot < player.getInventory().getSize() && remaining.getAmount() > 0; slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing == null || existing.getType().isAir()) { player.getInventory().setItem(slot, remaining); remaining.setAmount(0); }
            else if (existing.isSimilar(remaining) && existing.getAmount() < existing.getMaxStackSize()) {
                int moved = Math.min(remaining.getAmount(), existing.getMaxStackSize() - existing.getAmount());
                existing.setAmount(existing.getAmount() + moved); remaining.setAmount(remaining.getAmount() - moved);
            }
        }
        if (remaining.getAmount() == 0) event.getItem().remove(); else event.getItem().setItemStack(remaining);
    }
}
