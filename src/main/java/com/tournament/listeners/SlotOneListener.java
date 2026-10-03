package com.tournament.listeners;

import com.tournament.managers.TournamentParticipationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

/** Reserves displayed hotbar slot 1 (inventory slot 0) for team-selection dyes. */
public class SlotOneListener implements Listener {
    private final TournamentParticipationManager participationManager;

    public SlotOneListener(TournamentParticipationManager participationManager) {
        this.participationManager = participationManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !participationManager.isInTournamentWorld(player)) return;
        if (event.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory && event.getSlot() == 0
                || event.getHotbarButton() == 0) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !participationManager.isInTournamentWorld(player)) return;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == event.getView().getTopInventory().getSize() + 27) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldSlotChange(PlayerItemHeldEvent event) {
        if (!participationManager.isInTournamentWorld(event.getPlayer())) return;
        if (event.getNewSlot() == 0) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!participationManager.isInTournamentWorld(event.getPlayer())) return;
        ItemStack item = event.getItemDrop().getItemStack();
        if (event.getPlayer().getInventory().getHeldItemSlot() == 0 || item.getType().name().endsWith("_DYE")) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player) || !participationManager.isInTournamentWorld(player)
                || player.getInventory().getItem(0) != null) return;
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
        if (remaining.getAmount() == 0) event.getItem().remove();
        else event.getItem().setItemStack(remaining);
    }
}
