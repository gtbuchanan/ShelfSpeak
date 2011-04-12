package pyromanic.ShelfSpeak;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class AdvShelf
{
	private Block _block = null;
	private String _owner = null;
	private String _mod = null;
	private ArrayList<String> _lines = new ArrayList<String>();
	public static final int MAX_LINES = 10;
	
	public AdvShelf(Block block)
	{	_block = block;	}
	
	public void setBlock(Block block)
	{	_block = block;	}
	
	public Block getBlock()
	{	return _block;	}
	
	public void setOwner(String owner)
	{	_owner = owner;	}
	
	public String getOwner()
	{	return _owner;	}
	
	public boolean hasOwner()
	{	return !(_owner == null || _owner.length() == 0);	}
	
	public ArrayList<String> getLines()
	{	return _lines;	}
	
	public boolean hasLines()
	{	return !(_lines.isEmpty());	}
	
	public String getModifier()
	{	return _mod;	}
	
	public void setModifier(String modifier)
	{	_mod = modifier;	}
	
	public boolean hasModifier()
	{	return !(_mod == null || _mod.length() == 0);	}
	
	public String buildPath()
	{
		return buildPath(_block.getLocation());
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
		for(String line : _lines)
			output += line + "\r\n";
		return output;
	}
	
	public void save() throws IOException
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
	
	public void load() throws FileNotFoundException
	{
		String path = buildPath();
		File f = new File(path);
		Scanner scan = new Scanner(f);
		if(scan.hasNextLine())
			_owner = scan.nextLine();
		if(scan.hasNextLine())
			_mod = scan.nextLine();
		while(scan.hasNextLine())
			_lines.add(scan.nextLine());
		scan.close();
	}
	
	public boolean delete()
	{
		boolean ok = false;
		String path = buildPath();
		File f = new File(path);
		if(f.exists())
			ok = f.delete();
		return ok;
	}
	
	public boolean exists()
	{
		String path = buildPath();
		File f = new File(path);
		return f.exists();
	}
}