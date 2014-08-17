package bot.Robots;

import java.util.Arrays;

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
	private boolean[] canSpawnHere = new boolean[8];

	public void run(RobotController rc)
	{
		Arrays.fill(canSpawnHere, true);
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
					hqStep++;
					break;
				case 4:
					map.createMapTo(PointOfInterest.Team_Pastr, mapLogic.teamPastrLocation, rc);
					hqStep++;
					break;
				case 5:
					map.createMapTo(PointOfInterest.Pastr_Defense, mapLogic.teamPastrLocation, rc);
					hqStep++;
					break;
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
		
		if (Communication.getNoiseTowerBuildingStatus(rc) == Status.COMPLETED)
		{
			int signal = Communication.getNoiseTowerLifeSignal(rc);
			int currentRound = Clock.getRoundNum();
			if ((currentRound - signal) > 10)
			{
				System.out.println("!!" + signal + " " + Clock.getRoundNum());
				Communication.setNoiseTowerBuildingStatus(Status.NOT_SET, rc);
			}
		}
		
		if (Communication.getPastrBuildingStatus(rc) == Status.COMPLETED
			&& rc.sensePastrLocations(rc.getTeam()).length == 0)
		{
			
			Communication.setPastrBuildingStatus(Status.NOT_SET, rc);
		}

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
		} 
		else if (enemyPastrDestroyed)
		{
			enemyPastr = null;
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
		}
		else if (Clock.getRoundNum() > 1100)
		{
			Communication.setTactic(Tactic.BUILD_PASTR, rc);
		}
	}

	private double spawnRobot(RobotController rc)
	{
		if (rc.isActive() && rc.senseRobotCount() < 25)
		{
			for (int i = 0; i < 8; i++)
			{
				if (canSpawnHere[i])
				{
					try
					{
						rc.spawn(Direction.values()[i]);
						return Clock.getRoundNum()
								+ GameConstants.HQ_SPAWN_DELAY_CONSTANT_1
								+ (rc.senseRobotCount() + 1)
								* GameConstants.HQ_SPAWN_DELAY_CONSTANT_2;
					} 
					catch (GameActionException e)
					{
						canSpawnHere[i] = false;
					}
				}
			}
		}

		return -1;
	}
}
