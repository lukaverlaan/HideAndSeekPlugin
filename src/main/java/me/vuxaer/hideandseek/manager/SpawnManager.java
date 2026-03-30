package me.vuxaer.hideandseek.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.World;

public class SpawnManager {

    private final FileConfiguration config;

    public SpawnManager(FileConfiguration config) {
        this.config = config;
    }

    public void setSpawn(String key, Location loc) {
        config.set("spawns." + key + ".world", loc.getWorld().getName());
        config.set("spawns." + key + ".x", loc.getX());
        config.set("spawns." + key + ".y", loc.getY());
        config.set("spawns." + key + ".z", loc.getZ());
        config.set("spawns." + key + ".yaw", loc.getYaw());
        config.set("spawns." + key + ".pitch", loc.getPitch());
    }

    public Location getSpawn(String key) {
        String path = "spawns." + key;
        if (!config.contains(path)) return null;

        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }
}