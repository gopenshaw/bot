package bot;

import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import bot.Enums.Status;

public class CoarsenedMap extends Map implements Resumable {

	private int nodeCount;
	private MapNode[] nodes;
	private boolean[][] wasCounted;
	private boolean complete;
	
	private int i;
	private int j;
	
	public CoarsenedMap(RobotController rc) {
		super(rc);
		wasCounted = new boolean[super.MAP_WIDTH][super.MAP_HEIGHT];
		nodes = new MapNode[MapNode.MAX_MAP_NODES];
	}

	@Override
	public Status resume() 
	{
		if (complete)
		{
			return Status.COMPLETED;
		}
		
		if (coarsenMap(i, j) == Status.ABORTED)
		{
			return Status.ABORTED;
		}
		
		j = (j + 1) % super.MAP_HEIGHT;
		if (j == 0)
		{
			i = (i + 1) % super.MAP_WIDTH;
			if (i == 0) {
				complete = true;
				return Status.COMPLETED;
			}
		}
		
		return Status.IN_PROGRESS;
	}

	private Status coarsenMap(int i, int j)
	{	
		if (!wasCounted[i][j])
		{
			MapNode node = expandFromHere(i, j);
			if (node != null)
			{
				node.index = nodeCount;
				nodes[nodeCount++] = node;
				if (nodeCount == MapNode.MAX_MAP_NODES)
				{
					return Status.ABORTED;
				}
				
				setAdjacent(node);
			}
		}
		
		return null;
	}
	
	private MapNode expandFromHere(int x, int y)
	{
		wasCounted[x][y] = true;
		if (super.MAP[x][y] == TerrainTile.VOID)
		{
			return null;
		}
		
		int rectangleHeight = getMaxHeight(x, y);
		int rectangleWidth = getMaxWidth(x, y, rectangleHeight);
		return new MapNode(y, y + rectangleHeight, x, x + rectangleWidth);
	}
	
	private void setAdjacent(MapNode node)
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
	
	private int getMaxHeight(int x, int y)
	{
		int thisHeight = 0;
		while (y + thisHeight + 1 < super.MAP_HEIGHT
				&& !wasCounted[x][y + thisHeight + 1]
				&& super.MAP[x][y + thisHeight + 1] != TerrainTile.VOID)
		{
			wasCounted[x][y + thisHeight + 1] = true;
			thisHeight++;
		}
		
		return thisHeight;
	}
	
	private int getMaxWidth(int x, int y, int rectangleHeight)
	{
		int thisWidth = 0;
		boolean columnClear = true;
		while (columnClear 
				&& x + thisWidth + 1 < super.MAP_WIDTH)
		{
			for (int i = y; i <= y + rectangleHeight; i++)
			{
				if (super.MAP[x + thisWidth + 1][i] == TerrainTile.VOID)
				{
					columnClear = false;
					break;
				}
			}
			
			if (columnClear)
			{
				for (int i = y; i <= y + rectangleHeight; i++)
				{
					wasCounted[x + thisWidth + 1][i] = true;
				}
				thisWidth++;
			}
		}
		
		return thisWidth;
	}
}
