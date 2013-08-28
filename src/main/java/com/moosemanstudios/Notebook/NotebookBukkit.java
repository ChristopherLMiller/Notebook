package com.moosemanstudios.Notebook;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import com.moosemanstudios.Notebook.NoteManager;

public class NotebookBukkit  extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	NotebookCommandExecutorBukkit noteExecutor;
	String prefix = "[Notebook]";
	PluginDescriptionFile pdfFile = null;
	String backend;
	Boolean debug = false;
	Boolean broadcastMessage;
	
	@Override
	public void onEnable() {
		// go ahead and create config file if it doesn't exist
		setupConfig();
		
		// load the config
		loadConfig();
		
		// check if SQLibrary is found before proceeding
		Plugin SQLibrary = this.getServer().getPluginManager().getPlugin("SQLibrary");
		if (SQLibrary == null) {
			log.warning("SQLibrary required for database storage.  Please visit http://dev.bukkit.org/bukkit-plugins/sqlibrary/ to download");
			log.warning("Switching to flatfile storage");
			getConfig().set("backend", "flatfile");
			saveConfig();
			backend = "flatfile";
		}
		NoteManager.getInstance().init(backend);
	
		// register the command executor
		noteExecutor = new NotebookCommandExecutorBukkit(this);
		getCommand("note").setExecutor(noteExecutor);
		
		try {
			Metrics metrics = new Metrics(this);
			
			Graph graph = metrics.createGraph("Number of note entries");
			graph.addPlotter(new Metrics.Plotter("Notes") {
				@Override
				public int getValue() {
					return NoteManager.getInstance().getNumNotes();
				}
			});
			
			
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// everything is done, at this point let the player know its enabled.
		pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled");
	}
	
	@Override
	public void onDisable() {
		// save the hashmap to disk
		log.info("[Notebook] is disabled");
	}
	
	public void setupConfig() {
		if (!getConfig().contains("backend")) {
			getConfig().set("backend", "flatfile");
		}
		if (!getConfig().contains("debug")) {
			getConfig().set("debug", false);
		}
		if (!getConfig().contains("broadcast-message")) {
			getConfig().set("broadcast-message", true);
		}
		saveConfig();
		
		log.info(prefix + " config file created");
	}
	
	public void loadConfig() {
		reloadConfig();
		
		debug = getConfig().getBoolean("debug");
		if (debug) {
			log.info(prefix + " debugging mode enabled");
		}
		
		broadcastMessage = getConfig().getBoolean("broadcast-message");
		if (debug) {
			if (broadcastMessage) {
				log.info(prefix + " broadcast-messages enabled");
			}
		}
		
		backend = getConfig().getString("backend");
		if (debug) {
			log.info(prefix + " backend specified: " + backend);
		}
		
		
		log.info(prefix + " config loaded successfully");
	}
}