package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.ShelfSpeak;
import pyromanic.ShelfSpeak.ssPermissions;

/**
 * Handler for the /shelfwrite command.
 * 
 * @author pyromanic
 */
public class WriteCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public WriteCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		
		if(!ssPermissions.getInstance().write(player) 
				&& !ssPermissions.getInstance().writeAll(player))
		{
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to write.");
			return true;
		}
		
		if(split.length == 0)
			if(plugin.activeCmd.get(player) == null)
			{
				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the Bookshelf you wish to write to.");
				plugin.activeCmd.put(player, "write");
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must end the current write first!");
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect command usage. Use " + ChatColor.AQUA + "/shelfwrite");
		return true;
	}
}