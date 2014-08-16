package bot.Robots;

import battlecode.common.Direction;
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
	final int SELF_DESTRUCT_HEALTH_THRESHOLD = 38;
	final int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 3;
	final int SELF_DESTRUCT_WALK_STEPS = 2;
	final int CLOSE_ENOUGH_DISTANCE = 3;
	
	private MapLocation MAP_CENTER;
	private MapLocation NOT_SET = new MapLocation(0, 0);
	
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
						&& !(nearbyEnemies.length != 1
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
		rc.setIndicatorString(0, "build pastr");
		MapLocation destination = Communication.getPointOfInterest(PointOfInterest.Team_Pastr, rc);
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(destination) < CLOSE_ENOUGH_DISTANCE)
		{
			Status pastrStatus = Communication.getPastrBuildingStatus(rc);
			Status noiseTowerStatus = Communication.getNoiseTowerBuildingStatus(rc);
			
			if (noiseTowerStatus == Status.NOT_SET)
			{
				rc.construct(RobotType.NOISETOWER);
				Communication.setNoiseTowerBuildingStatus(Status.IN_PROGRESS, rc);
			}
			else if (pastrStatus == Status.NOT_SET
					&& noiseTowerStatus == Status.COMPLETED)
			{
				rc.construct(RobotType.PASTR);
				Communication.setPastrBuildingStatus(Status.IN_PROGRESS, rc);
			}
		}
		else
		{
			navigation.moveToward(PointOfInterest.Team_Pastr, rc);
		}
	}
	
	private void rally(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "rally");
		MapLocation destination = Communication.getPointOfInterest(PointOfInterest.Rally_Point, rc);
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(destination) > CLOSE_ENOUGH_DISTANCE)
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
		if (nearbyEnemies.length >= SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD
				&& rc.getHealth() < SELF_DESTRUCT_HEALTH_THRESHOLD)
		{
			initiateSelfDestruct(rc, nearbyEnemies);
		}
		else if (nearbyEnemies.length > 0)
		{
			rc.setIndicatorString(0, "attacking an enemy.");
			attackAnEnemy(rc, nearbyEnemies);
		}
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

	private void initiateSelfDestruct(RobotController rc,
			Robot[] nearbyEnemies) throws GameActionException {
		rc.setIndicatorString(0, "Self-destruct engaged.");
		for (int i = 0; i < SELF_DESTRUCT_WALK_STEPS; i++)
		{
			MapLocation currentLocation = rc.getLocation();
			MapLocation enemyLocation = rc.senseRobotInfo(nearbyEnemies[0]).location;
			Direction moveDirection = currentLocation.directionTo(enemyLocation);
			if (rc.canMove(moveDirection))
			{
				rc.move(moveDirection);
			}
			
			rc.yield();
		}
		
		rc.selfDestruct();
	}
}
