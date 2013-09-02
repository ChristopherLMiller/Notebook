package com.moosemanstudios.Notebook.Spout;

import java.util.ArrayList;
import java.util.HashMap;

import org.spout.api.Server;
import org.spout.api.Spout;
import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.CommandDescription;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.vanilla.ChatStyle;

import com.moosemanstudios.Notebook.Core.Note;
import com.moosemanstudios.Notebook.Core.NoteManager;

public class NotebookCommandExecutor {
	private final Notebook plugin;
	
	public NotebookCommandExecutor(Notebook plugin) {
		this.plugin = plugin;
	}

	@CommandDescription(aliases = "note", desc = "General command for notebook")
	public void note(CommandSource source, CommandArguments args) throws CommandException {
		if (source instanceof Player) {
			if (args.length() == 0) {
				source.sendMessage(ChatStyle.RED + "Type " + ChatStyle.WHITE + "/note help" + ChatStyle.RED + " for help");
			} else {
				String[] strArgs = args.toArray();
				
				if (strArgs[0].equalsIgnoreCase("help")) {
					// display the help screen
					source.sendMessage(ChatStyle.RED + "/note help" + ChatStyle.WHITE + ": Display this help screen");
					source.sendMessage(ChatStyle.RED + "/note version" + ChatStyle.WHITE + ": Show plugin version");
					
					if (source.hasPermission("notebook.add")) {
						source.sendMessage(ChatStyle.RED + "/note add <player> <note>" + ChatStyle.WHITE + ": Add note about specified player");
					}
					if (source.hasPermission("notebook.remove")) {
						source.sendMessage(ChatStyle.RED + "/note remove <player> <note number>" + ChatStyle.WHITE + ": Remove specified not about player");
					}
					if (source.hasPermission("notebook.show")) {
						source.sendMessage(ChatStyle.RED + "/note show <player>" + ChatStyle.WHITE + ": Show notes about player");
					}
					if (source.hasPermission("notebook.list")) {
						source.sendMessage(ChatStyle.RED + "/note list " + ChatStyle.WHITE + ": List all players who have notes");
					}
					if (source.hasPermission("notebook.admin")) {
						source.sendMessage(ChatStyle.RED+ "/note reload" + ChatStyle.WHITE + ": Reload the notes file");
					}
				} else if (strArgs[0].equalsIgnoreCase("version")) {
					source.sendMessage(ChatStyle.GOLD + plugin.getName() + " Version: " + ChatStyle.WHITE + plugin.getDescription().getVersion());
				}
				
			}
		}
		else {
			if (args.length() == 0) {
				source.sendMessage("Type /note help for help");
			} else {
				String[] strArgs = args.toArray();
				
				if (strArgs[0].equalsIgnoreCase("help")) {
					// display the help screen
					source.sendMessage("/note help: Display this help screen");
					source.sendMessage("/note version: Show plugin version");
					source.sendMessage("/note add <player> <note>: Add note about specified player");
					source.sendMessage("/note remove <player> <note number>: Remove note about specified player");
					source.sendMessage("/note show <player>: Show notes about specified player");
					source.sendMessage("/note list: List all players who have notes");
					source.sendMessage("/note reload: Reload the notes file");
				} else if (strArgs[0].equalsIgnoreCase("version")) {
					// display the version
					source.sendMessage("Notebook version: " + plugin.getDescription().getVersion());
				} else if (strArgs[0].equalsIgnoreCase("add")) {
					// add note
					if (strArgs.length < 3) {
						source.sendMessage("Must provide player and note to add");
					} else {
						if (NoteManager.getInstance().addNote(source.getName(), strArgs[1], arrayToString(strArgs, 2))) {
							source.sendMessage("Note successfully added on " + strArgs[1]);
							
							// broadcast message
							if (plugin.broadcastMessage) {
								Server server = (Server)Spout.getEngine();
								
								Player[] players = server.getOnlinePlayers();
								for (Player player : players) {
									if (player.hasPermission("notebook.note")) {
										player.sendMessage(source.getName() + " submitted note about " + strArgs[1]);
									}
								}
							}
							plugin.log.info(plugin.prefix + " " + source.getName() + " added note about player " + strArgs[1]);
						} else {
							source.sendMessage("Note failed to be added");
						}
					}
				} else if (strArgs[0].equalsIgnoreCase("remove")) {
					// remove a note
					if (strArgs.length >= 2) {
						if (strArgs.length >= 3) {
							if (NoteManager.getInstance().removeNote(strArgs[1],  Integer.parseInt(strArgs[2]) - 1)) {
								source.sendMessage("Note removed successfully");
								
								// let the others know with permission that it was removed
								if (plugin.broadcastMessage) {
									Server server = (Server) Spout.getEngine();
									
									Player[] players = server.getOnlinePlayers();
									for (Player player : players) {
										if (player.hasPermission("notebook.note")) {
											player.sendMessage(source.getName() + " removed not about player " + strArgs[1]);
										}
									}
								}
								plugin.log.info(plugin.prefix + " " + source.getName() +  " removed note about player " + strArgs[1]);
							} else {
								source.sendMessage("Note failed to be removed");
							}
						} else {
							source.sendMessage("Must specify note number to remove");
						}
					} else {
						source.sendMessage("Must Specify player and note number");
					}
				} else if (strArgs[0].equalsIgnoreCase("show")) {
					if (strArgs.length >= 2) {
						ArrayList<Note> notes = NoteManager.getInstance().getPlayer(strArgs[1]);
						
						if (notes != null) {
							source.sendMessage("Notebook - Notes on " + strArgs[1]);
							source.sendMessage("-----------------------------------------");
							
							int i = 0;
							for (Note note : notes) {
								source.sendMessage(Integer.toString(i) + ") " + note.getTime() + " " + note.getNote() + " - Poster: " + note.getPoster());
								i++;
							}
						} else {
							source.sendMessage("No notes could be found on " + strArgs[1]);
						}
					} else {
						source.sendMessage("Must specify player");
					}
				} else if (strArgs[0].equalsIgnoreCase("list")) {
					// list notes
					HashMap<String, Integer> players = NoteManager.getInstance().getPlayers();
					
					source.sendMessage("Notebook - Players with notes");
					source.sendMessage("-------------------------------------");
					
					if (players != null) {
						int i = 1;
						for (String player : players.keySet()) {
							source.sendMessage(Integer.toString(i) + ") " + player + " (" + players.get(player) + " notes)");
						}
					}
				} else if (strArgs[0].equalsIgnoreCase("reload")) {
					NoteManager.getInstance().reload();
					source.sendMessage("Notebook configuration reloaded");
				} else {
					source.sendMessage("Unknown command, type /note help for help");
				}
			}
		}
	}
	
	public String arrayToString(String[] input) {
		return arrayToString(input, 0);
	}
	public String arrayToString(String[] input, int start) {
		String result = "";
		result = input[start];
		for (int i = start+1; i < input.length; i++) {
			result = result + " " + input[i];
		}
		return result;
	}
}
