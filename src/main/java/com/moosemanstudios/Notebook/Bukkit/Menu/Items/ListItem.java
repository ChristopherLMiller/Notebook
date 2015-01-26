package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Bukkit.Menu.ListPlayerMenu;
import com.moosemanstudios.Notebook.Bukkit.Menu.MainMenu;
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
public class ListItem extends MenuItem {
	private static final String DISPLAY_NAME = ChatColor.GREEN + "List Notes";
	private static final ItemStack ICON = new ItemStack(Material.BOOKSHELF);

	public ListItem() {
		super(DISPLAY_NAME, ICON);
	}

	@Override
	public void onItemClick(ItemClickEvent event) {
		if (event.getPlayer().hasPermission("notebook.list")) {
			ListPlayerMenu listMenu = new ListPlayerMenu(Notebook.instance);
			listMenu.setParent(MainMenu.instance);
			listMenu.open(event.getPlayer());
		}
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = super.getFinalIcon(player);
		if (!player.hasPermission("notebook.list")) {
			finalIcon.setType(Material.AIR);
		}
		return finalIcon;
	}
}
