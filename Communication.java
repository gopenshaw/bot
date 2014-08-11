package bot;

import battlecode.common.*;
import bot.Enums.*;

public class Communication {
	
	private final static int TEAM_HQ_LOCATION_CHANNEL = 10000;
	private final static int ENEMY_PASTR_COUNT_CHANNEL = 10001;
	private final static int ENEMY_PASTR_LOCATION_CHANNEL = 10002;
	private final static int PASTR_LOCATION_CHANNEL = 10003;
	
	private final static int PASTR_STATUS_CHANNEL = 10004;
	private final static int NOISE_TOWER_STATUS_CHANNEL = 10005;
	
	private final static int MAP_CENTER_CHANNEL = 10006;
	private final static int TACTIC_CHANNEL = 10007;
	private final static int NAVIGATION_MODE_CHANNEL = 10008;
	
	public static void setNavigationMode(
			NavigationMode status, RobotController rc) throws GameActionException
	{
		rc.broadcast(NAVIGATION_MODE_CHANNEL, status.ordinal());
	}
	
	public static NavigationMode GetNavigationMode(RobotController rc) 
			throws GameActionException
	{
		return NavigationMode.values()[rc.readBroadcast(NAVIGATION_MODE_CHANNEL)];
	}
	
	public static void broadcastNodePath(MapNode destinationNode, RobotController rc) throws GameActionException
	{
		MapNode targetNode = destinationNode;
		while (true)
		{
			MapNode source = targetNode.parent;
			MapLocation destination = source.getAdjacentLocationIn(targetNode);
			for (int i = source.left; i <= source.right; i++)
			{
				for (int j = source.top; j <= source.bottom; j++)
				{
					MapLocation current = new MapLocation(i, j);
					rc.broadcast(encodeMapLocation(current), 
						current.directionTo(destination).ordinal());
				}
			}
			
			if (source.parent == null)
			{
				break;
			}
			
			targetNode = source;
		}
	}
	
	public static void setTeamHQ(
			MapLocation location, RobotController rc) throws GameActionException
	{
		rc.broadcast(TEAM_HQ_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getTeamHQ(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(TEAM_HQ_LOCATION_CHANNEL));
	}
	
	public static void setTactic(
			Tactic tactic, RobotController rc) throws GameActionException
	{
		rc.broadcast(TACTIC_CHANNEL, tactic.ordinal());
	}
	
	public static Tactic getTactic(RobotController rc) 
			throws GameActionException
	{
		return Tactic.values()[rc.readBroadcast(TACTIC_CHANNEL)];
	}
	
	public static void setMapCenter(
			MapLocation location, RobotController rc) throws GameActionException
	{
		rc.broadcast(MAP_CENTER_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getMapCenter(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(MAP_CENTER_CHANNEL));
	}
	
	public static void setPastrBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(PASTR_STATUS_CHANNEL, status.ordinal());
	}
	
	public static ConstructionStatus getPastrBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(PASTR_STATUS_CHANNEL)];
	}
	
	public static void setNoiseTowerBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(NOISE_TOWER_STATUS_CHANNEL, status.ordinal());
	}
	
	public static ConstructionStatus getNoiseTowerBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(NOISE_TOWER_STATUS_CHANNEL)];
	}
	
	public static void setEnemyPastrCount(int count, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(ENEMY_PASTR_COUNT_CHANNEL, count);
	}
	
	public static int getEnemyPastrCount(RobotController rc) 
			throws GameActionException
	{
		return rc.readBroadcast(ENEMY_PASTR_COUNT_CHANNEL);
	}
	
	public static void setEnemyPastrLocation(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(ENEMY_PASTR_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getEnemyPastrLocation(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_CHANNEL));
	}
	
	public static void setPastrLocation(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(PASTR_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(PASTR_LOCATION_CHANNEL));
	}
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
