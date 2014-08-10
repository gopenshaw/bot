package bot;

public class MapNode {
	int top;
	int bottom;
	int left;
	int right;
	
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
}
