package bot.Robots;

import battlecode.common.*;
import bot.Communication;
import bot.Enums.Tactic;

public class HQ {
	private static class Memory
	{
		int enemyPastrCount;
	}
	
	public static void run(RobotController rc)
	{
		Memory state = new Memory();
		
		while (true)
		{
			boolean firstTurn = true;
			
			try
			{
				spawnRobot(rc);
				
				if (firstTurn)
				{
					Communication.setMapCenter(
							new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2), rc);
					firstTurn = false;
				}
				
				setTactic(state, rc);
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
	
	private static void setTactic(Memory state, RobotController rc) 
			throws GameActionException
	{
		MapLocation[] enemyPastrLocations = 
				rc.sensePastrLocations(rc.getTeam().opponent());
		
		int enemyPastrCount = enemyPastrLocations.length;
		
		boolean enemyPastrDestroyed = enemyPastrCount < state.enemyPastrCount;
		state.enemyPastrCount = enemyPastrCount;
		
		if (enemyPastrCount > 0)
		{
			rc.setIndicatorString(0, "destroy enemy pastr!");
			Communication.setEnemyPastrLocation(enemyPastrLocations[0], rc);
			Communication.setTactic(Tactic.DESTROY_PASTR, rc);
		}
		else if (enemyPastrDestroyed)
		{
			rc.setIndicatorString(0, "build a pastr!");
			//--TODO: need intelligent choice of pastr location!
			Communication.setPastrLocation(Communication.getMapCenter(rc), rc);
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
		}
	}
}


