package pyromanic.ShelfSpeak;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class AdvShelf
{
	public static final int MAX_PAGES = 5;
	public static final int MAX_LINES = 10;
	public static final int MAX_CHARS = 50;
	
	private int _id = 0;
	private String _world;
	private double _x;
	private double _y;
	private double _z;
	private String _owner = null;
	private String _mod = null;
	private HashMap<Integer, HashMap<Integer, String>> _pages = new HashMap<Integer, HashMap<Integer, String>>();
	private boolean _breakable = true;
	
	public AdvShelf(String world, double x, double y, double z)
	{	setLocation(world, x, y, z);	}
	
	public AdvShelf(Location loc)
	{	setLocation(loc);	}
	
	public AdvShelf(AdvShelf shelf)
	{
		_id = shelf.getID();
		_world = shelf.getWorld();
		_x = shelf.getX();
		_y = shelf.getY();
		_z = shelf.getZ();
		_owner = shelf.getOwner();
		_mod = shelf.getModifier();
		_pages = shelf.getPages();
	}
	
	public void setID(int id)
	{	_id = id;	}
	
	public int getID()
	{	return _id;	}
	
	public void setLocation(Location loc)
	{	
		_world = loc.getWorld().getName();
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
	}
	
	public void setLocation(String world, double x, double y, double z)
	{	
		_world = world;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location getLocation()
	{
		World world = ShelfSpeak.session.getServer().getWorld(_world);
		Location loc = new Location(world, _x, _y, _z);
		return loc;
	}
	
	public String getWorld()
	{	return _world;	}
	
	public double getX()
	{	return _x;	}
	
	public double getY()
	{	return _y;	}
	
	public double getZ()
	{	return _z;	}
	
	public boolean isAt(Location loc)
	{
		return (loc.getWorld().getName() == _world &&
				loc.getX() == _x &&
				loc.getY() == _y &&
				loc.getZ() == _z);
	}
	
	public Block getBlock()
	{
		World world = ShelfSpeak.session.getServer().getWorld(_world);
		Block block = null;
		if(world != null)
			block = world.getBlockAt((int)_x, (int)_y, (int)_z);
		return block;
	}
	
	public void setOwner(String owner)
	{	_owner = owner;	}
	
	public String getOwner()
	{	return _owner;	}
	
	public boolean hasOwner()
	{	return !(_owner == null || _owner.length() == 0);	}
	
	public String getModifier()
	{	return _mod;	}
	
	public void setModifier(String modifier)
	{	_mod = modifier;	}
	
	public boolean hasModifier()
	{	return !(_mod == null || _mod.length() == 0);	}
	
	public HashMap<Integer, HashMap<Integer, String>> getPages()
	{	return _pages;	}
	
	public void setPages(HashMap<Integer, HashMap<Integer, String>> pages)
	{
		_pages = pages;
	}
	
	public boolean hasPages()
	{	return _pages.size() > 0;	}
	
	public int getMaxPage()
	{
		Set<Integer> keys = _pages.keySet();
		Object[] pageNos = keys.toArray();
		Arrays.sort(pageNos);
		return (pageNos.length == 0) ? 0 : (Integer)pageNos[pageNos.length - 1];
	}
	
	public int getMaxLine(int page)
	{
		HashMap<Integer, String> lines = _pages.get(page);
		if(lines != null)
		{
			Set<Integer> keys = lines.keySet();
			Object[] lineNos = keys.toArray();
			Arrays.sort(lineNos);
			return (lineNos.length == 0) ? 0 : (Integer)lineNos[lineNos.length - 1];
		}
		return 0;
	}
	
	public boolean isBreakable()
	{	return _breakable;	}
	
	public boolean save()
	{	return ssDBAccess.editShelf(this);	}
	
	public boolean delete()
	{	return ssDBAccess.deleteShelf(this);	}
	
	public boolean load()
	{
		AdvShelf shelf = ssDBAccess.getShelf(this);
		if(shelf != null)
		{
			_id = shelf.getID();
			_owner = shelf.getOwner();
			_mod = shelf.getModifier();
			_pages = shelf.getPages();
			return true;
		}
		else
			return false;
	}
	
	public boolean exists()
	{	return ssDBAccess.shelfExists(this);	}
	
	public String buildPath()
	{
		String worldPath = ShelfSpeak.shelfDir + _world + "/";
		File f = new File(worldPath);
		f.mkdir();
		String fileName = (int)_x + "_" + (int)_y + "_" + (int)_z;
		return worldPath + fileName;
	}
	
	public static String buildPath(Location loc)
	{
		String worldPath = ShelfSpeak.shelfDir + loc.getWorld().getName() + "/";
		File f = new File(worldPath);
		f.mkdir();
		String fileName =
			(int)loc.getX() + "_" +
			(int)loc.getY() + "_" +
			(int)loc.getZ();
		return worldPath + fileName;
	}
	
	private String buildString()
	{
		String output = (_owner == null ? "" : _owner) + "\r\n" + 
						(_mod == null ? "" : _mod) + "\r\n";
		if(_pages.containsKey(1))
			for(int x=1; x <= MAX_LINES; x++)
				if(!_pages.get(1).containsKey(x))
					output += "\r\n";
				else
				{
					output += _pages.get(1).get(x);
					break;
				}
		return output;
	}
	
	public void saveToFile() throws IOException
	{
		
		String path = buildPath();
		File f = new File(path);
		if(!f.exists())
			f.createNewFile();
		FileWriter writer = new FileWriter(f);
		String output = this.buildString();
		writer.write(output);
		writer.close();	
	}
	
	public void loadFromFile() throws FileNotFoundException
	{
		String path = buildPath();
		File f = new File(path);
		Scanner scan = new Scanner(f);
		if(scan.hasNextLine())
			_owner = scan.nextLine();
		if(scan.hasNextLine())
			_mod = scan.nextLine();
		HashMap<Integer, String> lines = new HashMap<Integer, String>();
		int x = 1;
		while(scan.hasNextLine())
		{
			String line = scan.nextLine();
			if(line != "")
				lines.put(x, line);
			x++;
		}
		scan.close();
		if(lines.size() > 0)
			_pages.put(1, lines);
		
	}
	
	public boolean deleteFile()
	{
		boolean ok = false;
		String path = buildPath();
		File f = new File(path);
		if(f.exists())
			ok = f.delete();
		return ok;
	}
	
	public boolean fileExists()
	{
		String path = buildPath();
		File f = new File(path);
		return f.exists();
	}
}