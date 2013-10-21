package com.moosemanstudios.Notebook.Bukkit;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.mcstats.Metrics;

public class MetricsBukkit extends Metrics {
	String pluginName;
	
	public MetricsBukkit(String pluginName, String pluginVersion) throws IOException {
 		super(pluginName, pluginVersion);
		this.pluginName = pluginName;
	}

	@Override
	public File getConfigFile() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		File pluginsFolder;
		if (plugin != null) {
			pluginsFolder = plugin.getDataFolder().getParentFile();
		} else {
			pluginsFolder = new File("plugins/");
		}
		
		return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
	}

	@Override
	public String getFullServerVersion() {
		return Bukkit.getName();
	}

	@Override
	public int getPlayersOnline() {
		return Bukkit.getServer().getOnlinePlayers().length;
	}

}
