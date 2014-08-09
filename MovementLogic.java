package bot;

import battlecode.common.*;

//--Throughout this class, if the robot hits a wall, it will turn to
//the left. In other words, it will keep the wall on its right.
public class MovementLogic {
	private MapLocation destination;
	private Direction currentDirection;
	private boolean followingWall;
	private int initialDistance;
	private MapLocation segmentStart;
	private boolean createWaypoint;
	
	public MovementLogic(RobotController rc)
	{
		this.segmentStart = rc.getLocation();
		this.destination = new MapLocation(-1, -1);
	}
	
	//--TODO: The moveTowards method will cause the robot to go into an infinite loop
	//if the distance to the destination from the corners of an obstacle are never
	//shorter than the initial distance when the robot first hit the obstacle.
	//--TODO: A robot needs to leave behind 'waypoints' to indicate that the path
	//has shortcuts. Other robots will follow these 'waypoints' if they exist
	public void moveToward(MapLocation destination, RobotController rc) 
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
		
		this.destination = destination;
		if (!this.followingWall)
		{
			MapLocation currentLocation = rc.getLocation();
			Direction direction = currentLocation.directionTo(destination);
			
			if (rc.canMove(direction))
			{
				rc.move(direction);
				this.currentDirection = direction;
				return;
			}

			//--We will now follow the wall, and we need a distance to
			//know when we can stop following the wall.
			this.currentDirection = getNavigableDirection(rc, direction);
			this.initialDistance = currentLocation.distanceSquaredTo(destination);
			rc.setIndicatorString(1, "initial distance: " + this.initialDistance);
			this.followingWall = true;
			//--Since we made a left turn, at our next right turn we
			//should make a waypoint
			this.createWaypoint = true;
		}
		
		//--The robot is following the wall and
		//must go in the right-most direction
		Direction oldDirection = this.currentDirection;
		this.currentDirection = 
				getNavigableDirection(rc, this.currentDirection.rotateRight().rotateRight());
		rc.move(this.currentDirection);
		
		if (this.currentDirection.ordinal() > oldDirection.ordinal())
		{
			MapLocation currentLocation = rc.getLocation();
			if (this.createWaypoint)
			{
				Communication.broadcastSegment(
						destination, this.segmentStart, currentLocation, rc);
				rc.setIndicatorString(1, "wp: " + this.segmentStart + " " + currentLocation);
				this.createWaypoint = false;
			}
			
			this.segmentStart = currentLocation;
			
			int currentDistance = currentLocation.distanceSquaredTo(this.destination);
			if (currentDistance < this.initialDistance)
			{
				this.followingWall = false;
			}
		}
		else if (this.currentDirection.ordinal() < oldDirection.ordinal())
		{
			//--Since we made a left turn, at our next right turn we
			//should make a waypoint
			this.createWaypoint = true;
		}
	}
	
	private static Direction getNavigableDirection(
			RobotController rc, Direction direction)
	{
		while (!rc.canMove(direction))
		{
			direction = direction.rotateLeft();
		}
		
		return direction;
	}
}
