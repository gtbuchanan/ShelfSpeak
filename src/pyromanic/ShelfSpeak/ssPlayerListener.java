package pyromanic.ShelfSpeak;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;

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
    	
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		else if(activeCmd == "write" && activeShelf != null 
				&& activeShelf.getBlock() != block && block.getType() == Material.BOOKSHELF)
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must end your write first.");
		else if((activeCmd != null && activeShelf == null) 
				&& block.getType() != Material.BOOKSHELF)
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] That isn't a Bookshelf.");
		else if(block.getType() != Material.BOOKSHELF)
			return;
		else if(activeCmd != null && activeCmd.startsWith("lock"))
		{
			String[] cmd = activeCmd.split(":");
			AdvShelf shelf = new AdvShelf(block.getLocation());
			if(shelf.exists())
				shelf.load();
			if(!shelf.hasOwner())
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Use '/shelfwrite' to take ownership first.");
				return;
			}
			else if(!player.getName().equalsIgnoreCase(shelf.getOwner()))
			{
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be the owner to use locks.");
				plugin.activeCmd.put(player, null);
				return;
			}
			if(cmd.length == 2)
			{
				// Set shelf read/write lock
				boolean locked = false;
				if(cmd[1].equals("read"))
					locked = shelf.setReadable(!shelf.canRead(""));
				else
					locked = shelf.setWritable(!shelf.canWrite(""));
				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
						(locked ? "Locked " : "Unlocked ") + cmd[1]);
			}
			else if(cmd.length == 3)
			{
				// Show read/write priv list of players
				if(cmd[2].equals("list"))
					shelf.showLocks(player, cmd[1]);
				else
				{
					boolean granted = false;
					if(cmd[1].equals("read"))
						granted = shelf.addReader(cmd[2]);
					else
						granted = shelf.addWriter(cmd[2]);
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] " + 
							(granted ? "Granted " : "Revoked ") + cmd[1] + 
							": " + ChatColor.GREEN + cmd[2]);
				}
			}
			shelf.save();
			plugin.activeCmd.put(player, null);
		}
		else
		{
			// Load shelf
			AdvShelf shelf = new AdvShelf(block.getLocation());
			if(shelf.exists())
				shelf.load();
			if(activeCmd == null || 
				(activeCmd != null && activeCmd.matches("[1-" + AdvShelf.MAX_PAGES + "]")) ||
				(activeShelf != null && activeShelf.isAt(shelf.getLocation())))
			{
				int page = 1;
				if(activeCmd != null && activeCmd.matches("[1-" + AdvShelf.MAX_PAGES + "]"))
					page = Integer.parseInt(activeCmd);
				if(activeShelf != null && activeShelf.isAt(shelf.getLocation()))
					AdvShelf.showPage(player, activeShelf, page);
				else
				{
					AdvShelf.showPage(player, shelf, page);
					plugin.activeCmd.put(player, null);
				}
			}
			else if(activeCmd == "write" && activeShelf == null)
			{
				if(!shelf.hasOwner() || shelf.canWrite(player.getName()))
				{
					if(!shelf.hasOwner())
					{
						shelf.setOwner(player.getName());
						player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] You took ownership of the Bookshelf!");
					}
					shelf.setModifier(player.getName());
					shelf.save();
					plugin.activeShelf.put(player, shelf);
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Write mode enabled!");
				}
				else if(!shelf.canWrite(player.getName()) && !ssPermissions.getInstance().writeAll(player))
				{
					plugin.activeCmd.put(player, null);
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] The owner has not granted you write permissions.");
				}
			}
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