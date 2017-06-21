// i am a comment added because bukkitdev fails so hardcore that you can't upload a file again without changing something.... seriously.....
package com.moosemanstudios.Notebook.Bukkit;

import com.moosemanstudios.Notebook.Bukkit.Listeners.InteractionListener;
import com.moosemanstudios.Notebook.Core.NoteManager;
import com.moosemanstudios.Notebook.Core.NoteManager.Backend;
import ninja.amp.ampmenus.MenuListener;
import org.bstats.Metrics;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class Notebook  extends JavaPlugin {
	public static Notebook instance;
	public Logger log = Logger.getLogger("minecraft");

	NotebookCommandExecutor noteExecutor;
	String prefix = "[Notebook]";
	static PluginDescriptionFile pdfFile;
	Backend backend;
	Boolean debug = false;
	Boolean broadcastMessage;

	public File pluginFile;
	public Boolean sqlibraryFound;

	@Override
	public void onEnable() {
		instance = this;
		pdfFile = this.getDescription();
		pluginFile = this.getFile();
		
		// load the config - creating if not exists
		loadConfig();
		

		// check if SQLibrary is found before proceeding
		if (getServer().getPluginManager().getPlugin("SQLibrary") == null) {
			sqlibraryFound = false;
				
			if ((backend == Backend.SQLITE) || (backend == Backend.MYSQL)) {
				// warn user that sqlibrary wasn't found, defaulting to flat file this time around
				log.warning(prefix + "SQlibrary was not found and is required for mysql and sqlite database storage.");
				log.warning(prefix + "Please download it from http://dev.bukkit.org/bukkit-plugins/sqlibrary");
				log.warning(prefix + "Defaulting to flatfile for now");
				backend = Backend.FLATFILE;
			}
		} else {
			sqlibraryFound = true;
		}

		// Register menu listener
		//MenuListener.getInstance().register(this);

		// Register interaction event
		getServer().getPluginManager().registerEvents(new InteractionListener(), this);
		
		// set the backend at this point
		NoteManager.getInstance().setBackend(backend);
		NoteManager.getInstance().init(this.getDataFolder().toString());
		
		// init based on backend method currently set
		if (NoteManager.getInstance().getBackend() == Backend.FLATFILE)
			NoteManager.getInstance().initFlatFile(getConfig().getString("storage.flatfile.filename"));
		else if (NoteManager.getInstance().getBackend() == Backend.SQLITE)
			NoteManager.getInstance().initSqlite(getConfig().getString("storage.sqlite.filename"), getConfig().getString("storage.sqlite.table"));
		else if (NoteManager.getInstance().getBackend() == Backend.MYSQL)
			NoteManager.getInstance().initMysql(getConfig().getString("storage.mysql.host"), getConfig().getInt("storage.mysql.port"), getConfig().getString("storage.mysql.username"), getConfig().getString("storage.mysql.password"), getConfig().getString("storage.mysql.database"), getConfig().getString("storage.mysql.table-prefix") + getConfig().getString("storage.mysql.table"));
	
		// register the command executor
		noteExecutor = new NotebookCommandExecutor(this);
		getCommand("note").setExecutor(noteExecutor);

		// lastly lets enable some metrics tracking
		Metrics metrics = new Metrics(instance);

		// load cstom chart
		metrics.addCustomChart(new Metrics.SimplePie("backend_type") {
			@Override
			public String getValue() {
				return NoteManager.getInstance().getBackend().getType().toLowerCase();
			}
		});

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

		// check backend types, make sure only ones enabled
		int i = 0;
		if (getConfig().getBoolean("storage.flatfile.enabled")) i++;
		if (getConfig().getBoolean("storage.sqlite.enabled")) i++;
		if (getConfig().getBoolean("storage.mysql.enabled")) i++;
		
		if (i == 0) {
			log.severe(prefix + "At least one storage method must be enabled.  Defaulting to flatfile");
			getConfig().set("storage.flatfile.enabled", true);
			saveConfig();
		}
		if (i != 1) {
			log.severe(prefix + "More than one storage method enabled, only one can be used at a time.");
			
			// find the first one thats enabled and enable it only
			if (getConfig().getBoolean("storage.flatfile.enabled")) {
				getConfig().set("storage.sqlite.enabled", false);
				getConfig().set("storage.mysql.enabled", false);
				saveConfig();
				log.severe(prefix + "Enabling flatfile only");
			}
			if (getConfig().getBoolean("storage.sqlite.enabled")) {
				getConfig().set("storage.mysql.enabled", false);
				saveConfig();
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

	public static String getVersion() { return pdfFile.getVersion(); }
}