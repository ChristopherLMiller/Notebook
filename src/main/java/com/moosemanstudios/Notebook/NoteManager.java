package com.moosemanstudios.Notebook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;
import lib.PatPeter.SQLibrary.MySQL;

public class NoteManager {
	private static NoteManager instance = null;
	HashSet<Note> noteHash;
	Boolean debugging;
	Logger log;
	String prefix;
	Backend currentBackend;
	String mainDirectory;
	SQLite sqlite;
	MySQL mysql;
	String mysqlTable;
	
	
	public enum Backend {
		FLATFILE("flatfile"),
		MYSQL("mysql"),
		SQLITE("sqlite");
		
		private String type;
		
		Backend(String backend) {
			this.type = backend;
		}
		
		public String getType() {
			return this.type;
		}
	}
	
	/**
	 * Default contructor - only exists to defeat default instantiation
	 */
	NoteManager() { }
	
	/**
	 *  Get the current instance of the note manager
	 * @return The instance
	 */
	public static NoteManager getInstance() {
		if (instance == null) {
			instance = new NoteManager();
		}
		return instance;
	}
	
	/**
	 * Create the hashset, setup any other needed essentials
	 * @param backend - the backend desired
	 */
	public void init(String backend) {
		// check for valid noteHash
		if (noteHash == null) {
			noteHash = new HashSet<Note>();
		}
		
		debugging = true;
		log = Logger.getLogger("minecraft");
		prefix = "[Notebook]";
		mainDirectory = "plugins/notebook/";
		
		setBackend(backend);
	}
	
	/**
	 * Set the backend to a specific type
	 * @param backend - the backend type to use
	 */
	public void setBackend(String backend) {
		// set the backend type based on the input
		if (backend.equalsIgnoreCase("flatfile")) {
			currentBackend = Backend.FLATFILE;
			initFlatFile();
		} else if (backend.equalsIgnoreCase("mysql")) {
			currentBackend = Backend.MYSQL;
			initMysql();
		} else if (backend.equalsIgnoreCase("sqlite")) {
			currentBackend = Backend.SQLITE;
			initSqlite();
		} else {
			// invalid option given, default to flatfile
			currentBackend = Backend.FLATFILE;
			initFlatFile();
		}
	}
	
	/**
	 * Get the current backend for the note manager
	 * @return ENUM - what backend is currently enabled
	 */
	public Backend getBackend() {
		return currentBackend;
	}
	
	/**
	 * Add a note about a player
	 * @param poster - the person adding the note
	 * @param player - the person the note is about
	 * @param note - the message of the note itself
	 * @return Returns true if note was created successfully
	 */
	public boolean addNote(String poster, String player, String note) {
		
		// make sure the noteHash exists
		if (noteHash == null) {
			noteHash = new HashSet<Note>();
		}
		
		// verify input
		if ((poster == null) || (poster.equals("")) || (player == null) || (player.equals("")) || (note == null) || (note.equals(""))) {
			return false;
		}
		
		
		// create the new note
		Note newNote = new Note(player, poster, note, new SimpleDateFormat("MM/dd/yy HH:mm").format(Calendar.getInstance().getTime()));
		
		// add the note
		if (noteHash.add(newNote)) {
			// update the backend
			saveRecord(newNote);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Remove note on player
	 * @param player - Player to have note removed on
	 * @param index - index of the note in the hashmap
	 * @return boolean - If the removal of the note was successful
	 */
	public boolean removeNote(String player, int index) {
		// if notehash is null, no notes exists, assumed false
		if (noteHash == null) {
			if (debugging) {
				log.info(prefix + " removeNote: notehash null: unable to remove");
			}
			return false;
		}
		
		// verify inputs
		if ((player.equals("")) || (player == null)) {
			if (debugging) {
				log.info(prefix + " removeNote: player name invalid");
			}
			return false;
		}
		
		// temp arraylist to hold all notes about the player
		ArrayList<Note> result = new ArrayList<Note>();
		
		// get all notes about the player
		for (Note note : noteHash) {
			if (note.getPlayer().equalsIgnoreCase(player)) {
				result.add(note);
			}
		}
		
		// if the arraylist is null then there were no records about the player, go ahead and exit
		if (result.isEmpty()) {
			if (debugging) {
				log.info(prefix + " removeNote: result arraylist empty");
			}
			return false;
		} else {
			// verify input on the index, make sure they didn't specify a number larger or smaller than is possible
			if (((index) >= 0) && ((index) <= result.size())) {
				if (noteHash.remove(result.get(index))) {
					removeRecord(result.get(index));
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Find all notes recorded about specified player
	 *  @Param player - player to query about
	 *  @Return ArrayList<Note> - array of all notes on player
	 */
	public ArrayList<Note> getPlayer(String player) {
		// check that the noteHash exist
		
		// temp arraylist to hold results
		ArrayList<Note> result = new ArrayList<Note>();
		for (Note note : noteHash) {
			if (note.getPlayer().equalsIgnoreCase(player)) {
				// add the note to the array
				result.add(note);
			}
		}	
		
		// we have all the notes, return it
		if (result.isEmpty()) {
			return null;
		}
		else {
			return result;
		}
	}
	

	/**
	 * Get names of all players with notes about them
	 * 
	 * @return HashMap<String, Integer> - Player names as well as how many notes are about them
	 */
	public HashMap<String, Integer> getPlayers() {
		// create temp arraylist to return
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		// loop through the hashset getting the player of each note, adding each to the arraylist
		for (Note note : noteHash) {
			// see if they are already in the hashmap
			if (result.containsKey(note.getPlayer())) {
				// increment the count by 1
				result.put(note.getPlayer(), result.get(note.getPlayer()) + 1);
			} else {
				// not in there add them and start the count
				result.put(note.getPlayer(), 1);
			}
		}
		
		// now return the resulting array
		if (result.isEmpty()) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * Force a reload of the notes
	 */
	public void reload() {
		noteHash.clear();
		
		// load the hashset from whatever backedn is specified
		if (currentBackend.equals(Backend.FLATFILE)) {
			try {
				BufferedReader input = new BufferedReader(new FileReader(mainDirectory + "notes.txt"));
				
				// loop through the file to get all the blocks
				String line = null;
				while ((line = input.readLine()) != null) {
					// split the string up
					String[] data = line.split(";");

					String player = data[0];
					String note = data[1];
					String time = data[2];
					String poster = data[3];
					
					// check that input wasn't malformed
					if (!(poster == null) || !(player == null) || !(note == null) || !(time == null)) {
						// good input
						noteHash.add(new Note(player, poster, note, time));
					}
				}
				
				input.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (currentBackend.equals(Backend.SQLITE)) {
			ResultSet results = sqlite.query("SELECT * FROM notes");
			
			try {
				while (results.next()) {
					String player = results.getString("player");
					String poster = results.getString("poster");
					String time = results.getString("time");
					String note = results.getString("note");
					
					noteHash.add(new Note(player, poster, note, time));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (currentBackend.equals(Backend.MYSQL)) {
			ResultSet results = mysql.query("SELECT * FROM " + mysqlTable);
			
			try {
				while (results.next()) {
					String player = results.getString("player");
					String poster = results.getString("poster");
					String time = results.getString("time");
					String note = results.getString("note");
					
					noteHash.add(new Note(player, poster, note, time));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	/**
	 * Enable debugging of the note manager instance
	 * @param debug - true=enabled, false=disabled
	 */
	public void setDebug(Boolean debug) {
		debugging = debug;
	}
	
	/**
	 * Save a note to whichever backend was specified
	 * @param note - the note to be saved
	 * @return Return true if note is saved successfully
	 */
	public Boolean saveRecord(Note note) {
		if (currentBackend.equals(Backend.FLATFILE)) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(mainDirectory + "notes.txt", true));
				out.append(note.getPlayer() + ";" + note.getNote() + ";" + note.getTime() + ";" + note.getPoster());
				out.newLine();
				out.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}	
		} else if (currentBackend.equals(Backend.SQLITE)) {
			sqlite.query("INSERT INTO notes ('player', 'poster', 'note', 'time') VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');");
			return true;
		} else if (currentBackend.equals(Backend.MYSQL)) {
			String query = "INSERT INTO " + mysqlTable + " (`player`, `poster`, `note`, `time`) VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');";
			mysql.query(query);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Remove a note from the note manager instance
	 * @param note - the note instance to be removed
	 * @return Return true if note was removed
	 */
	public Boolean removeRecord(Note note) {
		if (currentBackend.equals(Backend.FLATFILE)) {
			// for flatfile easiest method will be to clear the file and rewrite everything
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(mainDirectory + "notes.txt"));
				
				// loop through the noteHash and output
				for (Note noteTemp : noteHash) {
					// check if the current note is the one we want to skip
					if (!noteTemp.equals(note)) {
						out.append(noteTemp.getPlayer() + ";" + noteTemp.getNote() + ";" + noteTemp.getTime() + ";" + noteTemp.getPoster());
						out.newLine();
					}
				}
				
				out.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else if (currentBackend.equals(Backend.SQLITE)) {
			sqlite.query("DELETE FROM notes WHERE player='" + note.getPlayer() + "' AND poster='" + note.getPoster() + "' AND note='" + note.getNote() + "' AND time='" + note.getTime() + "'");
			return true;
		} else if (currentBackend.equals(Backend.MYSQL)) {
			mysql.query("DELETE FROM " + mysqlTable + " WHERE `player`='" + note.getPlayer() + "' AND `poster`='" + note.getPoster() + "' AND `note`='" + note.getNote() + "' AND `time`='" + note.getTime() + "'");
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Create flatfile storage file if it doesn't exist
	 * 
	 * @return Return true if file was created
	 */
	private Boolean initFlatFile() {
		File file = new File(mainDirectory, "notes.txt");
		if (!file.exists()) {
			try { 
				file.createNewFile();
				if (debugging) {
					log.info(prefix + " notes.txt created");
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		if (debugging) {
			log.info(prefix + " flatfile successfully loaded");
		}
		reload();
		
		return false;
	}

	/**
	 * Initialize mysql driver, check connection, and create table if need be
	 * 
	 * @return Returns true if mysql initialization was successful
	 */
	private Boolean initMysql() {
		File file = new File(mainDirectory + "mysql.properties");
		if (!file.exists()) {
			createMySQLPropertiesFile();
		}
		
		// load properties creating the mysql object
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(mainDirectory + "mysql.properties"));
			mysql = new MySQL(log, prefix, prop.getProperty("mysql-host"), prop.getProperty("mysql-port"), prop.getProperty("mysql-database"), prop.getProperty("mysql-username"), prop.getProperty("mysql-password"));
			mysqlTable = prop.getProperty("mysql-table");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		mysql.open();
		
		// check if the table exists
		if (!mysql.checkTable(mysqlTable)) {
			log.info(prefix + " created table " + mysqlTable);
			String query = "CREATE TABLE IF NOT EXISTS " + mysqlTable + " (id INT NOT NULL AUTO_INCREMENT, player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15), PRIMARY KEY (id) );";
			mysql.createTable(query);
		}
		
		reload();
		return true;		
	}
	
	/**
	 *  Create SQLite database file and populate initial table
	 *  
	 * @return Returns true if SQLite was created successfully
	 */
	private Boolean initSqlite() {
		sqlite = new SQLite(log, prefix, "notes", mainDirectory);
		
		// open the connection, which initializes it
		sqlite.open();
		
		// check if the table exists
		if (!sqlite.checkTable("notes")) {
			log.info(prefix + " created table notes");
			String query = "CREATE TABLE notes (id INT AUTO_INCREMENT PRIMARY_KEY, player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15));";
			sqlite.createTable(query);
		}
		
		reload();
		return false;
	}
	
	/**
	 * Create file to hold mysql properties
	 * 
	 * @return Returns true if properties file was created successfully
	 */
	private Boolean createMySQLPropertiesFile() {
		try {
			// create the fields
			Properties prop = new Properties();
			prop.setProperty("mysql-host", "localhost");
			prop.setProperty("mysql-port", "3306");
			prop.setProperty("mysql-username", "root");
			prop.setProperty("mysql-password", "password");
			prop.setProperty("mysql-database", "minecraft");
			prop.setProperty("mysql-table", "notebook");
			prop.store(new FileOutputStream(mainDirectory + "mysql.properties"), null);
			
			if (debugging) {
				log.info(prefix + " mysql.properties created");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the number of notes note manager knows about.
	 * 
	 * @return int - Number of notes
	 */
	public int getNumNotes() {
		return noteHash.size();
	}
}
