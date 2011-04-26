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
 * Handler for the /shelf command.
 * 
 * @author pyromanic
 */
public class ShelfCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public ShelfCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		
		if(split.length > 0 && !ssPermissions.getInstance().read(player))
		{
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to read.");
			return true;
		}
		
		if(split.length == 0)
			ShelfSpeak.showCommands(player);
		else if(split.length == 1)
			if(split[0].matches("[1-" + AdvShelf.MAX_PAGES + "]"))
				if(plugin.activeShelf.get(player) != null)
    				AdvShelf.showPage(player, plugin.activeShelf.get(player), Integer.parseInt(split[0]));
    			else
    			{
    				plugin.activeCmd.put(player, split[0]);
    				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the Bookshelf you wish to view.");
    			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Page must be 1-" + AdvShelf.MAX_PAGES);
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect command usage. Use " + ChatColor.AQUA + "/shelf <page>");
		return true;
	}
}