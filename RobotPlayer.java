package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	final static int ENEMY_PASTR_COUNT_INDEX = 0;
	final static int ENEMY_PASTR_START_INDEX = 1;
	final static int ENEMY_PASTR_DATA_SIZE = 20;
	
	public static void run(RobotController rc) {
		rand = new Random();
		
		if (rc.getType() == RobotType.SOLDIER)
		{
			runSoldier(rc);
		}
		else if (rc.getType() == RobotType.HQ)
		{
			runHQ(rc);
		}
	}
	
	public static void runSoldier(RobotController rc)
	{
		while (true)
		{
			try {
				if (rc.isActive()) {
					Direction moveDirection = directions[rand.nextInt(8)];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
					}
				}
			} 
			catch (Exception e) {
				System.out.println("Soldier Exception");
			}
		}
	}
	
	public static void runHQ(RobotController rc)
	{
		while (true)
		{
			try {
				broadcastEnemyPastrs(rc);
				spawnRobot(rc);
				rc.yield();
			} catch (Exception e) {
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
		MapLocation[] enemyPastrLocation;
		enemyPastrLocation = rc.sensePastrLocations(rc.getTeam().opponent());
		int count = 0;
		for (; count < enemyPastrLocation.length; count++)
		{
			rc.broadcast(ENEMY_PASTR_START_INDEX + count, enemyPastrLocation[count].x);
			rc.broadcast(ENEMY_PASTR_START_INDEX + count + 1, enemyPastrLocation[count].y);
		}
		
		rc.broadcast(ENEMY_PASTR_COUNT_INDEX, count);
	}
}
