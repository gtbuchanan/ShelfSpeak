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
			if(!permission.lockRead(player) || !permission.lockWrite(player))
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to use that lock.");
				return true;
			}
			String type = split[0].toLowerCase();
			// Begin interactive mode if user has no active shelf
			if(shelf == null)
			{
				if(split.length == 1 || (split.length == 2 && !split[1].equalsIgnoreCase("list")))
				{
					plugin.activeCmd.put(player, "lock:" + 
							type + ":" + ((split.length == 2) ? split[1] : ""));
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the shelf you wish to lock.");
				}
				else if(split.length == 2 && split[1].equalsIgnoreCase("list"))
				{
					plugin.activeCmd.put(player, "lock:" + type + ":list");
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the shelf you wish to view locks for.");
				}
			}
			else
			{
				if(!player.getName().equalsIgnoreCase(shelf.getOwner()))
				{
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be the owner to use locks.");
					return true;
				}
				if(split.length == 1)
				{
					// Set shelf read/write lock
					boolean locked = false;
					if(type.equalsIgnoreCase("read"))
						locked = shelf.setReadable(!shelf.canRead(""));
					else
						locked = shelf.setWritable(!shelf.canWrite(""));
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
							(locked ? "Locked " : "Unlocked ") + type);
				}
				else if(split.length == 2)
				{
					// Show read/write priv list of players
					if(split[1].equalsIgnoreCase("list"))
						shelf.showLocks(player, type);
					else
					{
						boolean granted = false;
						if(type.equalsIgnoreCase("read"))
							granted = shelf.addReader(split[1]);
						else
							granted = shelf.addWriter(split[1]);
						player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
								(granted ? "Granted " : "Revoked ") + type + 
								": " + ChatColor.GREEN + split[1]);
					}
				}
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
}