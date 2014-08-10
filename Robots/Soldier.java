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
import bot.Enums.ConstructionStatus;
import bot.Enums.Tactic;

public class Soldier {
	
	//--Soldier self destruct
	final static int SELF_DESTRUCT_HEALTH_THRESHOLD = 38;
	final static int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 3;
	final static int SELF_DESTRUCT_WALK_STEPS = 2;
	final static int CLOSE_ENOUGH_DISTANCE = 3;
	
	public static void run(RobotController rc)
	{
		MovementLogic navigation = new MovementLogic();
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
							Robot.class, 10, rc.getTeam().opponent());
					
					if (nearbyEnemies.length > 0)
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
						case CONTROL_CENTER: controlCenter(navigation, rc);
							break;
						case DESTROY_PASTR: destroyPastr(navigation, rc);
							break;
						case RALLY: rally(navigation, rc);
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
	
	private static void buildPastr(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "build pastr");
		MapLocation destination = Communication.getPastrLocation(rc);
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(destination) < CLOSE_ENOUGH_DISTANCE)
		{
			ConstructionStatus pastrStatus = Communication.getPastrBuildingStatus(rc);
			ConstructionStatus noiseTowerStatus = Communication.getNoiseTowerBuildingStatus(rc);
			
			if (noiseTowerStatus == ConstructionStatus.NOT_SET)
			{
				rc.construct(RobotType.NOISETOWER);
				Communication.setNoiseTowerBuildingStatus(ConstructionStatus.BUILDING, rc);
			}
			else if (pastrStatus == ConstructionStatus.NOT_SET
					&& noiseTowerStatus == ConstructionStatus.COMPLETED)
			{
				rc.construct(RobotType.PASTR);
				Communication.setPastrBuildingStatus(ConstructionStatus.BUILDING, rc);
			}
		}
		else
		{
			navigation.moveToward(destination, rc);
		}
	}
	
	private static void controlCenter(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "control center");
		MapLocation destination = Communication.getMapCenter(rc);
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(destination) > CLOSE_ENOUGH_DISTANCE)
		{
			navigation.moveToward(destination, rc);
		}
	}
	
	private static void rally(MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		rc.setIndicatorString(0, "rally");
		MapLocation destination = Communication.getPastrLocation(rc);
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(destination) > CLOSE_ENOUGH_DISTANCE)
		{
			navigation.moveToward(destination, rc);
		}
	}
	
	private static void destroyPastr(MovementLogic navigation, RobotController rc)
			throws GameActionException
	{
		rc.setIndicatorString(0, "destroy pastr");
		navigation.moveToward(Communication.getEnemyPastrLocation(rc), rc);
	}
	
	private static void defendFrom(Robot[] nearbyEnemies, RobotController rc) 
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
	
	private static void attackAnEnemy(RobotController rc, Robot[] nearbyEnemies)
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

	private static void initiateSelfDestruct(RobotController rc,
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
