package com.moosemanstudios.Notebook.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;

import net.h31ix.updater.Updater;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moosemanstudios.Notebook.Core.Note;
import com.moosemanstudios.Notebook.Core.NoteManager;
import com.moosemanstudios.Notebook.Core.NoteManager.Backend;

public class NotebookCommandExecutor implements CommandExecutor {

	private Notebook plugin;
	
	public NotebookCommandExecutor(Notebook instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("static-access")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String[] split = args;
		String commandName = cmd.getName().toLowerCase();
		
		if (commandName.equalsIgnoreCase("note")) {
			if (split.length == 0) {
				sender.sendMessage("/note help" + ChatColor.RED + ": Display this help screen");
				sender.sendMessage("/note version " + ChatColor.RED + ": Show plugin version");
				
				if (sender.hasPermission("notebook.add")) {
					sender.sendMessage("/note add <player> <note>" + ChatColor.RED + ": Add note about specified player");
				}
				if (sender.hasPermission("notebook.remove")) {
					sender.sendMessage("/note remove <player> <note number>" + ChatColor.RED + ": Remove note about specified player");
				}
				if (sender.hasPermission("notebook.show")) {
					sender.sendMessage("/note show <player>" + ChatColor.RED + ": Show notes about specified player");
				}
				if (sender.hasPermission("notebook.list")) {
					sender.sendMessage("/note list" + ChatColor.RED + ": List all players who have notes");
				}
				if (sender.hasPermission("notebook.admin")) {
					sender.sendMessage("/note reload" + ChatColor.RED + ": Reload the notes file");
					sender.sendMessage("/note update" + ChatColor.RED + ": Update plugin");
					sender.sendMessage("/note backend <flatfile/sqlite/mysql" + ChatColor.RED + ": Change backend storage type");
				}
			} else {
				if (split[0].equalsIgnoreCase("help")) {
					// display help screen
					sender.sendMessage("/note help" + ChatColor.RED + ": Display this help screen");
					sender.sendMessage("/note version " + ChatColor.RED + ": Show plugin version");
					
					if (sender.hasPermission("notebook.add")) {
						sender.sendMessage("/note add <player> <note>" + ChatColor.RED + ": Add note about specified player");
					}
					if (sender.hasPermission("notebook.remove")) {
						sender.sendMessage("/note remove <player> <note number>" + ChatColor.RED + ": Remove note about specified player");
					}
					if (sender.hasPermission("notebook.show")) {
						sender.sendMessage("/note show <player>" + ChatColor.RED + ": Show notes about specified player");
					}
					if (sender.hasPermission("notebook.list")) {
						sender.sendMessage("/note list" + ChatColor.RED + ": List all players who have notes");
					}
					if (sender.hasPermission("notebook.admin")) {
						sender.sendMessage("/note reload" + ChatColor.RED + ": Reload the notes file");
						sender.sendMessage("/note update" + ChatColor.RED + ": Update plugin");
						sender.sendMessage("/note backend <flatfile/sqlite/mysql" + ChatColor.RED + ": Change backend storage type");
					}
					
				} else if (split[0].equalsIgnoreCase("version")) {
					// display plugin version
					sender.sendMessage(ChatColor.GOLD + "Notebook Version: " + ChatColor.WHITE + plugin.pdfFile.getVersion() + ChatColor.GOLD + " - Author: moose517");
					
				} else if (split[0].equalsIgnoreCase("add")) {
					// add note about player
					
					// check permissions first of all
					if (sender.hasPermission("notebook.add")) {
						// make sure they have specified all the necessary inputs
						if (split.length < 3) {
							sender.sendMessage(ChatColor.RED + "Must provide player and note to add");
						} else {
							if (NoteManager.getInstance().addNote(sender.getName(), split[1], arrayToString(split, 2))) {
								sender.sendMessage("Note successfully added on " + split[1]);
							
								if (plugin.broadcastMessage) {
									// let all others with the permission know
									Player[] players = plugin.getServer().getOnlinePlayers();
									for (Player player : players) {
										// see if the player is the send, if so skip them
										if (!player.getName().equalsIgnoreCase(sender.getName())) {
											// now see if the player has the permission node
											if (player.hasPermission("notebook.add")) {
												player.sendMessage(sender.getName() + " submitted note about " + split[1]);
											}
										}
									}
								}
								
								// log it as well, paper trail
								plugin.log.info(plugin.prefix + " " + sender.getName() + " added note about player " + split[1]);
							} else {
								sender.sendMessage("Note failed to be added");
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.add");
					}
					
				} else if (split[0].equalsIgnoreCase("update")) {
					if (sender.hasPermission("notebook.admin")) {
						Updater updater = new Updater(plugin, "notebook", plugin.pluginFile, Updater.UpdateType.DEFAULT, true);
						if (updater.getResult() == updater.getResult().SUCCESS)
							sender.sendMessage(ChatColor.AQUA + "Download of update successful");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.admin");
					}
					
				} else if (split[0].equalsIgnoreCase("remove")) {
					// remove note about player
					if (sender.hasPermission("notebook.remove")) {
						if (split.length >= 2) {	// split[0] = remove, split[1] = playername, split[2] = indextoremove
							if (split.length >= 3) {
								if (NoteManager.getInstance().removeNote(split[1], Integer.parseInt(split[2]) - 1)) {
									sender.sendMessage("Note removed successfully");
									
									// let all others know with permission that it was removed
									if (plugin.broadcastMessage) {
										Player[] players = plugin.getServer().getOnlinePlayers();
										for (Player player : players) {
											if (!player.getName().equalsIgnoreCase(sender.getName())) {
												//now see if the player has the permission node
												if (player.hasPermission("notebook.remove")) {
													player.sendMessage(sender.getName() + " removed note about player " + split[1]);
												}
											}
										}
									}
									
									plugin.log.info(plugin.prefix + " " + sender.getName() + " removed note about player " + split[1]);
								} else {
									sender.sendMessage("Unable to remove note, please check entered information");
								}
							} else {
								sender.sendMessage("Must specify note number to remove");
							}
						} else {
							sender.sendMessage("Must specify player");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.remove");
					}
					

				} else if (split[0].equalsIgnoreCase("show")) {
					// show notes about player
					if (sender.hasPermission("notebook.show")) {
						if (split.length >= 2) {					
							ArrayList<Note> notes = NoteManager.getInstance().getPlayer(split[1]);
							
							if (notes != null) {
								sender.sendMessage("Notebook - Notes on " + split[1]);
								sender.sendMessage("------------------------------------");
								
								int i = 1;
								for (Note note : notes) {
									sender.sendMessage(ChatColor.GOLD + Integer.toString(i) + ") " + ChatColor.WHITE + note.getTime() + " " + note.getNote() + " - Poster: " + note.getPoster());
									i++;
								}
							
							} else {
								// no notes on the player, tell them
								sender.sendMessage("No notes could be found on " + split[1]);
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Must specify player");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.show");
					}
					
				} else if (split[0].equalsIgnoreCase("list")) {
					// list all players who have a note about them
					if (sender.hasPermission("notebook.list")) {
						HashMap<String, Integer> players = NoteManager.getInstance().getPlayers();
						
						sender.sendMessage("Notebook - Players with notes");
						sender.sendMessage("--------------------------------------");
						
						if (players != null) {
							int i = 1;
							for (String player : players.keySet()) {
								sender.sendMessage(ChatColor.GOLD + Integer.toString(i) + ") " + ChatColor.WHITE + player + " (" + players.get(player) + " notes)");
								i++;
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.list");
					}
				} else if (split[0].equalsIgnoreCase("reload")) {
					// reload the file from disk, in case of manual edit
					if (sender.hasPermission("notebook.admin")) {
						NoteManager.getInstance().reload();
						sender.sendMessage("Notebook configuration reloaded");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.admin");
					}
				} else if (split[0].equalsIgnoreCase("backend")) {
					if (sender.hasPermission("notebook.admin")) {
						if (split.length != 1) {
							// change the current backend and run command to save all current records out to that new backend
							if (split[1].equalsIgnoreCase("flatfile")) {
								// testing code but should be final
								if (NoteManager.getInstance().initFlatFile(plugin.getConfig().getString("storage.flatfile.filename"))) {
									if (NoteManager.getInstance().switchBackend(Backend.FLATFILE)) {
										plugin.getConfig().set("storage.flatfile.enabled", true);
										plugin.getConfig().set("storage.sqlite.enabled", false);
										plugin.getConfig().set("storage.mysql.enabled", false);
										plugin.saveConfig();
										sender.sendMessage("Backend switched successfully");
									} else 
										sender.sendMessage("Error switching backend types");
								} else
									sender.sendMessage("Unable to initialize flat file storage");
							} else if (split[1].equalsIgnoreCase("mysql") || split[1].equalsIgnoreCase("sqlite")) {
								if (plugin.sqlibraryFound) {
									if (split[1].equalsIgnoreCase("mysql")) {
										if (NoteManager.getInstance().initMysql(plugin.getConfig().getString("storage.mysql.host"), plugin.getConfig().getInt("storage.mysql.port"), plugin.getConfig().getString("storage.mysql.username"), plugin.getConfig().getString("storage.mysql.password"), plugin.getConfig().getString("storage.mysql.database"), plugin.getConfig().getString("storage.mysql.table"))) {
											if (NoteManager.getInstance().switchBackend(Backend.MYSQL)) {
												plugin.getConfig().set("storage.flatfile.enabled", false);
												plugin.getConfig().set("storage.sqlite.enabled", false);
												plugin.getConfig().set("storage.mysql.enabled", true);
												plugin.saveConfig();
												sender.sendMessage("Backend switched successfully");
											} else {
												sender.sendMessage("Error switching backend types");
											}
										} else {
											sender.sendMessage("Unable to initialize MySQL storage");
										}
									} else if (split[1].equalsIgnoreCase("sqlite")) {
										if (NoteManager.getInstance().initSqlite(plugin.getConfig().getString("storage.sqlite.filename"), plugin.getConfig().getString("storage.sqlite.table"))) {
											if (NoteManager.getInstance().switchBackend(Backend.SQLITE)) {
												plugin.getConfig().set("storage.flatfile.enabled", false);
												plugin.getConfig().set("storage.sqlite.enabled", true);
												plugin.getConfig().set("storage.mysql.enabled", false);
												plugin.saveConfig();
												sender.sendMessage("Backend switched successfully");
											} else {
												sender.sendMessage("Error switching backend types");
											}
										} else {
											sender.sendMessage("Unable to initialize SQLite storage");
										}
									}
								} else {
									sender.sendMessage(ChatColor.RED + "plugin SQLibrary not found and is required for sqlite and mysql storage");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Invalid backend specified");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Must specify backend type to change to");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing required permission: " + ChatColor.WHITE + "notebook.admin");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Unknown command, type " + ChatColor.WHITE + "/note help" + ChatColor.RED + " for help");
				}
				return true;
			}
		}
	
		return false;
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
