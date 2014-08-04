package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Soldier {
	
	//--Soldier self destruct
	final static int SELF_DESTRUCT_HEALTH_THRESHOLD = 35;
	final static int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 4;
	final static int SELF_DESTRUCT_WALK_STEPS = 3;
	
	public static void run(RobotController rc)
	{
		int robotID = rc.getRobot().getID();
		rc.setIndicatorString(2, "" + robotID);
		
		MovementInfo movementStatus = new MovementInfo(robotID);
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					Robot[] nearbyEnemies = 
							rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
					
					int enemyPastrCount = Communication.getEnemyPastrCount(rc);
					
					if (nearbyEnemies.length >= SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD
						&& rc.getHealth() < SELF_DESTRUCT_HEALTH_THRESHOLD)
					{
						initiateSelfDestruct(rc, nearbyEnemies);
					}
					else if (nearbyEnemies.length > 0)
					{
						attackAnEnemy(rc, nearbyEnemies);
					}
					else if (enemyPastrCount > 0)
					{
						int pastrIndexToAttack = robotID % enemyPastrCount;
						MapLocation enemyPastr = Communication.getEnemyPastrLocation(pastrIndexToAttack, rc);
						movementStatus = goToDestination(enemyPastr, rc, movementStatus);
					}
					else
					{
						goToDestination(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2), rc, movementStatus);
					}
					
					rc.yield();
				}
			}
			catch (Exception e) {
				System.out.println("Soldier Exception " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private static MovementInfo goToDestination(
			MapLocation destination, RobotController rc, MovementInfo movementStatus) 
			throws GameActionException 
	{
		if (movementStatus.followingWall) {
			return followWall(destination, rc, movementStatus);
		}
		
		//--check boolean and set direction
		MapLocation currentLocation = rc.getLocation();
		movementStatus.currentDirection = currentLocation.directionTo(destination);
		
		//--move
		if (rc.canMove(movementStatus.currentDirection))
		{
			rc.move(movementStatus.currentDirection);
		}
		else
		{
			movementStatus.followingWall = true;
			movementStatus.distance = currentLocation.distanceSquaredTo(destination);
			movementStatus.currentDirection = getNavigableDirection(rc, movementStatus);
			rc.move(movementStatus.currentDirection);
		}
		
		return movementStatus;
	}

	private static MovementInfo followWall(
			MapLocation destination, RobotController rc, MovementInfo movementStatus) 
			throws GameActionException {
		
		Direction checkDirection = getDirectionToCheck(movementStatus);
		
		if (rc.canMove(checkDirection))
		{
			movementStatus.currentDirection = checkDirection;
			int currentDistance = rc.getLocation().distanceSquaredTo(destination);
			if (currentDistance < movementStatus.distance) {
				movementStatus.followingWall = false;
			}
		}
		
		if (rc.canMove(movementStatus.currentDirection))
		{
			rc.move(movementStatus.currentDirection);
		}
		else
		{
			movementStatus.followingWall = true;
			movementStatus.currentDirection = getNavigableDirection(rc, movementStatus);
			rc.move(movementStatus.currentDirection);
		}
		
		return movementStatus;
	}
	
	private static Direction getDirectionToCheck(MovementInfo movementStatus) {
		if (movementStatus.turningRight)
		{
			return movementStatus.currentDirection.rotateLeft().rotateLeft();
		}
		
		return movementStatus.currentDirection.rotateRight().rotateRight();
	}
	
	private static void attackAnEnemy(RobotController rc, Robot[] nearbyEnemies)
			throws GameActionException {
		rc.setIndicatorString(0, "Attacking an enemy");
		for (int i = 0; i < nearbyEnemies.length; i++)
		{
			RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
			if (robotInfo.type != RobotType.HQ)
			{
				rc.attackSquare(robotInfo.location);
				break;
			}
		}
	}

	private static void initiateSelfDestruct(RobotController rc,
			Robot[] nearbyEnemies) throws GameActionException {
		rc.setIndicatorString(0, "Self-destruct engaged.");
		for (int i = 0; i < SELF_DESTRUCT_WALK_STEPS; i++)
		{
			MapLocation currentLocation = rc.getLocation();
			MapLocation enemyLocation = rc.senseRobotInfo(nearbyEnemies[0]).location;
			Direction moveDirection = currentLocation.directionTo(enemyLocation);
			if (rc.canMove(moveDirection))
			{
				rc.move(moveDirection);
			}
			
			rc.selfDestruct();
		}
	}
	
	private static Direction getNavigableDirection(
			RobotController rc, MovementInfo movementStatus)
	{
		Direction navigableDirection = movementStatus.currentDirection;
		while (!rc.canMove(navigableDirection))
		{
			if (movementStatus.turningRight)
			{
				navigableDirection = navigableDirection.rotateRight();
			}
			else
			{
				navigableDirection = navigableDirection.rotateLeft();
			}
		}
		
		return navigableDirection;
	}
}
