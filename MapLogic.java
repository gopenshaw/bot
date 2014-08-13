package bot;

import battlecode.common.*;

public class MapLogic 
{
	private final static int NOT_SET_VALUE = -1;
	public static int nodeCount;
	private static int mapWidth = NOT_SET_VALUE;
	private static int mapHeight = NOT_SET_VALUE;
	private static TerrainTile[][] map;
	private static MapNode[] nodes = new MapNode[MapNode.MAX_MAP_NODES];
	
	public static int getNodeCount()
	{
		return nodeCount;
	}
	
	private static MapLocation calculatePastrLocation(RobotController rc) 
			throws GameActionException
	{
		if (mapWidth == NOT_SET_VALUE
			|| mapHeight == NOT_SET_VALUE)
		{
			mapWidth = rc.getMapWidth();
			mapHeight = rc.getMapHeight();
		}
		
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
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
				
				double value = calculatePastrValue(i, j, cowGrowth[i][j], enemyHQ);
				if (value > max)
				{
					max = value;
					xMax = i;
					yMax = j;
				}
			}
		}
		
		return new MapLocation(xMax, yMax);
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
	
	private static double calculatePastrValue(int x, int y, double cowGrowth, MapLocation enemyHQ)
	{
		final double COW_GROWTH_WEIGHT = 1000;
		final double DISTANCE_FROM_ENEMY_WEIGHT = 0.01;
		return cowGrowth * COW_GROWTH_WEIGHT
				+ new MapLocation(x, y).distanceSquaredTo(enemyHQ) * DISTANCE_FROM_ENEMY_WEIGHT;
	}
	
	private static TerrainTile[][] getMap(RobotController rc)
	{
		if (mapWidth == NOT_SET_VALUE
				|| mapHeight == NOT_SET_VALUE)
		{
			mapWidth = rc.getMapWidth();
			mapHeight = rc.getMapHeight();
		}
		
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
	
	public static boolean coarsenMap(RobotController rc) throws GameActionException
	{
		if (map == null)
		{
			map = getMap(rc);
		}
		
		System.out.println("index grid set");
		boolean[][] squareCounted = new boolean[mapWidth][mapHeight];
		
		for (int i = 0; i < mapWidth; i++)
		{
			for (int j = 0; j < mapHeight; j++)
			{
				if (!squareCounted[i][j])
				{
					MapNode node = expandFromHere(i, j, mapWidth, mapHeight, map, squareCounted);
					if (node != null)
					{
						node.index = nodeCount;
						nodes[nodeCount++] = node;
						markNodeIndexOnGrid(node, rc);
						if (nodeCount == MapNode.MAX_MAP_NODES)
						{
							return false;
						}
						setAdjacent(node);
					}
					
				}
			}
		}
		
		return true;
	}
	
	private static void markNodeIndexOnGrid(MapNode node, RobotController rc) 
			throws GameActionException
	{
		int index = node.index;
		for (int x = node.xLo; x <= node.xHi; x++)
		{
			for (int y = node.yLo; y <= node.yHi; y++)
			{
				Communication.setNodeIndex(index, new MapLocation(x, y), rc);
			}
		}
	}
	
	private static void setAdjacent(MapNode node)
	{
		int nodeIndex = node.index;
		for (int i = 0; i < nodeIndex; i++)
		{
			MapNode otherNode = nodes[i];
			if (node.isAdjacent(otherNode))
			{
				node.adjacent[node.adjacentCount++] = otherNode;
				otherNode.adjacent[otherNode.adjacentCount++] = node;
			}
		}
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
	
	public static void createMapTo(MapLocation destination, RobotController rc) 
		throws GameActionException
	{
		boolean[] wasMapped = new boolean[nodeCount];
		int nodeIndex = Communication.getNodeThatContains(destination, rc);
		wasMapped[nodeIndex] = true;
		MapNode node = nodes[nodeIndex];
		System.out.println("destination is " + destination.toString());
		System.out.println("node " + nodeIndex + " contains the destination.");
		System.out.println(node.toString());
		System.out.println();
		Communication.setNodeTarget(nodeIndex, destination, rc);
		
		recursiveMap(node, wasMapped, rc);
	}
	
	private static void recursiveMap(MapNode destination, boolean[] wasMapped, RobotController rc) 
			throws GameActionException
	{	
		//--This method will map all unmapped adjacent nodes to their destination
		for (int i = 0; i < destination.adjacentCount; i++)
		{
			MapNode adjacent = destination.adjacent[i];
			int index = adjacent.index;
			if (wasMapped[index])
			{
				continue;
			}
			
			System.out.println("node " + adjacent.index + " is mapped to node " + destination.index);
			wasMapped[index] = true;
			MapLocation target = adjacent.getAdjacentLocationIn(destination);
			Communication.setNodeTarget(index, target, rc);
			recursiveMap(adjacent, wasMapped, rc);
		}
	}
}
