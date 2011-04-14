package pyromanic.ShelfSpeak;
/*
 * Developer:
 * 		pyromanic
 * Libraries:
 * 		Bukkit [674]
 * 		Permissions [2.6]
 */

import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        new File(shelfDir).mkdir();
        
        createDatabase();
        Connection conn = ssDBAccess.initialize(this.getDataFolder());
        if (conn == null) 
        {
            log.log(Level.SEVERE, "[ShelfSpeak] Could not establish SQL connection. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        	ssConversion.perform();	
        //ssPermissions.initialize(getServer());	// Initialize permissions

        // Register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        log.log(Level.INFO, "[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " is enabled!");
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
            {	log.log(Level.SEVERE, "[ShelfSpeak] Could not create the database", e);	}
        }
    }
}