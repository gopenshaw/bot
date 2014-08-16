package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bot.Enums.PointOfInterest;

public class MovementLogic {
	private MapLocation destination;
	private Direction currentDirection;
	private boolean followingWall;
	private int initialDistanceToDestAtWall;
	boolean turningLeft;
	
	public MovementLogic(int robotID)
	{
		this.turningLeft = (robotID % 2 == 0);
	}
	
	//--TODO: The moveTowards method will cause the robot to go into an infinite loop
	//if the distance to the destination from the corners of an obstacle are never
	//shorter than the initial distance when the robot first hit the obstacle.
	//--TODO: There are a few ways that this bug movement algorithm could be optimized..
	public void moveToward(PointOfInterest poi, boolean sneak, RobotController rc) 
			throws GameActionException
	{
		MapLocation currentLocation = rc.getLocation();
		MapLocation segmentDestination = Communication.getNodeDestination(poi, currentLocation, rc);
		if (segmentDestination == null)
		{
			MapLocation destination = Communication.getPointOfInterest(poi, rc);
			this.moveToward(destination, sneak, rc);
			return;
		}
		
		rc.setIndicatorString(0, "going to: " + segmentDestination.toString());
		Direction direction = currentLocation.directionTo(segmentDestination);
		
		if (rc.canMove(direction))
		{
			sneakIfTrueElseMove(direction, sneak, rc);
		}
		else if (rc.canMove(direction.rotateRight()))
		{
			sneakIfTrueElseMove(direction.rotateRight(), sneak, rc);
		}
		else if (rc.canMove(direction.rotateLeft()))
		{
			sneakIfTrueElseMove(direction.rotateLeft(), sneak, rc);
		}
		
		return;
	}
	
	public void moveToward(MapLocation destination, boolean sneak, RobotController rc) throws GameActionException
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
				sneakIfTrueElseMove(direction, sneak, rc);
				this.currentDirection = direction;
				return;
			}

			//--We will now follow the wall, and we need a distance to
			//know when we can stop following the wall.
			this.currentDirection = turningLeft ? 
					turnLeft(rc, direction) : turnRight(rc, direction);
			this.initialDistanceToDestAtWall = 
					locationBeforeMove.distanceSquaredTo(this.destination);
			this.followingWall = true;
		}
		
		//--The robot is following the wall and
		//must go in the right-most direction
		Direction oldDirection = this.currentDirection;
		this.currentDirection = turningLeft ?
				turnLeft(rc, this.currentDirection.rotateRight().rotateRight()):
				turnRight(rc, this.currentDirection.rotateLeft().rotateLeft());
				
		sneakIfTrueElseMove(this.currentDirection, sneak, rc);
		
		//--If the robot normally turns left but turned right, or normally
		//turns right but has turned left, then it just rounded an exterior
		//corner
		
		boolean roundedCorner = turningLeft ?
				(this.currentDirection == oldDirection.rotateRight()
				|| this.currentDirection == oldDirection.rotateRight().rotateRight()) :
				(this.currentDirection == oldDirection.rotateLeft()
				|| this.currentDirection == oldDirection.rotateLeft().rotateLeft());		
		if (roundedCorner)
		{
			MapLocation locationAfterMove = rc.getLocation();
			int currentDistance = locationAfterMove.distanceSquaredTo(destination);
			if (currentDistance < this.initialDistanceToDestAtWall)
			{
				this.followingWall = false;
			}
		}
	}
	
	private void sneakIfTrueElseMove(Direction direction, boolean sneak, RobotController rc) 
			throws GameActionException
	{
		if (sneak)
		{
			rc.sneak(direction);
		}
		else
		{
			rc.move(direction);
		}
	}
	
	private Direction turnRight(
			RobotController rc, Direction direction)
	{
		while (!rc.canMove(direction))
		{
			direction = direction.rotateRight();
		}
		
		return direction;
	}
	
	private static Direction turnLeft(
			RobotController rc, Direction direction)
	{
		while (!rc.canMove(direction))
		{
			direction = direction.rotateLeft();
		}
		
		return direction;
	}
}
