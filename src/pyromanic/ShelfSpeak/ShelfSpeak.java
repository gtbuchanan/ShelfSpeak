package pyromanic.ShelfSpeak;
/*
 * Developer:
 * 		pyromanic
 * Libraries:
 * 		Bukkit [669]
 * 		CraftBukkit [740]
 * 		Permissions [2.7]
 */

import pyromanic.ShelfSpeak.Commands.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;

/**
 * ShelfSpeak for Bukkit
 *
 * @author pyromanic
 */
public class ShelfSpeak extends JavaPlugin 
{
	public static ShelfSpeak session;
	public static final String mainDir = "plugins/ShelfSpeak/";
	public static final String shelfDir = mainDir + "Shelves/";
	public static final String dbName = "ShelfSpeak.db";
	public static final Logger log = Logger.getLogger("Minecraft");
    private final ssPlayerListener playerListener = new ssPlayerListener(this);
    private final ssBlockListener blockListener = new ssBlockListener(this);
    public final HashMap<Player, String> activeCmd = new HashMap<Player, String>();
    public final HashMap<Player, AdvShelf> activeShelf = new HashMap<Player, AdvShelf>();

    public void onEnable() 
    {
    	session = this;
        new File(mainDir).mkdir();
        
        createDatabase();
        Connection conn = ssDBAccess.initialize(this.getDataFolder());
        if (conn == null) 
        {
            log.log(Level.SEVERE, "[ShelfSpeak] Could not establish SQL connection. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else if(!ssConversion.perform())	// Convert versions
        {
        	log.log(Level.SEVERE, "[ShelfSpeak] Database alteration failed. Disabling plugin...");
        	getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ssPermissions.initialize(getServer());	// Initialize permissions

        // Register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);

        // Register commands
        getCommand("shelf").setExecutor(new ShelfCommand(this));
        getCommand("shelfcolors").setExecutor(new ColorsCommand(this));
        getCommand("shelfwrite").setExecutor(new WriteCommand(this));
        getCommand("shelflock").setExecutor(new LockCommand(this));
        getCommand("shelfline").setExecutor(new LineCommand(this));
        getCommand("shelfclear").setExecutor(new ClearCommand(this));
        getCommand("shelfremove").setExecutor(new RemoveCommand(this));
        getCommand("shelfcancel").setExecutor(new CancelCommand(this));
        getCommand("shelfsave").setExecutor(new SaveCommand(this));
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.log(Level.INFO, "[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!");
    }
    
    public void onDisable() 
    {	
    	ssDBAccess.closeConnection();
    	log.log(Level.INFO, "[ShelfSpeak] Plugin disabled.");
    }
    
    private void createDatabase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File database = new File(getDataFolder() + "/" + dbName);
        if (!database.exists()) {
            try 
            {	database.createNewFile();	} 
            catch (IOException e) 
            {	log.log(Level.SEVERE, "[ShelfSpeak] Could not create database", e);	}
        }
    }
    
    public static int[] parseLineArgs(String cmdArg)
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

    public static boolean checkRanges(Player player, int page, int line)
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