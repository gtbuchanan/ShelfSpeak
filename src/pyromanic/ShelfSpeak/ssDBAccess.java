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
						shelf.setBreakable(set.getBoolean("Breakable"));
						shelf.setReadable(set.getBoolean("Readable"));
						shelf.setWritable(set.getBoolean("Writable"));
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
					set = stmt.executeQuery("SELECT * FROM [Locks] " + 
											"WHERE [ShelfID] = " + shelf.getID());
					while(set.next())
					{
						String type = set.getString("Type");
						String player = set.getString("Player");
						if(type.equals("write"))
							shelf.addWriter(player);
						else if(type.equals("read"))
							shelf.addReader(player);
					}
	    		}
			} catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing getShelf", e);	}
	    	return shelf;
	    }
	    
	    public static boolean writeShelf(AdvShelf shelf)
	    {
	    	if(!shelfExists(shelf))
	    		return insertShelf(shelf);
	    	else
	    		return updateShelf(shelf);
	    }
	    
	    private static boolean insertShelf(AdvShelf shelf)
	    {
	    	boolean ok = false;
	    	Statement stmt = null;
	    	ResultSet set = null;
			try 
			{
				stmt = conn.createStatement();
				String query = 
					"INSERT INTO [Shelves] " + 
					"VALUES(NULL, '" + shelf.getWorld() + "', " + 
					shelf.getX() + ", " + shelf.getY() + ", " + shelf.getZ() + 
					", '" + shelf.getOwner().replace("'", "''") + 
					"', '" + shelf.getModifier().replace("'", "''") + "', " +
					(int)(shelf.isBreakable()?1:0) + ", " + 
					(int)(shelf.canWrite("")?1:0) + ", " +
					(int)(shelf.canRead("")?1:0) + ")";
				stmt.executeUpdate(query);
				conn.commit();
				shelf.setID(getShelfID(shelf));
				if(shelf.getID() != 0)
				{
					ok &= insertMessage(shelf);
					ok &= insertLocks(shelf);
				}
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
			return ok;
	    }
	    
	    private static boolean updateShelf(AdvShelf shelf)
	    {
	    	boolean ok = false;
	    	Statement stmt = null;
	    	shelf.setID(getShelfID(shelf));
	    	if(shelf.getID() != 0)
	    	{
		    	try 
		    	{
					stmt = conn.createStatement();
					stmt.executeUpdate("UPDATE [Shelves] SET [Owner] = '" + 
							shelf.getOwner().replace("'", "''") + "', " +
							"[Modifier] = '" + shelf.getModifier().replace("'", "''") + 
							"', [Breakable] = " + (int)(shelf.isBreakable()?1:0) + 
							", [Writable] = " + (int)(shelf.canWrite("")?1:0) + 
							", [Readable] = " + (int)(shelf.canRead("")?1:0) + 
							" WHERE [ShelfID] = " + shelf.getID());
					conn.commit();
					ok &= deleteMessage(shelf);
					ok &= insertMessage(shelf);
					ok &= deleteLocks(shelf);
					if(shelf.getWriters() != null || shelf.getReaders() != null)
						ok &= insertLocks(shelf);
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
	    	return ok;
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
					deleteMessage(shelf);
					deleteLocks(shelf);
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
				// Iterate existing pages
				while(i1.hasNext()) {
					Map.Entry me1 = (Map.Entry)i1.next();
					int page = (Integer)me1.getKey();
					Set lines = ((HashMap) me1.getValue()).entrySet();
					Iterator i2 = lines.iterator();
					// Iterate existing lines
					while(i2.hasNext()) {
						Map.Entry me2 = (Map.Entry)i2.next();
						int line = (Integer)me2.getKey();
						String text = me2.getValue().toString().replace("'", "''");
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
	    
	    public static boolean deleteMessage(AdvShelf shelf)
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
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing deleteMessage", e);	}
	    	finally
	    	{
	    		try 
	    		{
	    			if(stmt != null)
	    				stmt.close();
				} 
	    		catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing deleteMessage", e);	}
	    	}
	    	
	    	return false;
	    }
	     
	    public static boolean insertLocks(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
	    		if(shelf.getID() == 0)
					shelf.setID(getShelfID(shelf));
				stmt = conn.createStatement();
				if(shelf.getWriters() != null)
					for(String player : shelf.getWriters())
					{
						stmt.executeUpdate("INSERT INTO [Locks] " +
								"VALUES(" + shelf.getID() + ", 'write', '" + 
								player.replace("'", "''") + "')");
					}
				if(shelf.getReaders() != null)
					for(String player : shelf.getReaders())
					{
						stmt.executeUpdate("INSERT INTO [Locks] " +
							"VALUES(" + shelf.getID() + ", 'read', '" + 
							player.replace("'", "''") + "')");
					}
				conn.commit();
				return true;
			} 
	    	catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing insertLocks", e);	}
	    	finally
	    	{
	    		try 
	    		{
					if(stmt != null)
	    				stmt.close();
				} 
	    		catch (SQLException e) 
	    		{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing insertLocks", e);	}
	    	}
	    	return false;
	    }
	    
	    public static boolean deleteLocks(AdvShelf shelf)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
				stmt = conn.createStatement();
				if(shelf.getID() == 0)
					shelf.setID(getShelfID(shelf));
				stmt.executeUpdate("DELETE FROM [Locks] " + 
								   "WHERE [ShelfID] = " + shelf.getID());
				conn.commit();
				return true;
			} 
	    	catch (SQLException e) 
			{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing deleteLocks", e);	}
	    	finally
	    	{
	    		try 
	    		{
	    			if(stmt != null)
	    				stmt.close();
				} 
	    		catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing deleteLocks", e);	}
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
	    
	    public static boolean executeQuery(String query)
	    {
	    	Statement stmt = null;
	        try {
	            stmt = conn.createStatement();
	            stmt.executeUpdate(query);
	            conn.commit();
	            return true;
	        } catch (SQLException e) {
	            ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing executeQuery", e);
	        } finally {
	            try {
	                if (stmt != null) {
	                    stmt.close();
	                }
	            } catch (SQLException e) {
	                ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing executeQuery");
	            }
	        }
	        return false;
	    }

	    public static boolean setVersion(String version)
	    {
	    	Statement stmt = null;
	    	try 
	    	{
				stmt = conn.createStatement();
				int check = stmt.executeUpdate("UPDATE [DBInfo] SET [Version] = '" + version + "'");
				if(check == 0)
					stmt.executeUpdate("INSERT INTO [DBInfo]([Version]) VALUES(" + version + ")");
				conn.commit();
				return true;
			} 
	    	catch (SQLException e) 
	    	{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error performing setVersion");	}
	    	finally
	    	{
	    		try 
	    		{	
	    			if(stmt != null)
	    				stmt.close();	
	    		} 
	    		catch (SQLException e) 
				{	ShelfSpeak.log.log(Level.SEVERE, "[ShelfSpeak] Error closing setVersion");	}
	    	}
	    	return false;
	    }
	    
	    public static String getVersion()
	    {
	    	String version = "0.1";
	    	Statement stmt = null;
	    	ResultSet set = null;
	    	try 
	    	{
				stmt = conn.createStatement();
				set = stmt.executeQuery("SELECT [Version] FROM DBInfo");
				if(set.next())
					version = set.getString("Version");
			} 
	    	catch (SQLException e) 
	    	{
	    		if(tableExists("Shelves"))
	    		{	version = "0.2";	}
	    	}
	    	finally
	    	{
	    		try 
	    		{
	    			if(set != null)
	    				set.close();
	    			if(stmt != null)
	    				stmt.close();
				} 
	    		catch (SQLException e) {}
	    	}
	    	return version;
	    }
}
