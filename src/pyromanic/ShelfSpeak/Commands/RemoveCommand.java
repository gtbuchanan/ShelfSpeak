package pyromanic.ShelfSpeak.Commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.AdvShelf;
import pyromanic.ShelfSpeak.ShelfSpeak;

/**
 * Handler for the /shelfremove command.
 * 
 * @author pyromanic
 */
public class RemoveCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public RemoveCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if(plugin.activeCmd.get(player) == "write")
		{
			AdvShelf shelf = plugin.activeShelf.get(player);
			if(split.length == 1)
			{
				int[] temp = ShelfSpeak.parseLineArgs(player, split[0]);
				int page = temp[0];
				int line = temp[1];
				if(ShelfSpeak.checkRanges(player, page, line))
				{
					HashMap<Integer, String> lines = shelf.getPages().get(page);
					if(lines != null && shelf.getMaxLine(page) > line - 1)
					{
						lines.remove(line);
						HashMap<Integer, String> newLines = new HashMap<Integer, String>();
						Set set = lines.entrySet();
						Iterator i = set.iterator();
						// Fill new page with correct lines
						while(i.hasNext()) {
							Map.Entry me = (Map.Entry)i.next();
							int line2 = (Integer)me.getKey();
							if(line2 > line)
								line2--;
							newLines.put(line2, me.getValue().toString());	
						}
						shelf.getPages().put(page, newLines);	// Overwrite existing page
						player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Page " + page + " line " + line + " removed.");
					}
				}
				else
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page " + page + " line " + line + " does not exist yet.");
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use " + ChatColor.AQUA + "/shelfremove {<page>:}<line>");
		}
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
}
