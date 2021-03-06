package bot.Robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import bot.Communication;
import bot.MovementLogic;
import bot.Enums.PointOfInterest;
import bot.Enums.Status;
import bot.Enums.Tactic;

public class Soldier {
	
	//--Soldier self destruct
	private final int SOLDIER_CLOSE_ENOUGH_DISTANCE = 5;
	private final int FARM_CLOSE_ENOUGH_DISTANCE = 2;
	private final int FRIENDLY_COUNT_FOR_FARM = 5;
	
	public void run(RobotController rc)
	{
		MovementLogic navigation = new MovementLogic(rc.getRobot().getID());
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
							Robot.class, 10, rc.getTeam().opponent());
					
					if (nearbyEnemies.length > 0
						&& !(nearbyEnemies.length == 1
						&& rc.senseRobotInfo(nearbyEnemies[0]).type == RobotType.HQ))
					{
						defendFrom(nearbyEnemies, rc);
					}
					else
					{
						Tactic tactic = Communication.getTactic(rc);
						switch (tactic)
						{
						case BUILD_PASTR: buildPastr(navigation, rc);
							break;
						case DESTROY_PASTR: destroyPastr(navigation, rc);
							break;
						case RALLY: rally(navigation, rc);
						rc.setIndicatorString(1, "rally");
							break;
						}
					}
					
					rc.yield();
				}
			}
			catch (Exception e) {
				System.out.println("Soldier Exception " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private void buildPastr(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		MapLocation currentLocation = rc.getLocation();
		MapLocation destination = null;
		Status pastrStatus = Communication.getPastrBuildingStatus(rc);
		Status noiseTowerStatus = Communication.getNoiseTowerBuildingStatus(rc);
		boolean farmBuilt = (pastrStatus == Status.IN_PROGRESS
				|| pastrStatus == Status.COMPLETED)
				&& (noiseTowerStatus == Status.IN_PROGRESS
				|| noiseTowerStatus == Status.COMPLETED);
		
		if (farmBuilt)
		{
			destination = Communication.getPointOfInterest(PointOfInterest.Pastr_Defense, rc);
		}
		else
		{
			destination = Communication.getPointOfInterest(PointOfInterest.Team_Pastr, rc);
		}
		
		
		int distance = currentLocation.distanceSquaredTo(destination);
		if (distance > SOLDIER_CLOSE_ENOUGH_DISTANCE)
		{
			rc.setIndicatorString(2, "going to pastr");
			navigation.moveToward(PointOfInterest.Team_Pastr, rc);
		}
		else if (!farmBuilt)
		{
			int friendlyCount = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()).length;
			if (noiseTowerStatus == Status.NOT_SET
				&& distance <= FARM_CLOSE_ENOUGH_DISTANCE
				&& rc.getHealth() > 70
				&& friendlyCount > FRIENDLY_COUNT_FOR_FARM)
			{
				rc.construct(RobotType.NOISETOWER);
				Communication.setNoiseTowerBuildingStatus(Status.IN_PROGRESS, rc);
			}
			else if (pastrStatus == Status.NOT_SET
					&& noiseTowerStatus == Status.IN_PROGRESS
					&& distance <= FARM_CLOSE_ENOUGH_DISTANCE
					&& rc.getHealth() > 70
					&& friendlyCount > FRIENDLY_COUNT_FOR_FARM)
			{
				rc.construct(RobotType.PASTR);
				Communication.setPastrBuildingStatus(Status.IN_PROGRESS, rc);
			}
			else if (distance > FARM_CLOSE_ENOUGH_DISTANCE)
			{
				navigation.moveToward(PointOfInterest.Team_Pastr, rc);
			}
		}
	}
	
	private void rally(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "rally");
		MapLocation currentLocation = rc.getLocation();
		MapLocation destination = Communication.getPointOfInterest(PointOfInterest.Rally_Point, rc);
		if (currentLocation.distanceSquaredTo(destination) > SOLDIER_CLOSE_ENOUGH_DISTANCE)
		{
			navigation.moveToward(PointOfInterest.Rally_Point, rc);
		}
	}
	
	private void destroyPastr(MovementLogic navigation, RobotController rc)
			throws GameActionException
	{
		rc.setIndicatorString(0, "destroy pastr");
		navigation.moveToward(PointOfInterest.Enemy_Pastr, rc);
	}
	
	private void defendFrom(Robot[] nearbyEnemies, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "attacking an enemy.");
		attackAnEnemy(rc, nearbyEnemies);
	}
	
	private void attackAnEnemy(RobotController rc, Robot[] nearbyEnemies)
			throws GameActionException {
		for (int i = 0; i < nearbyEnemies.length; i++)
		{
			RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
			if (robotInfo.type != RobotType.HQ)
			{
				rc.attackSquare(robotInfo.location);
				break;
			}
		}
	}
}
