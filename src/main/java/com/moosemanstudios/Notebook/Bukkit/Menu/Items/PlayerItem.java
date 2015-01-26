package com.moosemanstudios.Notebook.Bukkit.Menu.Items;

import com.moosemanstudios.Notebook.Bukkit.Menu.ListPlayerMenu;
import com.moosemanstudios.Notebook.Bukkit.Menu.PlayerNotesMenu;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

/**
 * Created by Chris on 1/26/2015.
 */
public class PlayerItem extends MenuItem {

	private String playerName;
	private Integer playerCount;

	public PlayerItem(String player, Integer count) {
		super("player notes", new ItemStack(Material.SKULL_ITEM, 1, (short)3));

		playerName = player;
		playerCount = count;
	}

	@Override
	public void onItemClick(ItemClickEvent event) {
		PlayerNotesMenu playerNotes = new PlayerNotesMenu(Notebook.instance, playerName);
		playerNotes.setParent(ListPlayerMenu.instance);
		playerNotes.open(event.getPlayer());
	}

	@Override
	public ItemStack getFinalIcon(Player player) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta)skull.getItemMeta();
		meta.setOwner(playerName);
		meta.setDisplayName(ChatColor.BLUE + playerName);

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.BLUE + "------------");
		lore.add(playerCount + " notes");
		meta.setLore(lore);
		skull.setItemMeta(meta);
		return skull;
	}
}
