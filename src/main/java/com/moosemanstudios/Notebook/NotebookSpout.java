package com.moosemanstudios.Notebook;

import java.util.logging.Logger;

import org.spout.api.plugin.Plugin;
import org.spout.cereal.config.ConfigurationException;

import com.moosemanstudios.Notebook.NoteManager.Backend;

public class NotebookSpout extends Plugin {
	public Logger log = Logger.getLogger("minecraft");
	String prefix = "[Notebook] ";
	Backend backend;
	Boolean debug;
	Boolean broadcastMessage;
	
	private NotebookConfigSpout config;

	@SuppressWarnings("static-access")
	@Override
	public void onEnable() {
		// load the config
		loadConfig();
		
		// create the notemanager and set backend and initialize, check for sqlite and mysql support first
		if ((backend == Backend.SQLITE) || (backend == Backend.MYSQL)) {
			if (getEngine().getPluginManager().getPlugin("SQLibrary") == null) {
				log.warning(prefix + "SQLibrary was not found and is required for mysql and sqlite database storage.");
				log.warning(prefix + "Please download it from http://dev.bukkit.org/bukkit-plugins/sqlibrary");
				log.warning(prefix + "Defaulting to flatfile storage for now");
				backend = Backend.FLATFILE;
			}
		}
		
		NoteManager.getInstance().setBackend(backend);
		NoteManager.getInstance().init();
		
		if (NoteManager.getInstance().getBackend() == Backend.FLATFILE) NoteManager.getInstance().initFlatFile(config.FLATFILE_FILENAME.getString());
		if (NoteManager.getInstance().getBackend() == Backend.SQLITE) NoteManager.getInstance().initSqlite(config.SQLITE_FILENAME.getString(), config.SQLITE_TABLE.getString());
		if (NoteManager.getInstance().getBackend() == Backend.MYSQL) NoteManager.getInstance().initMysql(config.MYSQL_HOST.getString(), config.MYSQL_PORT.getInt(), config.MYSQL_USERNAME.getString(), config.MYSQL_PASSWORD.getString(), config.MYSQL_DATABASE.getString(), config.MYSQL_TABLE.getString());
		
		//register command executor
		
		// enable metrics TODO: gotta fix so it works with spout
		/*try {
			Metrics metrics = new Metrics((org.bukkit.plugin.Plugin) this);
			
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
		
		log.info(prefix + " version " + getDescription().getVersion() + " is enabled");
	}
	
	@Override
	public void onDisable() {
		log.info(prefix + "is disabled");
	}
	
	
	public NotebookConfigSpout getConfig() {
		return config;
	}
	
	@SuppressWarnings("static-access")
	private void loadConfig() {
		try {
			config = new NotebookConfigSpout(getDataFolder());
			config.load();
		} catch (ConfigurationException e) {
			log.info(prefix + "Failed to load configuration");
		}
		
		debug = config.DEBUG.getBoolean();
		if (debug)
			log.info(prefix + "debugging mode enabled");
		
		broadcastMessage = config.BROADCAST_MESSAGE.getBoolean();
		if (broadcastMessage)
			log.info(prefix + "broadcast-message enabled");
		
		// check backends, make sure only one is enabled
		int i = 0;
		if (config.FLATFILE_ENABLED.getBoolean()) i++;
		if (config.SQLITE_ENABLED.getBoolean()) i++;
		if (config.MYSQL_ENABLED.getBoolean()) i++;
		
		if (i == 0) {
			log.severe(prefix + "At least one storage method must be enabled.  Defaulting to flatfile");
			config.FLATFILE_ENABLED.setValue(true);
		} else if (i != 1) {
			log.severe(prefix + "More than one storage method enabled, only one can be used at a time");
			
			// find the first enabled one
			if (config.FLATFILE_ENABLED.getBoolean()) {
				config.SQLITE_ENABLED.setValue(false);
				config.MYSQL_ENABLED.setValue(false);
				log.severe(prefix + "Enabling flat file only");
			} else if (config.SQLITE_ENABLED.getBoolean()) {
				config.MYSQL_ENABLED.setValue(false);
				log.severe(prefix + "Enabling SQLite only");
			}
		}
		
		// at this point set the backend type
		if (config.FLATFILE_ENABLED.getBoolean()) backend = Backend.FLATFILE;
		if (config.SQLITE_ENABLED.getBoolean()) backend = Backend.SQLITE;
		if (config.MYSQL_ENABLED.getBoolean()) backend = Backend.MYSQL;
		
		try {
			config.save();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info(prefix + "Config loaded successfully");
	}
}
