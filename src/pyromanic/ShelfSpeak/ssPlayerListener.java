package pyromanic.ShelfSpeak;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerChatEvent;
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
    {
        plugin = instance;
    }

    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	Player player = event.getPlayer();
    	Block block = event.getClickedBlock();
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.isEnabled())
		{
			if(block.getType() == Material.BOOKSHELF)
			{
				// Load shelf
				AdvShelf shelf = new AdvShelf(block);
				if(shelf.exists())
					try
					{	shelf.load();	} 
					catch (FileNotFoundException e) 
					{	e.printStackTrace();	}
				if(plugin.activeCmd.get(player) == null || (plugin.activeShelf.get(player) != null && 
						plugin.activeShelf.get(player).getBlock() == block))	// If no commands active or if current active block
				{
					if((plugin.activeShelf.get(player) != null && 
						plugin.activeShelf.get(player).getBlock() == block))
						shelf = plugin.activeShelf.get(player);
					if(shelf.hasOwner())
						if(!shelf.hasLines())
						{
							// Display default message if no message exists
							player.sendMessage(ChatColor.DARK_AQUA + "The Bookshelf is filled with " + shelf.getOwner() + "'s books!");
						}
						else
						{
							player.sendMessage("***************ShelfSpeak***************");
							player.sendMessage(ChatColor.DARK_AQUA + "Owner: " + ChatColor.AQUA + shelf.getOwner() + 
									ChatColor.DARK_AQUA + "; Last Modified By: " + ChatColor.AQUA + (shelf.hasModifier() ? shelf.getModifier() : "N/A"));
							for(String text : shelf.getLines())
								player.sendMessage(text);
							player.sendMessage("*****************************************");
						}
					else
					{
						// Display default message if no owner exists
						player.sendMessage(ChatColor.DARK_AQUA + "The Bookshelf seems to be empty...");
					}
				}
				else if(plugin.activeCmd.get(player) == "edit" && plugin.activeShelf.get(player) == null)	// If command active
				{
					if(!shelf.hasOwner() || player.getName().equalsIgnoreCase(shelf.getOwner()))
					{
						if(!shelf.hasOwner())
						{
							shelf.setOwner(player.getName());
							player.sendMessage(ChatColor.DARK_AQUA + "You took ownership of the Bookshelf.");
						}
						shelf.setModifier(player.getName());
						try
						{	shelf.save();	}
						catch (IOException e)
						{	e.printStackTrace();	}
						plugin.activeShelf.put(player, shelf);
						player.sendMessage(ChatColor.DARK_AQUA + "Interactive mode enabled. Type /cancel or /end to finish.");
					}
					else
					{
						plugin.activeCmd.put(player, null);
						player.sendMessage(ChatColor.RED + "You must be the owner of the Bookshelf to edit.");
					}
				}
				else if(plugin.activeCmd.get(player) == "edit" && plugin.activeShelf.get(player) != null 
						&& plugin.activeShelf.get(player).getBlock() != block)	// If command and shelf active
					player.sendMessage(ChatColor.RED + "You must end your current session before starting another.");
			}
			else if(plugin.activeCmd.get(player) != null && plugin.activeShelf.get(player) == null)	// If material not Bookshelf
			{
				// Inform player of incorrect block click
				player.sendMessage(ChatColor.RED + "That isn't a Bookshelf.");
			}
		}
    }
    
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player player = event.getPlayer();
    	plugin.activeCmd.put(player, null);
    }
    
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	Player player = event.getPlayer();
    	plugin.activeCmd.remove(player);
    }
    
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
    	Player player = event.getPlayer();
    	String[] cmdParts = event.getMessage().split(" ");
    	
    	if(cmdParts[0].equalsIgnoreCase("/shelf"))
    	{
    		if(cmdParts.length == 1)
    		{
    			event.setCancelled(true);
    			player.sendMessage(ChatColor.DARK_AQUA + "**********ShelfSpeak***********");
    			player.sendMessage(ChatColor.AQUA + "/shelf" + ChatColor.WHITE + " - View Commands");
    			player.sendMessage(ChatColor.AQUA + "/shelf edit" + ChatColor.WHITE + " - Start Interactive Edit Mode");
    			//player.sendMessage(ChatColor.AQUA + "/shelf setowner" + ChatColor.WHITE + " - Set Bookshelf Owner");
    			player.sendMessage(ChatColor.AQUA + "/shelf colors" + ChatColor.WHITE + " - List Color Codes");
    			player.sendMessage(ChatColor.DARK_AQUA + "*******Interactive Edit*********");
    			player.sendMessage(ChatColor.AQUA + "/line <line> {text}" + ChatColor.WHITE + " - Set line text");
    			player.sendMessage(ChatColor.AQUA + "/clear {line}" + ChatColor.WHITE + " - Clear line text");
    			player.sendMessage(ChatColor.AQUA + "/remove {line}" + ChatColor.WHITE + " - Remove line");
    			player.sendMessage(ChatColor.AQUA + "/cancel" + ChatColor.WHITE + " - Cancel, end session");
    			player.sendMessage(ChatColor.AQUA + "/end" + ChatColor.WHITE + " - Save, end session");
    			player.sendMessage(ChatColor.DARK_AQUA + "********************************");
    		}
    		else if(cmdParts.length == 2 && cmdParts[1].equalsIgnoreCase("edit") && plugin.activeCmd.get(player) == null)
    		{
    			event.setCancelled(true);
    			player.sendMessage(ChatColor.DARK_AQUA + "Right click the Bookshelf you wish to edit.");
    			plugin.activeCmd.put(player, "edit");
    		}
    		else if(cmdParts.length == 3 && cmdParts[1].equalsIgnoreCase("setowner") && plugin.activeCmd.get(player) == null)
    		{
    			event.setCancelled(true);
    		}
    		else if(cmdParts.length == 2 && cmdParts[1].equalsIgnoreCase("colors"))
    		{
    			event.setCancelled(true);
    			player.sendMessage(ChatColor.DARK_AQUA + "********Colors********");
    			player.sendMessage(ChatColor.AQUA + "(Format: &<code>)");
    			for (ChatColor color : ChatColor.values())
    				player.sendMessage(color + color.name() + ": " + Integer.toHexString(color.getCode()));
    		}
    		else if(plugin.activeCmd.get(player) != null)
    		{
    			event.setCancelled(true);
    			player.sendMessage(ChatColor.RED + "You must end the session to use other commands.");
    		}
    	}
    	
    	// Interactive mode commands
    	if(plugin.activeCmd.get(player) == "edit")
    	{
    		AdvShelf shelf = plugin.activeShelf.get(player);
    		if(cmdParts[0].equalsIgnoreCase("/line"))
    		{
    			event.setCancelled(true);
    			// Parse line number
    			int line = 0;
    			try
    			{	line = Integer.parseInt(cmdParts[1]);	}
    			catch(NumberFormatException e)
    			{}
    			// Check if line in range
    			if(line >= 1 && line <= AdvShelf.MAX_LINES)
    			{
    				int count = shelf.getLines().size();
    				// Fill preceding lines
    				if(count < line - 1)
    					for(int x = 0; x < line - 1; x++)
    						shelf.getLines().add("");
    				if(cmdParts.length > 2)
    				{
    					// Rebuild user message
    					String output = "";
    					for(int x = 2; x < cmdParts.length; x++)
    						output += cmdParts[x] + " ";
    					if(count > line - 1)
    						shelf.getLines().set(line - 1, output);
    					else
    						shelf.getLines().add(output);
    					player.sendMessage("line" + line + ": " + output);
    				}
    				else
    					shelf.getLines().add("");
    			}
    			else
    				player.sendMessage(ChatColor.RED + "Line must be between 1 and " + AdvShelf.MAX_LINES + ".");
    		}
    		else if(cmdParts[0].equalsIgnoreCase("/clear"))	// Clear command
    		{
    			// Clear all if line not specified
    			if(cmdParts.length == 1)
    			{
    				event.setCancelled(true);
    				shelf.getLines().clear();
    				player.sendMessage(ChatColor.DARK_AQUA + "Bookshelf cleared.");
    			}
    			else if(cmdParts.length == 2)
    			{
    				event.setCancelled(true);
    				// Parse line number
    				int line = 0;
        			try
        			{	line = Integer.parseInt(cmdParts[1]); }
        			catch(NumberFormatException e)
        			{}
        			if(line >= 1 && line <= AdvShelf.MAX_LINES)
        			{
        				if(shelf.getLines().size() > line - 1)
        				{
        					shelf.getLines().set(line - 1, "");
        					player.sendMessage(ChatColor.DARK_AQUA + "Bookshelf line " + line + " cleared.");
        				}
        				else
        					player.sendMessage(ChatColor.RED + "Line " + line + " does not exist yet.");
        			}
        			else
        				player.sendMessage(ChatColor.RED + "Line must be between 1 and " + AdvShelf.MAX_LINES + ".");
    			}
    		}
    		else if(cmdParts.length == 2 && cmdParts[0].equalsIgnoreCase("/remove"))
    		{
				event.setCancelled(true);
				int line = 0;
    			try
    			{	line = Integer.parseInt(cmdParts[1]); }
    			catch(NumberFormatException e)
    			{}
    			if(line >= 1 && line <= AdvShelf.MAX_LINES)
    			{
    				if(shelf.getLines().size() > line - 1)
    				{
    					shelf.getLines().remove(line - 1);
    					player.sendMessage(ChatColor.DARK_AQUA + "Bookshelf line " + line + " removed.");
    				}
    				else
    					player.sendMessage(ChatColor.RED + "Line " + line + " does not exist yet.");
    			}
    			else
    				player.sendMessage(ChatColor.RED + "Line must be between 1 and " + AdvShelf.MAX_LINES + ".");
    		}
    		else if(cmdParts.length == 1 && (cmdParts[0].equalsIgnoreCase("/cancel") || cmdParts[0].equalsIgnoreCase("/end")))
    		{
        		event.setCancelled(true);
        		if(cmdParts[0].equalsIgnoreCase("/cancel"))
        			player.sendMessage(ChatColor.RED + "Edit Cancelled.");
        		else if(shelf != null)
        		{
        			try 
        			{
						shelf.save();
						player.sendMessage(ChatColor.DARK_AQUA + "Edit Saved.");
					} 
        			catch (IOException e) 
        			{	e.printStackTrace();	}
        		}
        		player.sendMessage(ChatColor.DARK_AQUA + "Interactive mode disabled.");
        		plugin.activeCmd.put(player, null);
        		plugin.activeShelf.put(player, null);
    		}
    	}
    }
    
    public void onPlayerChat(PlayerChatEvent event)
    {
    	/*
    	Player player = event.getPlayer();
    	String msg = event.getMessage();
    	if(plugin.activeCmd.get(player) == "edit")
    	{
    		event.setCancelled(true);
    		player.sendMessage(msg);
    	}
    	*/
    }
}