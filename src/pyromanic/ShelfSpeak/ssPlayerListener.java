package pyromanic.ShelfSpeak;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.isEnabled())
		{
			if(block.getType() == Material.BOOKSHELF)
			{
				event.setCancelled(true);
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
						showPage(player, activeShelf, page);
					else
					{
						showPage(player, shelf, page);
						plugin.activeCmd.put(player, null);
					}
				}
				else if(plugin.activeCmd.get(player) == "edit" && plugin.activeShelf.get(player) == null)
				{
					if(!shelf.hasOwner() || player.getName().equalsIgnoreCase(shelf.getOwner()))
					{
						if(!shelf.hasOwner())
						{
							shelf.setOwner(player.getName());
							player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] You took ownership of the Bookshelf!");
						}
						shelf.setModifier(player.getName());
						shelf.save();
						plugin.activeShelf.put(player, shelf);
						player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Interactive mode enabled.");
					}
					else
					{
						plugin.activeCmd.put(player, null);
						player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must be the owner of the Bookshelf to edit.");
					}
				}
				else if(plugin.activeCmd.get(player) == "edit" && plugin.activeShelf.get(player) != null 
						&& plugin.activeShelf.get(player).getBlock() != block)	// If command and shelf active
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must end your edit first.");
			}
			else if(plugin.activeCmd.get(player) != null && plugin.activeShelf.get(player) == null)	// If material not Bookshelf
			{
				// Inform player of incorrect block click
				player.sendMessage(ChatColor.RED + "[ShelfSpeak] That isn't a Bookshelf.");
			}
		}
    }
    
    @SuppressWarnings("rawtypes")
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
    	Player player = event.getPlayer();
    	String[] cmdParts = event.getMessage().split(" ");
    	
    	// Regular shelf commands
    	if(cmdParts[0].equalsIgnoreCase("/shelf"))
    	{
    		event.setCancelled(true);
    		if(cmdParts.length == 1)
    			showCommands(player);
    		else if(cmdParts[1].equalsIgnoreCase("edit") && plugin.activeCmd.get(player) == null)
    		{
    			player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the Bookshelf you wish to edit.");
    			plugin.activeCmd.put(player, "edit");
    		}
    		else if(cmdParts[1].equalsIgnoreCase("edit") && plugin.activeCmd.get(player) != null)
    			player.sendMessage(ChatColor.RED + "[ShelfSpeak] You must stop editing first!");
    		else if(cmdParts[1].equalsIgnoreCase("colors"))
    			showColors(player);
    		else if(cmdParts[1].matches("[1-" + AdvShelf.MAX_PAGES + "]"))
    		{
    			if(plugin.activeShelf.get(player) != null)
    				showPage(player, plugin.activeShelf.get(player), Integer.parseInt(cmdParts[1]));
    			else
    			{
    				plugin.activeCmd.put(player, cmdParts[1]);
    				player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Right click the Bookshelf you wish to view.");
    			}
    		}
    		else if(!cmdParts[1].matches("[1-" + AdvShelf.MAX_PAGES + "]"))
    			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page must be 1-" + AdvShelf.MAX_PAGES);
    		else
    			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Command was incorrect, inexistant, or you do not have permissions.");
    	}
    	
    	/*
    	 *  Interactive Edit Commands
    	 */
    	if(plugin.activeCmd.get(player) == "edit") {
    		AdvShelf shelf = plugin.activeShelf.get(player);
    		
    		/*
    		 * Line Command
    		 */
    		if(cmdParts[0].equalsIgnoreCase("/line") && cmdParts.length >= 2)
    		{
    			event.setCancelled(true);    			
    			int[] temp = parseLineArgs(cmdParts[1]);
    			int page = temp[0];
    			int line = temp[1];
    			
    			if(!checkRanges(player, page, line))
    				return;
    				
				// Rebuild user specified text
				String output = "";
				for(int x = 2; x < cmdParts.length; x++)
					output += cmdParts[x] + ((x < cmdParts.length-1) ? " " : "");
				output = output.replaceAll("\n", "");	// Strip new lines
				output = output.replace('#', '§');	// Transform colors
				// Check length without color
				if(ChatColor.stripColor(output).length() <= AdvShelf.MAX_CHARS)
				{
					HashMap<Integer, HashMap<Integer, String>> pages = shelf.getPages();
					// Add new page HashMap if output is not blank
					if(!output.isEmpty() && !pages.containsKey(page))
						pages.put(page, new HashMap<Integer, String>());
					HashMap<Integer, String> lines = pages.get(page);
					// Put line if page exists
					if(lines != null)
					{
						lines.put(line, output);
						// Delete line if output was empty
						if(output.isEmpty())
						{
							lines.remove(line);
							// Delete page if empty
							if(pages.containsKey(page) && pages.get(page).isEmpty())
								pages.remove(page);
						}
					}
					player.sendMessage(String.format("%02d:%02d", page, line) + ": " + output);
				}
				else
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] Char max exceeded (" + AdvShelf.MAX_CHARS + " excluding colors)");
    		}
    		
    		/*
    		 *  Clear Command
    		 */
    		if(cmdParts[0].equalsIgnoreCase("/clear"))
    		{
    			// Clear all if line not specified
    			if(cmdParts.length == 1)
    			{
    				event.setCancelled(true);
    				shelf.getPages().clear();
    				player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Bookshelf cleared.");
    			}
    			else if(cmdParts.length == 2)
    			{
    				event.setCancelled(true);
    				// Parse page number
    				int page = 0;
        			try
        			{	page = Integer.parseInt(cmdParts[1]); }
        			catch(NumberFormatException e)
        			{}
        			if(page >= 1 && page <= AdvShelf.MAX_PAGES)
        			{
        				HashMap<Integer, HashMap<Integer, String>> pages = shelf.getPages();
        				if(pages.containsKey(page))
        				{
        					pages.remove(page);
        					player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Page " + page + " cleared.");
        				}
        				else
        					player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page " + page + " does not exist yet.");
        			}
        			else
        				player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page must be 1-" + AdvShelf.MAX_PAGES);
    			}
    		}
    		
    		/*
    		 *  Remove Command
    		 */
    		if(cmdParts[0].equalsIgnoreCase("/remove"))
    		{
				event.setCancelled(true);
				int[] temp = parseLineArgs(cmdParts[1]);
				int page = temp[0];
				int line = temp[1];
				if(!checkRanges(player, page, line))
					return;

				HashMap<Integer, String> lines = shelf.getPages().get(page);
				if(lines != null && shelf.getMaxLine(page) > line - 1)
				{
					lines.remove(line);
					HashMap<Integer, String> newLines = new HashMap<Integer, String>();
					Set set = lines.entrySet();
					Iterator i = set.iterator();
					// Fill new page with correct lines
					while(i.hasNext()) {
						Map.Entry me = (Map.Entry)i.next();
						int line2 = (Integer)me.getKey();
						if(line2 > line)
							line2--;
						newLines.put(line2, me.getValue().toString());	
					}
					shelf.getPages().put(page, newLines);	// Overwrite existing page
					player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Page " + page + " line " + line + " removed.");
				}
				else
					player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page " + page + " line " + line + " does not exist yet.");
    		}
    		
    		/*
    		 *  Cancel/End Command
    		 */
    		if(cmdParts[0].equalsIgnoreCase("/cancel") || cmdParts[0].equalsIgnoreCase("/end"))
    		{
        		event.setCancelled(true);
        		if(cmdParts[0].equalsIgnoreCase("/cancel"))
        			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Changes discarded.");
        		else if(shelf != null)
        		{
					shelf.save();
					player.sendMessage(ChatColor.AQUA + "[ShelfSpeak] Shelf Saved!");
        		}
        		player.sendMessage(ChatColor.DARK_AQUA + "[ShelfSpeak] Interactive mode disabled.");
        		plugin.activeCmd.put(player, null);
        		plugin.activeShelf.put(player, null);
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

    public void showCommands(Player player)
    {
    	player.sendMessage(ChatColor.DARK_AQUA + "**********ShelfSpeak***********");
    	player.sendMessage(ChatColor.GREEN + "<Required> {Optional} [Choice]");
		player.sendMessage(ChatColor.AQUA + "/shelf" + ChatColor.WHITE + " - View Commands");
		player.sendMessage(ChatColor.AQUA + "/shelf <page>" + ChatColor.WHITE + " - View shelf page");
		player.sendMessage(ChatColor.AQUA + "/shelf edit" + ChatColor.WHITE + " - Start Interactive Edit");
		player.sendMessage(ChatColor.AQUA + "/shelf colors" + ChatColor.WHITE + " - List Color Codes");
		player.sendMessage(ChatColor.DARK_AQUA + "*******Interactive Edit*********");
		player.sendMessage(ChatColor.AQUA + "/line {<page>:}<line> {text}" + ChatColor.WHITE + " - Set line text");
		player.sendMessage(ChatColor.AQUA + "/clear {<page>}" + ChatColor.WHITE + " - Clear shelf or page text");
		player.sendMessage(ChatColor.AQUA + "/remove {<page>:}<line>" + ChatColor.WHITE + " - Remove a line");
		player.sendMessage(ChatColor.AQUA + "/cancel" + ChatColor.WHITE + " - Cancel, end session");
		player.sendMessage(ChatColor.AQUA + "/end" + ChatColor.WHITE + " - Save, end session");
		player.sendMessage(ChatColor.DARK_AQUA + "********************************");
    }

    public void showColors(Player player)
    {
    	player.sendMessage(ChatColor.DARK_AQUA + "********Colors********");
		player.sendMessage(ChatColor.AQUA + "(Format: #<code>)");
		for (ChatColor color : ChatColor.values())
			player.sendMessage(color + color.name() + ": " + Integer.toHexString(color.getCode()));
		player.sendMessage(ChatColor.DARK_AQUA + "**********************");
    }

    public void showPage(Player player, AdvShelf shelf, int page)
    {
    	HashMap<Integer, HashMap<Integer, String>> pages = shelf.getPages();
    	if(!shelf.hasOwner())
    		player.sendMessage(ChatColor.DARK_AQUA + "The Bookshelf seems to be empty...");
    	else if(!shelf.hasPages() && plugin.activeCmd.get(player) == null)
    		player.sendMessage(ChatColor.DARK_AQUA + "The Bookshelf is filled with " + 
    				ChatColor.GREEN + shelf.getOwner() + ChatColor.DARK_AQUA + "'s books!");
    	else if(page > shelf.getMaxPage())
    		player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page " + page + " does not exist yet.");
    	else
    	{
	    	player.sendMessage(ChatColor.DARK_AQUA + 
	    			String.format("********" + ChatColor.GREEN + "%1$s" + ChatColor.DARK_AQUA + 
	    					"'s BookShelf - Page: " + ChatColor.GREEN + 
	    					"%2$s of %3$s" + ChatColor.DARK_AQUA + "********", 
	    					shelf.getOwner(), page, shelf.getMaxPage()));
	    	
	    	if(pages.containsKey(page) && pages.get(page).size() > 0)
	    	{
	    		HashMap<Integer, String> lines = shelf.getPages().get(page);
	    		for(int x = 1; x <= shelf.getMaxLine(page); x++)
	    		{
	    			String line = "";
	    			if(lines.containsKey(x))
	    				line = lines.get(x);
	    			player.sendMessage(String.format("%02d", (x)) + ": " + line);
	    		}
	    	}
	    	else
	    		player.sendMessage("Page " + page + " contains no text.");
			player.sendMessage(ChatColor.DARK_AQUA + "**********Last Modified By: " + ChatColor.GREEN +
					(shelf.hasModifier() ? shelf.getModifier() : "N/A") + ChatColor.DARK_AQUA +  "**********");
    	}
    		
    }

    public int[] parseLineArgs(String cmdArg)
    {
    	String[] cmdArgs = cmdArg.split(":");
    	int page = 0;
    	int line = 0;
    	// Page:Line parameter parsing
		if(cmdArgs.length == 1) {
			try	{
				line = Integer.parseInt(cmdArgs[0]);
				page = (line + 9) / 10;
				line = (line % 10 == 0) ? 10 : line % 10;
			}
			catch(NumberFormatException e) {}
		}
		else if(cmdArgs.length == 2) {
			try {
				page = Integer.parseInt(cmdArgs[0]);
				line = Integer.parseInt(cmdArgs[1]);
			}
			catch(NumberFormatException e) {}
		}
		int[] output = {page, line};
		return output;
    }

    public boolean checkRanges(Player player, int page, int line)
    {
    	boolean ok = false;
    	// Check page/line ranges
		if(page < 1 || page > AdvShelf.MAX_PAGES)
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Page must be 1-" + AdvShelf.MAX_PAGES);
		else if(line < 1 || line > AdvShelf.MAX_LINES)
			player.sendMessage(ChatColor.RED + "[ShelfSpeak] Line must be 1-" + AdvShelf.MAX_LINES);
		else
			ok = true;
		return ok;
    }
}