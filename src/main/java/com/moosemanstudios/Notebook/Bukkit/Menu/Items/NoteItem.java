package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Core.Note;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Chris on 1/26/2015.
 */
public class NoteItem extends MenuItem {
	private static ItemStack ICON = new ItemStack(Material.PAPER);
	private Note note;
	private int count;

	public NoteItem(Note note, int count) {
		super("Entry", ICON);

		this.note = note;
		this.count = count;
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack finalIcon = new ItemStack(Material.PAPER);
		ItemMeta meta = finalIcon.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Note #" + count);

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.BLUE + "------------");
		lore.add(ChatColor.AQUA + "Note: " + ChatColor.WHITE + note.getNote());
		lore.add(ChatColor.AQUA + "Posted By: " + ChatColor.WHITE + note.getPoster());
		lore.add(ChatColor.AQUA + "Date: " + ChatColor.WHITE + note.getTime());

		meta.setLore(lore);
		finalIcon.setItemMeta(meta);
		return finalIcon;
	}
}
