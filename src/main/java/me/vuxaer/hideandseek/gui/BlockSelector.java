package me.vuxaer.hideandseek.gui;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockSelector {

    private static final Random RANDOM = new Random();
    public static final String TITLE = HideAndSeekPlugin.getInstance()
            .getMessageManager()
            .get("choose_block");
    private static final int[] DISPLAY_SLOTS = {3, 5};
    private static final List<Material> BLOCKS = List.of(
            Material.BRICKS, Material.BRICK_SLAB, Material.BRICK_STAIRS,
            Material.DARK_OAK_PLANKS, Material.SPRUCE_PLANKS, Material.DARK_OAK_LOG, Material.SPRUCE_LOG,
            Material.WARPED_FENCE, Material.DARK_OAK_FENCE, Material.SPRUCE_FENCE,
            Material.GREEN_CONCRETE, Material.GREEN_TERRACOTTA, Material.MOSS_BLOCK,
            Material.WARPED_PLANKS, Material.WARPED_STEM, Material.WARPED_HYPHAE,
            Material.STONE, Material.SMOOTH_STONE, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
            Material.ANDESITE, Material.POLISHED_ANDESITE,
            Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CUT_SANDSTONE,
            Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL,
            Material.SNOW_BLOCK,
            Material.BOOKSHELF, Material.HAY_BLOCK
    );
    private static final Map<UUID, List<Material>> PLAYER_BLOCKS = new HashMap<>();

    public static void open(Player player) {
        int size = 9;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        List<Material> blocks = PLAYER_BLOCKS.computeIfAbsent(
                player.getUniqueId(),
                uuid -> getRandomBlocks(2)
        );

        for (int i = 0; i < blocks.size(); i++) {
            Material mat = blocks.get(i);

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + formatMaterial(mat));
                item.setItemMeta(meta);
            }

            inv.setItem(DISPLAY_SLOTS[i], item);
        }

        player.openInventory(inv);
    }

    public static void clear(Player player) {
        PLAYER_BLOCKS.remove(player.getUniqueId());
    }

    public static List<Material> getPlayerBlocks(Player player) {
        return PLAYER_BLOCKS.getOrDefault(player.getUniqueId(), getRandomBlocks(2));
    }

    public static String formatMaterial(Material mat) {
        return Arrays.stream(mat.name().toLowerCase().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private static List<Material> getRandomBlocks(int amount) {
        List<Material> shuffled = new ArrayList<>(BLOCKS);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, amount);
    }
}