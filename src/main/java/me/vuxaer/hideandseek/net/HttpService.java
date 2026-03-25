package me.vuxaer.hideandseek.net;

import com.google.gson.Gson;
import me.vuxaer.hideandseek.domain.GameResult;
import me.vuxaer.hideandseek.domain.GameResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpService {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public HttpService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendGameResult(String endpoint, GameResult result) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = gson.toJson(result);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                plugin.getLogger().info("POST Response: " + responseCode);

            } catch (Exception e) {
                plugin.getLogger().warning("POST failed: " + e.getMessage());
                e.printStackTrace();
            }

        });
    }
}