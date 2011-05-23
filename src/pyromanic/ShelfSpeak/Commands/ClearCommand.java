package pyromanic.ShelfSpeak.Commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.AdvShelf;
import pyromanic.ShelfSpeak.ShelfSpeak;

/**
 * Handler for the /shelfclear command.
 * 
 * @author pyromanic
 */
public class ClearCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public ClearCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if(plugin.activeCmd.get(player) == "write")
		{
			AdvShelf shelf = plugin.activeShelf.get(player);
			// Clear all if line not specified
			if(split.length == 0)
			{
				shelf.getPages().clear();
				player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Bookshelf cleared.");
			}
			else if(split.length == 1)
			{
				// Parse page number
				int page = ShelfSpeak.parseLineArgs(player, split[0] + ":0")[0];
    			if(ShelfSpeak.checkRanges(player, page, 1))
    				clearPage(player, shelf, page);
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect command usage. Use " + ChatColor.AQUA + "/shelfclear {<page>}");
		}
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
	
	private void clearPage(Player player, AdvShelf shelf, int page)
	{
		HashMap<Integer, HashMap<Integer, String>> pages = shelf.getPages();
		if(pages.containsKey(page))
		{
			pages.remove(page);
			player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Page " + page + " cleared.");
		}
		else
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page " + page + " does not exist yet.");
	}
}
