package bot.Robots;

import battlecode.common.*;
import bot.*;
import bot.Enums.*;

public class HQ {
	private CoarsenedMap map;
	
	public void run(RobotController rc)
	{
		while (true)
		{
			try
			{
				spawnRobot(rc);
				double nextSpawnRound = Clock.getRoundNum()
						+ GameConstants.HQ_SPAWN_DELAY_CONSTANT_1 
						+ (rc.senseRobotCount() + 1) * GameConstants.HQ_SPAWN_DELAY_CONSTANT_2;
				rc.setIndicatorString(1, "next spawn round: " + nextSpawnRound);
				
				map = new CoarsenedMap(rc);
				
				
				while (map.resume() == Status.IN_PROGRESS)
				{
					if (Clock.getRoundNum() >= nextSpawnRound)
					{
						spawnRobot(rc);
						nextSpawnRound = Clock.getRoundNum()
								+ GameConstants.HQ_SPAWN_DELAY_CONSTANT_1 
								+ (rc.senseRobotCount() + 1) * GameConstants.HQ_SPAWN_DELAY_CONSTANT_2;
						rc.setIndicatorString(1, "next spawn round: " + nextSpawnRound);
					}
				}
				
				rc.setIndicatorString(0, "coarsen complete!");
				spawnRobot(rc);
				
				
				
//				switch (calculationPhase)
//				{
//				case 1:
//					MapLogic.buildMap(rc);
//					rc.setIndicatorString(0, "calc 1 complete.");
//					break;
//				case 2:
//					coarsenSucceeded = MapLogic.coarsenMap(rc);
//					rc.setIndicatorString(0, "calc 2 complete.");
//					break;
//				case 3:
//					MapLogic.markNodeIndexOnGrid(rc);
//					rc.setIndicatorString(0, "calc 3 complete.");
//					break;
//				}
				
//				setTactic(rc);
//				rc.yield();
			}
			catch (Exception e)
			{
				System.out.println("HQ Exception");
				e.printStackTrace();
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
	
//	private static void setTactic(RobotController rc) 
//			throws GameActionException
//	{
//		MapLocation[] enemyPastrLocations = 
//				rc.sensePastrLocations(rc.getTeam().opponent());
//		
//		int currentEnemyPastrCount = enemyPastrLocations.length;
//		
//		boolean enemyPastrDestroyed = currentEnemyPastrCount < enemyPastrCount;
//		enemyPastrCount = currentEnemyPastrCount;
//		
//		if (enemyPastrCount > 0)
//		{
//			rc.setIndicatorString(1, "destroy enemy pastr!");
//			Communication.setEnemyPastrLocation(enemyPastrLocations[0], rc);
//			Communication.setTactic(Tactic.DESTROY_PASTR, rc);
//		}
//		else if (enemyPastrDestroyed)
//		{
//			rc.setIndicatorString(1, "build a pastr!");
//			Communication.setTactic(Tactic.BUILD_PASTR, rc);
//			pastrBuild = true;
//		}
//		else if (rallyPointSet
//				&& !pastrBuild)
//		{
//			rc.setIndicatorString(1, "rally!");
//			Communication.setTactic(Tactic.RALLY, rc);
//		}
//	}
}


