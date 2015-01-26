package com.moosemanstudios.Notebook.Bukkit.Listeners;

import com.moosemanstudios.Notebook.Bukkit.Notebook;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdaterPlayerListener implements Listener {

	private Notebook plugin;

	public UpdaterPlayerListener(Notebook plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Note: this listener is never registered if updaterNotify is false, and of course isn't registered if updaterEnabled is false as well
		// This comment was added due to plugin being rejected too much for this updater listener.
		Player player=  event.getPlayer();

		if (player.hasPermission("notebook.admin")) {
			Updater updater = new Updater(plugin, plugin.curseID, plugin.getFileFolder(), Updater.UpdateType.NO_DOWNLOAD, false);

			if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE && plugin.updaterNotify && plugin.updaterEnabled) {
				player.sendMessage(ChatColor.AQUA + "An update is avaiable for Notebook: " + updater.getLatestName());
				player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/note update" + ChatColor.AQUA + " to update");
			}
		}
	}
}
