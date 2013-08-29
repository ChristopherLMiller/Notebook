package com.moosemanstudios.Notebook;

import java.util.HashMap;
import java.util.List;

import org.spout.api.Platform;
import org.spout.api.Server;
import org.spout.api.Spout;
import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.CommandDescription;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.vanilla.ChatStyle;

public class NotebookCommandExecutorSpout {
	private final NotebookSpout plugin;
	
	public NotebookCommandExecutorSpout(NotebookSpout plugin) {
		this.plugin = plugin;
	}

	@CommandDescription(aliases = "note", desc = "General command for notebook")
	public void note(CommandSource source, CommandArguments args) throws CommandException {
		if (source instanceof Player) {
			if (args.length() == 0) {
				source.sendMessage(ChatStyle.RED + "Type " + ChatStyle.WHITE + "/note help" + ChatStyle.RED + " for help");
			} else {
				String[] strArgs = args.toArray();
				
				for(String arg : strArgs) {
					plugin.log.info(plugin.prefix + arg);
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
						}
					}
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
