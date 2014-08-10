package bot.Robots;

import battlecode.common.*;
import bot.Communication;
import bot.Enums.Tactic;

public class HQ {
	static int mapWidth = 0;
	static int mapHeight = 0;
	static int enemyPastrCount = 0;
	static TerrainTile[][] map;
	
	public static void run(RobotController rc)
	{
		int calculationPhase = 0;
		
		while (true)
		{
			try
			{
				calculationPhase++;
				spawnRobot(rc);
				
				switch (calculationPhase)
				{
				case 1:
					rc.setIndicatorString(0, "calc 1 complete");
					mapWidth = rc.getMapWidth();
					mapHeight = rc.getMapHeight();
					Communication.setMapCenter(new MapLocation(mapWidth / 2, mapHeight / 2), rc);
					map = getMap(mapWidth, mapHeight, rc);
					break;
				case 2:
					rc.setIndicatorString(0, "calc 2 complete");
					setPastrLocation(rc);
					break;
				case 3:
					rc.setIndicatorString(0, "done with calc");
				}
				
				setTactic(rc);
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
			rc.setIndicatorString(0, "destroy enemy pastr!");
			Communication.setEnemyPastrLocation(enemyPastrLocations[0], rc);
			Communication.setTactic(Tactic.DESTROY_PASTR, rc);
		}
		else if (enemyPastrDestroyed)
		{
			rc.setIndicatorString(0, "build a pastr!");
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
		}
	}
	
	private static void setPastrLocation(RobotController rc) 
		throws GameActionException
	{
		final double[][] cowGrowth = rc.senseCowGrowth();
		final int skipCount = 2;
		int xMax = -1;
		int yMax = -1;
		double max = 0;
		for (int i = 0; i < mapWidth; i+= skipCount)
		{
			for (int j = 0; j < mapHeight; j+= skipCount)
			{
				if (cowGrowth[i][j] > max)
				{
					max = cowGrowth[i][j];
					xMax = i;
					yMax = j;
				}
			}
		}

		Communication.setPastrLocation(new MapLocation(xMax, yMax), rc);
	}
	
	private static TerrainTile[][] getMap(int mapWidth, int mapHeight, RobotController rc)
	{
		TerrainTile[][] map = new TerrainTile[mapWidth][mapHeight];
		
		for (int i = 0; i < mapWidth; i++)
		{
			for (int j = 0; j < mapHeight; j++) 
			{
				map[i][j] = rc.senseTerrainTile(new MapLocation(i, j));
			}
		}
		
		return map;
	}
}

