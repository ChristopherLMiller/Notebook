package com.moosemanstudios.Notebook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLite;


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
	String mysqlHost;
	String mysqlUsername;
	String mysqlPassword;
	String mysqlDatabase;
	int mysqlPort;
	String flatFileFilename;
	String sqliteFilename;
	String sqliteTable;
	
	
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
	 */
	public void init() {
		// check for valid noteHash
		if (noteHash == null) {
			noteHash = new HashSet<Note>();
		}
		
		debugging = true;
		log = Logger.getLogger("minecraft");
		prefix = "[Notebook]";
		mainDirectory = "plugins/notebook/";
	}
	
	/**
	 * Set the backend to a specific type
	 * @param backend - the backend type to use
	 */
	public void setBackend(Backend backend) {
		// set the backend type based on the input
		currentBackend = backend;
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
				BufferedReader input = new BufferedReader(new FileReader(mainDirectory + flatFileFilename));
				
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
			
			try {
				String query = "SELECT * FROM " + getSQLiteTable();
				ResultSet results = sqlite.query(query);
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
			try {
				ResultSet results = mysql.query("SELECT * FROM " + getMySQLTable());
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
				BufferedWriter out = new BufferedWriter(new FileWriter(mainDirectory + flatFileFilename, true));
				out.append(note.getPlayer() + ";" + note.getNote() + ";" + note.getTime() + ";" + note.getPoster());
				out.newLine();
				out.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}	
		} else if (currentBackend.equals(Backend.SQLITE)) {
			try {
				sqlite.query("INSERT INTO " + getSQLiteTable() + " ('player', 'poster', 'note', 'time') VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else if (currentBackend.equals(Backend.MYSQL)) {
			try {
				String query = "INSERT INTO " + getMySQLTable() + " (`player`, `poster`, `note`, `time`) VALUES ('" + note.getPlayer() + "', '" + note.getPoster() + "', '" + note.getNote() + "', '" + note.getTime() + "');";
				mysql.query(query);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
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
				BufferedWriter out = new BufferedWriter(new FileWriter(mainDirectory + flatFileFilename));
				
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
			try {
				sqlite.query("DELETE FROM " + getSQLiteTable() + " WHERE player='" + note.getPlayer() + "' AND poster='" + note.getPoster() + "' AND note='" + note.getNote() + "' AND time='" + note.getTime() + "'");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else if (currentBackend.equals(Backend.MYSQL)) {
			try {
				mysql.query("DELETE FROM " + getMySQLTable() + " WHERE `player`='" + note.getPlayer() + "' AND `poster`='" + note.getPoster() + "' AND `note`='" + note.getNote() + "' AND `time`='" + note.getTime() + "'");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Create flatfile storage file if it doesn't exist
	 * 
	 * @return Return true if file was created
	 */
	public Boolean initFlatFile(String filename) {
		setFlatFileFilename(filename);
		File file = new File(mainDirectory, getFlatFileFilename());
		if (!file.exists()) {
			try { 
				file.createNewFile();
				if (debugging) {
					log.info(prefix + " " + flatFileFilename + " created");
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
	 * set the local name of the flat file
	 * @param input filename
	 */
	private void setFlatFileFilename(String filename)
	{
		flatFileFilename = filename;
	}
	
	/**
	 * Get the name of the flat file
	 * @return String - filename
	 */
	private String getFlatFileFilename()
	{
		return flatFileFilename;
	}

	/**
	 * Initialize mysql driver, check connection, and create table if need be
	 * 
	 * @return Returns true if mysql initialization was successful
	 */
	@SuppressWarnings("deprecation")
	public Boolean initMysql(String host, int port, String username, String password, String database, String table) {
		setMySQLproperties(host, port, username, password, database, table);
		mysql = new MySQL(log, prefix, getMySQLHost(), getMySQLPort(), getMySQLDatabase(), getMySQLUsername(), getMySQLPassword());
		mysql.open();
		
		// check if the table exists
		if (!mysql.checkTable(getMySQLTable())) {
			log.info(prefix + " created table " + getMySQLTable());
			String query = "CREATE TABLE IF NOT EXISTS " + getMySQLTable() + " (id INT NOT NULL AUTO_INCREMENT, player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15), PRIMARY KEY (id) );";
			mysql.createTable(query);
		}
		
		reload();
		return true;		
	}
	
	private void setMySQLproperties(String host, int port, String username, String password, String database, String table)
	{
		mysqlHost = host;
		mysqlUsername = username;
		mysqlPassword = password;
		mysqlDatabase = database;
		mysqlTable = table;
	}
	
	private String getMySQLHost() {
		return mysqlHost;
	}
	
	private int getMySQLPort() {
		return mysqlPort;
	}
	
	private String getMySQLDatabase() {
		return mysqlDatabase;
	}
	
	private String getMySQLUsername() {
		return mysqlUsername;
	}
	
	private String getMySQLPassword() {
		return mysqlPassword;
	}
	
	private String getMySQLTable() {
		return mysqlTable;
	}
	
	/**
	 *  Create SQLite database file and populate initial table
	 * @return Returns true if SQLite was created successfully
	 */
	@SuppressWarnings("deprecation")
	public Boolean initSqlite(String filename, String table) {
		setSQliteProperties(filename, table);
		sqlite = new SQLite(log, prefix, getSQliteFilename(), mainDirectory);
		
		// open the connection, which initializes it
		sqlite.open();
		
		// check if the table exists
		if (!sqlite.checkTable("notes")) {
			log.info(prefix + " created table notes");
			String query = "CREATE TABLE " + getSQLiteTable() + " (id INT AUTO_INCREMENT PRIMARY_KEY, player VARCHAR(16), poster VARCHAR(16), note VARCHAR(255), time VARCHAR(15));";
			sqlite.createTable(query);
		}
		
		reload();
		return false;
	}
	
	/**
	 * Set the sqlite properties
	 * @param String filename
	 * @param String table
	 */
	private void setSQliteProperties(String filename, String table)
	{
		sqliteFilename = filename;
		sqliteTable = table;
	}
	
	private String getSQliteFilename()
	{
		return sqliteFilename;
	}
	
	private String getSQLiteTable()
	{
		return sqliteTable;
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
