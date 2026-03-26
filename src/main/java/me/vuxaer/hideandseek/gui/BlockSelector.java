package me.vuxaer.hideandseek.gui;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockSelector {

    public static final String TITLE = HideAndSeekPlugin.getInstance()
            .getMessageManager()
            .get("choose_block");

    private static final List<Material> BLOCKS = Arrays.asList(
            Material.OAK_PLANKS,
            Material.SPRUCE_PLANKS,
            Material.BIRCH_PLANKS,
            Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.BRICKS,
            Material.SANDSTONE,
            Material.RED_SANDSTONE,
            Material.NETHER_BRICKS,
            Material.BLACKSTONE,
            Material.END_STONE,
            Material.DEEPSLATE,
            Material.POLISHED_DEEPSLATE,
            Material.OBSIDIAN,
            Material.QUARTZ_BLOCK,
            Material.SMOOTH_STONE,
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.MYCELIUM,
            Material.SNOW_BLOCK,
            Material.HAY_BLOCK,
            Material.BOOKSHELF,
            Material.OAK_FENCE,
            Material.TNT
    );

    public static void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        List<Material> randomBlocks = getRandomBlocks(2);

        int[] slots = {3, 5};

        for (int i = 0; i < randomBlocks.size(); i++) {
            Material mat = randomBlocks.get(i);

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§e" + formatMaterial(mat));
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    private static String formatMaterial(Material mat) {

        String name = mat.name().toLowerCase().replace("_", " ");

        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return formatted.toString().trim();
    }

    private static List<Material> getRandomBlocks(int amount) {
        List<Material> shuffled = new ArrayList<>(BLOCKS);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, amount);
    }
}