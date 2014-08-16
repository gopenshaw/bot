package bot;

import battlecode.common.*;

public class MapLogic 
{
	public static Map map;
	public static int nodeCount;
	private static MapNode[] nodes = new MapNode[MapNode.MAX_MAP_NODES];
	
	public MapLogic(Map map) {
		map = map;
	}
	
	public static int getNodeCount()
	{
		return nodeCount;
	}
	
	private static MapLocation calculatePastrLocation(RobotController rc) 
			throws GameActionException
	{
		
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		final double[][] cowGrowth = rc.senseCowGrowth();
		final int skipCount = 2;
		int xMax = -1;
		int yMax = -1;
		double max = 0;
		
		int mapWidth = map.MAP_WIDTH;
		int mapHeight = map.MAP_HEIGHT;
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
			|| x + MIN_SQUARES >= map.MAP_WIDTH
			|| y + MIN_SQUARES >= map.MAP_HEIGHT)
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
	
	public static void markNodeIndexOnGrid(RobotController rc) 
			throws GameActionException
	{
		for (int i = 0; i < nodeCount; i++)
		{
			MapNode node = nodes[i];
			for (int x = node.xLo; x <= node.xHi; x++)
			{
				for (int y = node.yLo; y <= node.yHi; y++)
				{
					Communication.setNodeIndex(i, new MapLocation(x, y), rc);
				}
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
	
	//--Need to do breadth first instead of depth first
	//--Test on longetylong128 for a good example!
	public static void createMapTo(MapLocation destination, RobotController rc) 
		throws GameActionException
	{
		boolean[] wasMapped = new boolean[nodeCount];
		MapNode[] nodeQueue = new MapNode[MapNode.MAX_MAP_NODES];
		
		int nodeIndex = Communication.getNodeThatContains(destination, rc);
		MapNode node = nodes[nodeIndex];
		
		wasMapped[nodeIndex] = true;
//		System.out.println("destination is " + destination.toString());
//		System.out.println("node " + nodeIndex + " contains the destination.");
//		System.out.println(node.toString());
//		System.out.println();
		Communication.setNodeTarget(nodeIndex, destination, rc);
		
		int queueIndex = 0;
		nodeQueue[queueIndex] = node;
		int queueSize = 1;
		
		while (true)
		{
			node = nodeQueue[queueIndex++];
			if (node == null)
			{
				return;
			}
			
			for (int i = 0; i < node.adjacentCount; i++)
			{
				MapNode adjacent = node.adjacent[i];
				int index = adjacent.index;
				if (wasMapped[index])
				{
					continue;
				}
				wasMapped[index] = true;
				
				MapLocation target = adjacent.getAdjacentLocationIn(node);
				Communication.setNodeTarget(index, target, rc);
				nodeQueue[queueSize++] = adjacent;
			}
		}
	}
}
