package com.moosemanstudios.Notebook.Bukkit;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.mcstats.Metrics;

public class MetricsBukkit extends Metrics {
	String pluginName;
	
	public MetricsBukkit(String pluginName, String pluginVersion) throws IOException {
		super(pluginName, pluginVersion);
		this.pluginName = pluginName;
	}

	@Override
	public File getConfigFile() {
		File  pluginsFolder = Bukkit.getPluginManager().getPlugin(pluginName).getDataFolder().getParentFile();
		return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
	}

	@Override
	public String getFullServerVersion() {
		return "Bukkit";
	}

	@Override
	public int getPlayersOnline() {
		return Bukkit.getServer().getOnlinePlayers().length;
	}

}
