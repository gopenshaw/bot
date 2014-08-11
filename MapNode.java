package bot;

import battlecode.common.*;

public class MapNode {
	public final static int MAX_MAP_NODES = 4000;
	
	int top;
	int bottom;
	int left;
	int right;
	MapNode parent;
	
	public MapNode(int top, int bottom, int left, int right)
	{
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
	
	public String toString()
	{
		return "Top: " + this.top +
				" Bottom: " + this.bottom +
				" Left: " + this.left +
				" Right: " + this.right;
	}
	
	public boolean contains(MapLocation location)
	{
		return location.x >= this.top
				&& location.x <= this.bottom
				&& location.y >= this.left
				&& location.y <= this.right;
	}
	
	public boolean isAdjacent(MapNode node)
	{
		return this.top == node.bottom - 1
				|| this.bottom == node.top + 1
				|| this.left == node.right - 1
				|| this.right == node.left + 1;
	}
	
	public MapLocation getAdjacentLocationIn(MapNode node)
	{
		if (this.top == node.bottom - 1)
		{
			int x = node.left > this.left ? node.left : this.left;
			return new MapLocation(x, node.bottom);
		}
		else if (this.bottom == node.top + 1)
		{
			int x = node.left > this.left ? node.left : this.left;
			return new MapLocation(x, node.top);
		}
		else if (this.left == node.right - 1)
		{
			int y = node.top > this.top ? node.top : this.top;
			return new MapLocation(node.right, y);
		}
		else
		{
			int y = node.top > this.top ? node.top : this.top;
			return new MapLocation(node.left, y);
		}
	}
}
