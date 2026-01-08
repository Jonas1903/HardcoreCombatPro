package com.hardcorecombat.plugin;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Record to store cuboid zone coordinates
 */
record ZoneCoordinates(String world, double x1, double y1, double z1, double x2, double y2, double z2) {
    
    public double getMinX() {
        return Math.min(x1, x2);
    }
    
    public double getMaxX() {
        return Math.max(x1, x2);
    }
    
    public double getMinY() {
        return Math.min(y1, y2);
    }
    
    public double getMaxY() {
        return Math.max(y1, y2);
    }
    
    public double getMinZ() {
        return Math.min(z1, z2);
    }
    
    public double getMaxZ() {
        return Math.max(z1, z2);
    }
}

public class ZoneManager {

    private final HardcoreCombatPro plugin;
    private ZoneCoordinates zoneCoords;
    private final int detectionDistance;

    public ZoneManager(HardcoreCombatPro plugin) {
        this.plugin = plugin;
        this.detectionDistance = plugin.getConfig().getInt("safe-zone.particles.detection-distance", 5);
        loadZoneFromConfig();
    }

    /**
     * Loads safe zone coordinates from config
     */
    private void loadZoneFromConfig() {
        ConfigurationSection pos1 = plugin.getConfig().getConfigurationSection("safe-zone.pos1");
        ConfigurationSection pos2 = plugin.getConfig().getConfigurationSection("safe-zone.pos2");

        if (pos1 != null && pos2 != null) {
            String world = pos1.getString("world", "world");
            double x1 = pos1.getDouble("x");
            double y1 = pos1.getDouble("y");
            double z1 = pos1.getDouble("z");
            double x2 = pos2.getDouble("x");
            double y2 = pos2.getDouble("y");
            double z2 = pos2.getDouble("z");

            zoneCoords = new ZoneCoordinates(world, x1, y1, z1, x2, y2, z2);
        }
    }

    /**
     * Sets position 1 of the safe zone
     * @param location The location to set
     */
    public void setPosition1(Location location) {
        plugin.getConfig().set("safe-zone.pos1.world", location.getWorld().getName());
        plugin.getConfig().set("safe-zone.pos1.x", location.getBlockX());
        plugin.getConfig().set("safe-zone.pos1.y", location.getBlockY());
        plugin.getConfig().set("safe-zone.pos1.z", location.getBlockZ());
        plugin.saveConfig();
        loadZoneFromConfig();
    }

    /**
     * Sets position 2 of the safe zone
     * @param location The location to set
     */
    public void setPosition2(Location location) {
        plugin.getConfig().set("safe-zone.pos2.world", location.getWorld().getName());
        plugin.getConfig().set("safe-zone.pos2.x", location.getBlockX());
        plugin.getConfig().set("safe-zone.pos2.y", location.getBlockY());
        plugin.getConfig().set("safe-zone.pos2.z", location.getBlockZ());
        plugin.saveConfig();
        loadZoneFromConfig();
    }

    /**
     * Checks if a location is inside the safe zone
     * @param location The location to check
     * @return true if the location is inside the safe zone
     */
    public boolean isInSafeZone(Location location) {
        if (zoneCoords == null || !plugin.getConfig().getBoolean("safe-zone.enabled", true)) {
            return false;
        }

        if (!location.getWorld().getName().equals(zoneCoords.world())) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= zoneCoords.getMinX() && x <= zoneCoords.getMaxX() &&
               y >= zoneCoords.getMinY() && y <= zoneCoords.getMaxY() &&
               z >= zoneCoords.getMinZ() && z <= zoneCoords.getMaxZ();
    }

    /**
     * Checks if a player is within detection distance of the safe zone boundary
     * @param player The player to check
     * @return true if the player is near the boundary
     */
    private boolean isNearBoundary(Player player) {
        if (zoneCoords == null) {
            return false;
        }

        Location loc = player.getLocation();
        if (!loc.getWorld().getName().equals(zoneCoords.world())) {
            return false;
        }

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        // Calculate distance to the nearest boundary face
        double distToMinX = Math.abs(x - zoneCoords.getMinX());
        double distToMaxX = Math.abs(x - zoneCoords.getMaxX());
        double distToMinY = Math.abs(y - zoneCoords.getMinY());
        double distToMaxY = Math.abs(y - zoneCoords.getMaxY());
        double distToMinZ = Math.abs(z - zoneCoords.getMinZ());
        double distToMaxZ = Math.abs(z - zoneCoords.getMaxZ());

        double minDist = Math.min(Math.min(distToMinX, distToMaxX), 
                         Math.min(Math.min(distToMinY, distToMaxY), 
                         Math.min(distToMinZ, distToMaxZ)));

        return minDist <= detectionDistance;
    }

    /**
     * Displays particle effects around the safe zone boundary
     * Called every 5 ticks by the scheduler
     */
    public void displayParticles() {
        if (!plugin.getConfig().getBoolean("safe-zone.particles.enabled", true) || zoneCoords == null) {
            return;
        }

        World world = plugin.getServer().getWorld(zoneCoords.world());
        if (world == null) {
            return;
        }

        // Check if any tagged players are near the boundary
        boolean shouldShowParticles = false;
        for (Player player : world.getPlayers()) {
            if (plugin.getCombatManager().isInCombat(player) && isNearBoundary(player)) {
                shouldShowParticles = true;
                break;
            }
        }

        if (!shouldShowParticles) {
            return;
        }

        // Get particle type from config
        String particleType = plugin.getConfig().getString("safe-zone.particles.type", "FLAME");
        Particle particle;
        try {
            particle = Particle.valueOf(particleType);
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }

        // Draw particle outline on the edges of the cuboid
        double minX = zoneCoords.getMinX();
        double maxX = zoneCoords.getMaxX();
        double minY = zoneCoords.getMinY();
        double maxY = zoneCoords.getMaxY();
        double minZ = zoneCoords.getMinZ();
        double maxZ = zoneCoords.getMaxZ();

        // Draw edges (simplified to reduce particle count)
        // Bottom edges
        drawLine(world, particle, minX, minY, minZ, maxX, minY, minZ);
        drawLine(world, particle, minX, minY, maxZ, maxX, minY, maxZ);
        drawLine(world, particle, minX, minY, minZ, minX, minY, maxZ);
        drawLine(world, particle, maxX, minY, minZ, maxX, minY, maxZ);

        // Top edges
        drawLine(world, particle, minX, maxY, minZ, maxX, maxY, minZ);
        drawLine(world, particle, minX, maxY, maxZ, maxX, maxY, maxZ);
        drawLine(world, particle, minX, maxY, minZ, minX, maxY, maxZ);
        drawLine(world, particle, maxX, maxY, minZ, maxX, maxY, maxZ);

        // Vertical edges
        drawLine(world, particle, minX, minY, minZ, minX, maxY, minZ);
        drawLine(world, particle, maxX, minY, minZ, maxX, maxY, minZ);
        drawLine(world, particle, minX, minY, maxZ, minX, maxY, maxZ);
        drawLine(world, particle, maxX, minY, maxZ, maxX, maxY, maxZ);
    }

    /**
     * Draws a line of particles between two points
     */
    private void drawLine(World world, Particle particle, double x1, double y1, double z1, double x2, double y2, double z2) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        int points = (int) Math.max(distance * 2, 1); // 2 particles per block

        for (int i = 0; i < points; i++) {
            double ratio = (double) i / points;
            double x = x1 + (x2 - x1) * ratio;
            double y = y1 + (y2 - y1) * ratio;
            double z = z1 + (z2 - z1) * ratio;

            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
