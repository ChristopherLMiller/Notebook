package com.moosemanstudios.Notebook.Spout;

import java.io.File;
import java.io.IOException;

import org.mcstats.Metrics;
import org.spout.api.Server;
import org.spout.api.Spout;

public class SpoutMetrics extends Metrics{

	public SpoutMetrics(String pluginName, String pluginVersion) throws IOException {
		super(pluginName, pluginVersion);
		
	}

	@Override
	public File getConfigFile() {
		File pluginsFolder = Spout.getEngine().getPluginFolder();
		return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
	}

	@Override
	public String getFullServerVersion() {
		return "Spout";
	}

	@Override
	public int getPlayersOnline() {
		Server server = (Server)Spout.getEngine();
		return server.getOnlinePlayers().length;
	}

}
