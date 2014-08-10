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
	static MapNode[] mapNodes;
	static int nodeCount = 0;
	
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
					mapNodes = coarsenMap(map, mapWidth, mapHeight);
					rc.setIndicatorString(0, nodeCount + " nodes");
					break;
				case 4:
					for (int i = 0; i < nodeCount; i++)
					{
						rc.setIndicatorString(2, mapNodes[i].toString());
						rc.yield();
					}
					break;
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
	
	private static MapNode[] coarsenMap(TerrainTile[][] map, int width, int height)
	{
		MapNode[] nodes = new MapNode[10000];
		boolean[][] squareCounted = new boolean[width][height];
		
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				if (!squareCounted[i][j])
				{
					MapNode node = expandFromHere(i, j, width, height, map, squareCounted);
					if (node != null)
					{
						nodes[nodeCount++] = node;
					}
					
				}
			}
		}
		
		return nodes;
	}
	
	private static int getMaxHeight(int x, int y, int mapHeight, TerrainTile[][] map, boolean[][] squareCounted)
	{
		int thisHeight = 0;
		while (y + thisHeight + 1 < mapHeight
				&& !squareCounted[x][y + thisHeight + 1]
				&& map[x][y + thisHeight + 1] != TerrainTile.VOID)
		{
			squareCounted[x][y + thisHeight + 1] = true;
			thisHeight++;
		}
		
		return thisHeight;
	}
	
	private static int getMaxWidth(int x, int y, int rectangleHeight, int mapWidth, int mapHeight, 
			TerrainTile[][] map, boolean[][] squareCounted)
	{
		int thisWidth = 0;
		boolean columnClear = true;
		while (columnClear 
				&& x + thisWidth + 1 < mapWidth)
		{
			for (int i = y; i <= y + rectangleHeight; i++)
			{
				if (map[x + thisWidth + 1][i] == TerrainTile.VOID)
				{
					columnClear = false;
					break;
				}
			}
			
			if (columnClear)
			{
				for (int i = y; i <= y + rectangleHeight; i++)
				{
					squareCounted[x + thisWidth + 1][i] = true;
				}
				thisWidth++;
			}
		}
		
		return thisWidth;
	}
	
	private static MapNode expandFromHere(int x, int y, int mapWidth, int mapHeight, 
			TerrainTile[][] map, boolean[][] squareCounted)
	{
		squareCounted[x][y] = true;
		if (map[x][y] == TerrainTile.VOID)
		{
			return null;
		}
		
		int rectangleHeight = getMaxHeight(x, y, mapHeight, map, squareCounted);
		int rectangleWidth = getMaxWidth(x, y, rectangleHeight, mapWidth, mapHeight, map, squareCounted);
		return new MapNode(y, y + rectangleHeight, x, x + rectangleWidth);
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


