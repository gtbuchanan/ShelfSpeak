package pyromanic.ShelfSpeak;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;

public class ssConversion 
{
	//public final static String DATABASE = "jdbc:sqlite:" + ShelfSpeak.dbName;
	public static final String CREATE_DBINFO = 
		"CREATE TABLE [DBInfo] (\n" +
		"[Version] VARCHAR(10) NOT NULL DEFAULT '0')";
	public static final String CREATE_SHELVES = 
    	"CREATE TABLE [Shelves] (\n" +
    	"[ShelfID] INTEGER,\n" +
    	"[World] VARCHAR(15),\n" +
    	"[X] INTEGER,\n" +
    	"[Y] INTEGER,\n" +
    	"[Z] INTEGER,\n" +
    	"[Owner] VARCHAR(15) NOT NULL,\n" +
    	"[Modifier] VARCHAR(15) NOT NULL,\n" +
    	"[Breakable] BOOLEAN NOT NULL DEFAULT 1,\n" +
    	"PRIMARY KEY([ShelfID]))";
    public static final String CREATE_MESSAGES =
    	"CREATE TABLE [Messages] (\n" +
    	"[ShelfID] INTEGER,\n" +
    	"[PageNo] INTEGER,\n" +
    	"[LineNo] INTEGER,\n" +
    	"[Text] VARCHAR(100),\n" +
    	"PRIMARY KEY([ShelfID], [PageNo], [LineNo]),\n" +
    	"FOREIGN KEY ([ShelfID]) REFERENCES [Shelves]([ShelfID]))";	    
    public static final String CREATE_LOCKS =
    	"CREATE TABLE [Locks] (\n" +
    	"[ShelfID] INTEGER,\n" +
    	"[Type] VARCHAR(5) NOT NULL DEFAULT 'write',\n" +
    	"[Player] VARCHAR(15) NOT NULL,\n" +
    	"FOREIGN KEY ([ShelfID]) REFERENCES [Shelves]([ShelfID]))";
    public static final String ADD_SHELVES_LOCKS = 
    	"ALTER TABLE [Shelves] ADD COLUMN [Writable] BOOLEAN NOT NULL DEFAULT 1;" +
    	"ALTER TABLE [Shelves] ADD COLUMN [Readable] BOOLEAN NOT NULL DEFAULT 1;";
	
	public static boolean perform()
	{
		boolean ok = true;
		int count = 0;
		String oldVersion = ssDBAccess.getVersion();	// Get old database version
		String newVersion = ShelfSpeak.session.getDescription().getVersion();
		// Update Version 0.1
		if(oldVersion == "0.1")
		{
			ok &= ssDBAccess.executeQuery(CREATE_SHELVES);
			ok &= ssDBAccess.executeQuery(CREATE_MESSAGES);
			ok &= ssDBAccess.executeQuery(CREATE_LOCKS);
		}
		// Update Version 0.2 and earlier
		if(oldVersion.compareTo("0.2") <= 0)
		{
			ok &= ssDBAccess.executeQuery(CREATE_DBINFO);
			ok &= ssDBAccess.executeQuery(ADD_SHELVES_LOCKS);
		}
		
		if(ok)
		{
			ok &= ssDBAccess.setVersion(newVersion);	// Set new database version
			count = importFiles();	// Import Version 0.1 saves
			if(count > 0)
				ShelfSpeak.log.log(Level.INFO, "[ShelfSpeak] Imported " + count + 
					" shelves from old saves!");
		}
		return ok;
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
						if(ssDBAccess.writeShelf(shelf))
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
