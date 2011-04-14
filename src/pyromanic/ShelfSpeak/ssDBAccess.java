package pyromanic.ShelfSpeak;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ssDBAccess 
{
	    private static Connection conn = null;
	    public static final String SHELVES = 
	    	"CREATE TABLE [Shelves] (\n" +
	    	"[ShelfID] INTEGER,\n" +
	    	"[World] VARCHAR(15),\n" +
	    	"[X] INTEGER,\n" +
	    	"[Y] INTEGER,\n" +
	    	"[Z] INTEGER,\n" +
	    	"[Owner] VARCHAR(15) NOT NULL,\n" +
	    	"[Modifier] VARCHAR(15) NOT NULL,\n" +
	    	"[Breakable] BOOLEAN NOT NULL DEFAULT TRUE,\n" +
	    	"PRIMARY KEY([ShelfID]))";
	    public static final String MESSAGES =
	    	"CREATE TABLE [Messages] (\n" +
	    	"[ShelfID] INTEGER,\n" +
	    	"[PageNo] INTEGER,\n" +
	    	"[LineNo] INTEGER,\n" +
	    	"[Text] VARCHAR(100),\n" +
	    	"PRIMARY KEY([ShelfID], [PageNo], [LineNo]),\n" +
	    	"FOREIGN KEY ([ShelfID]) REFERENCES [Shelves]([ShelfID]))";	    
	    public static final String LOCKS =
	    	"CREATE TABLE [Locks] (\n" +
	    	"[ShelfID] INTEGER,\n" +
	    	"[Type] VARCHAR(5) NOT NULL DEFAULT 'write',\n" +
	    	"[Player] VARCHAR(15) NOT NULL,\n" +
	    	"FOREIGN KEY ([ShelfID]) REFERENCES [Shelves]([ShelfID]))";
	    
	    public static Connection initialize(File dataFolder) {
	        try 
	        {
	            Class.forName("org.sqlite.JDBC");
	            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/" + ShelfSpeak.dbName);
	            conn.setAutoCommit(false);
	            return conn;
	        } 
	        catch (SQLException ex) 
	        {	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error initializing SQLite", ex);	} 
	        catch (ClassNotFoundException ex) 
	        {	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Could not find SQLite library.", ex);	}
	        return conn;
	    }

	    public static Connection getConnection() 
	    {	return conn;	}

	    public static void closeConnection() 
	    {
	        if(conn != null) 
	            try 
		        {	conn.close();	} 
	            catch (SQLException ex) 
	            {	ShelfSpeak.log.log(Level.SEVERE, "Error closing connection", ex);	}
	    }
	    
	    public static boolean shelfExists(AdvShelf shelf)
		{
	    	ResultSet set = null;
			try {
				Statement stmt = conn.createStatement();
				set = stmt.executeQuery("SELECT * FROM Shelves " +
						"WHERE X = " + shelf.getX() + 
						" AND Y = " + shelf.getY() + 
						" AND Z = " + shelf.getZ());
				if(set.next())
					return true;
			} catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error on shelfExists check", e);	}
			return false;
		}
	    
	    public static int getShelfID(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	ResultSet set = null;
	    	try 
	    	{
	    		stmt = conn.createStatement();
				set = stmt.executeQuery("SELECT [ShelfID] FROM [Shelves] " +
						"WHERE [World] = '"+ shelf.getWorld() +
						"' AND [X] = " + shelf.getX() + 
						" AND [Y] = " + shelf.getY() +
						" AND [Z] = " + shelf.getZ());
				if(set.next())
				{
					shelf.setID(set.getInt("ShelfID"));
					return shelf.getID();
				}
			} 
	    	catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing getShelfID", e);	}
	    	finally
	    	{
	    		
	    		try {
	    			if(set != null)
	    				set.close();
	    			if(stmt != null)
	    				stmt.close();
				} catch (SQLException e) {
					ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing getShelfID", e);
				}
	    	}
			return 0;
	    }
	    
	    public static AdvShelf getShelf(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	ResultSet set = null;
	    	try 
	    	{
	    		shelf.setID(getShelfID(shelf));
	    		if(shelf.getID() != 0)
	    		{
					stmt = conn.createStatement();
					set = stmt.executeQuery("SELECT * FROM [Shelves] " +
											"WHERE [ShelfID] = " + shelf.getID());
					if(set.next())
					{
						shelf.setOwner(set.getString("Owner"));
						shelf.setModifier(set.getString("Modifier"));
					}
					set = stmt.executeQuery("SELECT * FROM [Messages] " +
											"WHERE [ShelfID] = " + shelf.getID() +
											" ORDER BY ShelfID, PageNo, LineNo");
					while(set.next())
					{
						int page = set.getInt("PageNo");
						int line = set.getInt("LineNo");
						String text = set.getString("Text");
						if(!shelf.getPages().containsKey(page))
							shelf.getPages().put(page, new HashMap<Integer, String>());
						shelf.getPages().get(page).put(line, text);
					}
	    		}
			} catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing getShelf", e);	}
	    	return shelf;
	    }
	    
	    public static boolean editShelf(AdvShelf shelf)
	    {
	    	if(!shelfExists(shelf))
	    		return insertShelf(shelf);
	    	else
	    		return updateShelf(shelf);
	    }
	    
	    private static boolean insertShelf(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	ResultSet set = null;
			try 
			{
				stmt = conn.createStatement();
				String query = 
					"INSERT INTO [Shelves] " + 
					"VALUES(NULL, '" + shelf.getWorld() + "', " + 
					shelf.getX() + ", " + shelf.getY() + ", " + shelf.getZ() + 
					", '" + shelf.getOwner() + "', '" + shelf.getModifier() + "', " +
					(int)(shelf.isBreakable()?1:0) + ")";
				stmt.executeUpdate(query);
				conn.commit();
				shelf.setID(getShelfID(shelf));
				if(shelf.getID() != 0)
					return insertMessage(shelf);
				else
					ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error on insertShelf, getShelfID returned 0");
			} 
			catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing insertShelf", e);	}
			finally
			{
				try 
				{
					if(set != null)
						set.close();
					if(stmt != null)
						stmt.close();
				} 
				catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing insertShelf", e);	}
				
			}
			return false;
	    }
	    
	    private static boolean updateShelf(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	shelf.setID(getShelfID(shelf));
	    	if(shelf.getID() != 0)
	    	{
		    	try 
		    	{
					stmt = conn.createStatement();
					stmt.executeUpdate("UPDATE [Shelves] SET [Owner] = '" + shelf.getOwner() + "', " +
							"[Modifier] = '" + shelf.getModifier() + 
							"', [Breakable] = " + (int)(shelf.isBreakable()?1:0));
					conn.commit();
					clearMessage(shelf);
					return insertMessage(shelf);
				} 
		    	catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing updateShelf", e);	}
		    	finally
		    	{
					try 
					{
						if(stmt != null)
							stmt.close();
					} 
					catch (SQLException e) 
					{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing updateShelf", e);}
		    	}
	    	}
	    	else
	    		ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error on updateShelf, getShelfID returned 0");
	    	return false;
	    }
	    
	    public static boolean deleteShelf(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
	    		shelf.setID(getShelfID(shelf));
	    		if(shelf.getID() != 0)
	    		{
					stmt = conn.createStatement();
					clearMessage(shelf);
					stmt.executeUpdate("DELETE FROM [Shelves] " +
										"WHERE [ShelfID] = " + shelf.getID());
					conn.commit();
					return true;
	    		}
	    		else
	    			ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error on deleteShelf, getShelfID returned 0");
			} 
	    	catch (SQLException e) 
	    	{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing deleteShelf", e);	}
	    	finally
	    	{
				try 
				{
					if(stmt != null)
						stmt.close();
				} 
				catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing deleteShelf", e);	}
	    	}
	    	return false;
	    }
	    
	    @SuppressWarnings("rawtypes")
		public static boolean insertMessage(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
				stmt = conn.createStatement();
				Set<?> pages = shelf.getPages().entrySet();
				Iterator<?> i1 = pages.iterator();
				while(i1.hasNext()) {
					Map.Entry me1 = (Map.Entry)i1.next();
					int page = (Integer)me1.getKey();
					Set lines = ((HashMap) me1.getValue()).entrySet();
					Iterator i2 = lines.iterator();
					while(i2.hasNext()) {
						Map.Entry me2 = (Map.Entry)i2.next();
						int line = (Integer)me2.getKey();
						String text = me2.getValue().toString();
						stmt.executeUpdate("INSERT INTO [Messages] " +
								"VALUES(" + shelf.getID() + ", " + page + ", " + 
								line + ", '" + text + "')");
					}
				}
				conn.commit();
				return true;
	    	} 
	    	catch (SQLException e) 
	    	{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing insertMessage", e);	}
	    	finally
	    	{
	    		try 
	    		{
	    			if(stmt != null)
					stmt.close();
				} 
	    		catch (SQLException e) 
	    		{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing insertMessage");	}
	    	}
	    	return false;
	    }
	    
	    public static boolean clearMessage(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
				stmt = conn.createStatement();
				if(shelf.getID() == 0)
					shelf.setID(getShelfID(shelf));
				stmt.executeUpdate("DELETE FROM [Messages] " + 
								   "WHERE [ShelfID] = " + shelf.getID());
				conn.commit();
				return true;
			} 
	    	catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing clearMessage", e);	}
	    	finally
	    	{
	    		try 
	    		{
	    			if(stmt != null)
	    				stmt.close();
				} 
	    		catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing clearMessage", e);	}
	    	}
	    	
	    	return false;
	    }
	     
	    public static boolean tableExists(String tableName) {
	        ResultSet rs = null;
	        try 
	        {
	            DatabaseMetaData dbm = conn.getMetaData();
	            rs = dbm.getTables(null, null, tableName, null);
	            if (rs.next()) 
	            	return true;
	        } 
	        catch (SQLException ex) 
	        {
	            ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak]: " + tableName + 
	            		" Table Check Exception", ex);
	        } 
	        finally 
	        {
	            try 
	            {
	                if (rs != null) 
	                	rs.close();
	            } 
	            catch (SQLException ex) 
	            {	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak]: " + tableName + 
	            		" Table Check SQL Exception (on closing)");	}
	        }
	        return false;
	    }
	    
	    public static boolean createTable(String query)
	    {
	    	Statement stmt = null;
	        try {
	            stmt = conn.createStatement();
	            stmt.executeUpdate(query);
	            conn.commit();
	            return true;
	        } catch (SQLException e) {
	            ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Create Table Exception", e);
	        } finally {
	            try {
	                if (stmt != null) {
	                    stmt.close();
	                }
	            } catch (SQLException e) {
	                ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Could not create table (on close)");
	            }
	        }
	        return false;
	    }
}
