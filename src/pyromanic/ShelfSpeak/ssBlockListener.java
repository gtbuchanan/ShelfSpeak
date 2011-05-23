package pyromanic.ShelfSpeak;

import org.bukkit.inventory.ItemStack;

import org.bukkit.block.Block;
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
    	if (event.isCancelled())
    		return;
    	Player player = event.getPlayer();
    	Block block = event.getBlockPlaced();
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
    	if (event.isCancelled())
    		return;
    	Block block = event.getBlock();
    	if(block.getType() == Material.BOOKSHELF)
    	{
    		AdvShelf shelf = new AdvShelf(block.getLocation());
    		shelf.delete();
    		ItemStack stack = new ItemStack(block.getType(), 1);
    		block.getWorld().dropItemNaturally(block.getLocation(), stack);
    	}
    }
}