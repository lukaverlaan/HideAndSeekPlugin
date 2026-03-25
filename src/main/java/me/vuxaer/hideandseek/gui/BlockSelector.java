package me.vuxaer.hideandseek.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BlockSelector {

    public static final String TITLE = "§aChoose your block";

    public static void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        inv.setItem(3, new ItemStack(Material.OAK_PLANKS));
        inv.setItem(5, new ItemStack(Material.STONE));

        player.openInventory(inv);
    }
}