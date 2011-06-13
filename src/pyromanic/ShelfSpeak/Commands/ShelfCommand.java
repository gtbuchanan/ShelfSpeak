package pyromanic.ShelfSpeak.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//import pyromanic.ShelfSpeak.AdvShelf;
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
			showCommands(player);
		else if(split.length == 1)
			if(split[0].matches("(?=[^A-Za-z]+$).*[0-9].*")) // Player provided a number
				if(plugin.activeShelf.get(player) != null)
					plugin.activeShelf.get(player).showPage(player, Integer.parseInt(split[0]));
    			else
    			{
    				plugin.activeCmd.put(player, split[0]);
    				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the Bookshelf you wish to view.");
    			}
		else
			sender.sendMessage(ChatColor.RED + "[ShelfSpeak] Incorrect usage. Use " + ChatColor.AQUA + "/shelf <page>");
		return true;
	}
	
	public static void showCommands(Player player)
    {
    	ssPermissions permission = ssPermissions.getInstance();
    	player.sendMessage(ChatColor.DARK_AQUA + "**********ShelfSpeak***********");
    	player.sendMessage(ChatColor.GREEN + "<Required> {Optional} [Choice]");
		player.sendMessage(ChatColor.AQUA + "/shelf" + ChatColor.WHITE + " - View Commands");
		if(permission.read(player) || permission.readAll(player))
			player.sendMessage(ChatColor.AQUA + "/shelf <page>" + ChatColor.WHITE + " - View Shelf Page");
		if(permission.colors(player))
			player.sendMessage(ChatColor.AQUA + "/shelfcolors" + ChatColor.WHITE + " - List Color Codes");
		if(permission.lockWrite(player) || permission.lockRead(player))
		{
			player.sendMessage(ChatColor.AQUA + "/shelflock [read:write] {player}" + ChatColor.WHITE + " - Toggle lock");
			player.sendMessage(ChatColor.AQUA + "/shelflock [read:write] list" + ChatColor.WHITE + " - List Privs");
		}
		player.sendMessage(ChatColor.AQUA + "/shelfcancel" + ChatColor.WHITE + " - Cancel Active Command");
		if(permission.write(player) || permission.writeAll(player))
		{
			player.sendMessage(ChatColor.AQUA + "/shelfwrite" + ChatColor.WHITE + " - Enter Write Mode");
			player.sendMessage(ChatColor.DARK_AQUA + "*******Write Mode*********");
			player.sendMessage(ChatColor.AQUA + "/shelfline {page:}<line> {text}" + ChatColor.WHITE + " - Set Line Text");
			player.sendMessage(ChatColor.AQUA + "/shelfclear {page}" + ChatColor.WHITE + " - Clear Shelf or Page Text");
			player.sendMessage(ChatColor.AQUA + "/shelfremove {page:}<line>" + ChatColor.WHITE + " - Remove a Line");
			if(permission.importFile(player))
				player.sendMessage(ChatColor.AQUA + "/shelfimport <file>" + ChatColor.WHITE + " - Import text file");
			player.sendMessage(ChatColor.AQUA + "/shelfsave" + ChatColor.WHITE + " - Save, End Write");
		}
		player.sendMessage(ChatColor.DARK_AQUA + "********************************");
    }
}