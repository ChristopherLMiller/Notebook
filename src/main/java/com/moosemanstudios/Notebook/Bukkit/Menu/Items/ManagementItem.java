package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Bukkit.Menu.MainMenu;
import com.moosemanstudios.Notebook.Bukkit.Menu.ManagementMenu;
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
public class ManagementItem extends MenuItem {
	private static final String DISPLAY_NAME = ChatColor.GREEN + "Plugin Management";
	private static final ItemStack ICON = new ItemStack(Material.BEDROCK);

	public ManagementItem() {
		super(DISPLAY_NAME, ICON);
	}

	@Override
	public void onItemClick(ItemClickEvent event) {
		if (event.getPlayer().hasPermission("notebook.admin")) {
			ManagementMenu management = new ManagementMenu(Notebook.instance);
			management.setParent(MainMenu.instance);
			management.open(event.getPlayer());

		}
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = super.getFinalIcon(player);
		if (!player.hasPermission("notebook.admin")) {
			finalIcon.setType(Material.AIR);
		}
		return finalIcon;
	}
}
