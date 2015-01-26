package com.moosemanstudios.Notebook.Bukkit.Menu;

import com.moosemanstudios.Notebook.Bukkit.Menu.Items.BackendItem;
import com.moosemanstudios.Notebook.Bukkit.Menu.Items.ReloadItem;
import com.moosemanstudios.Notebook.Bukkit.Menu.Items.UpdaterItem;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import ninja.amp.ampmenus.items.BackItem;
import ninja.amp.ampmenus.menus.ItemMenu;

/**
 * Created by Chris on 1/19/2015.
 */
public class ManagementMenu extends ItemMenu {

	public ManagementMenu(Notebook plugin) {
		super("Notebook - Management", ItemMenu.Size.ONE_LINE, plugin);

		// set items
		setItem(0, new ReloadItem());
		setItem(1, new UpdaterItem());
		setItem(2, new BackendItem());
	}

	@Override
	public void setParent(ItemMenu parent) {
		super.setParent(parent);
		if (parent != null) {
			setItem(8, new BackItem());
		}
	}
}
