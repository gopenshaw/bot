package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class HQ {
	protected static void run(RobotController rc)
	{
		MapLocation center = new MapLocation(
				rc.getMapWidth() / 2, rc.getMapHeight() / 2);
		int previousPastrCount = 0;
		
		while (true)
		{
			try
			{
				spawnRobot(rc);
				
				MapLocation[] enemyPastrLocations = 
						rc.sensePastrLocations(rc.getTeam().opponent());
				int enemyPastrCount = enemyPastrLocations.length;
				boolean enemyPastrDestroyed = enemyPastrCount < previousPastrCount;
				previousPastrCount = enemyPastrCount;
				
				if (enemyPastrCount > 0)
				{
					Communication.broadcastDestination(enemyPastrLocations[0], rc);
				}
				else if (enemyPastrDestroyed)
				{
					Communication.buildPastr(center, rc);
				}
				else
				{
					Communication.delayPastr(rc);
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
