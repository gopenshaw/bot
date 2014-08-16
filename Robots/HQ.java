package bot.Robots;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bot.CoarsenedMap;
import bot.Communication;
import bot.MapLogic;
import bot.Enums.PointOfInterest;
import bot.Enums.Status;
import bot.Enums.Tactic;

public class HQ
{
	private CoarsenedMap map;
	private double nextSpawnRound;
	private MapLogic mapLogic;
	private int enemyPastrCount;
	private MapLocation enemyPastr;
	private int previousRoundNumber;
	private boolean coarsenComplete;
	public static final int TACTIC_INDICATOR_INDEX = 0;

	public void run(RobotController rc)
	{
		int hqStep = 1;
		MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

		while (true)
		{
			try
			{
				switch (hqStep)
				{
				case 1:
					// --init maps and calculate team pastr location
					nextSpawnRound = spawnRobot(rc);
					map = new CoarsenedMap(rc);
					mapLogic = new MapLogic(map);
					mapLogic.calculateTeamPastrLocation(rc);
					Communication.setPointOfInterest(
							PointOfInterest.Rally_Point, center, rc);
					hqStep++;
				case 2:
					// --build the coarsened map
					while (map.resume() == Status.IN_PROGRESS)
					{
						int roundNumber = Clock.getRoundNum();
						if (roundNumber > previousRoundNumber)
						{
							setTactic(rc);
							previousRoundNumber = roundNumber;
						}

						if (Clock.getRoundNum() >= nextSpawnRound)
						{
							double temp = spawnRobot(rc);
							if (temp != -1)
							{
								nextSpawnRound = temp;
							}

							rc.setIndicatorString(1, "next spawn round: "
									+ nextSpawnRound);
						}
					}
					coarsenComplete = true;
					rc.setIndicatorString(1, "coarsen complete!");
					hqStep++;
				case 3:
					// --build maps
					map.createMapTo(PointOfInterest.Rally_Point, center, rc);
					map.createMapTo(PointOfInterest.Team_Pastr,
							mapLogic.teamPastrLocation, rc);
					hqStep++;
				}

				spawnRobot(rc);
				setTactic(rc);
				rc.yield();
			} catch (Exception e)
			{
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}

	private void setTactic(RobotController rc) throws GameActionException
	{
		MapLocation[] enemyPastrLocations = rc.sensePastrLocations(rc.getTeam()
				.opponent());

		int currentEnemyPastrCount = enemyPastrLocations.length;

		boolean enemyPastrDestroyed = currentEnemyPastrCount < enemyPastrCount;
		enemyPastrCount = currentEnemyPastrCount;

		if (enemyPastrCount > 0 && !enemyPastrLocations[0].equals(enemyPastr))
		{
			Communication.setPointOfInterest(PointOfInterest.Enemy_Pastr,
					enemyPastrLocations[0], rc);
			Communication.setTactic(Tactic.DESTROY_PASTR, rc);

			if (coarsenComplete)
			{
				map.createMapTo(PointOfInterest.Enemy_Pastr,
						enemyPastrLocations[0], rc);
			}
		} else if (enemyPastrDestroyed)
		{
			enemyPastr = null;
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
		}
	}

	// --TODO: Must have more intelligent spawn location(s).
	private static double spawnRobot(RobotController rc)
			throws GameActionException
	{
		if (rc.isActive() && rc.senseRobotCount() < 25)
		{
			Direction toEnemy = rc.getLocation().directionTo(
					rc.senseEnemyHQLocation());
			if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null)
			{
				rc.spawn(toEnemy);
				return Clock.getRoundNum()
						+ GameConstants.HQ_SPAWN_DELAY_CONSTANT_1
						+ (rc.senseRobotCount() + 1)
						* GameConstants.HQ_SPAWN_DELAY_CONSTANT_2;
			}
		}

		return -1;
	}
}
