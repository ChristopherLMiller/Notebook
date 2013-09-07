package com.moosemanstudios.Notebook.Core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class FlatFile {
	
	private String filename;
	private String directory;
	private File file;
	
	/**
	 * Initializes the FlatFile object
	 * @param filename The name of the file to hold the notes
	 * @return If it was created successfully
	 */
	public Boolean create(String directory, String filename) {
		setFileName(filename);
		setDirectory(directory);
		
		this.file = new File(getDirectory(), getFileName());
		
		if (!this.file.exists()) {
			try {
				if (this.file.createNewFile()) 
					return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
		return false;
	}
	
	/**
	 * check if the file exists
	 * @return file found
	 */
	public Boolean fileExists() {
		if (file != null)
			return this.file.exists();
		else {
			return false;
		}
	}
	
	/**
	 * Set the file name
	 * @param filename The name of the file
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Get the filename
	 * @return
	 */
	public String getFileName() {
		return filename;
	}
	
	/**
	 * Set the directory where the file will be stored
	 * @param directory directoy location
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	/**
	 * Get the directory
	 * @return
	 */
	public String getDirectory() {
		return directory;
	}
	
	/**
	 * Get all records in the file
	 * @return HashSet containing all the note records loaded from the file
	 */
	public HashSet<Note> getRecords() {
		
		HashSet<Note> records = new HashSet<Note>();
		
		// make sure the file exists before we try and open it to read in records
		if (fileExists()) {
			BufferedReader input;
			try {
				input = new BufferedReader(new FileReader(getDirectory() + getFileName()));
				
				String line = null;
				while ((line = input.readLine()) != null) {
					String[] data = line.split(";");
					String player = data[0];
					String note = data[1];
					String time = data[2];
					String poster = data[3];
					
					// check that input wasn't malformed
					if (!(poster == null) || !(player == null) || !(note == null) || !(time == null)) {
						// good input
						records.add(new Note(player, poster, note, time));
					}
				}
				
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			
			return records;

		} else {
			return null;
		}
	}
	
	/**
	 * Save a record to the file
	 * @param note The note to be written to the file
	 * @return if it was written successfully
	 */
	public Boolean saveRecord(Note note) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getDirectory() + getFileName(), true));
			out.append(note.getPlayer() + ";" + note.getNote() + ";" + note.getTime() + ";" + note.getPoster());
			out.newLine();
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}	
	}
	
	/**
	 * Save all the records to file
	 * @param notes HashSet containing all notes
	 * @return if notes all saved successfully
	 */
	public Boolean saveRecords(HashSet<Note> notes) {
		for (Note note : notes) {
			saveRecord(note);
		}
		return true;
	}
	
	/**
	 * Remove the specified record from the file
	 * @param note The note to be removed
	 * @param notes HashSet containing all notes, required due to way removal is done
	 * @return If the not was removed successfully
	 */
	public Boolean removeRecord(Note note, HashSet<Note> notes) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getDirectory() + getFileName()));
			
			// loop through the noteHash and output
			for (Note noteTemp : notes) {
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
	}
}
