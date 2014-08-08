package bot;

import battlecode.common.*;

public class MovementLogic {
	private MapLocation destination;
	private MapLocation segmentBegin;
	private MapLocation segmentEnd;
	private Direction currentDirection;
	private boolean followingWall;
	
	//--We will assume the robot keeps the wall on its left
	public void moveTowards(MapLocation destination, RobotController rc) 
			throws GameActionException
	{
		//--If the robot was following a wall but there is a new destination,
		//this will force the robot to recalculate its distance from the destination
		//so it does not get stuck on a wall
		boolean destinationIsNew = !this.destination.equals(destination);
		if (destinationIsNew)
		{
			this.followingWall = false;
		}
		
		if (this.followingWall)
		{
			Direction checkDirection = this.currentDirection.rotateRight();
			if (rc.canMove(checkDirection))
			{
				rc.move(checkDirection);
			}
		}
	}
}
