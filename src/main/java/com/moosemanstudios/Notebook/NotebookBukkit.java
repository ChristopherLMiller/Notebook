package com.moosemanstudios.Notebook;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import com.moosemanstudios.Notebook.NoteManager;
import com.moosemanstudios.Notebook.NoteManager.Backend;

public class NotebookBukkit  extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	NotebookCommandExecutorBukkit noteExecutor;
	String prefix = "[Notebook]";
	PluginDescriptionFile pdfFile = null;
	Backend backend;
	Boolean debug = false;
	Boolean broadcastMessage;
	
	@Override
	public void onEnable() {
		// go ahead and create config file if it doesn't exist
		setupConfig();
		
		// load the config
		loadConfig();
		
		// check if SQLibrary is found before proceeding
		if ((backend == Backend.SQLITE) || (backend == Backend.MYSQL)) {
			if (getServer().getPluginManager().getPlugin("SQLibrary") == null)
				// warn user that sqlibrary wasn't found, defaulting to flat file this time around
				log.warning(prefix + "SQlibrary was not found and is required for mysql and sqlite database storage.");
				log.warning(prefix + "Please download it from http://dev.bukkit.org/bukkit-plugins/sqlibrary");
				log.warning(prefix + "Defaulting to flatfile for now");
				backend = Backend.FLATFILE;
		}
		
		// set the backend at this point
		NoteManager.getInstance().setBackend(backend);
		NoteManager.getInstance().init();
		
		// init based on backend method currently set
		if (NoteManager.getInstance().getBackend() == Backend.FLATFILE)
			NoteManager.getInstance().initFlatFile(getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getString("filename"));
		else if (NoteManager.getInstance().getBackend() == Backend.SQLITE)
			NoteManager.getInstance().initSqlite(getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getString("filename"));
		else if (NoteManager.getInstance().getBackend() == Backend.MYSQL)
			NoteManager.getInstance().initMysql();
	
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
		if (!getConfig().contains("do-not-change-config-version")) {
			getConfig().set("do-not-change-config-version", 2);
			
			// remove the backend property, no longer needed
			getConfig().set("backend", null);	
			saveConfig();
		}
		
		// check config version and apply appropriately
		switch(getConfig().getInt("do-not-change-config-version")) {
			case(2):
				if (!getConfig().contains("broadcast-message")) {
					getConfig().set("broadcast-message", true);
				}
				if(!getConfig().contains("debug")) {
					getConfig().set("debug", false);
				}
				if (!getConfig().contains("storage")) {
					getConfig().createSection("storage");
					
					// flat file
					if(!getConfig().getConfigurationSection("storage").contains("flatfile")) {
						getConfig().getConfigurationSection("storage").createSection("flatfile");
						
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").contains("enabled")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").set("enabled", true);
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").contains("filename")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").set("filename", "Notes.txt");
						}
					}
					
					// sqlite
					if (!getConfig().getConfigurationSection("storage").contains("sqlite")) {
						getConfig().getConfigurationSection("storage").createSection("sqlite");
						
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").contains("enabled")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("enabled", false);
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").contains("filename")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("filename", "notes.db");
						}
					}
					
					// mysql
					if (!getConfig().getConfigurationSection("storage").contains("mysql")) {
						getConfig().getConfigurationSection("storage").createSection("mysql");
						
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("enabled")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("enabled", false);
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("host")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("host", "localhost");
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("port")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("host", 3306);
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("username")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("username", "root");
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("password")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("password", "password");
						}
						if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("database")) {
							getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("database", "minecraft");
						}
					}
				}
				saveConfig();
				break;
			default:
				break;
		}
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
		
		// check backend types, make sure only ones enabled
		int i = 0;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getBoolean("enabled")) i++;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getBoolean("enabled")) i++;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getBoolean("enabled")) i++;
		
		if (i == 0) {
			log.severe(prefix + "At least one storage method must be enabled.  Defaulting to flatfile");
			getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").set("enabled", true);
		}
		if (i != 1) {
			log.severe(prefix + "More than one storage method enabled, only one can be used at a time.");
			
			// find the first one thats enabled and enable it only
			if (getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getBoolean("enabled")) {
				getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("enabled", false);
				getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("enabled", false);
				log.severe(prefix + "Enabling flatfile only");
			}
			if (getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getBoolean("enabled")) {
				getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("enabled", false);
				log.severe(prefix + "Enabling flatfile only");
			}
		}
		
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getBoolean("enabled")) backend = Backend.FLATFILE;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getBoolean("enabled")) backend = Backend.SQLITE;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getBoolean("enabled")) backend = Backend.MYSQL;
		
		
		log.info(prefix + " config loaded successfully");
	}
}