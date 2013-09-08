package com.moosemanstudios.Notebook.Core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.MySQL;

public class Mysql {
	private String table;
	private String host;
	private String username;
	private String password;
	private String database;
	private String prefix;
	private int port;
	private MySQL mysql;
	private Logger log;
	
	@SuppressWarnings("deprecation")
	public Boolean create(Logger log, String prefix, String host, int port, String database, String username, String password, String table) {
		
		mysql = new MySQL(log, prefix, getHost(), getPort(), getDatabase(), getUsername(), getPassword());
		mysql.open();
		
		// check if the table exists
		if (!mysql.checkTable(getTable())) {
			log.info(prefix + " created table " + getTable());
			String query = "CREATE TABLE IF NOT EXISTS " + getTable() + " (id INT NOT NULL AUTO_INCREMENT, player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15), PRIMARY KEY (id) );";
			mysql.createTable(query);
		}
		
		return mysql.open();
	}
	
	public HashSet<Note> getRecords() {
		HashSet<Note> notes = new HashSet<Note>();
		try {
			if (!mysql.isOpen())
				mysql.open();
			ResultSet results = mysql.query("SELECT * FROM " + getTable());
			while (results.next()) {
				String player = results.getString("player");
				String poster = results.getString("poster");
				String time = results.getString("time");
				String note = results.getString("note");
				
				// check that input wasn't malformed
				if (!(poster == null) || !(player == null) || !(note == null) || !(time == null)) {
					notes.add(new Note(player, poster, note, time));
				}
			}
			
			return notes;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Boolean saveRecord(Note note) {
		try {
			String query = "INSERT INTO " + getTable() + " (`player`, `poster`, `note`, `time`) VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');";
			if (!mysql.isOpen()) {
				mysql.open();
			}
			mysql.query(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean saveRecords(HashSet<Note> notes) {
		for (Note note : notes) {
			saveRecord(note);
		}
		return true;
	}
	
	public Boolean removeRecord(Note note) {
		try {
			if (!mysql.isOpen())
				mysql.open();
			mysql.query("DELETE FROM " + getTable() + " WHERE `player`='" + note.getPlayer() + "' AND `poster`='" + note.getPoster() + "' AND `note`='" + note.getNote() + "' AND `time`='" + note.getTime() + "'");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void setLogger(Logger log) {
		this.log = log;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public Logger getLogger() {
		return log;
	}
	public String getTable() {
		return table;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() { 
		return password;
	}
	
	public String getDatabase() {
		return database;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public int getPort() {
		return port;
	}
}
