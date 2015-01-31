package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Bukkit.Notebook;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Chris on 1/19/2015.
 */
public class VersionItem extends MenuItem {
	private static final String DISPLAY_NAME = ChatColor.GREEN + "Plugin Version";
	private static final ItemStack ICON = new ItemStack(Material.STICK);

	public VersionItem() {
		super(DISPLAY_NAME, ICON);
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = super.getFinalIcon(player);
		ItemMeta meta = finalIcon.getItemMeta();

		ArrayList<String> lore = new ArrayList<String> ();
		lore.add(ChatColor.GOLD + "Notebook Version: " + ChatColor.WHITE + Notebook.getVersion());
		lore.add(ChatColor.GOLD + "Author: " + ChatColor.WHITE  + "moose517");
		meta.setLore(lore);
		finalIcon.setItemMeta(meta);
		return finalIcon;
	}
}
