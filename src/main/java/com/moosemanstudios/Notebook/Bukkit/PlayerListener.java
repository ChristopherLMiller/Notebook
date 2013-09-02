package com.moosemanstudios.Notebook.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener {
	private Notebook plugin;
	
	PlayerListener(Notebook plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("notebook.admin") && plugin.updaterNotify && plugin.updaterEnabled) {
			player.sendMessage(ChatColor.AQUA + "An update is available: " + plugin.updateName + "(" + plugin.updateSize + " bytes)");
			player.sendMessage(ChatColor.RED + "Type " + ChatColor.WHITE + "/note update" + ChatColor.RED + " to update");
		}
	}

}
