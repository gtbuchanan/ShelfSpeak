package pyromanic.ShelfSpeak.Commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.AdvShelf;
import pyromanic.ShelfSpeak.ShelfSpeak;
import pyromanic.ShelfSpeak.ssPermissions;

/**
 * Handler for the /shelfimport command.
 * 
 * @author pyromanic
 */
public class ImportCommand implements CommandExecutor
{
	private final ShelfSpeak plugin;
	
	public ImportCommand(ShelfSpeak plugin)
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, 
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if (!ssPermissions.getInstance().importFile(player))
		{
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to import.");
			return true;
		}
		if (plugin.activeCmd.get(player) == "write")
			if (split.length == 1 || 
					((split.length == 2) && 
							(split[0].equalsIgnoreCase("normal") ||
							 split[0].equalsIgnoreCase("strict"))))
			{
				String method = "normal";
				String fileName = "";
				if (split.length == 1)
					fileName = split[0];
				else
				{
					method = split[0].toLowerCase();
					fileName = split[1];
				}
				String path = ShelfSpeak.mainDir + fileName;
				File f = new File(path);
				if (f.exists())
					try
					{
						AdvShelf shelf = plugin.activeShelf.get(player);
						if (method.equals("normal"))
							shelf.importNormal(player, path);
						else
							shelf.importStrict(player, path);
						player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] File imported.");
					}
					catch(IOException e)
					{	player.sendMessage(ChatColor.RED + "[ShelfSpeak] Error reading file.");	}
				else
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] File not found. Check ShelfSpeak directory.");
			}
			else
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage.");
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Use /shelfimport {[normal:strict]} <file>");
			}
		else
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
	
}
