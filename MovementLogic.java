package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bot.Enums.PointOfInterest;

public class MovementLogic {
	private final int FARM_SNEAK_DISTANCE = 50;
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
	public void moveToward(PointOfInterest poi, RobotController rc) 
			throws GameActionException
	{
		MapLocation currentLocation = rc.getLocation();
		MapLocation segmentDestination = Communication.getNodeDestination(poi, currentLocation, rc);
		if (segmentDestination == null)
		{
			MapLocation destination = Communication.getPointOfInterest(poi, rc);
			this.moveToward(destination, rc);
			return;
		}
		
		rc.setIndicatorString(0, "going to: " + segmentDestination.toString());
		Direction direction = currentLocation.directionTo(segmentDestination);
		
		if (rc.canMove(direction))
		{
			moveOrSneak(direction, currentLocation, rc);
		}
		else
		{
			Direction newRight = direction.rotateRight();
			if (rc.canMove(newRight))
			{
				moveOrSneak(newRight, currentLocation, rc);
			}
			else
			{
				Direction newLeft = direction.rotateLeft();
				if (rc.canMove(newLeft))
				{
					moveOrSneak(newLeft, currentLocation, rc);
				}
				else
				{
					newRight = newRight.rotateRight();
					if (rc.canMove(newRight))
					{
						moveOrSneak(newRight, currentLocation, rc);
					}
					else
					{
						newLeft = newLeft.rotateLeft();
						if (rc.canMove(newLeft))
						{
							moveOrSneak(newLeft, currentLocation, rc);
						}
					}
				}
			}
		}
		
		return;
	}
	
	public void moveToward(MapLocation destination, RobotController rc) throws GameActionException
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
				moveOrSneak(direction, locationBeforeMove, rc);
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
				
		moveOrSneak(this.currentDirection, locationBeforeMove, rc);
		
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
			//--TODO: This is probably the same as location before move
			//according to how turns progress
			MapLocation locationAfterMove = rc.getLocation();
			int currentDistance = locationAfterMove.distanceSquaredTo(destination);
			if (currentDistance < this.initialDistanceToDestAtWall)
			{
				this.followingWall = false;
			}
		}
	}
	
	private void moveOrSneak(Direction direction, MapLocation currentLocation, RobotController rc) 
			throws GameActionException
	{
		MapLocation farm = Communication.getPointOfInterest(PointOfInterest.Team_Pastr, rc);
		int distanceToDestination = currentLocation.distanceSquaredTo(farm);
		if (distanceToDestination <= FARM_SNEAK_DISTANCE)
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
