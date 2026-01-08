package com.hardcorecombat.plugin;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final HardcoreCombatPro plugin;

    public CombatListener(HardcoreCombatPro plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player receiving damage from any source
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        // Tag player for receiving damage
        plugin.getCombatManager().tagPlayer(player);
    }

    /**
     * Handles player dealing damage to entities
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        // Tag player for dealing damage
        plugin.getCombatManager().tagPlayer(player);
    }

    /**
     * Handles player death - removes combat tag
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        plugin.getCombatManager().removeTag(player);
    }

    /**
     * Handles player movement - prevents entering safe zone while in combat
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        // Only check if player is in combat
        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }

        // Check if player moved to a different block (optimization)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Check if player is trying to enter safe zone
        boolean wasInZone = plugin.getZoneManager().isInSafeZone(event.getFrom());
        boolean isInZone = plugin.getZoneManager().isInSafeZone(event.getTo());

        // If player is trying to enter the zone (wasn't in, now is in)
        if (!wasInZone && isInZone) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("messages.zone-entry-blocked", 
                "<red>You cannot enter the safe zone while in combat!");
            player.sendMessage(plugin.getMiniMessage().deserialize(message));
        }
    }

    /**
     * Handles player commands - blocks commands while in combat
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        // Only check if player is in combat
        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }

        // Block all commands during combat
        event.setCancelled(true);
        String message = plugin.getConfig().getString("messages.command-blocked", 
            "<red>You cannot use commands while in combat!");
        player.sendMessage(plugin.getMiniMessage().deserialize(message));
    }

    /**
     * Handles combat logging - kills player if they disconnect while in combat
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        // Check if player is in combat
        if (plugin.getCombatManager().isInCombat(player)) {
            // Kill the player (combat logging penalty)
            player.setHealth(0);
            plugin.getCombatManager().removeTag(player);
        }
    }
}
