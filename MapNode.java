package bot;

import battlecode.common.*;

public class MapNode {
	public final static int MAX_MAP_NODES = 500;
	
	int index;
	int yLo;
	int yHi;
	int xLo;
	int xHi;
	MapNode parent;
	
	public MapNode(int top, int bottom, int left, int right)
	{
		this.yLo = top;
		this.yHi = bottom;
		this.xLo = left;
		this.xHi = right;
	}
	
	public String toString()
	{
		return "Top: " + this.yLo +
				" Bottom: " + this.yHi +
				" Left: " + this.xLo +
				" Right: " + this.xHi;
	}
	
	public boolean contains(MapLocation location)
	{
		return location.y >= this.yLo
				&& location.y <= this.yHi
				&& location.x >= this.xLo
				&& location.x <= this.xHi;
	}
	
	public boolean isAdjacent(MapNode node)
	{
		return this.yLo == node.yHi + 1 && cross(this.xLo, this.xHi, node.xLo, node.xHi)
			|| this.yHi == node.yLo - 1 && cross(this.xLo, this.xHi, node.xLo, node.xHi)
			|| this.xLo == node.xHi + 1 && cross(this.yLo, this.yHi, node.yLo, node.yHi)
			|| this.xHi == node.xLo - 1 && cross(this.yLo, this.yHi, node.yLo, node.yHi);
	}
	
	private boolean cross(int x1, int x2, int y1, int y2)
	{
		//if x1 is within the y range
		return x1 >= y1 && x1 <= y2
		//or if y1 is within the x range
			|| y1 >= x1 && y1 <= x2;
	}
	
	private int aveMinRange(int lo1, int lo2, int hi1, int hi2)
	{
		//average the largest low with the smallest high
		int largestLow = lo1 > lo2 ? lo1 : lo2;
		int smallestHigh = hi1 < hi2 ? hi1 : hi2;
		return (largestLow + smallestHigh) / 2;
	}
	
	public MapLocation getAdjacentLocationIn(MapNode node)
	{
		if (this.yLo == node.yHi + 1 && cross(this.xLo, this.xHi, node.xLo, node.xHi))
		{
			int x = aveMinRange(this.xLo, node.xLo, this.xHi, node.xHi);
			return new MapLocation(x, node.yHi);
		}
		else if (this.yHi == node.yLo - 1 && cross(this.xLo, this.xHi, node.xLo, node.xHi))
		{
			int x = aveMinRange(this.xLo, node.xLo, this.xHi, node.xHi);
			return new MapLocation(x, node.yLo);
		}
		else if (this.xLo == node.xHi + 1 && cross(this.yLo, this.yHi, node.yLo, node.yHi))
		{
			int y = aveMinRange(this.yLo, node.yLo, this.yHi, node.yHi);
			return new MapLocation(node.xHi, y);
		}
		else
		{
			int y = aveMinRange(this.yLo, node.yLo, this.yHi, node.yHi);
			return new MapLocation(node.xLo, y);
		}
	}
}
