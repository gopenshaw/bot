package bot;

import battlecode.common.*;

public class MapLogic 
{
	private static int nodeCount;
	private static MapNode[] nodes = new MapNode[MapNode.MAX_MAP_NODES];
	
	public static int getNodeCount()
	{
		return nodeCount;
	}
	
	public static void coarsenMap(TerrainTile[][] map, int width, int height)
	{
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

	//--PRECONDITION: coarsenMap has been called
	public static MapNode getRoute(MapLocation source, MapLocation destination) {
		MapNode sourceNode = getNodeContaining(source);
		MapNode destinationNode = getNodeContaining(destination);
		int nodeIndex = 0;
		
		MapNode[] nodeQueue = new MapNode[1000];
		nodeQueue[nodeIndex++] = sourceNode;
		int currentIndex = 0;
		
		MapNode finalNode = null;
		while (currentIndex < nodeIndex)
		{
			MapNode current = nodes[currentIndex++];
			if (current == destinationNode)
			{
				finalNode = current;
				break;
			}
			
			//--Add all nodes that are adjacent to the current node
			for (int i = 0; i < nodeCount; i++)
			{
				MapNode node = nodes[i];
				if (node.isAdjacent(current))
				{
					node.parent = current;
					nodeQueue[nodeIndex++] = node;
				}
			}
		}
		
		return finalNode;
	}
	
	private static MapNode getNodeContaining(MapLocation location)
	{
		for (int i = 0; i < nodeCount; i++)
		{
			if (nodes[i].contains(location))
			{
				return nodes[i];
			}
		}
		return null;
	}
}
