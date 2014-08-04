package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static class MovementStatus
	{
		Direction currentDirection;
		boolean followingWall;
		boolean turningRight;
		
		MovementStatus(int robotID)
		{
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
	
	static Random rand;
	
	public static void run(RobotController rc) {
		rand = new Random(rc.getRobot().getID());
		
		if (rc.getType() == RobotType.SOLDIER)
		{
			Soldier.run(rc);
		}
		else if (rc.getType() == RobotType.HQ)
		{
			runHq(rc);
		}
	}
	
	private static void runHq(RobotController rc)
	{
		while (true)
		{
			try
			{
				spawnRobot(rc);
				broadcastEnemyPastrLocations(rc);
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
			Communication.broadcastEnemyPastrLocations(count, enemyPastrLocation[count], rc);
		}
		
		Communication.setEnemyPastrCount(count, rc);
	}
}
