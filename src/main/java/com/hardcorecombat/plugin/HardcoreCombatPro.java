package com.hardcorecombat.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public final class HardcoreCombatPro extends JavaPlugin {

    private CombatManager combatManager;
    private ZoneManager zoneManager;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();
        
        // Initialize managers
        combatManager = new CombatManager(this);
        zoneManager = new ZoneManager(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        
        // Start combat timer task (runs every second)
        getServer().getScheduler().runTaskTimer(this, () -> {
            combatManager.updateCombatTimers();
        }, 20L, 20L); // 20 ticks = 1 second
        
        // Start particle task (runs every 5 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            zoneManager.displayParticles();
        }, 5L, 5L); // 5 ticks
        
        getLogger().info("HardcoreCombatPro has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HardcoreCombatPro has been disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("combat.admin") && !player.isOp()) {
            player.sendMessage(miniMessage.deserialize(getConfig().getString("messages.no-permission", "<red>No permission!")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /ct <set1|set2>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set1" -> {
                zoneManager.setPosition1(player.getLocation());
                String message = getConfig().getString("messages.zone-set1", "<green>Position 1 set!");
                message = message.replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                        .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                        .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
                player.sendMessage(miniMessage.deserialize(message));
                return true;
            }
            case "set2" -> {
                zoneManager.setPosition2(player.getLocation());
                String message = getConfig().getString("messages.zone-set2", "<green>Position 2 set!");
                message = message.replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                        .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                        .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
                player.sendMessage(miniMessage.deserialize(message));
                return true;
            }
            default -> {
                player.sendMessage("Usage: /ct <set1|set2>");
                return true;
            }
        }
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
