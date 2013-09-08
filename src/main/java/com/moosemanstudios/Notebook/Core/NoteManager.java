package com.moosemanstudios.Notebook.Core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.MySQL;


public class NoteManager {
	private static NoteManager instance = null;
	HashSet<Note> noteHash;
	Boolean debugging;
	Logger log;
	String prefix;
	Backend currentBackend;
	String mainDirectory;

	// new variables
	FlatFile flatFile;
	SQlite sqlite;
	Mysql mysql;
	
	
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
		mainDirectory = "plugins/Notebook/";
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
	
	public Boolean switchBackend(Backend backend) {
		if (backend.equals(getBackend())) {
			// why would we do anything if we aren't changing anything!
			return false;
		} else if (backend == Backend.FLATFILE) {
			if (flatFile.saveRecords(noteHash)) {
				setBackend(Backend.FLATFILE);
				return true;
			}
		}
		// TODO: handle other cases
		return false;
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
		
		switch(getBackend()) {
		case FLATFILE:
			noteHash.addAll(flatFile.getRecords());
			break;
		case MYSQL:
			noteHash.addAll(mysql.getRecords());
			break;
		case SQLITE:
			noteHash.addAll(sqlite.getRecords());
			break;
		default:
			log.severe(prefix + "Invalid backend in reload()");
			break;
		}
		
		log.info(prefix + " Loaded "  + getNumNotes() +  " notes");
		
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
		
		switch (getBackend()) {
		case FLATFILE:
			return flatFile.saveRecord(note);
		case MYSQL:
			return mysql.saveRecord(note);
		case SQLITE:
			return sqlite.saveRecord(note);
		default:
			log.severe(prefix + "Invalid backend in saveRecord()");
			return false;
		
		}
	}
	
	/**
	 * Remove a note from the note manager instance
	 * @param note - the note instance to be removed
	 * @return Return true if note was removed
	 */
	public Boolean removeRecord(Note note) {
		switch (getBackend()) {
		case FLATFILE:
			return flatFile.removeRecord(note, noteHash);
		case MYSQL:
			return mysql.removeRecord(note);
		case SQLITE:
			return sqlite.removeRecord(note);
		default:
			log.severe(prefix + " Invalid backedn in removeRecord()");
			return false;
		}
	}
	
	/**
	 * Create flatfile storage file if it doesn't exist
	 * 
	 * @return Return true if file was created
	 */
	public Boolean initFlatFile(String filename) {
		flatFile = new FlatFile();
		
		if (!flatFile.fileExists()) {
			if (flatFile.create(mainDirectory, filename)) {
				log.info(prefix + " " + filename + " created successfully");
			} else {
				log.severe(prefix + "Unable to create the file: " + filename);
				return false;
			}
		}
		reload();
		return true;
	}
	
	/**
	 *  Create SQLite database file and populate initial table
	 * @return Returns true if SQLite was created successfully
	 */
	public Boolean initSqlite(String filename, String table) {
		sqlite = new SQlite();
		
		if (sqlite.create(mainDirectory, filename, log, prefix, table)) {
			log.info(prefix + " " + filename + " created successfully");
		} else {
			log.severe(prefix + "Unable to create the file: " + filename);
			return false;
		}

		reload();
		return true;
	}

	/**
	 * Initialize mysql driver, check connection, and create table if need be
	 * 
	 * @return Returns true if mysql initialization was successful
	 */
	public Boolean initMysql(String host, int port, String username, String password, String database, String table) {
		mysql = new Mysql();
		
		if (mysql.create(log, table, host, port, database, username, password, table)) {
			log.info(prefix + " mysql database created successfully");
		} else {
			log.severe(prefix + "Unable to create the mysql database");
			return false;
		}
		
		reload();
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
