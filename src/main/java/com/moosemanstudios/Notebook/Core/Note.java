package com.moosemanstudios.Notebook.Core;

// Note - class designed to handle a note about a player
public class Note {
	private String player;	// who the notes about
	private String poster;  // who made the note
	private String timestamp;	// when the note was created
	private String note;	// the actual note
	
	
	Note(String whoAbout, String whoPosted, String message, String time) {
		player = whoAbout;
		poster = whoPosted;
		note = message;
		timestamp = time;	// new SimpleDateFormat("MM/dd/yy HH:mm").format(Calendar.getInstance().getTime());
	}
	
	public String getNote() {
		return note;
	}
	
	public String getPlayer() {
		return player;
	}
	
	public String getPoster() {
		return poster;
	}
	
	public String getTime() {
		return timestamp;
	}
}