package bot;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Map {
	public final int MAP_WIDTH;
	public final int MAP_HEIGHT;
	public final TerrainTile[][] MAP;
	
	public Map(RobotController rc) 
	{
		MAP_WIDTH = rc.getMapWidth();
		MAP_HEIGHT = rc.getMapHeight();
		TerrainTile[][] map = new TerrainTile[MAP_WIDTH][MAP_HEIGHT];
		
		for (int i = 0; i < MAP_WIDTH; i++)
		{
			for (int j = 0; j < MAP_HEIGHT; j++) 
			{
				map[i][j] = rc.senseTerrainTile(new MapLocation(i, j));
			}
		}
		
		MAP = map;
	}
}
