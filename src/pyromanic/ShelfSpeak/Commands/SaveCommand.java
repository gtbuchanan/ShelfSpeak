package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.AdvShelf;
import pyromanic.ShelfSpeak.ShelfSpeak;

/**
 * Handler for the /shelfend command.
 * 
 * @author pyromanic
 */
public class SaveCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public SaveCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if(plugin.activeCmd.get(player) == "write")
		{
			AdvShelf shelf = plugin.activeShelf.get(player);
			if(split.length == 0)
			{
				shelf.save();
				player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Shelf Saved!");
				WriteCommand.disableWriteMode(player);
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use " + ChatColor.AQUA + "/shelfsave");
		}
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
}