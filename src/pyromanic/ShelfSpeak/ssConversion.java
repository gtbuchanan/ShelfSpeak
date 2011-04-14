package pyromanic.ShelfSpeak;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;

public class ssConversion 
{
	//public final static String DATABASE = "jdbc:sqlite:" + ShelfSpeak.dbName;
	
	public static void perform()
	{
		int count = 0;
		if(!ssDBAccess.tableExists("Shelves"))
			ssDBAccess.createTable(ssDBAccess.SHELVES);
		if(!ssDBAccess.tableExists("Messages"))
			ssDBAccess.createTable(ssDBAccess.MESSAGES);
		if(!ssDBAccess.tableExists("Locks"))
			ssDBAccess.createTable(ssDBAccess.LOCKS);
		count = importFiles();
		if(count > 0)
			ShelfSpeak.log.log(Level.INFO, "[ShelfSpeak] Imported " + count + 
					" shelves from old saves!");
	}
	
	private static ArrayList<String> getSavedWorldNames()
    {
    	ArrayList<String> worldDirs = new ArrayList<String>();
    	File shelves = new File(ShelfSpeak.shelfDir);
    	File[] children;
    	if(shelves.exists() && shelves.isDirectory())
    	{
    		children = shelves.listFiles();
    		for(File f : children)
    			if(f.isDirectory())
    				worldDirs.add(f.getName());
    	}
    	return worldDirs;
    }
	
	private static int importFiles()
	{
		int count = 0;
		ArrayList<String> worldNames = getSavedWorldNames();
		for(String worldName : worldNames)
		{
			File worldDir = new File(ShelfSpeak.shelfDir + worldName);
			for(File shelfPath : worldDir.listFiles())
				if(shelfPath.isFile())
				{
					String[] coord = shelfPath.getName().split("_");
					try
					{
						double x = Double.parseDouble(coord[0]);
						double y = Double.parseDouble(coord[1]);
						double z = Double.parseDouble(coord[2]);
						AdvShelf shelf = new AdvShelf(worldName, x, y, z);
						shelf.loadFromFile();
						if(ssDBAccess.editShelf(shelf))
						{
							count++;
							shelfPath.delete();
						}
					} 
					catch(NumberFormatException e) {} 
					catch (FileNotFoundException e) {}
				}
			if(worldDir.exists() && worldDir.list().length == 0)
				worldDir.delete();
		}
		File shelfDir = new File(ShelfSpeak.shelfDir);
		if(shelfDir.exists() && shelfDir.list().length == 0)
			shelfDir.delete();
		return count;
	}
}
