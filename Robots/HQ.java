package bot.Robots;

import battlecode.common.*;
import bot.*;
import bot.Enums.*;

public class HQ {
	static int enemyPastrCount = 0;
	static MapLocation enemyHQ;
	static boolean rallyPointSet = false;
	static boolean pastrBuild = false;
	static MapNode route;
	static boolean coarsenSucceeded;
	
	public static void run(RobotController rc)
	{
		int calculationPhase = 0;
//		while (true)
//		{
			try
			{	
				rc.setIndicatorString(0, "coarsening...");
				coarsenSucceeded = MapLogic.coarsenMap(rc);
				System.out.println("coarsen success: " + coarsenSucceeded);
				System.out.println("node count: " + MapLogic.nodeCount);
				rc.yield();
				
				rc.setIndicatorString(0, "creating map...");
				MapLogic.createMapTo(rc.senseEnemyHQLocation(), rc);
				System.out.println("map created and broadcasted");
				rc.yield();
				
				rc.setIndicatorString(0, "Nav mode set.");
				Communication.setNavigationMode(NavigationMode.MAP_NODES, rc);
				Communication.setRallyPoint(rc.senseEnemyHQLocation(), rc);
				Communication.setTactic(Tactic.RALLY, rc);
				spawnRobot(rc);
				
				while (true)
				{
					rc.yield();
				}
				
//				calculationPhase++;
//				spawnRobot(rc);
//				
//				switch (calculationPhase)
//				{
//				case 1:
//					mapWidth = rc.getMapWidth();
//					mapHeight = rc.getMapHeight();
//					Communication.setMapCenter(new MapLocation(mapWidth / 2, mapHeight / 2), rc);
//					Communication.setTeamHQ(rc.senseHQLocation(), rc);
//					enemyHQ = rc.senseEnemyHQLocation();
//					MapLocation pastrLocation = calculatePastrLocation(rc);
//					Communication.setPastrLocation(pastrLocation, rc);
//					Communication.setRallyPoint(pastrLocation, rc);
//					rallyPointSet = true;
//					rc.setIndicatorString(0, "calc 1 complete");
//					break;
//				case 2:
//					map = getMap(mapWidth, mapHeight, rc);
//					rc.setIndicatorString(0, "calc 2 complete");
//					break;
//				case 3:
//					rc.setIndicatorString(0, "coarsening map...");
//					coarsenSucceeded = MapLogic.coarsenMap(map, mapWidth, mapHeight);
//					if (!coarsenSucceeded)
//					{
//						calculationPhase += 10;
//						rc.setIndicatorString(0, "coarsening aborted.");
//					}
//					break;
//				case 4:
//					rc.setIndicatorString(0, "calculating route from team hq to team pastr...");
//					route = getRouteTo(Communication.getPastrLocation(rc), rc);
//				case 5:
//					rc.setIndicatorString(0, "calculating route from team hq to team pastr...");
//					broadcastRoute(route, rc);
//					rc.setIndicatorString(0, "calc done");
//				}
//				
//				setTactic(rc);
//				rc.yield();
			}
			catch (Exception e)
			{
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		//}
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
	
	private static void setTactic(RobotController rc) 
			throws GameActionException
	{
		MapLocation[] enemyPastrLocations = 
				rc.sensePastrLocations(rc.getTeam().opponent());
		
		int currentEnemyPastrCount = enemyPastrLocations.length;
		
		boolean enemyPastrDestroyed = currentEnemyPastrCount < enemyPastrCount;
		enemyPastrCount = currentEnemyPastrCount;
		
		if (enemyPastrCount > 0)
		{
			rc.setIndicatorString(1, "destroy enemy pastr!");
			Communication.setEnemyPastrLocation(enemyPastrLocations[0], rc);
			Communication.setTactic(Tactic.DESTROY_PASTR, rc);
		}
		else if (enemyPastrDestroyed)
		{
			rc.setIndicatorString(1, "build a pastr!");
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
			pastrBuild = true;
		}
		else if (rallyPointSet
				&& !pastrBuild)
		{
			rc.setIndicatorString(1, "rally!");
			Communication.setTactic(Tactic.RALLY, rc);
		}
	}
}


