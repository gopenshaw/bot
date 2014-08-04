package bot;

import battlecode.common.Direction;

public class MovementInfo {
	
	Direction currentDirection;
	boolean followingWall;
	boolean turningRight;
	int distance;
		
	public MovementInfo(int robotID) {
		this.turningRight = robotID % 2 == 0;
	};
}
