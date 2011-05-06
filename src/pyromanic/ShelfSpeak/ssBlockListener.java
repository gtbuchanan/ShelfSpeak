package pyromanic.ShelfSpeak;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;


/**
 * ShelfSpeak block listener
 * @author pyromanic
 */
public class ssBlockListener extends BlockListener 
{
    public static ShelfSpeak plugin;

    public ssBlockListener(ShelfSpeak instance) 
    {	plugin = instance;	}

    public void onBlockPlace(BlockPlaceEvent event)
    {
    	Player player = event.getPlayer();
    	Block block = event.getBlockPlaced();
    	Block against = event.getBlockAgainst();
    	if(against.getType() == Material.BOOKSHELF)
    	{
    		event.setCancelled(true);
    		return;
    	}
    	if(block.getType() == Material.BOOKSHELF)
    	{
    		AdvShelf shelf = new AdvShelf(block.getLocation());
    		shelf.setOwner(player.getName());
    		shelf.setModifier(player.getName());
    		shelf.save();
    	}
    }
    
    public void onBlockBreak(BlockBreakEvent event)
    {
    	Player player = event.getPlayer();
    	Block block = event.getBlock();
    	
    	if(block.getType() == Material.BOOKSHELF)
    	{
    		AdvShelf shelf = new AdvShelf(block.getLocation());
    		if(shelf.delete())
    			player.sendMessage(ChatColor.RED + "Bookshelf destroyed.");
    	}
    }
}