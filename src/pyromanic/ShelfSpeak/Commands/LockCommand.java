package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.AdvShelf;
import pyromanic.ShelfSpeak.ShelfSpeak;
import pyromanic.ShelfSpeak.ssPermissions;

/**
 * Handler for the /shelflock command.
 * 
 * @author pyromanic
 */
public class LockCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public LockCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		AdvShelf shelf = plugin.activeShelf.get(player);
		ssPermissions permission = ssPermissions.getInstance();
		
		// Check command length & type
		if((split.length == 1 || split.length == 2) 
				&& (split[0].equalsIgnoreCase("read") || 
						split[0].equalsIgnoreCase("write")))
		{
			split[0] = split[0].toLowerCase();	// 1st arg to lower for quick comparison
			if((split[0].equalsIgnoreCase("read") && !permission.lockRead(player)) 
					|| (split[0].equalsIgnoreCase("write") && !permission.lockWrite(player)))
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to use that lock.");
				return true;
			}
			if(shelf == null)	// Begin interactive mode if user has no active shelf
			{
				if(split.length == 1 || (split.length == 2 && !split[1].equalsIgnoreCase("list")))
				{
					plugin.activeCmd.put(player, "lock:" + 
							split[0] + ":" + ((split.length == 2) ? split[1] : ""));
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the shelf you wish to lock.");
				}
				else if(split.length == 2 && split[1].equalsIgnoreCase("list"))
				{
					plugin.activeCmd.put(player, "lock:" + split[0] + ":list");
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the shelf you wish to view locks for.");
				}
			}
			else
			{
				performLock(player, shelf, split);
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use one of the following:");
			sender.sendMessage(ChatColor.AQUA + "[ShelfSpeak] /shelflock <[read:write]> {player}");
			sender.sendMessage(ChatColor.AQUA + "[ShelfSpeak] /shelflock <[read:write]> list");
		}
		return true;
	}
	
	public static void performLock(Player player, AdvShelf shelf, String[] args)
	{
		ssPermissions permission = ssPermissions.getInstance();
		if(!shelf.isOwner(player) && !permission.lockAll(player))
		{
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be the owner to use locks.");
			return;
		}
		if(args.length == 1)	// Full lock
		{
			// Set shelf read/write lock
			boolean locked = false;
			if(args[0].equals("read"))
				locked = shelf.setReadable(!shelf.canRead(""));
			else
				locked = shelf.setWritable(!shelf.canWrite(""));
			player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
					(locked ? "Locked " : "Unlocked ") + args[0]);
		}
		else if(args.length == 2)	// View list or Grant/revoke user priv
		{
			// Show read/write priv list of players
			if(args[1].equalsIgnoreCase("list"))
				shelf.showLocks(player, args[0]);
			else
			{
				boolean granted = false;
				if(args[0].equals("read"))
					granted = shelf.addReader(args[1]);
				else if(args[0].equals("write"))
					granted = shelf.addWriter(args[1]);
				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
						(granted ? "Granted " : "Revoked ") + args[0] + 
						": " + ChatColor.GREEN + args[1]);
			}
		}
	}

}