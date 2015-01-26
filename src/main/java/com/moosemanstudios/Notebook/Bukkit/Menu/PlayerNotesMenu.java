package com.moosemanstudios.Notebook.Bukkit.Menu;

import com.moosemanstudios.Notebook.Bukkit.Menu.Items.*;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import com.moosemanstudios.Notebook.Core.Note;
import com.moosemanstudios.Notebook.Core.NoteManager;
import ninja.amp.ampmenus.items.BackItem;
import ninja.amp.ampmenus.menus.ItemMenu;

import java.util.ArrayList;


/**
 * Created by Chris on 1/19/2015.
 */
public class PlayerNotesMenu extends ItemMenu {
	private String playerName;

	public PlayerNotesMenu(Notebook plugin, String playerName) {
		super("Notebook - " + playerName, Size.SIX_LINE, plugin);

		this.playerName = playerName;

		// set the Items
		setItem(0, new ListItem());
		setItem(1, new AddItem());
		setItem(2, new RemoveItem());
		setItem(6, new ManagementItem());
		setItem(7, new VersionItem());

		// get all notes on this player
		ArrayList<Note> notes = NoteManager.getInstance().getPlayer(playerName);

		int i = 9;
		for (Note note : notes) {
			setItem(i, new NoteItem(note, i-8));
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
