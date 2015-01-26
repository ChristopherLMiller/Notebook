package com.moosemanstudios.Notebook.Bukkit.Menu;

import com.moosemanstudios.Notebook.Bukkit.Menu.Items.*;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import ninja.amp.ampmenus.items.BackItem;
import ninja.amp.ampmenus.menus.ItemMenu;

/**
 * Created by Chris on 1/19/2015.
 */
public class MainMenu extends ItemMenu {
	public static MainMenu instance;

	public MainMenu(Notebook plugin) {
		super("Notebook - Main Menu", Size.ONE_LINE, plugin);
		instance = this;

		// set the Items
		setItem(0, new ListItem());
		setItem(1, new AddItem());
		setItem(2, new RemoveItem());
		setItem(6, new ManagementItem());
		setItem(7, new VersionItem());
		setItem(8, new BackItem());
	}
}
