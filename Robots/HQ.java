package bot.Robots;

import battlecode.common.*;
import bot.*;
import bot.Enums.*;

public class HQ {
	static int mapWidth = 0;
	static int mapHeight = 0;
	static int enemyPastrCount = 0;
	static TerrainTile[][] map;
	static MapLocation enemyHQ;
	static boolean rallyPointSet = false;
	static boolean pastrBuild = false;
	
	public static void run(RobotController rc)
	{
		int calculationPhase = 0;
		
		while (true)
		{
			try
			{
				calculationPhase++;
				spawnRobot(rc);
				
				switch (calculationPhase)
				{
				case 1:
					mapWidth = rc.getMapWidth();
					mapHeight = rc.getMapHeight();
					Communication.setMapCenter(new MapLocation(mapWidth / 2, mapHeight / 2), rc);
					Communication.setTeamHQ(rc.senseHQLocation(), rc);
					enemyHQ = rc.senseEnemyHQLocation();
					setPastrLocation(rc);
					rc.setIndicatorString(0, "calc 1 complete");
					break;
				case 2:
					map = getMap(mapWidth, mapHeight, rc);
					rc.setIndicatorString(0, "calc 2 complete");
					break;
				case 3:
					rc.setIndicatorString(0, "coarsening map...");
					MapLogic.coarsenMap(map, mapWidth, mapHeight);
					break;
				case 4:
					rc.setIndicatorString(0, "broadcasting route to our pastr");
					broadcastTeamPastrRoute(rc);
				}
				
				setTactic(rc);
				rc.yield();
			}
			catch (Exception e)
			{
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}

	private static void broadcastTeamPastrRoute(RobotController rc) 
			throws GameActionException 
	{
		MapLocation teamHQLocation = Communication.getTeamHQ(rc);
		MapLocation teamPastrLocation = Communication.getPastrLocation(rc);
		MapNode destinationNode = MapLogic.getRoute(teamHQLocation, teamPastrLocation);
		Communication.broadcastNodePath(destinationNode, rc);
		Communication.setNavigationMode(NavigationMode.MAP_NODES, rc);
	}

	//--TODO: Must have more intelligent spawn location(s).
	private static void spawnRobot(RobotController rc)
			throws GameActionException {
		if (rc.isActive() && rc.senseRobotCount() < 25) {
			Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
				rc.spawn(toEnemy);
			}
		}
	}
	
	private static void setTactic(RobotController rc) 
			throws GameActionException
	{
		MapLocation[] enemyPastrLocations = 
				rc.sensePastrLocations(rc.getTeam().opponent());
		
		int currentEnemyPastrCount = enemyPastrLocations.length;
		
		boolean enemyPastrDestroyed = currentEnemyPastrCount < enemyPastrCount;
		enemyPastrCount = currentEnemyPastrCount;
		
		if (enemyPastrCount > 0)
		{
			rc.setIndicatorString(1, "destroy enemy pastr!");
			Communication.setEnemyPastrLocation(enemyPastrLocations[0], rc);
			Communication.setTactic(Tactic.DESTROY_PASTR, rc);
		}
		else if (enemyPastrDestroyed)
		{
			rc.setIndicatorString(1, "build a pastr!");
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
			pastrBuild = true;
		}
		else if (rallyPointSet
				&& !pastrBuild)
		{
			rc.setIndicatorString(1, "rally!");
			Communication.setTactic(Tactic.RALLY, rc);
		}
	}
	
	private static void setPastrLocation(RobotController rc) 
		throws GameActionException
	{
		final double[][] cowGrowth = rc.senseCowGrowth();
		final int skipCount = 2;
		int xMax = -1;
		int yMax = -1;
		double max = 0;
		
		for (int i = 0; i < mapWidth; i+= skipCount)
		{
			for (int j = 0; j < mapHeight; j+= skipCount)
			{
				if (!adjacentSquaresAreNonZero(cowGrowth, i, j))
				{
					continue;
				}
				
				double value = calculatePastrValue(i, j, cowGrowth[i][j]);
				if (value > max)
				{
					max = value;
					xMax = i;
					yMax = j;
				}
			}
		}
		Communication.setPastrLocation(new MapLocation(xMax, yMax), rc);
		rallyPointSet = true;
	}
	
	private static boolean adjacentSquaresAreNonZero(double[][] cowGrowth, int x, int y)
	{
		final int MIN_SQUARES = 4;
		
		if (x < MIN_SQUARES 
			|| y < MIN_SQUARES 
			|| x + MIN_SQUARES >= mapWidth
			|| y + MIN_SQUARES >= mapHeight)
			return false;
		
		for (int i = 1; i <= MIN_SQUARES; i ++)
		{
			if (cowGrowth[x + i][y] == 0)
				return false;
			if (cowGrowth[x - i][y] == 0)
				return false;
			if (cowGrowth[x][y + i] == 0)
				return false;
			if (cowGrowth[x][y - i] == 0)
				return false;
		}
	
		return true;
	}
	
	private static double calculatePastrValue(int x, int y, double cowGrowth)
	{
		final double COW_GROWTH_WEIGHT = 1000;
		final double DISTANCE_FROM_ENEMY_WEIGHT = 0.01;
		return cowGrowth * COW_GROWTH_WEIGHT
				+ new MapLocation(x, y).distanceSquaredTo(enemyHQ) * DISTANCE_FROM_ENEMY_WEIGHT;
	}
	
	private static TerrainTile[][] getMap(int mapWidth, int mapHeight, RobotController rc)
	{
		TerrainTile[][] map = new TerrainTile[mapWidth][mapHeight];
		
		for (int i = 0; i < mapWidth; i++)
		{
			for (int j = 0; j < mapHeight; j++) 
			{
				map[i][j] = rc.senseTerrainTile(new MapLocation(i, j));
			}
		}
		
		return map;
	}
}


