package pyromanic.ShelfSpeak;


import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;


import java.io.File;

/**
 * ShelfSpeak for Bukkit
 *
 * @author pyromanic
 */
public class ShelfSpeak extends JavaPlugin 
{
	static String mainDir = "plugins/ShelfSpeak/";
	static String shelfDir = mainDir + "Shelves/";
    private final ssPlayerListener playerListener = new ssPlayerListener(this);
    private final ssBlockListener blockListener = new ssBlockListener(this);
    public final HashMap<Player, String> activeCmd = new HashMap<Player, String>();
    public final HashMap<Player, AdvShelf> activeShelf = new HashMap<Player, AdvShelf>();

    public void onEnable() 
    {
        new File(mainDir).mkdir();
        new File(shelfDir).mkdir();

        // Register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version <" + pdfFile.getVersion() + "> is enabled!");
    }
    
    public void onDisable() 
    {
        System.out.println("ShelfSpeak has been disabled.");
    }
}