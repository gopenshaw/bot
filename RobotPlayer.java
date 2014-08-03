package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static class MovementStatus
	{
		Direction currentDirection;
		boolean followingWall;
		int distanceFromDestination;
		boolean turningRight;
		int robotID;
		
		MovementStatus(int robotID)
		{
			this.robotID = robotID;
			this.turningRight = robotID % 2 == 0;
		};
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
	final static int DISTANCE_NOT_SET_VALUE = Integer.MAX_VALUE;
	
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
		
		MovementStatus movementStatus = new MovementStatus(robotID);
		int closestDistance = Integer.MAX_VALUE;
		
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
					else if (enemyPastrCount > 0)
					{
						movementStatus = goToAnEnemyPastr(rc, movementStatus);
						
						if (movementStatus.followingWall
							&& movementStatus.distanceFromDestination < closestDistance)
						{
							closestDistance = movementStatus.distanceFromDestination;
						}
						
						if (!movementStatus.followingWall
							&& movementStatus.distanceFromDestination >= closestDistance)
						{
							movementStatus.followingWall = true;
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
	
	private static MovementStatus goToAnEnemyPastr(RobotController rc, MovementStatus movementStatus) 
			throws GameActionException 
	{
		int pastrIndexToAttack = movementStatus.robotID % rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX);
		
		MapLocation currentLocation = rc.getLocation();
		MapLocation enemyPastr = 
				new MapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2),
								rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + pastrIndexToAttack * 2 + 1));
		
		if (!movementStatus.followingWall)
		{
			Direction moveDirection = currentLocation.directionTo(enemyPastr);
			
			if (rc.canMove(moveDirection))
			{
				rc.move(moveDirection);
				movementStatus.currentDirection = moveDirection;
				movementStatus.distanceFromDestination = DISTANCE_NOT_SET_VALUE;
				return movementStatus;
			}
			
			//--We were not able to move, so we put the wall on our right/left
			while (!rc.canMove(moveDirection))
			{
				if (movementStatus.turningRight)
				{
					moveDirection = moveDirection.rotateRight();
				}
				else
				{
					moveDirection = moveDirection.rotateLeft();
				}
			}
			
			int distanceToEnemyPastr = currentLocation.distanceSquaredTo(enemyPastr);
			movementStatus.currentDirection = moveDirection;
			movementStatus.followingWall = true;
			movementStatus.distanceFromDestination = distanceToEnemyPastr;
			return movementStatus;
		}
		//--We are following a wall
		else
		{
			Direction checkDirection;
			if (movementStatus.turningRight)
			{
				checkDirection = movementStatus.currentDirection.rotateLeft().rotateLeft();
			}
			else
			{
				checkDirection = movementStatus.currentDirection.rotateRight().rotateRight();
			}
			
			if (rc.canMove(checkDirection))
			{
				rc.move(checkDirection);
				movementStatus.followingWall = false;
				movementStatus.currentDirection = checkDirection;
				movementStatus.distanceFromDestination = currentLocation.distanceSquaredTo(enemyPastr);
				return movementStatus;
			}
			else
			{
				if (rc.canMove(movementStatus.currentDirection))
				{
					rc.move(movementStatus.currentDirection);
				}
			}
			
			//--We put the wall on our side
			while (!rc.canMove(movementStatus.currentDirection))
			{
				if (movementStatus.turningRight)
				{
					movementStatus.currentDirection = movementStatus.currentDirection.rotateRight();
				}
				else
				{
					movementStatus.currentDirection = movementStatus.currentDirection.rotateLeft();
				}
			}
			
			movementStatus.followingWall = true;
			movementStatus.distanceFromDestination = DISTANCE_NOT_SET_VALUE;
			return movementStatus;
		}
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
