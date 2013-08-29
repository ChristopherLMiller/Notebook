package com.moosemanstudios.Notebook;

import java.io.File;

import org.spout.cereal.config.ConfigurationException;
import org.spout.cereal.config.ConfigurationHolder;
import org.spout.cereal.config.ConfigurationHolderConfiguration;
import org.spout.cereal.config.yaml.YamlConfiguration;

public class NotebookConfigSpout extends ConfigurationHolderConfiguration {
	// misc
	public static final ConfigurationHolder DEBUG = new ConfigurationHolder(true, "debug");
	public static final ConfigurationHolder BROADCAST_MESSAGE = new ConfigurationHolder(true, "broadcast-message");
	
	// flat file
	public static final ConfigurationHolder FLATFILE_ENABLED = new ConfigurationHolder(true, "storage", "flatfile", "enabled");
	public static final ConfigurationHolder FLATFILE_FILENAME = new ConfigurationHolder("notes.txt", "storage", "flatfile", "filename");
	
	// sqlite
	public static final ConfigurationHolder SQLITE_ENABLED = new ConfigurationHolder(false, "storage", "sqlite", "enabled");
	public static final ConfigurationHolder SQLITE_FILENAME = new ConfigurationHolder("notes.db", "storage", "sqlite", "filename");
	public static final ConfigurationHolder SQLITE_TABLE = new ConfigurationHolder("notes", "storage", "sqlite", "table");
	
	// mysql
	public static final ConfigurationHolder MYSQL_ENABLED = new ConfigurationHolder(false, "storage", "mysql", "enabled");
	public static final ConfigurationHolder MYSQL_HOST = new ConfigurationHolder("localhost", "storage", "mysql", "host");
	public static final ConfigurationHolder MYSQL_PORT = new ConfigurationHolder(3306, "storage", "mysql", "port");
	public static final ConfigurationHolder MYSQL_USERNAME = new ConfigurationHolder("root", "storage", "mysql", "username");
	public static final ConfigurationHolder MYSQL_PASSWORD = new ConfigurationHolder("password", "storage", "mysql", "password");
	public static final ConfigurationHolder MYSQL_DATABASE = new ConfigurationHolder("minecraft", "storage", "mysql", "database");
	public static final ConfigurationHolder MYSQL_TABLE = new ConfigurationHolder("notes", "storage", "mysql", "table");
	public NotebookConfigSpout(File dataFolder) {
		super(new YamlConfiguration(new File(dataFolder, "config.yml")));
	}
	
	@Override
	public void load() throws ConfigurationException {
		super.load();
		super.save();
	}

}
