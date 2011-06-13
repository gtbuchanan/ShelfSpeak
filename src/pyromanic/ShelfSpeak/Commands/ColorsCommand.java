package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pyromanic.ShelfSpeak.ShelfSpeak;
import pyromanic.ShelfSpeak.ssPermissions;

/**
 * Handler for the /shelfcolors command.
 * 
 * @author pyromanic
 */
public class ColorsCommand implements CommandExecutor 
{
	@SuppressWarnings("unused")
	private final ShelfSpeak plugin;

	public ColorsCommand(ShelfSpeak plugin) 
	{	this.plugin = plugin;	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) 
	{
		Player player = (Player)sender;
		
		if(!ssPermissions.getInstance().colors(player))
		{
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You do not have permission to view colors.");
			return true;
		}
		
		if(split.length == 0)
			showColors(player);
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use " + ChatColor.AQUA + "/shelfcolors");
		return true;
	}
	
	private void showColors(Player player)
    {
    	player.sendMessage(ChatColor.DARK_AQUA + "********Colors********");
		player.sendMessage(ChatColor.AQUA + "(Format: #<code>)");
		for (ChatColor color : ChatColor.values())
			player.sendMessage(color + color.name() + ": " + Integer.toHexString(color.getCode()));
		player.sendMessage(ChatColor.DARK_AQUA + "**********************");
    }
}