package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
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
	final static int ENEMY_PASTR_COUNT_INDEX = 0;
	final static int ENEMY_PASTR_LOCATION_DATA_START = 1;
	final static int ENEMY_PASTR_LOCATION_DATA_SIZE = 20;
	
	//--Soldier self destruct
	final static int SELF_DESTRUCT_HEALTH_THRESHOLD = 35;
	final static int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 4;
	final static int SELF_DESTRUCT_WALK_STEPS = 3;
	
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
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
					
					if (nearbyEnemies.length >= SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD
						&& rc.getHealth() < SELF_DESTRUCT_HEALTH_THRESHOLD)
					{
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
					
					for (int i = 0; i < nearbyEnemies.length; i++)
					{
						RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
						if (robotInfo.type != RobotType.HQ)
						{
							rc.attackSquare(robotInfo.location);
							rc.yield();
						}
					}
					
					if (rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX) > 0)
					{
						MapLocation currentLocation = rc.getLocation();
						MapLocation enemyPastr = new MapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START),
																rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + 1));
						Direction moveDirection = currentLocation.directionTo(enemyPastr);
						
						while (!rc.canMove(moveDirection)) {
							moveDirection = moveDirection.rotateLeft();
						}
						
						rc.move(moveDirection);
					}
					else
					{
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
			}
		}
	}
	
	public static void runHq(RobotController rc)
	{
		try
		{
			for (int i = 0; i < ENEMY_PASTR_LOCATION_DATA_SIZE; i++)
			{
				rc.broadcast(i + ENEMY_PASTR_LOCATION_DATA_START, -1);
			}
		}
		catch (Exception e)
		{
			System.out.println("HQ Exception");
		}
		
		while (true)
		{
			try
			{
				broadcastEnemyPastrs(rc);
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

	private static void broadcastEnemyPastrs(RobotController rc)
			throws GameActionException {
		MapLocation[] enemyPastrLocation = rc.sensePastrLocations(rc.getTeam().opponent());
		int count = 0;
		for (; count < enemyPastrLocation.length; count++)
		{
			rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + count, enemyPastrLocation[count].x);
			rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + count + 1, enemyPastrLocation[count].y);
		}
		
		rc.broadcast(ENEMY_PASTR_COUNT_INDEX, count);
	}
}
