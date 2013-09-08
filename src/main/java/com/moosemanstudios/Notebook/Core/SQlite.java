package com.moosemanstudios.Notebook.Core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

public class SQlite {

	private String filename;
	private String directory;
	private String extension;
	private SQLite sqlite;
	private String table;
	private Logger log;
	private String prefix;
	
	@SuppressWarnings("deprecation")
	public Boolean create(String directory, String filename, Logger log, String prefix, String table) {
		setDirectory(directory);
		setFileName(filename);
		setLogger(log);
		setPrefix(prefix);
		setTable(table);
		
		checkFileName();
		
		if (getExtension() != "") {
			sqlite = new SQLite(getLogger(), getPrefix(), getDirectory(), getFileName());
		} else {
			sqlite = new SQLite(getLogger(), getPrefix(), getDirectory(), getFileName(), getExtension());
		}

		sqlite.open();
		
		if (!sqlite.checkTable(getTable())) {
			log.info(prefix + " created table notes");
			String query = "CREATE TABLE " + getTable() + " (id INT AUTO_INCREMENT PRIMARY_KEY,  player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15));";
			return sqlite.createTable(query);
		} else {
			return true;
		}
	}
	
	public void checkFileName() {
		// see if there is an extension on the filename, if so strip it off
		if (getFileName().contains(".")) {
			setExtension(getFileName().substring(getFileName().indexOf(".")));
			setFileName(getFileName().substring(0, getFileName().indexOf(".")));
		}
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public String getTable() {
		return table;
	}
	
	public void setLogger(Logger log) {
		this.log = log;
	}
	
	public Logger getLogger() {
		return log;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}	
	
	public HashSet<Note> getRecords() {
		HashSet<Note> notes = new HashSet<Note>();
		
		try {
			String query = "SELECT * FROM " + getTable();
			ResultSet results = sqlite.query(query);
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
			sqlite.query("INSERT INTO " + getTable() + " ('player', 'poster', 'note', 'time') VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean saveRecords(HashSet<Note> notes) {
		for (Note note : notes) {
			saveRecord(note);
		}
		return true;
	}
	
	public Boolean removeRecord(Note note) {
		try {
			sqlite.query("DELETE FROM " + getTable() + " WHERE player='" + note.getPlayer() + "' AND poster='" + note.getPoster() + "' AND note='" + note.getNote() + "' AND time='" + note.getTime() + "'");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
