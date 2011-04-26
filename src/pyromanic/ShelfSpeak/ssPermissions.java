package pyromanic.ShelfSpeak;

import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ssPermissions 
{
	private static Permissions permissionsPlugin;
    public static boolean enabled = false;
    private static volatile ssPermissions instance;

    public static void initialize(Server server) 
    {
        Plugin test = server.getPluginManager().getPlugin("Permissions");
        if (test != null) 
        {
            permissionsPlugin = ((Permissions) test);
            enabled = true;
        } 
        else 
        	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Permissions isn't loaded. Defaults enabled.");
    }
    
	@SuppressWarnings("static-access")
	private static boolean permission(Player player, String string) 
    {	return permissionsPlugin.Security.permission(player, string);	}
	
	public static ssPermissions getInstance() 
	{
    	if (instance == null)
    		instance = new ssPermissions();
    	return instance;
    }
	
	public boolean read(Player player)
    {
    	if (enabled) 
        	return permission(player, "shelfspeak.read");
        else 
        	return true;
    }
    
    public boolean write(Player player) 
    {
        if (enabled) 
        	return permission(player, "shelfspeak.write");
        else 
        	return true;
    }
    
    public boolean colors(Player player)
    {
    	if (enabled) 
        	return permission(player, "shelfspeak.colors");
        else 
        	return true;
    }
    
    public boolean lockRead(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.lock.read");
    	else if(player.isOp())
    		return true;
    	else
    		return false;
    }
    
    public boolean lockWrite(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.lock.write");
    	else
    		return true;
    }
    
    public boolean writeAll(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.admin.writeall");
    	else if(player.isOp())
    		return true;
    	else
    		return false;
    }
    
    public boolean readAll(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.admin.readall");
    	else if(player.isOp())
    		return true;
    	else
    		return false;
    }
    
    /*
    @SuppressWarnings("static-access")
	public int maxLines(Player player)
    {
    	int count = 0;
    	if(enabled)
    		count = permissionsPlugin.Security.getPermissionInteger(
    					player.getWorld().getName(), 
    					player.getName(), 
    					"shelfspeakmaxlines");
    	if(count <= 0)
    		count = 10;
    	return count;
    }
     
    @SuppressWarnings("static-access")
	public int maxPages(Player player)
    {
    	int count = 0;
    	if(enabled)
    		count = permissionsPlugin.Security.getPermissionInteger(
    					player.getWorld().getName(), 
    					player.getName(), 
    					"shelfspeakmaxpages");
    	if(count <= 0)
    		count = 5
    	return count;
    }
    
    public boolean unbreakable(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.admin.unbreakable");
    	else if(player.isOp())
    		return true;
    	else
    		return false;
    }
    */
    
    /*
    public boolean breakAll(Player player)
    {
    	if (enabled)
    		return permission(player, "shelfspeak.admin.breakall");
    	else if(player.isOp())
    		return true;
    	else
    		return false;
    }
    */
}
