package bot;

import battlecode.common.*;

//--Throughout this class, if the robot hits a wall, it will turn to
//the left. In other words, it will keep the wall on its right.
public class MovementLogic {
	private MapLocation destination;
	private Direction currentDirection;
	private boolean followingWall;
	private int initialDistanceToDestAtWall;
	
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
		if (this.destination != null)
		{
			boolean destinationIsNew = !this.destination.equals(destination);
			if (destinationIsNew)
			{
				this.followingWall = false;
			}
		}

		this.destination = destination;
		
		MapLocation locationBeforeMove = rc.getLocation();
		
		if (!this.followingWall)
		{
			Direction direction = locationBeforeMove.directionTo(this.destination);
			
			if (rc.canMove(direction))
			{
				rc.move(direction);
				this.currentDirection = direction;
				return;
			}

			//--We will now follow the wall, and we need a distance to
			//know when we can stop following the wall.
			this.currentDirection = getNavigableDirection(rc, direction);
			this.initialDistanceToDestAtWall = 
					locationBeforeMove.distanceSquaredTo(this.destination);
			this.followingWall = true;
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
			int currentDistance = locationAfterMove.distanceSquaredTo(destination);
			if (currentDistance < this.initialDistanceToDestAtWall)
			{
				this.followingWall = false;
			}
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
