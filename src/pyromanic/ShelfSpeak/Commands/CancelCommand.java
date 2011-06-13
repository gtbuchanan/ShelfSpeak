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
		
		if(split.length == 0)
		{
			if(plugin.activeCmd.get(player) == "write")
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Changes discarded.");
				WriteCommand.disableWriteMode(player);
			}
			else if(plugin.activeCmd.get(player) != null)
			{
				plugin.activeCmd.put(player, null);
				plugin.activeShelf.put(player, null);
				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Active Command Cancelled.");
			}
		}
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use " + ChatColor.AQUA + "/shelfcancel");
		return true;
	}
}