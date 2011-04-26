package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.ShelfSpeak;

/**
 * Handler for the /shelfcancel command.
 * 
 * @author pyromanic
 */
public class CancelCommand implements CommandExecutor 
{
	private final ShelfSpeak plugin;

	public CancelCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		if(plugin.activeCmd.get(player) == "write")
			if(split.length == 0)
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Changes discarded.");
				ShelfSpeak.disableWriteMode(player);
			}
			else
				sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect command usage. Use " + ChatColor.AQUA + "/shelfcancel");
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be in write mode to use this command.");
		return true;
	}
}