package com.hardcorecombat.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final HardcoreCombatPro plugin;
    private final Map<UUID, Long> combatTags;
    private final long combatDuration;

    public CombatManager(HardcoreCombatPro plugin) {
        this.plugin = plugin;
        this.combatTags = new HashMap<>();
        this.combatDuration = plugin.getConfig().getLong("combat-duration", 15) * 1000; // Convert to milliseconds
    }

    /**
     * Tags a player for combat
     * @param player The player to tag
     */
    public void tagPlayer(Player player) {
        // Skip if player is admin or op
        if (player.hasPermission("combat.admin") || player.isOp()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        boolean wasTagged = isInCombat(player);
        
        combatTags.put(player.getUniqueId(), currentTime + combatDuration);
        
        // Only send the tagged message if they weren't already in combat
        if (!wasTagged) {
            String message = plugin.getConfig().getString("messages.combat-tagged", "<red>You are now in combat!");
            message = message.replace("{time}", String.valueOf(combatDuration / 1000));
            player.sendMessage(plugin.getMiniMessage().deserialize(message));
        }
    }

    /**
     * Checks if a player is currently in combat
     * @param player The player to check
     * @return true if the player is in combat
     */
    public boolean isInCombat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!combatTags.containsKey(uuid)) {
            return false;
        }

        long endTime = combatTags.get(uuid);
        long currentTime = System.currentTimeMillis();

        if (currentTime >= endTime) {
            combatTags.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Removes combat tag from a player
     * @param player The player to untag
     */
    public void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    /**
     * Gets the remaining combat time for a player in seconds
     * @param player The player to check
     * @return Remaining time in seconds, or 0 if not in combat
     */
    public long getRemainingTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (!combatTags.containsKey(uuid)) {
            return 0;
        }

        long endTime = combatTags.get(uuid);
        long currentTime = System.currentTimeMillis();
        long remaining = endTime - currentTime;

        return remaining > 0 ? (remaining / 1000) : 0;
    }

    /**
     * Updates combat timers for all online players
     * This method is called every second by the scheduler
     */
    public void updateCombatTimers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isInCombat(player)) {
                long remaining = getRemainingTime(player);
                
                if (remaining > 0) {
                    // Display action bar with remaining time
                    String message = plugin.getConfig().getString("messages.combat-timer", "<red>Combat Status: {time}s remaining");
                    message = message.replace("{time}", String.valueOf(remaining));
                    Component component = plugin.getMiniMessage().deserialize(message);
                    player.sendActionBar(component);
                } else {
                    // Combat ended
                    removeTag(player);
                    String message = plugin.getConfig().getString("messages.combat-over", "<green>You are no longer in combat.");
                    player.sendMessage(plugin.getMiniMessage().deserialize(message));
                }
            }
        }
    }
}
