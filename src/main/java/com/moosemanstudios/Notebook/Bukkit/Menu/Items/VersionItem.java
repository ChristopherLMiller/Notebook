package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Bukkit.Notebook;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	public void onItemClick(ItemClickEvent event) {
		event.getPlayer().closeInventory();
		event.getPlayer().sendMessage(ChatColor.GOLD + "Notebook Version: " + ChatColor.WHITE + Notebook.getVersion() + ChatColor.GOLD + " - Author: moose517");
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = super.getFinalIcon(player);
		return finalIcon;
	}
}
