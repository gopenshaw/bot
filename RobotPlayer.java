package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static class MovementStatus
	{
		Direction currentDirection;
		boolean encounteredObstacle;
		int distance;
		
		MovementStatus()
		{
			
		};
		
		MovementStatus(Direction currentDirection, boolean encounteredObstacle, int distance)
		{
			this.currentDirection = currentDirection;
			this.encounteredObstacle = encounteredObstacle;
			this.distance = distance;
		}
	}
	
	final static Direction[] directions = 
	{
		Direction.NORTH, 
		Direction.NORTH_EAST, 
		Direction.EAST, 
		Direction.SOUTH_EAST, 
		Direction.SOUTH, 
		Direction.SOUTH_WEST, 
		Direction.WEST, 
		Direction.NORTH_WEST
	};
	//--Broadcasting
	final static int BUILD_PASTR_INDEX = 0;
	final static int ENEMY_PASTR_COUNT_INDEX = 1;
	final static int ENEMY_PASTR_LOCATION_DATA_START = 2;
	final static int ENEMY_PASTR_LOCATION_DATA_SIZE = 20;
	
	//--Soldier self destruct
	final static int SELF_DESTRUCT_HEALTH_THRESHOLD = 35;
	final static int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 4;
	final static int SELF_DESTRUCT_WALK_STEPS = 3;
	
	//--Navigation
	final static int NO_OBSTACLE_ENCOUNTERED_VALUE = -1;
	
	static Random rand;
	
	public static void run(RobotController rc) {
		rand = new Random(rc.getRobot().getID());
		
		if (rc.getType() == RobotType.SOLDIER)
		{
			runSoldier(rc);
		}
		else if (rc.getType() == RobotType.HQ)
		{
			runHq(rc);
		}
	}
	
	public static void runSoldier(RobotController rc)
	{
		int robotID = rc.getRobot().getID();
		rc.setIndicatorString(2, "" + robotID);
		
		MovementStatus movementStatus = new MovementStatus();
		int closestDistance = Integer.MAX_VALUE;
		boolean followingWall = false;
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					Robot[] nearbyEnemies = 
							rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
					
					int enemyPastrCount = rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX);
					
					if (nearbyEnemies.length >= SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD
						&& rc.getHealth() < SELF_DESTRUCT_HEALTH_THRESHOLD)
					{
						initiateSelfDestruct(rc, nearbyEnemies);
					}
					else if (nearbyEnemies.length > 0)
					{
						attackAnEnemy(rc, nearbyEnemies);
					}
					else if (!followingWall && 
							enemyPastrCount > 0)
					{
						movementStatus = goToAnEnemyPastr(rc, robotID);
						if (movementStatus.encounteredObstacle)
						{
							closestDistance = movementStatus.distance;
							followingWall = true;
						}
					}
					else if (followingWall
							&& enemyPastrCount > 0)
					{
						movementStatus = followWallToEnemyPastr(rc, robotID, movementStatus.currentDirection);
						if (movementStatus.distance < closestDistance)
						{
							followingWall = false;
						}
					}
					else
					{
						//--TODO this should go to center of board
						rc.setIndicatorString(0, "Moving randomly");
						Direction moveDirection = directions[rand.nextInt(8)];
						if (rc.canMove(moveDirection)) {
							rc.move(moveDirection);
						}
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
	
	private static MovementStatus goToAnEnemyPastr(RobotController rc, int robotID) 
			throws GameActionException 
	{
		int pastrIndexToAttack = robotID % rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX);
		rc.setIndicatorString(0, "Moving toward enemy PASTR " + pastrIndexToAttack);
		
		MapLocation currentLocation = rc.getLocation();
		MapLocation enemyPastr = 
				new MapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2),
								rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2 + 1));
		
		rc.setIndicatorString(1, "" + enemyPastr.x + " " + enemyPastr.y);
		Direction moveDirection = currentLocation.directionTo(enemyPastr);
		
		if (rc.canMove(moveDirection))
		{
			rc.move(moveDirection);
			return new MovementStatus(moveDirection, false, 0);
		}
		
		rc.setIndicatorString(0, "Was not able to move. About to rotate...");
		
		//--We put the wall on our right
		while (!rc.canMove(moveDirection))
		{
			moveDirection = moveDirection.rotateLeft();
		}
		
		rc.setIndicatorString(0, "Returning direction " + moveDirection.toString());
		
		//--and return the distance for future comparison
		int distanceToEnemyPastr = currentLocation.distanceSquaredTo(enemyPastr);
		return new MovementStatus(moveDirection, true, distanceToEnemyPastr);
	}

	private static MovementStatus followWallToEnemyPastr(
			RobotController rc, int robotID, Direction currentDirection) throws GameActionException 
	{
		int pastrIndexToAttack = robotID % rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX);
		rc.setIndicatorString(0, "Following wall toward enemy PASTR " + pastrIndexToAttack);
		
		MapLocation currentLocation = rc.getLocation();
		MapLocation enemyPastr = 
				new MapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2),
								rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2 + 1));
		
		rc.setIndicatorString(1, "" + enemyPastr.x + " " + enemyPastr.y);
		
		
		//--We either have the wall on our right
		//or we have just passed a corner
		Direction checkDirection = currentDirection.rotateRight().rotateRight();
		if (rc.canMove(checkDirection))
		{
			rc.move(checkDirection);
			return new MovementStatus(checkDirection, false, currentLocation.distanceSquaredTo(enemyPastr));
		}
		else
		{
			if (rc.canMove(currentDirection))
			{
				rc.move(currentDirection);
			}
		}
		
		//--We put the wall on our right
		while (!rc.canMove(currentDirection))
		{
			currentDirection = currentDirection.rotateLeft();
		}
		//--and return the distance for future comparison
		return new MovementStatus(currentDirection, false, currentLocation.distanceSquaredTo(enemyPastr));
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
	
	public static void runHq(RobotController rc)
	{
		while (true)
		{
			try
			{
				broadcastEnemyPastrLocations(rc);
				spawnRobot(rc);
				rc.yield();
			}
			catch (Exception e)
			{
				System.out.println("HQ Exception");
			}
		}
	}

	private static void spawnRobot(RobotController rc)
			throws GameActionException {
		if (rc.isActive() && rc.senseRobotCount() < 25) {
			Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
				rc.spawn(toEnemy);
			}
		}
	}

	private static void broadcastEnemyPastrLocations(RobotController rc)
			throws GameActionException {
		MapLocation[] enemyPastrLocation = rc.sensePastrLocations(rc.getTeam().opponent());
		int count = 0;
		for (; count < enemyPastrLocation.length; count++)
		{
			rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + count * 2, enemyPastrLocation[count].x);
			//System.out.println("Broadcasting " + enemyPastrLocation[count].x + " on channel " + (ENEMY_PASTR_LOCATION_DATA_START + count));
			rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + count * 2 + 1, enemyPastrLocation[count].y);
			//System.out.println("Broadcasting " + enemyPastrLocation[count].y + " on channel " + (ENEMY_PASTR_LOCATION_DATA_START + count + 1));
		}
		
		rc.broadcast(ENEMY_PASTR_COUNT_INDEX, count);
	}
}
