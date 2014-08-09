package bot;

import battlecode.common.*;

//--Throughout this class, if the robot hits a wall, it will turn to
//the left. In other words, it will keep the wall on its right.
public class MovementLogic {
	private MapLocation destination;
	private Direction currentDirection;
	private boolean followingWall;
	private int initialDistanceToDestination;
	private int intialSegmentDistance;
	private MapLocation segmentStart;
	private MapLocation segmentEnd;
	private boolean createWaypoint;
	
	public MovementLogic(MapLocation initialDestination, RobotController rc)
	{
		this.segmentStart = rc.getLocation();
		this.destination = initialDestination;
		this.segmentEnd = initialDestination;
	}
	
	//--TODO: The moveTowards method will cause the robot to go into an infinite loop
	//if the distance to the destination from the corners of an obstacle are never
	//shorter than the initial distance when the robot first hit the obstacle.
	//--TODO: A robot needs to leave behind 'waypoints' to indicate that the path
	//has shortcuts. Other robots will follow these 'waypoints' if they exist
	public void moveToward(MapLocation destination, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(2, "");
		//--If the robot was following a wall but there is a new destination,
		//this will force the robot to recalculate its distance from the destination
		//so it does not get stuck on a wall
		boolean destinationIsNew = !this.destination.equals(destination);
		if (destinationIsNew)
		{
			this.followingWall = false;
		}
		
		this.destination = destination;
		MapLocation locationBeforeMove = rc.getLocation();
		MapLocation newSegmentEnd = Communication.getSegment(destination, locationBeforeMove, rc);
		if (newSegmentEnd != null)
		{
			this.segmentEnd = newSegmentEnd;
			this.followingWall = false;
		}
		
		if (locationBeforeMove.equals(this.segmentEnd))
		{
			this.segmentEnd = destination;
			this.followingWall = true;
		}
		
		rc.setIndicatorString(1, "going to " + this.segmentEnd);
		
		if (!this.followingWall)
		{
			Direction direction = locationBeforeMove.directionTo(this.segmentEnd);
			
			if (rc.canMove(direction))
			{
				rc.move(direction);
				this.currentDirection = direction;
				return;
			}

			//--We will now follow the wall, and we need a distance to
			//know when we can stop following the wall.
			this.currentDirection = getNavigableDirection(rc, direction);
			
			if (this.initialDistanceToDestination == 0)
			{
				this.initialDistanceToDestination = locationBeforeMove.distanceSquaredTo(this.segmentEnd);
			}
			
			this.intialSegmentDistance = locationBeforeMove.distanceSquaredTo(this.segmentEnd);
			
			this.followingWall = true;
			//--Since we made a left turn, at our next right turn we
			//should make a waypoint
			this.createWaypoint = true;
			rc.setIndicatorString(2, "hit a wall");
		}
		
		//--The robot is following the wall and
		//must go in the right-most direction
		Direction oldDirection = this.currentDirection;
		this.currentDirection = 
				getNavigableDirection(rc, this.currentDirection.rotateRight().rotateRight());
		rc.move(this.currentDirection);
		
		//--Did we turn to the right?
		if (this.currentDirection == oldDirection.rotateRight()
				|| this.currentDirection == oldDirection.rotateRight().rotateRight())
		{
			MapLocation locationAfterMove = rc.getLocation();
			if (this.createWaypoint
				&& !this.segmentStart.equals(locationAfterMove))
			{
				Communication.broadcastSegment(
						destination, this.segmentStart, locationAfterMove, rc);
				rc.setIndicatorString(1, "wp: " + this.segmentStart + " " + locationAfterMove);
				this.createWaypoint = false;
			}
			
			this.segmentStart = locationAfterMove;
			
			int currentDistance = locationAfterMove.distanceSquaredTo(destination);
			if (currentDistance < this.initialDistanceToDestination
				|| currentDistance < this.intialSegmentDistance)
			{
				this.followingWall = false;
			}
		}
		//--Or did we turn left?
		else if (this.currentDirection != oldDirection)
		{
			//--Since we made a left turn, at our next right turn we
			//should make a waypoint
			rc.setIndicatorString(2, "turned left");
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
