package bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Soldier {
	
	//--Soldier self destruct
	final static int SELF_DESTRUCT_HEALTH_THRESHOLD = 38;
	final static int SELF_DESTRUCT_ENEMY_COUNT_TRESHOLD = 3;
	final static int SELF_DESTRUCT_WALK_STEPS = 2;
	
	public static void run(RobotController rc)
	{
		MovementLogic navigation = new MovementLogic(new MapLocation(-1, -1), rc);
		
		while (true)
		{
			try {
				if (rc.isActive()) {
					
					ConstructionCommand pastrCommand = Communication.getPastrCommand(rc);
					
					Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
							Robot.class, 10, rc.getTeam().opponent());
					
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
					else if (pastrCommand == ConstructionCommand.BUILD)
					{
						MapLocation location = Communication.getPastrLocation(rc);
						rc.setIndicatorString(0, "going to build pastr at " + location);
						buildPastr(location, navigation, rc);
					}
					else
					{
						MapLocation destination = Communication.getDestination(rc);
						rc.setIndicatorString(0, "navigating to " + destination);
						if (navigation == null)
						{
							navigation = new MovementLogic(destination, rc);
						}
						
						navigation.moveToward(destination, rc);
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
	
	private static void buildPastr(
			MapLocation location, MovementLogic navigation, RobotController rc) 
			throws GameActionException
	{
		
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.distanceSquaredTo(location) < 5)
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
			navigation.moveToward(location, rc);
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
