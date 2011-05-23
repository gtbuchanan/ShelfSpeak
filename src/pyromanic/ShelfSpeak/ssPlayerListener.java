package pyromanic.ShelfSpeak;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;

import pyromanic.ShelfSpeak.Commands.LockCommand;
import pyromanic.ShelfSpeak.Commands.WriteCommand;

/**
 * Handle all Player related events
 * @author pyromanic
 */
public class ssPlayerListener extends PlayerListener 
{
    public static ShelfSpeak plugin;

    public ssPlayerListener(ShelfSpeak instance) 
    {	plugin = instance;	}

    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	Player player = event.getPlayer();
    	Block block = event.getClickedBlock();
    	String activeCmd = plugin.activeCmd.get(player);
    	AdvShelf activeShelf = plugin.activeShelf.get(player);
    	
    	/*
    	 * Handle Interactive Mode
    	 */
    	// End if isn't Right_Click
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		// Inform if writing and block isn't Bookshelf
		else if((activeCmd != null && activeShelf == null) 	
				&& block.getType() != Material.BOOKSHELF)
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] That isn't a Bookshelf.");
		// End if not writing and block isn't Bookshelf
		else if(block.getType() != Material.BOOKSHELF)		
			return;
		// Inform if user clicks another Bookshelf
		else if(activeCmd != null && activeCmd.equals("write") && activeShelf != null 
				&& activeShelf.getBlock() != block)			
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must end your write first.");
		// Write command
		else if(activeCmd != null && activeCmd.equals("write") && activeShelf == null)	
		{
			AdvShelf shelf = new AdvShelf(block.getLocation());
			if(shelf.exists())
				shelf.load();
			WriteCommand.startWriteMode(player, shelf);
		}
		// Lock command
		else if(activeCmd != null && activeCmd.startsWith("lock"))
		{
			String[] cmd = activeCmd.split(":");
			AdvShelf shelf = new AdvShelf(block.getLocation());
			if(shelf.exists())
				shelf.load();
			if(!shelf.hasOwner())
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Use '/shelfwrite' to take ownership first.");
			else
			{
				ArrayList<String> args = new ArrayList<String>();
				for(int x=1; x<cmd.length; x++)
					args.add(cmd[x]);
				LockCommand.performLock(player, shelf, args.toArray(new String[args.size()]));
				shelf.save();
				plugin.activeCmd.put(player, null);
			}
		}
		else	// Display Shelf text
		{
			AdvShelf shelf = new AdvShelf(block.getLocation());
			if(shelf.exists())
				shelf.load();
			if(activeCmd == null 
					|| (activeCmd != null && activeCmd.matches("(?=[^A-Za-z]+$).*[0-9].*")) 
					|| (activeShelf != null && activeShelf.isAt(shelf.getLocation())))
			{
				int page = 1;
				if(activeCmd != null && activeCmd.matches("(?=[^A-Za-z]+$).*[0-9].*"))
					page = Integer.parseInt(activeCmd);
				if(activeShelf != null && activeShelf.isAt(shelf.getLocation()))
					activeShelf.showPage(player, page);
				else
				{
					shelf.showPage(player, page);
					plugin.activeCmd.put(player, null);
				}
			}
		}
    }
    
    public void onPlayerMove(PlayerMoveEvent event)
    {
    	Player player = event.getPlayer();
    	Location loc = event.getTo();
    	AdvShelf shelf = plugin.activeShelf.get(player);
    	int radius = ssPermissions.getInstance().maxRadius(player);
    	
    	if(shelf != null)
    		if(Math.abs(shelf.getX() - loc.getX()) >= radius 
    				|| Math.abs(shelf.getY() - loc.getY()) >= radius
    				|| Math.abs(shelf.getZ() - loc.getZ()) >= radius)
    		{
    			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Max radius exceeded.");
    			player.performCommand("shelfcancel");
    		}
    }
    
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player player = event.getPlayer();
    	plugin.activeCmd.put(player, null);
    	plugin.activeShelf.put(player, null);
    }
    
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	Player player = event.getPlayer();
    	plugin.activeCmd.remove(player);
    	plugin.activeShelf.remove(player);
    }
}