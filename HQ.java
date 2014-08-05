package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class HQ {
	protected static void run(RobotController rc)
	{
		MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
		
		while (true)
		{
			try
			{
				spawnRobot(rc);
				
				MapLocation[] enemyPastrLocation = rc.sensePastrLocations(rc.getTeam().opponent());
				if (enemyPastrLocation.length > 0)
				{
					Communication.broadcastDestination(enemyPastrLocation[0], rc);
				}
				else
				{
					Communication.broadcastDestination(center, rc);
				}
				
				rc.yield();
			}
			catch (Exception e)
			{
				System.out.println("HQ Exception");
			}
		}
	}

	//--TODO: Must have more intelligent spawn location(s).
	private static void spawnRobot(RobotController rc)
			throws GameActionException {
		if (rc.isActive() && rc.senseRobotCount() < 25) {
			Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
				rc.spawn(toEnemy);
			}
		}
	}
}
