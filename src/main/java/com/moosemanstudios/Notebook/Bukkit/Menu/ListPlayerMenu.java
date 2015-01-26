package com.moosemanstudios.Notebook.Bukkit.Menu;

import com.moosemanstudios.Notebook.Bukkit.Menu.Items.*;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import com.moosemanstudios.Notebook.Core.NoteManager;
import ninja.amp.ampmenus.items.BackItem;
import ninja.amp.ampmenus.menus.ItemMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 1/19/2015.
 */
public class ListPlayerMenu extends ItemMenu {

	public static ListPlayerMenu instance;

	public ListPlayerMenu(Notebook plugin) {
		super("Notebook - List Players", Size.SIX_LINE, plugin);

		this.instance = this;

		// set the Items
		setItem(0, new ListItem());
		setItem(1, new AddItem());
		setItem(2, new RemoveItem());
		setItem(6, new ManagementItem());
		setItem(7, new VersionItem());

		HashMap<String, Integer> players = NoteManager.getInstance().getPlayers();

		int i = 9;
		for (Map.Entry<String, Integer> entry : players.entrySet()) {
			setItem(i, new PlayerItem(entry.getKey(), entry.getValue()));
			i++;
		}
	}

	@Override
	public void setParent(ItemMenu parent) {
		super.setParent(parent);
		if (parent != null) {
			setItem(8, new BackItem());
		}
	}
}
