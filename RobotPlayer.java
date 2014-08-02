package bot;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void run(RobotController rc) {
		rand = new Random();
		
		if (rc.getType() == RobotType.SOLDIER)
		{
			runSoldier(rc);
		}
		else if (rc.getType() == RobotType.HQ)
		{
			runHQ(rc);
		}
	}
	
	public static void runSoldier(RobotController rc)
	{
		final int MY_ID = rc.getRobot().getID();
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					Direction moveDirection = directions[rand.nextInt(8)];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
					}
				}
			} 
			catch (Exception e) {
				System.out.println("Soldier Exception");
			}
		}
	}
	
	public static void runHQ(RobotController rc)
	{
		final int MAP_WIDTH = rc.getMapWidth();
		final int MAP_HEIGHT = rc.getMapHeight();
		boolean hasMap = false;
		TerrainTile[][] map;
		
		while (true)
		{
			if (!hasMap)
			{
				map = getMap(rc, MAP_WIDTH, MAP_HEIGHT);
				hasMap = true;
			}
			
			try {					
				//Check if a robot is spawnable and spawn one if it is
				if (rc.isActive() && rc.senseRobotCount() < 25) {
					Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
					if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
						rc.spawn(toEnemy);
					}
				}
			} catch (Exception e) {
				System.out.println("HQ Exception");
			}
		}
	}
	
	public static TerrainTile[][] getMap(RobotController rc, int width, int height)
	{
		TerrainTile[][] map = new TerrainTile[width][height];
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				map[i][j] = rc.senseTerrainTile(new MapLocation(i, j));
			}
		}
		
		return map;
	}
}
