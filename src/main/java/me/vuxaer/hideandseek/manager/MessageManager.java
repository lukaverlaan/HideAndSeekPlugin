package me.vuxaer.hideandseek.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private String language;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {

        File file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        language = config.getString("language", "en");
    }

    public String get(String path) {
        String fullPath = "messages." + language + "." + path;
        return config.getString(fullPath, "Missing: " + path);
    }

    public String get(String path, Map<String, String> placeholders) {

        String msg = get(path);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return msg;
    }

    public String getTime(String singularKey, String pluralKey, int time) {

        String key = (time == 1) ? singularKey : pluralKey;

        return get(key, Map.of("time", String.valueOf(time)));
    }
}