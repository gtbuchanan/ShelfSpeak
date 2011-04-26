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
 * Handler for the /shelfline command.
 * 
 * @author pyromanic
 */
public class LineCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public LineCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if(plugin.activeCmd.get(player) == "write")
			if(split.length >= 1)
			{
				int[] temp = ShelfSpeak.parseLineArgs(split[0]);
    			int page = temp[0];
    			int line = temp[1];
    			
    			if(ShelfSpeak.checkRanges(player, page, line))
    			{
					// Rebuild user specified text
					String output = "";
					for(int x = 1; x < split.length; x++)
						output += split[x] + ((x < split.length-1) ? " " : "");
					output = output.replaceAll("\n", "");	// Strip new lines
					output = output.replace('#', '§');	// Transform colors
					// Check length without color
					if(ChatColor.stripColor(output).length() <= AdvShelf.MAX_CHARS)
						setLine(player, page, line, output);
					else
						sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Char max exceeded (" + AdvShelf.MAX_CHARS + " excluding colors)");
    			}
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect command usage. Use " + ChatColor.AQUA + "/shelfline {<page>:}<line> {text}");
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
	
	private void setLine(Player player, int page, int line, String output)
	{
		AdvShelf shelf = plugin.activeShelf.get(player);
		HashMap<Integer, HashMap<Integer, String>> pages = shelf.getPages();
		// Add new page HashMap if output is not blank
		if(!output.isEmpty() && !pages.containsKey(page))
			pages.put(page, new HashMap<Integer, String>());
		HashMap<Integer, String> lines = pages.get(page);
		// Put line if page exists
		if(lines != null)
		{
			lines.put(line, output);
			// Delete line if output was empty
			if(output.isEmpty())
			{
				lines.remove(line);
				// Delete page if empty
				if(pages.containsKey(page) && pages.get(page).isEmpty())
					pages.remove(page);
			}
		}
		player.sendMessage(String.format("%02d:%02d", page, line) + ": " + output);
	}
}