package com.moosemanstudios.Notebook.Bukkit;

import net.h31ix.updater.Updater;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
	private Notebook plugin;
	
	PlayerListener(Notebook plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("notebook.admin")) {			
			// go ahead and check if there is something newer
			Updater updater = new Updater(plugin, "notebook", plugin.getFileFolder(), Updater.UpdateType.NO_DOWNLOAD, false);
			
			if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE && plugin.updaterNotify) {
				// update available, let the player know about it.
				player.sendMessage(ChatColor.AQUA + "An update is available for Notebook: " + updater.getLatestVersionString() + "(" + updater.getFileSize() + " bytes)");
				player.sendMessage(ChatColor.RED + "Type " + ChatColor.WHITE + "/note update" + ChatColor.RED + " to update");
			}
		}
	}
}
