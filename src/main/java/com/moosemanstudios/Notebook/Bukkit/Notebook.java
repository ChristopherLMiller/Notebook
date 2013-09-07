package com.moosemanstudios.Notebook.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.h31ix.updater.Updater;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import com.moosemanstudios.Notebook.Core.NoteManager;
import com.moosemanstudios.Notebook.Core.NoteManager.Backend;

public class Notebook  extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	NotebookCommandExecutor noteExecutor;
	String prefix = "[Notebook]";
	PluginDescriptionFile pdfFile;
	Backend backend;
	Boolean debug = false;
	Boolean broadcastMessage;
	public Boolean updaterEnabled, updaterAuto, updaterNotify, updateAvailable;
	String updateName = "";
	Long updateSize = 0L;
	public File pluginFile;
	
	@Override
	public void onEnable() {
		pdfFile = this.getDescription();
		
		// load the config - creating if not exists
		loadConfig();
		
		pluginFile = this.getFile();
		
		// check on updater info
		if (updaterAuto && updaterEnabled) {
			// auto update enabled, go ahead and check!
			Updater updater = new Updater(this, "notebook", this.getFile(), Updater.UpdateType.DEFAULT, true);
		} else if (updaterNotify && updaterEnabled) {
			// register the listener, when a player joins we will check for an update then
			this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		}
		
		// check if SQLibrary is found before proceeding
		if ((backend == Backend.SQLITE) || (backend == Backend.MYSQL)) {
			if (getServer().getPluginManager().getPlugin("SQLibrary") == null) {
				// warn user that sqlibrary wasn't found, defaulting to flat file this time around
				log.warning(prefix + "SQlibrary was not found and is required for mysql and sqlite database storage.");
				log.warning(prefix + "Please download it from http://dev.bukkit.org/bukkit-plugins/sqlibrary");
				log.warning(prefix + "Defaulting to flatfile for now");
				backend = Backend.FLATFILE;
			}
		}
		
		// set the backend at this point
		NoteManager.getInstance().setBackend(backend);
		NoteManager.getInstance().init();
		
		// init based on backend method currently set
		if (NoteManager.getInstance().getBackend() == Backend.FLATFILE)
			NoteManager.getInstance().initFlatFile(getConfig().getString("storage.flatfile.filename"));
		else if (NoteManager.getInstance().getBackend() == Backend.SQLITE)
			NoteManager.getInstance().initSqlite(getConfig().getString("storage.sqlite.filename"), getConfig().getString("storage.sqlite.table"));
		else if (NoteManager.getInstance().getBackend() == Backend.MYSQL)
			NoteManager.getInstance().initMysql(getConfig().getString("storage.mysql.host"), getConfig().getInt("storage.mysql.port"), getConfig().getString("storage.mysql.username"), getConfig().getString("storage.mysql.password"), getConfig().getString("storage.mysql.database"), getConfig().getString("storage.mysql.table"));
	
		// register the command executor
		noteExecutor = new NotebookCommandExecutor(this);
		getCommand("note").setExecutor(noteExecutor);
		
		/*try {
			Metrics metrics = new MetricsBukkit(this.getName(), this.getDescription().getVersion());
			
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
		}*/
		
		// everything is done, at this point let the player know its enabled.
		log.info(prefix + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	@Override
	public void onDisable() {
		// save the hashmap to disk
		log.info(prefix + " is disabled");
	}
	
	public void loadConfig() {
		if (!getConfig().contains("do-not-change-config-version")) {
			getConfig().set("do-not-change-config-version", 2);
			
			// remove the backend property, no longer needed
			getConfig().set("backend", null);	
			saveConfig();
		}
		
		// check config version and apply appropriately
		switch(getConfig().getInt("do-not-change-config-version")) {
			case(2):
				// misc stuff				
				if (!getConfig().contains("broadcast-message")) getConfig().set("broadcast-message", true);
				if (!getConfig().contains("debug")) getConfig().set("debug", false);
				
				// updater stuff
				if (!getConfig().contains("updater.enabled")) getConfig().set("updater.enabled", true);
				if (!getConfig().contains("updater.auto")) getConfig().set("updater.auto", true);
				if (!getConfig().contains("updater.notify")) getConfig().set("updater.notify", false);

				// flat file stuff
				if (!getConfig().contains("storage.flatfile.enabled")) getConfig().set("storage.flatfile.enabled", true);
				if (!getConfig().contains("storage.flatfile.filename")) getConfig().set("storage.flatfile.filename", "notes.txt");

				// sqlite stuff
				if (!getConfig().contains("storage.sqlite.enabled")) getConfig().set("storage.sqlite.enabled", false);
				if (!getConfig().contains("storage.sqlite.filename")) getConfig().set("storage.sqlite.filename", "notes.db");
				if (!getConfig().contains("storage.sqlite.table")) getConfig().set("storage.sqlite.table", "notes");
					
				// mysql stuff
				if (!getConfig().contains("storage.mysql.enabled")) getConfig().set("storage.mysql.enabled", false);
				if (!getConfig().contains("storage.mysql.host")) getConfig().set("storage.mysql.host", "localhost");
				if (!getConfig().contains("storage.mysql.port")) getConfig().set("storage.mysql.port", 3306);
				if (!getConfig().contains("storage.mysql.username")) getConfig().set("storage.mysql.username", "root");
				if (!getConfig().contains("storage.mysql.password")) getConfig().set("storage.mysql.password", "password");
				if (!getConfig().contains("storage.mysql.database")) getConfig().set("storage.mysql.database", "minecraft");
				if (!getConfig().contains("storage.mysql.table")) getConfig().set("storage.mysql.table", "notes");

				saveConfig();
				break;
			default:
				break;
		}

		// now read in the values
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
		
		// updater code
		updaterEnabled = getConfig().getBoolean("updater.enabled");
		if (updaterEnabled) {			
			// see if auto or notify are set
			updaterAuto = getConfig().getBoolean("updater.auto");
			updaterNotify = getConfig().getBoolean("updater.notify");
			
			if (updaterAuto && debug) {
				log.info(prefix + "Auto updating is enabled");
			} else if (updaterNotify && debug) {
				log.info(prefix + "Alerting to updates enabled");
			}
			
			// if both are set then just notify only
			if (updaterAuto && updaterNotify) {
				log.info(prefix + "Both notify and auto update of updates enabled.  Only notifying for now");
				updaterAuto = false;
				updaterNotify = true;
			}
		}
		
		// check backend types, make sure only ones enabled
		int i = 0;
		if (getConfig().getBoolean("storage.flatfile.enabled")) i++;
		if (getConfig().getBoolean("storage.sqlite.enabled")) i++;
		if (getConfig().getBoolean("storage.mysql.enabled")) i++;
		
		if (i == 0) {
			log.severe(prefix + "At least one storage method must be enabled.  Defaulting to flatfile");
			getConfig().set("storage.flatfile.enabled", true);
		}
		if (i != 1) {
			log.severe(prefix + "More than one storage method enabled, only one can be used at a time.");
			
			// find the first one thats enabled and enable it only
			if (getConfig().getBoolean("storage.flatfile.enabled")) {
				getConfig().set("storage.mysql.enabled", false);
				log.severe(prefix + "Enabling flatfile only");
			}
			if (getConfig().getBoolean("storage.sqlite.enabled")) {
				getConfig().set("storage.mysql.enabled", false);
				log.severe(prefix + "Enabling sqlite only");
			}
		}
		
		if (getConfig().getBoolean("storage.flatfile.enabled")) backend = Backend.FLATFILE;
		if (getConfig().getBoolean("storage.sqlite.enabled")) backend = Backend.SQLITE;
		if (getConfig().getBoolean("storage.mysql.enabled")) backend = Backend.MYSQL;
		
		
		log.info(prefix + " config loaded successfully");
	}
	
	public File getFileFolder() {
		return this.getFile();
	}
}