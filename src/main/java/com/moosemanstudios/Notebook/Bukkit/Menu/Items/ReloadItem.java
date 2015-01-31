package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chris on 1/19/2015.
 */
public class ReloadItem extends MenuItem {
	private static final String DISPLAY_NAME = ChatColor.GREEN + "Add Note";
	private static final ItemStack ICON = new ItemStack(Material.BOOK_AND_QUILL);

	public ReloadItem() {
		super(DISPLAY_NAME, ICON);
	}

	@Override
	public void onItemClick(ItemClickEvent event) {
		if (event.getPlayer().hasPermission("notebook.add")) {
			event.getPlayer().sendRawMessage("Open sub menu here");
			// TODO: open sub menu
		}
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = super.getFinalIcon(player);
		if (!player.hasPermission("notebook.add")) {
			finalIcon.setType(Material.AIR);
		}
		return finalIcon;
	}
}