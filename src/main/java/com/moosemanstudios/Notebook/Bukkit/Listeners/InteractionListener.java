package com.moosemanstudios.Notebook.Bukkit.Listeners;

import com.moosemanstudios.Notebook.Bukkit.Menu.MainMenu;
import com.moosemanstudios.Notebook.Bukkit.Notebook;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Created by Chris on 1/20/2015.
 */
public class InteractionListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// open the gui on click of the book
		Player player = event.getPlayer();

		// check for right click
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// see if the item in hand is a book
			if (player.getItemInHand().getType().equals(Material.BOOK)) {
				// Get the lore of the book held
				List<String> lore = player.getItemInHand().getItemMeta().getLore();

				// verify that the lore isn't null first
				if (lore != null) {
					// Iterate over the lore and see if anything matches, if so open the menu
					for (String key : lore) {
						if (key.equalsIgnoreCase("notebook")) {
							MainMenu menu = new MainMenu(Notebook.instance);
							menu.open(player);
						}
					}
				}
			}
		}
	}
}
