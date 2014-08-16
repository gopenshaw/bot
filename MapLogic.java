package bot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bot.Enums.PointOfInterest;

public class MapLogic 
{
	private final Map MAP;
	public MapLocation teamPastrLocation;
	
	public MapLogic(Map map) {
		this.MAP = map;
	}
	
	public void calculateTeamPastrLocation(RobotController rc) 
			throws GameActionException
	{
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		final double[][] cowGrowth = rc.senseCowGrowth();
		final int skipCount = 2;
		int xMax = -1;
		int yMax = -1;
		double max = 0;
		
		for (int i = 0; i < MAP.MAP_WIDTH; i+= skipCount)
		{
			for (int j = 0; j < MAP.MAP_HEIGHT; j+= skipCount)
			{
				if (!adjacentSquaresAreNonZero(cowGrowth, i, j))
				{
					continue;
				}
				
				double value = calculatePastrValue(i, j, cowGrowth[i][j], enemyHQ);
				if (value > max)
				{
					max = value;
					xMax = i;
					yMax = j;
				}
			}
		}
		
		teamPastrLocation = new MapLocation(xMax, yMax);
		Communication.setPointOfInterest(PointOfInterest.Team_Pastr, teamPastrLocation, rc);
	}
	
	private boolean adjacentSquaresAreNonZero(double[][] cowGrowth, int x, int y)
	{
		final int MIN_SQUARES = 4;
		
		if (x < MIN_SQUARES 
			|| y < MIN_SQUARES 
			|| x + MIN_SQUARES >= MAP.MAP_WIDTH
			|| y + MIN_SQUARES >= MAP.MAP_HEIGHT)
			return false;
		
		for (int i = 1; i <= MIN_SQUARES; i ++)
		{
			if (cowGrowth[x + i][y] == 0)
				return false;
			if (cowGrowth[x - i][y] == 0)
				return false;
			if (cowGrowth[x][y + i] == 0)
				return false;
			if (cowGrowth[x][y - i] == 0)
				return false;
		}
	
		return true;
	}
	
	private double calculatePastrValue(int x, int y, double cowGrowth, MapLocation enemyHQ)
	{
		final double COW_GROWTH_WEIGHT = 1000;
		final double DISTANCE_FROM_ENEMY_WEIGHT = 0.01;
		return cowGrowth * COW_GROWTH_WEIGHT
				+ new MapLocation(x, y).distanceSquaredTo(enemyHQ) * DISTANCE_FROM_ENEMY_WEIGHT;
	}
}
